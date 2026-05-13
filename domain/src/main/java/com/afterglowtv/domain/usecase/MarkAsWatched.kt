package com.afterglowtv.domain.usecase

import com.afterglowtv.domain.model.ContentType
import com.afterglowtv.domain.model.PlaybackHistory
import com.afterglowtv.domain.model.Result
import com.afterglowtv.domain.repository.PlaybackHistoryRepository
import com.afterglowtv.domain.util.isPlaybackComplete
import javax.inject.Inject

class MarkAsWatched @Inject constructor(
    private val playbackHistoryRepository: PlaybackHistoryRepository
) {
    suspend operator fun invoke(history: PlaybackHistory): Result<Unit> {
        val normalizedHistory = if (
            history.contentType != ContentType.LIVE &&
            isPlaybackComplete(history.resumePositionMs, history.totalDurationMs)
        ) {
            history.copy(
                resumePositionMs = history.totalDurationMs.coerceAtLeast(history.resumePositionMs),
                lastWatchedAt = System.currentTimeMillis()
            )
        } else {
            history
        }
        return playbackHistoryRepository.markAsWatched(normalizedHistory)
    }
}