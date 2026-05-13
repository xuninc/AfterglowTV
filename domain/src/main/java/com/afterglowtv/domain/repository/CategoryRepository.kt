package com.afterglowtv.domain.repository

import com.afterglowtv.domain.model.Category
import com.afterglowtv.domain.model.ContentType
import com.afterglowtv.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(providerId: Long): Flow<List<Category>>
    suspend fun setCategoryProtection(
        providerId: Long,
        categoryId: Long,
        type: ContentType,
        isProtected: Boolean
    ): Result<Unit>
}
