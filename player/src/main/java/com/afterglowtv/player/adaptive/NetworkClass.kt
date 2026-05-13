package com.afterglowtv.player.adaptive

import androidx.media3.common.C

/**
 * Transport-derived priors for adaptive playback configuration.
 *
 * Initial bitrate estimates are borrowed from Shaka Player's defaults
 * (`shaka.config.streaming.bandwidthEstimator`) and tuned slightly upward
 * for residential IPTV — typical users have wired broadband or strong
 * 5GHz Wi-Fi, not the global-average values Shaka picks for general
 * web video.
 *
 * The values represent what the player should *assume* before measuring,
 * not what the network is actually capable of. The bandwidth meter will
 * converge to truth within ~10 seconds; the prior just prevents the
 * "starts at 480p, ladders up over 15 seconds" pattern.
 */
enum class NetworkClass(
    /** ExoPlayer's `C.NETWORK_TYPE_*` constant for [DefaultBandwidthMeter] keying. */
    val exoNetworkType: Int,
    /** Initial bandwidth estimate in bits per second. */
    val initialBitrateEstimate: Long,
    val displayName: String,
) {
    WIFI(
        exoNetworkType = C.NETWORK_TYPE_WIFI,
        initialBitrateEstimate = 6_000_000L,
        displayName = "Wi-Fi",
    ),
    ETHERNET(
        exoNetworkType = C.NETWORK_TYPE_ETHERNET,
        initialBitrateEstimate = 10_000_000L,
        displayName = "Ethernet",
    ),
    CELLULAR_5G(
        exoNetworkType = C.NETWORK_TYPE_5G_NSA,
        initialBitrateEstimate = 4_000_000L,
        displayName = "5G",
    ),
    CELLULAR_4G(
        exoNetworkType = C.NETWORK_TYPE_4G,
        initialBitrateEstimate = 1_500_000L,
        displayName = "4G",
    ),
    CELLULAR_3G(
        exoNetworkType = C.NETWORK_TYPE_3G,
        initialBitrateEstimate = 700_000L,
        displayName = "3G",
    ),
    CELLULAR_2G(
        exoNetworkType = C.NETWORK_TYPE_2G,
        initialBitrateEstimate = 250_000L,
        displayName = "2G",
    ),
    UNKNOWN(
        exoNetworkType = C.NETWORK_TYPE_UNKNOWN,
        // When we don't know what we're on, assume average Wi-Fi. Better to
        // pick decent quality and ladder down than start blurry.
        initialBitrateEstimate = 3_000_000L,
        displayName = "Unknown",
    ),
}
