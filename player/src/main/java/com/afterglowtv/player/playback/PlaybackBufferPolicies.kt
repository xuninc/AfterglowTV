package com.afterglowtv.player.playback

internal data class PlaybackBufferPolicy(
    val label: String,
    val minBufferMs: Int,
    val maxBufferMs: Int,
    val playbackBufferMs: Int,
    val rebufferMs: Int
)

/**
 * Adaptive class chosen by [com.afterglowtv.player.adaptive.AdaptiveBufferController]
 * based on observed rebuffer rate + network class. Each value maps to a
 * different live-buffer policy below.
 */
enum class BufferClass {
    /** Clean recent playback — go snappier: smaller buffer, faster zap. */
    AGGRESSIVE,
    /** Default — the policy that used to be hardcoded as "stable-live". */
    STABLE,
    /** Recent rebuffers observed — grow the buffer to absorb blips. */
    COMPAT,
}

internal object PlaybackBufferPolicies {
    // AGGRESSIVE live — for clean networks where snappy zap is more
    // valuable than 60s of headroom. Sized to feel close to instant.
    private const val AGGRESSIVE_LIVE_MIN_BUFFER_MS = 8_000
    private const val AGGRESSIVE_LIVE_MAX_BUFFER_MS = 30_000

    // STABLE live — the previous hardcoded default. Generous max (60s)
    // absorbs typical CDN hiccups without interrupting; modest min (8s)
    // keeps memory bounded and start time short.
    private const val LIVE_MIN_BUFFER_MS = 12_000
    private const val LIVE_MAX_BUFFER_MS = 60_000

    // COMPAT live — used when the controller has observed real
    // rebuffers recently. Bigger headroom; player tolerates flakier
    // upstream conditions before interrupting.
    private const val COMPAT_LIVE_MIN_BUFFER_MS = 20_000
    private const val COMPAT_LIVE_MAX_BUFFER_MS = 75_000

    // VOD can buffer aggressively because the source is seekable / non-realtime.
    private const val VOD_MIN_BUFFER_MS = 50_000
    private const val VOD_MAX_BUFFER_MS = 180_000

    // Time we wait before starting playback (cold start). Still short, but
    // no longer so short that unstable IPTV edges immediately flap.
    // We lean on adaptive bitrate (track selector) and the larger max buffer
    // for resilience instead of padding the cold-start wait.
    private const val PLAYBACK_BUFFER_MS = 2_500
    // Time we wait after a rebuffer before resuming. IPTV CDNs often recover
    // just enough data for Media3 to leave BUFFERING, then stall again within
    // a second; this gives live playback a real cushion before restarting.
    private const val REBUFFER_MS = 8_000

    /**
     * Legacy entry point. Compatibility-mode flag is now a derived signal
     * — the adaptive controller will call [forBufferClass] directly. This
     * remains so existing callers (manual user toggle in settings) keep
     * working.
     */
    fun forPlayback(isLive: Boolean, compatibilityMode: Boolean): PlaybackBufferPolicy = when {
        compatibilityMode && isLive -> forBufferClass(BufferClass.COMPAT, isLive = true)
        isLive -> forBufferClass(BufferClass.STABLE, isLive = true)
        else -> PlaybackBufferPolicy("stable-vod", VOD_MIN_BUFFER_MS, VOD_MAX_BUFFER_MS, PLAYBACK_BUFFER_MS, REBUFFER_MS)
    }

    /**
     * Adaptive entry point. Called by [com.afterglowtv.player.adaptive.AdaptiveBufferController]
     * after it reads recent telemetry from [com.afterglowtv.player.adaptive.AdaptivePlaybackRecorder].
     */
    internal fun forBufferClass(bufferClass: BufferClass, isLive: Boolean): PlaybackBufferPolicy {
        if (!isLive) {
            return PlaybackBufferPolicy("stable-vod", VOD_MIN_BUFFER_MS, VOD_MAX_BUFFER_MS, PLAYBACK_BUFFER_MS, REBUFFER_MS)
        }
        return when (bufferClass) {
            BufferClass.AGGRESSIVE -> PlaybackBufferPolicy(
                label = "aggressive-live",
                minBufferMs = AGGRESSIVE_LIVE_MIN_BUFFER_MS,
                maxBufferMs = AGGRESSIVE_LIVE_MAX_BUFFER_MS,
                playbackBufferMs = PLAYBACK_BUFFER_MS,
                rebufferMs = REBUFFER_MS,
            )
            BufferClass.STABLE -> PlaybackBufferPolicy(
                label = "stable-live",
                minBufferMs = LIVE_MIN_BUFFER_MS,
                maxBufferMs = LIVE_MAX_BUFFER_MS,
                playbackBufferMs = PLAYBACK_BUFFER_MS,
                rebufferMs = REBUFFER_MS,
            )
            BufferClass.COMPAT -> PlaybackBufferPolicy(
                label = "compat-live",
                minBufferMs = COMPAT_LIVE_MIN_BUFFER_MS,
                maxBufferMs = COMPAT_LIVE_MAX_BUFFER_MS,
                playbackBufferMs = PLAYBACK_BUFFER_MS,
                rebufferMs = REBUFFER_MS,
            )
        }
    }
}
