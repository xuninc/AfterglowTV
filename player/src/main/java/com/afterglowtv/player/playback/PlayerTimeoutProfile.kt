package com.afterglowtv.player.playback

import com.afterglowtv.domain.model.StreamInfo
import com.afterglowtv.domain.model.StreamType

enum class PlayerTimeoutProfile(
    val connectTimeoutMs: Long,
    val readTimeoutMs: Long,
    val writeTimeoutMs: Long
) {
    // Live channels — HLS / MPEG-TS / RTSP. Read timeout is the most failure-
    // prone here: HLS segments can take 6-10s to fetch on weak Wi-Fi. Bumping
    // read to 30s gives the network a fair chance before we declare it dead.
    LIVE(
        connectTimeoutMs = 15_000L,
        readTimeoutMs = 30_000L,
        writeTimeoutMs = 20_000L
    ),
    // VOD — already generous because users tolerate longer start times for
    // long-form content.
    VOD(
        connectTimeoutMs = 15_000L,
        readTimeoutMs = 45_000L,
        writeTimeoutMs = 30_000L
    ),
    PROGRESSIVE(
        connectTimeoutMs = 15_000L,
        readTimeoutMs = 35_000L,
        writeTimeoutMs = 30_000L
    ),
    // Preload — kept tight on purpose. If a preload can't fetch within these
    // limits, we'd rather give up than hold up the real playback.
    PRELOAD(
        connectTimeoutMs = 10_000L,
        readTimeoutMs = 15_000L,
        writeTimeoutMs = 15_000L
    );

    companion object {
        fun resolve(
            streamInfo: StreamInfo,
            resolvedStreamType: ResolvedStreamType,
            preload: Boolean
        ): PlayerTimeoutProfile {
            if (preload) return PRELOAD
            if (streamInfo.streamType == StreamType.RTSP) return LIVE
            return when {
                resolvedStreamType == ResolvedStreamType.HLS -> LIVE
                resolvedStreamType == ResolvedStreamType.MPEG_TS_LIVE -> LIVE
                resolvedStreamType == ResolvedStreamType.RTSP -> LIVE
                resolvedStreamType == ResolvedStreamType.PROGRESSIVE -> PROGRESSIVE
                streamInfo.streamType == StreamType.PROGRESSIVE -> PROGRESSIVE
                else -> VOD
            }
        }
    }
}

