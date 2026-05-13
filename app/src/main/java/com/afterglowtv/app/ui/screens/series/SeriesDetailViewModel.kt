package com.afterglowtv.app.ui.screens.series

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afterglowtv.domain.model.ContentType
import com.afterglowtv.domain.model.Episode
import com.afterglowtv.domain.model.ExternalRatings
import com.afterglowtv.domain.model.ExternalRatingsLookup
import com.afterglowtv.domain.model.Result
import com.afterglowtv.domain.model.Season
import com.afterglowtv.domain.model.Series
import com.afterglowtv.domain.repository.ExternalRatingsRepository
import com.afterglowtv.domain.repository.PlaybackHistoryRepository
import com.afterglowtv.domain.repository.SeriesRepository
import com.afterglowtv.domain.repository.ProviderRepository
import com.afterglowtv.domain.util.isPlaybackComplete
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeriesDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val seriesRepository: SeriesRepository,
    private val providerRepository: ProviderRepository,
    private val playbackHistoryRepository: PlaybackHistoryRepository,
    private val externalRatingsRepository: ExternalRatingsRepository
) : ViewModel() {

    private val seriesId: Long = checkNotNull(
        savedStateHandle.get<Long>("seriesId")
            ?: savedStateHandle.get<String>("seriesId")?.toLongOrNull()
    )

    private val _uiState = MutableStateFlow(SeriesDetailUiState())
    val uiState: StateFlow<SeriesDetailUiState> = _uiState.asStateFlow()

    private var providerDetailJob: Job? = null

    init {
        viewModelScope.launch {
            providerRepository.getActiveProvider().collect { provider ->
                providerDetailJob?.cancel()
                _uiState.value = SeriesDetailUiState(isLoading = true)
                if (provider == null) {
                    _uiState.update { it.copy(isLoading = false, error = "No active provider") }
                    return@collect
                }
                providerDetailJob = launch {
                    launch {
                        playbackHistoryRepository.getUnwatchedCount(
                            providerId = provider.id,
                            seriesId = seriesId
                        ).collect { count ->
                            _uiState.update { it.copy(unwatchedEpisodeCount = count) }
                        }
                    }
                    loadSeriesDetailsForProvider(provider.id)
                }
            }
        }
    }

    private suspend fun loadSeriesDetailsForProvider(providerId: Long) {
        try {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = seriesRepository.getSeriesDetails(providerId, seriesId)) {
                is Result.Success -> {
                    loadExternalRatings(result.data)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            series = result.data,
                            selectedSeason = result.data.seasons.firstOrNull(),
                            resumeEpisode = findResumeEpisode(result.data),
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load series details"
                )
            }
        }
    }

    private fun loadExternalRatings(series: Series) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingExternalRatings = true) }
            val ratingsResult = externalRatingsRepository.getRatings(
                ExternalRatingsLookup(
                    contentType = ContentType.SERIES,
                    title = series.name,
                    releaseYear = series.releaseDate,
                    tmdbId = series.tmdbId
                )
            )
            _uiState.update { currentState ->
                when (ratingsResult) {
                    is Result.Success -> currentState.copy(
                        isLoadingExternalRatings = false,
                        externalRatings = ratingsResult.data
                    )
                    is Result.Error -> currentState.copy(
                        isLoadingExternalRatings = false,
                        externalRatings = ExternalRatings.unavailable()
                    )
                    is Result.Loading -> currentState
                }
            }
        }
    }

    fun selectSeason(season: Season) {
        _uiState.update { it.copy(selectedSeason = season) }
    }

    private fun findResumeEpisode(series: Series): Episode? {
        val ordered = series.seasons
            .sortedBy { it.seasonNumber }
            .flatMap { season -> season.episodes.sortedBy { it.episodeNumber } }
        // Prefer the most-recently-watched in-progress episode
        val inProgress = ordered
            .filter { ep ->
                ep.watchProgress > 5000L &&
                    !isPlaybackComplete(ep.watchProgress, ep.durationSeconds.toLong() * 1000L)
            }
            .maxByOrNull { it.lastWatchedAt }
        if (inProgress != null) return inProgress
        // Fall back to the first episode that has never been started
        return ordered.firstOrNull { ep -> ep.lastWatchedAt == 0L }
    }
}

data class SeriesDetailUiState(
    val isLoading: Boolean = false,
    val series: Series? = null,
    val selectedSeason: Season? = null,
    val resumeEpisode: Episode? = null,
    val unwatchedEpisodeCount: Int = 0,
    val error: String? = null,
    val isLoadingExternalRatings: Boolean = false,
    val externalRatings: ExternalRatings = ExternalRatings.unavailable()
)
