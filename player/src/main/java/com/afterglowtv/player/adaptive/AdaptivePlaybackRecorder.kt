package com.afterglowtv.player.adaptive

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The sink for all [AdaptivePlaybackEvent]s.
 *
 * Holds a rolling window of recent events and computes [AdaptivePlaybackSnapshot]
 * aggregates on demand. Designed to be the SINGLE shared instance across the
 * app — every player engine, every OkHttp interceptor, every controller reads
 * from and writes to this one recorder.
 *
 * Thread-safety: external callers use [record] from any thread. Internal
 * state is guarded by a single `synchronized` block per write — total cost
 * per event is one ArrayDeque add + one optional Log.i + one bounded drop
 * if the window is full. Snapshot computation is also synchronized, runs
 * in O(n log n) on the window size (sorting for percentiles) — only call
 * it when a controller actually needs fresh data, not on a tight loop.
 *
 * @param windowMs how far back to keep events. Defaults to 5 minutes; that's
 *     long enough to absorb a typical commercial break or temporary network
 *     blip without losing context.
 * @param maxEvents safety valve on the event queue when the window is busy.
 *     5000 events ≈ 17 events/sec sustained, which is well above any
 *     realistic emission rate.
 * @param logToLogcat when true, each recorded event also lands in logcat
 *     under the [TAG]. Useful in dev; should be off in release.
 */
@Singleton
class AdaptivePlaybackRecorder @Inject constructor() {

    @Volatile private var windowMs: Long = DEFAULT_WINDOW_MS
    @Volatile private var maxEvents: Int = DEFAULT_MAX_EVENTS
    @Volatile var logToLogcat: Boolean = false

    private val lock = Any()
    private val window = ArrayDeque<AdaptivePlaybackEvent>()

    private val _events = MutableSharedFlow<AdaptivePlaybackEvent>(
        replay = 0,
        extraBufferCapacity = 64,
    )
    val events: SharedFlow<AdaptivePlaybackEvent> = _events.asSharedFlow()

    fun configure(windowMs: Long = DEFAULT_WINDOW_MS, maxEvents: Int = DEFAULT_MAX_EVENTS) {
        this.windowMs = windowMs
        this.maxEvents = maxEvents
    }

    fun record(event: AdaptivePlaybackEvent) {
        synchronized(lock) {
            window.addLast(event)
            prune(event.timestampMs)
        }
        if (logToLogcat) {
            Log.i(TAG, formatForLog(event))
        }
        _events.tryEmit(event)
    }

    /** Clear the rolling window. Used on engine reset or app cold start. */
    fun reset() {
        synchronized(lock) { window.clear() }
    }

