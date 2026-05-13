package com.afterglowtv.data.repository

import com.afterglowtv.domain.model.ExternalRatings
import com.afterglowtv.domain.model.ExternalRatingsLookup
import com.afterglowtv.domain.model.Result
import com.afterglowtv.domain.repository.ExternalRatingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExternalRatingsRepositoryImpl @Inject constructor() : ExternalRatingsRepository {

    override suspend fun getRatings(lookup: ExternalRatingsLookup): Result<ExternalRatings> {
        return Result.success(ExternalRatings.unavailable())
    }
}