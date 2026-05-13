package com.afterglowtv.player.adaptive

/**
 * Aggregated view over a rolling window of [AdaptivePlaybackEvent]s.
 * This is what controllers READ to make policy decisions — they never
 * iterate the raw event log themselves.
 *
 * All percentile fields use linear interpolation between the two
 * surrounding samples; samples are sorted ascending and indexed by
 * `ceil(p * (n - 1))`. Returns null when the window has too few samples
 * to be meaningful (configurable per field, default min-samples = 3).
 *
 * Windowed counts (rebufferCount, codecInitsCount) are over the same
 * rolling window the recorder was configured with — typically 5 minutes.
 */
data class AdaptivePlaybackSnapshot(
    /** Wall-clock at snapshot time. */
    val snapshotTimestampMs: Long,

    /** Total events currently in the rolling window. */
    val eventCount: Int,

    // ── Time-to-first-frame ─────────────────────────────────────────
    val medianTtffMs: Long?,
    val p95TtffMs: Long?,
    val ttffSampleCount: Int,

    // ── Time-to-first-byte (HTTP) ───────────────────────────────────
    val medianTtfbMs: Long?,
    val p95TtfbMs: Long?,
    val ttfbSampleCount: Int,

    // ── Rebuffer ────────────────────────────────────────────────────
    /** Number of rebuffer events in the rolling window. */
    val rebufferCount: Int,
    /** Per-minute rate, projected from the window. */
    val rebufferRatePerMinute: Double,
    val medianRecoveryMs: Long?,
    val p95RecoveryMs: Long?,

    // ── Bandwidth ───────────────────────────────────────────────────
    /** Exponentially-weighted moving average of the last N bandwidth samples. */
    val emaBitsPerSecond: Long?,
    /** Most recent bandwidth sample. */
    val lastBitsPerSecond: Long?,
    val bandwidthSampleCount: Int,

    // ── Codec ───────────────────────────────────────────────────────
    val medianCodecInitMs: Long?,
    val codecInitsCount: Int,
    val recentDecoderNames: List<String>,

    // ── Format changes ──────────────────────────────────────────────
    val formatChangesCount: Int,

    // ── Stall detector ──────────────────────────────────────────────
    val stallsDetectedCount: Int,
) {
    companion object {
        val EMPTY = AdaptivePlaybackSnapshot(
            snapshotTimestampMs = 0L,
            eventCount = 0,
            medianTtffMs = null,
            p95TtffMs = null,
            ttffSampleCount = 0,
            medianTtfbMs = null,
            p95TtfbMs = null,
            ttfbSampleCount = 0,
            rebufferCount = 0,
            rebufferRatePerMinute = 0.0,
            medianRecoveryMs = null,
            p95RecoveryMs = null,
            emaBitsPerSecond = null,
            lastBitsPerSecond = null,
            bandwidthSampleCount = 0,
            medianCodecInitMs = null,
            codecInitsCount = 0,
            recentDecoderNames = emptyList(),
            formatChangesCount = 0,
            stallsDetectedCount = 0,
        )
    }
}
