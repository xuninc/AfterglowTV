package com.afterglowtv.app.ui.screens.player

import android.util.Log

private const val TAG = "PlaybackProbe"

internal data class PlaybackProbeFailure(
    val message: String,
    val recoveryType: PlayerRecoveryType
)

/**
 * Record an advisory probe failure to logcat without blocking playback.
 * Use this for response codes we INTENTIONALLY don't hard-fail on (404,
 * 5xx) so the diagnostic info isn't entirely lost. If Media3 later fails
 * playback we have a paper trail of "the probe got a 404 first."
 */
internal fun logAdvisoryProbeResult(responseCode: Int, hostHint: String? = null) {
    if (responseCode in 200..299) return
    val host = hostHint?.takeIf { it.isNotBlank() } ?: "<unknown>"
    Log.i(TAG, "advisory-probe-result code=$responseCode host=$host (letting Media3 try anyway)")
}

/**
 * Map a `Range: bytes=0-0` probe response into a hard playback failure.
 *
 * Treat ONLY auth-class responses (401 / 403 / 456) as definitive — those
 * mean the user's credentials or subscription are wrong, and Media3 will
 * fail the same way. Other codes (404, 5xx) are *advisory*: many IPTV
 * CDNs route probe requests differently than real playback (different
 * edge cache, different range handling), so a 404 on a probe doesn't
 * mean the stream is unavailable, and a 5xx on a probe doesn't mean the
 * server is down for real media fetches. We let Media3 try those and
 * surface its own error if playback genuinely fails.
 *
 * Previously this returned hard failures for 404 / 5xx, which caused
 * "channel won't play" reports for streams that actually worked in
 * Media3 / VLC.
 */
internal fun resolvePlaybackProbeFailure(responseCode: Int): PlaybackProbeFailure? = when (responseCode) {
    401, 403 -> PlaybackProbeFailure(
        message = "This provider stream was rejected ($responseCode Unauthorized/Forbidden).",
        recoveryType = PlayerRecoveryType.SOURCE
    )

    456 -> PlaybackProbeFailure(
        message = "This provider rejected playback for this channel (HTTP 456). The MAC or subscription may not have access to this stream.",
        recoveryType = PlayerRecoveryType.SOURCE
    )

    // 404 + 5xx: deliberately NOT treated as hard failures here. Let Media3
    // try real playback; if the stream really is broken, Media3 will surface
    // a proper error with its own retry policy.
    else -> null
}