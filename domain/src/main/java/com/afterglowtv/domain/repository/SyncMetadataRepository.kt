package com.afterglowtv.domain.repository

import com.afterglowtv.domain.model.SyncMetadata
import kotlinx.coroutines.flow.Flow

interface SyncMetadataRepository {
    fun observeMetadata(providerId: Long): Flow<SyncMetadata?>
    suspend fun getMetadata(providerId: Long): SyncMetadata?
    suspend fun updateMetadata(metadata: SyncMetadata)
    suspend fun clearMetadata(providerId: Long)
}
