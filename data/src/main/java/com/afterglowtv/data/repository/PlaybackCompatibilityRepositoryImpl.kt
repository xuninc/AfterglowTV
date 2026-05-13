package com.afterglowtv.data.repository

import com.afterglowtv.data.local.dao.PlaybackCompatibilityDao
import com.afterglowtv.data.local.entity.PlaybackCompatibilityRecordEntity
import com.afterglowtv.domain.model.PlaybackCompatibilityKey
import com.afterglowtv.domain.model.PlaybackCompatibilityRecord
import com.afterglowtv.domain.repository.PlaybackCompatibilityRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackCompatibilityRepositoryImpl @Inject constructor(
    private val dao: PlaybackCompatibilityDao
) : PlaybackCompatibilityRepository {

    override suspend fun getKnownBadRecords(
        deviceFingerprint: String,
        streamType: String,
        videoMimeType: String,
        resolutionBucket: String
    ): List<PlaybackCompatibilityRecord> {
        return dao.getKnownBadCandidates(deviceFingerprint, streamType, videoMimeType, resolutionBucket)
            .map { it.toDomain() }
            .filter(PlaybackCompatibilityRecord::isKnownBad)
    }

    override suspend fun recordFailure(key: PlaybackCompatibilityKey, failureType: String, at: Long) {
        dao.recordFailure(
            deviceFingerprint = key.deviceFingerprint,
            deviceModel = key.deviceModel,
            androidSdk = key.androidSdk,
            streamType = key.streamType,
            videoMimeType = key.videoMimeType,
            resolutionBucket = key.resolutionBucket,
            decoderName = key.decoderName,
            surfaceType = key.surfaceType,
            failureType = failureType,
            failedAt = at
        )
        prune()
    }

    override suspend fun recordSuccess(key: PlaybackCompatibilityKey, at: Long) {
        dao.recordSuccess(
            deviceFingerprint = key.deviceFingerprint,
            deviceModel = key.deviceModel,
            androidSdk = key.androidSdk,
            streamType = key.streamType,
            videoMimeType = key.videoMimeType,
            resolutionBucket = key.resolutionBucket,
            decoderName = key.decoderName,
            surfaceType = key.surfaceType,
            succeededAt = at
        )
    }

    override suspend fun prune(maxRecords: Int, olderThanMs: Long) {
        dao.deleteOlderThan(olderThanMs)
        dao.keepMostRecent(maxRecords)
    }

    private fun PlaybackCompatibilityRecordEntity.toDomain(): PlaybackCompatibilityRecord =
        PlaybackCompatibilityRecord(
            key = PlaybackCompatibilityKey(
                deviceFingerprint = deviceFingerprint,
                deviceModel = deviceModel,
                androidSdk = androidSdk,
                streamType = streamType,
                videoMimeType = videoMimeType,
                resolutionBucket = resolutionBucket,
                decoderName = decoderName,
                surfaceType = surfaceType
            ),
            failureType = failureType,
            lastFailedAt = lastFailedAt,
            lastSucceededAt = lastSucceededAt,
            failureCount = failureCount,
            successCount = successCount
        )
}