    /** Compute aggregates over the current window. O(n log n) due to percentile sorts. */
    fun snapshot(nowMs: Long = System.currentTimeMillis()): AdaptivePlaybackSnapshot {
        val events = synchronized(lock) {
            prune(nowMs)
            window.toList()
        }
        if (events.isEmpty()) return AdaptivePlaybackSnapshot.EMPTY.copy(snapshotTimestampMs = nowMs)

        // Partition by event type so each percentile/aggregation only sees relevant samples.
        val ttffs = events.asSequence().filterIsInstance<AdaptivePlaybackEvent.FirstFrame>().map { it.ttffMs }.toList()
        val ttfbs = events.asSequence().filterIsInstance<AdaptivePlaybackEvent.HttpFirstByte>().map { it.ttfbMs }.toList()
        val rebufferEnds = events.asSequence().filterIsInstance<AdaptivePlaybackEvent.RebufferEnd>().toList()
        val rebufferStarts = events.asSequence().filterIsInstance<AdaptivePlaybackEvent.RebufferStart>().toList()
        val recoveries = rebufferEnds.map { it.recoveryMs }
        val bandwidths = events.asSequence().filterIsInstance<AdaptivePlaybackEvent.BandwidthSample>().toList()
        val codecInits = events.asSequence().filterIsInstance<AdaptivePlaybackEvent.CodecInit>().toList()
        val formatChanges = events.count { it is AdaptivePlaybackEvent.FormatChange }
        val stallsDetected = events.count { it is AdaptivePlaybackEvent.StallDetected }

        val windowDurationMs = windowMs.coerceAtLeast(1L)
        val rebufferRatePerMinute = rebufferStarts.size * 60_000.0 / windowDurationMs

        return AdaptivePlaybackSnapshot(
            snapshotTimestampMs = nowMs,
            eventCount = events.size,
            medianTtffMs = ttffs.percentile(0.50),
            p95TtffMs = ttffs.percentile(0.95),
            ttffSampleCount = ttffs.size,
            medianTtfbMs = ttfbs.percentile(0.50),
            p95TtfbMs = ttfbs.percentile(0.95),
            ttfbSampleCount = ttfbs.size,
            rebufferCount = rebufferStarts.size,
            rebufferRatePerMinute = rebufferRatePerMinute,
            medianRecoveryMs = recoveries.percentile(0.50),
            p95RecoveryMs = recoveries.percentile(0.95),
            emaBitsPerSecond = bandwidths.takeIf { it.isNotEmpty() }?.let { ema(it.map { s -> s.bitsPerSecond }) },
            lastBitsPerSecond = bandwidths.lastOrNull()?.bitsPerSecond,
            bandwidthSampleCount = bandwidths.size,
            medianCodecInitMs = codecInits.map { it.initDurationMs }.percentile(0.50),
            codecInitsCount = codecInits.size,
            recentDecoderNames = codecInits.takeLast(5).map { it.decoderName },
            formatChangesCount = formatChanges,
            stallsDetectedCount = stallsDetected,
        )
    }

    private fun prune(nowMs: Long) {
        val cutoff = nowMs - windowMs
        while (window.isNotEmpty() && window.first().timestampMs < cutoff) {
            window.removeFirst()
        }
        // Hard cap as a safety valve in case the window logic is wrong.
        while (window.size > maxEvents) {
            window.removeFirst()
        }
    }

    private fun List<Long>.percentile(p: Double): Long? {
        if (size < MIN_SAMPLES_FOR_PERCENTILE) return null
        val sorted = sorted()
        val idx = ((sorted.size - 1) * p).toInt().coerceIn(0, sorted.size - 1)
        return sorted[idx]
    }

    /** Exponentially-weighted moving average — newer samples weigh more. */
    private fun ema(samples: List<Long>): Long {
        if (samples.isEmpty()) return 0L
        var ema = samples.first().toDouble()
        val alpha = 0.3
        for (i in 1 until samples.size) {
            ema = alpha * samples[i] + (1.0 - alpha) * ema
        }
        return ema.toLong()
    }

    private fun formatForLog(event: AdaptivePlaybackEvent): String = when (event) {
        is AdaptivePlaybackEvent.FirstFrame -> "first-frame ttff=${event.ttffMs}ms"
        is AdaptivePlaybackEvent.RebufferStart -> "rebuffer-start buffered=${event.bufferedDurationMs}ms"
        is AdaptivePlaybackEvent.RebufferEnd -> "rebuffer-end recovery=${event.recoveryMs}ms"
        is AdaptivePlaybackEvent.BandwidthSample -> "bandwidth bps=${event.bitsPerSecond}"
        is AdaptivePlaybackEvent.CodecInit -> "codec-init name=${event.decoderName} dur=${event.initDurationMs}ms hw=${event.isHardware}"
        is AdaptivePlaybackEvent.FormatChange -> "format-change to-mime=${event.toMime} bitrate=${event.toBitrate}"
        is AdaptivePlaybackEvent.StallDetected -> "stall-detected buffered=${event.bufferedDurationMs}ms"
        is AdaptivePlaybackEvent.HttpFirstByte -> "http-ttfb host=${event.host} ttfb=${event.ttfbMs}ms bytes=${event.responseBodyBytes}"
    }

    companion object {
        private const val TAG = "AdaptivePlayback"
        const val DEFAULT_WINDOW_MS = 5L * 60_000L // 5 minutes
        const val DEFAULT_MAX_EVENTS = 5000
        const val MIN_SAMPLES_FOR_PERCENTILE = 3
    }
}
