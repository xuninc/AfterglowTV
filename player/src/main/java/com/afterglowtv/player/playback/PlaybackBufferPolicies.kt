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
    // Time we wait before starting playback (cold start). Kept tight at 1.5s.
    // We lean on adaptive bitrate (track selector) and the larger max buffer
    // for resilience instead of padding the cold-start wait.
    private const val PLAYBACK_BUFFER_MS = 1_500
    // Time we wait after a rebuffer before resuming. 5s gives enough margin
    // to avoid immediate re-stall on the same network blip without making
    // recovery feel sluggish.
    private const val REBUFFER_MS = 5_000

    fun forPlayback(isLive: Boolean, compatibilityMode: Boolean): PlaybackBufferPolicy = when {
        compatibilityMode && isLive ->
            PlaybackBufferPolicy("compat-live", COMPAT_LIVE_MIN_BUFFER_MS, COMPAT_LIVE_MAX_BUFFER_MS, PLAYBACK_BUFFER_MS, REBUFFER_MS)
        isLive ->
            PlaybackBufferPolicy("stable-live", LIVE_MIN_BUFFER_MS, LIVE_MAX_BUFFER_MS, PLAYBACK_BUFFER_MS, REBUFFER_MS)
        else ->
            PlaybackBufferPolicy("stable-vod", VOD_MIN_BUFFER_MS, VOD_MAX_BUFFER_MS, PLAYBACK_BUFFER_MS, REBUFFER_MS)
    }
}
