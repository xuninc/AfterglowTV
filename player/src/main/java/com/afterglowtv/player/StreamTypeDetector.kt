package com.afterglowtv.player

import com.afterglowtv.domain.model.StreamType
import com.afterglowtv.player.playback.ResolvedStreamType
import com.afterglowtv.player.playback.StreamTypeResolver

@Deprecated("Use StreamTypeResolver")
object StreamTypeDetector {
    fun detect(url: String): StreamType {
        return when (StreamTypeResolver.resolve(url = url, isLive = url.contains("/live/", ignoreCase = true))) {
            ResolvedStreamType.HLS -> StreamType.HLS
            ResolvedStreamType.DASH -> StreamType.DASH
            ResolvedStreamType.MPEG_TS_LIVE -> StreamType.MPEG_TS
            ResolvedStreamType.PROGRESSIVE -> StreamType.PROGRESSIVE
            ResolvedStreamType.RTSP -> StreamType.RTSP
            ResolvedStreamType.UNKNOWN -> StreamType.UNKNOWN
        }
    }
}
