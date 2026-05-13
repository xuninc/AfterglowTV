package com.afterglowtv.domain.repository

import com.afterglowtv.domain.model.ExternalRatings
import com.afterglowtv.domain.model.ExternalRatingsLookup
import com.afterglowtv.domain.model.Result

interface ExternalRatingsRepository {
    suspend fun getRatings(lookup: ExternalRatingsLookup): Result<ExternalRatings>
}