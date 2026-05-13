package com.afterglowtv.player.playback

internal data class PlaybackBufferPolicy(
    val label: String,
    val minBufferMs: Int,
    val maxBufferMs: Int,
    val playbackBufferMs: Int,
    val rebufferMs: Int
)

internal object PlaybackBufferPolicies {
    // Live playback — generous max buffer (60s) absorbs transient network blips
    // without the player having to interrupt. Min buffer (8s) keeps memory
    // bounded; bufferForPlayback (2s) and rebuffer (8s) give enough margin that
    // we don't immediately re-stall after a recovery event.
    private const val LIVE_MIN_BUFFER_MS = 8_000
    private const val LIVE_MAX_BUFFER_MS = 60_000
    // Compat mode is for slow/flaky streams — even more headroom, slower start.
    private const val COMPAT_LIVE_MIN_BUFFER_MS = 15_000
    private const val COMPAT_LIVE_MAX_BUFFER_MS = 75_000
    // VOD can buffer aggressively because the source is seekable / non-realtime.
    private const val VOD_MIN_BUFFER_MS = 50_000
    private const val VOD_MAX_BUFFER_MS = 180_000
    // Time we wait before starting playback (cold start) — 2.0s is the sweet
    // spot: faster than the Media3 default (2.5s) but with enough margin to
    // avoid first-frame stutter on weak Wi-Fi.
    private const val PLAYBACK_BUFFER_MS = 2_000
    // Time we wait after a rebuffer before resuming — bigger than playback
    // buffer because, if we just stalled, the network is probably still flaky
    // and a small resume buffer means we'll just immediately re-stall.
    private const val REBUFFER_MS = 8_000

    fun forPlayback(isLive: Boolean, compatibilityMode: Boolean): PlaybackBufferPolicy = when {
        compatibilityMode && isLive ->
            PlaybackBufferPolicy("compat-live", COMPAT_LIVE_MIN_BUFFER_MS, COMPAT_LIVE_MAX_BUFFER_MS, PLAYBACK_BUFFER_MS, REBUFFER_MS)
        isLive ->
            PlaybackBufferPolicy("stable-live", LIVE_MIN_BUFFER_MS, LIVE_MAX_BUFFER_MS, PLAYBACK_BUFFER_MS, REBUFFER_MS)
        else ->
            PlaybackBufferPolicy("stable-vod", VOD_MIN_BUFFER_MS, VOD_MAX_BUFFER_MS, PLAYBACK_BUFFER_MS, REBUFFER_MS)
    }
}
