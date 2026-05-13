package com.afterglowtv.player.adaptive

/**
 * Typed events emitted by the player as observable units of telemetry.
 *
 * Controllers (BandwidthController, BufferController, etc.) read aggregated
 * windows of these to make policy decisions. The events themselves are
 * append-only and cheap to record — aggregation happens lazily in
 * [AdaptivePlaybackRecorder.snapshot].
 *
 * Every event carries a wall-clock [timestampMs] so windowed aggregates
 * (rebuffer rate per minute, recent throughput percentile, etc.) can be
 * computed deterministically without coupling to monotonic clocks.
 */
sealed class AdaptivePlaybackEvent {
    abstract val timestampMs: Long

    /** First frame on screen since the most recent prepare. */
    data class FirstFrame(
        val ttffMs: Long,
        val mediaId: String,
        override val timestampMs: Long,
    ) : AdaptivePlaybackEvent()

    /** Playback dropped from READY → BUFFERING (started rebuffering). */
    data class RebufferStart(
        val mediaId: String,
        val bufferedDurationMs: Long,
        override val timestampMs: Long,
    ) : AdaptivePlaybackEvent()

    /** Playback recovered from BUFFERING → READY (rebuffer ended). */
    data class RebufferEnd(
        val mediaId: String,
        val recoveryMs: Long,
        override val timestampMs: Long,
    ) : AdaptivePlaybackEvent()

    /**
     * Throughput sample from ExoPlayer's bandwidth meter. Multiple per
     * second during steady-state HLS playback.
     */
    data class BandwidthSample(
        val bitsPerSecond: Long,
        override val timestampMs: Long,
    ) : AdaptivePlaybackEvent()

    /** MediaCodec initialised. Duration is wall-clock from queueInputBuffer to ready. */
    data class CodecInit(
        val decoderName: String,
        val initDurationMs: Long,
        val isHardware: Boolean,
        val mimeType: String?,
        override val timestampMs: Long,
    ) : AdaptivePlaybackEvent()

    /**
     * Mid-stream format change — typically an HLS variant switch or DASH
     * representation change. Cost matters because it may trigger a decoder
     * re-init.
     */
    data class FormatChange(
        val mediaId: String,
        val fromMime: String?,
        val toMime: String,
        val toBitrate: Int,
        override val timestampMs: Long,
    ) : AdaptivePlaybackEvent()

    /** VideoStallDetector flagged a stall the player itself didn't catch. */
    data class StallDetected(
        val mediaId: String,
        val bufferedDurationMs: Long,
        override val timestampMs: Long,
    ) : AdaptivePlaybackEvent()

    /** Per-HTTP-request time-to-first-byte, recorded by the network interceptor. */
    data class HttpFirstByte(
        val host: String,
        val ttfbMs: Long,
        val responseBodyBytes: Long,
        override val timestampMs: Long,
    ) : AdaptivePlaybackEvent()
}
