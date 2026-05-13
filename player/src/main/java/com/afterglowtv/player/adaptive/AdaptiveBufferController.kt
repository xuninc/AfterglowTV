package com.afterglowtv.player.adaptive

import com.afterglowtv.player.playback.BufferClass
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Decides which [BufferClass] policy the player should use, based on
 * recent telemetry from [AdaptivePlaybackRecorder] and the current
 * transport from [NetworkClassDetector].
 *
 * ## How this is built from BBA-2 with hardened defaults
 *
 * Stanford 2014's Buffer-Based Approach (BBA) uses buffer occupancy
 * to pick *bitrate*. We borrow the threshold-hysteresis pattern but
 * apply it to *buffer SIZE* instead — bitrate selection stays with
 * Media3's DefaultTrackSelector, which already does its own BBA-style
 * variant selection.
 *
 * The decision is a single classifier over rebuffer rate (events per
 * minute) and recovery duration (p95 in ms), with three tiers:
 *
 *  - **AGGRESSIVE**: clean playback with enough history AND rebuffer
 *    rate < [CLEAN_RATE_THRESHOLD] AND p95 recovery
 *    < [CLEAN_RECOVERY_THRESHOLD_MS]. Reward: snappier zap, less RAM.
 *  - **COMPAT**: rebuffer rate ≥ [STRESS_RATE_THRESHOLD] OR p95
 *    recovery ≥ [STRESS_RECOVERY_THRESHOLD_MS]. Reward: more headroom.
 *  - **STABLE**: everything else, including cold-start with no history.
 *
 * Cellular networks anchor on STABLE rather than AGGRESSIVE even
 * during a clean window — they're far more prone to handover stalls
 * than Wi-Fi/Ethernet, so the smaller AGGRESSIVE buffer is too risky
 * even when recent samples look good.
 *
 * Hysteresis: the thresholds are *asymmetric* on purpose. To enter
 * AGGRESSIVE requires very clean playback; to leave AGGRESSIVE only
 * requires a single rebuffer event. This prevents the classifier from
 * oscillating between modes during transient blips.
 */
@Singleton
class AdaptiveBufferController @Inject constructor(
    private val recorder: AdaptivePlaybackRecorder,
    private val networkClassDetector: NetworkClassDetector,
) {
    /**
     * Returns the recommended [BufferClass] for the next player
     * preparation. Safe to call from any thread.
     */
    fun classify(): BufferClass {
        return classify(recorder.snapshot(), networkClassDetector.current())
    }

    companion object {
        /** Below this many recorded events, treat as cold-start (no signal). */
        private const val COLD_START_MIN_EVENTS = 20
        /** Rebuffers per minute below which playback counts as clean. */
        private const val CLEAN_RATE_THRESHOLD = 0.2
        /** p95 recovery duration below which recoveries count as snappy. */
        private const val CLEAN_RECOVERY_THRESHOLD_MS = 5_000L
        /** Rebuffers per minute at or above which playback counts as stressed. */
        private const val STRESS_RATE_THRESHOLD = 2.0
        /** p95 recovery duration at or above which recoveries count as slow. */
        private const val STRESS_RECOVERY_THRESHOLD_MS = 12_000L

        /**
         * Pure decision function — unit-test friendly. Same logic as the
         * instance method but takes its inputs directly so tests can
         * construct synthetic snapshots without needing a real recorder
         * or Android-backed detector.
         */
        fun classify(snapshot: AdaptivePlaybackSnapshot, network: NetworkClass): BufferClass {
            // Cold start with no telemetry — give the user our best guess from
            // network class alone. Cellular tiers prefer COMPAT because mobile
            // links are unstable; wired and Wi-Fi prefer STABLE.
            val coldStart = snapshot.rebufferCount == 0 && snapshot.eventCount < COLD_START_MIN_EVENTS
            if (coldStart) {
                return when (network) {
                    NetworkClass.CELLULAR_2G, NetworkClass.CELLULAR_3G -> BufferClass.COMPAT
                    else -> BufferClass.STABLE
                }
            }

            // Stress signal: high rebuffer rate or slow recovery.
            if (snapshot.rebufferRatePerMinute >= STRESS_RATE_THRESHOLD ||
                (snapshot.p95RecoveryMs ?: 0L) >= STRESS_RECOVERY_THRESHOLD_MS
            ) {
                return BufferClass.COMPAT
            }

            // Clean signal: must be on a stable transport AND have a
            // meaningful sample AND be measurably clean across both rate
            // and recovery duration.
            val transportAllowsAggressive = when (network) {
                NetworkClass.ETHERNET, NetworkClass.WIFI -> true
                else -> false
            }
            val cleanRebufferRate = snapshot.rebufferRatePerMinute <= CLEAN_RATE_THRESHOLD
            val cleanRecovery = (snapshot.p95RecoveryMs ?: 0L) <= CLEAN_RECOVERY_THRESHOLD_MS
            val enoughHistory = snapshot.eventCount >= COLD_START_MIN_EVENTS

            if (transportAllowsAggressive && cleanRebufferRate && cleanRecovery && enoughHistory) {
                return BufferClass.AGGRESSIVE
            }

            return BufferClass.STABLE
        }
    }
}
