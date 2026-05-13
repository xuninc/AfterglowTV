package com.afterglowtv.domain.repository

import com.afterglowtv.domain.model.Category
import com.afterglowtv.domain.model.Episode
import com.afterglowtv.domain.model.LibraryBrowseQuery
import com.afterglowtv.domain.model.PagedResult
import com.afterglowtv.domain.model.Result
import com.afterglowtv.domain.model.Series
import com.afterglowtv.domain.model.StreamInfo
import kotlinx.coroutines.flow.Flow

interface SeriesRepository {
    fun getSeries(providerId: Long): Flow<List<Series>>
    fun getSeriesByCategory(providerId: Long, categoryId: Long): Flow<List<Series>>
    fun getSeriesByCategoryPage(providerId: Long, categoryId: Long, limit: Int, offset: Int): Flow<List<Series>>
    fun getSeriesByCategoryPreview(providerId: Long, categoryId: Long, limit: Int): Flow<List<Series>>
    fun getCategoryPreviewRows(providerId: Long, categoryIds: List<Long>, limitPerCategory: Int): Flow<Map<Long?, List<Series>>>
    fun getTopRatedPreview(providerId: Long, limit: Int): Flow<List<Series>>
    fun getFreshPreview(providerId: Long, limit: Int): Flow<List<Series>>
    fun getSeriesByIds(ids: List<Long>): Flow<List<Series>>
    fun getCategories(providerId: Long): Flow<List<Category>>
    fun getCategoryItemCounts(providerId: Long): Flow<Map<Long, Int>>
    fun getLibraryCount(providerId: Long): Flow<Int>
    fun browseSeries(query: LibraryBrowseQuery): Flow<PagedResult<Series>>
    fun searchSeries(providerId: Long, query: String): Flow<List<Series>>
    suspend fun getSeriesById(seriesId: Long): Series?
    suspend fun getEpisodeById(episodeId: Long): Episode?
    suspend fun getSeriesDetails(providerId: Long, seriesId: Long): Result<Series>
    suspend fun getEpisodeStreamInfo(episode: Episode): Result<StreamInfo>
    suspend fun refreshSeries(providerId: Long): Result<Unit>
}
