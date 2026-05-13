package com.afterglowtv.player.adaptive

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter

/**
 * Factory that builds a [DefaultBandwidthMeter] configured with our
 * [NetworkClass]-aware initial bitrate estimates.
 *
 * ## How this is better than Media3's default
 *
 * `DefaultBandwidthMeter.Builder()` without configuration uses Media3's
 * country-keyed defaults (per `BandwidthMeter` source). Those values
 * average across cellular and Wi-Fi globally and are conservative — on
 * residential Wi-Fi a player will start at SD quality and ladder up over
 * 10-15 seconds because the meter starts from a "country average" prior
 * that includes everyone on weak cellular too.
 *
 * Here we feed in a per-transport estimate from [NetworkClass] — pulled
 * from `NetworkCapabilities` at construction time — which is closer to
 * the actual throughput available right now. The Wi-Fi prior is 4-6 Mbps
 * (vs Media3's ~700 kbps country default), Ethernet is 10 Mbps, and
 * cellular tiers are split by actual radio technology (5G NSA = 4 Mbps,
 * LTE = 1.5 Mbps, 3G = 700 kbps).
 *
 * The meter still does its EMA smoothing on real samples afterward —
 * the prior just prevents the cold-start drag.
 *
 * Also passes a `slidingWindowMaxWeight` of 4_000_000 (default is
 * 2_000_000) so the EMA gives more weight to recent samples — IPTV
 * networks change conditions faster than the general-purpose web
 * default expects.
 */
@UnstableApi
class AdaptiveBandwidthMeter {
    fun build(context: Context, networkClass: NetworkClass): DefaultBandwidthMeter {
        return DefaultBandwidthMeter.Builder(context)
            // Prior per transport — overrides the country-keyed default for
            // the matching NETWORK_TYPE only. Others fall back to Media3's
            // built-in country defaults.
            .setInitialBitrateEstimate(networkClass.exoNetworkType, networkClass.initialBitrateEstimate)
            // Also seed the most-likely-to-be-active alternatives at our
            // higher priors so handoff (Wi-Fi → Cellular) doesn't drag.
            .setInitialBitrateEstimate(NetworkClass.WIFI.exoNetworkType, NetworkClass.WIFI.initialBitrateEstimate)
            .setInitialBitrateEstimate(NetworkClass.ETHERNET.exoNetworkType, NetworkClass.ETHERNET.initialBitrateEstimate)
            .setInitialBitrateEstimate(NetworkClass.CELLULAR_5G.exoNetworkType, NetworkClass.CELLULAR_5G.initialBitrateEstimate)
            .setInitialBitrateEstimate(NetworkClass.CELLULAR_4G.exoNetworkType, NetworkClass.CELLULAR_4G.initialBitrateEstimate)
            // Larger sliding window = more weight on recent samples. IPTV
            // networks shift faster than the general-purpose web default
            // anticipates (peak hours, neighbour Wi-Fi, etc.).
            .setSlidingWindowMaxWeight(SLIDING_WINDOW_MAX_WEIGHT)
            .build()
    }

    companion object {
        // Default is 2_000_000 (Media3 source). Doubling gives ~2x the
        // history weight, which biases the EMA toward recent reality.
        private const val SLIDING_WINDOW_MAX_WEIGHT = 4_000_000
    }
}
