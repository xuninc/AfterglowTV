package com.afterglowtv.player.adaptive

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AdaptivePlaybackRecorderTest {

    @Test
    fun `empty recorder yields EMPTY snapshot with current timestamp`() {
        val recorder = AdaptivePlaybackRecorder()
        val snapshot = recorder.snapshot(nowMs = 1_000L)
        assertThat(snapshot.eventCount).isEqualTo(0)
        assertThat(snapshot.snapshotTimestampMs).isEqualTo(1_000L)
        assertThat(snapshot.medianTtffMs).isNull()
        assertThat(snapshot.rebufferCount).isEqualTo(0)
    }

    @Test
    fun `first-frame events produce median and p95`() {
        val recorder = AdaptivePlaybackRecorder()
        val baseTimestamp = 1_000L
        // 5 TTFF samples: 100, 200, 300, 400, 500
        listOf(100L, 200L, 300L, 400L, 500L).forEachIndexed { i, ttff ->
            recorder.record(
                AdaptivePlaybackEvent.FirstFrame(
                    ttffMs = ttff,
                    mediaId = "m$i",
                    timestampMs = baseTimestamp + i,
                )
            )
        }
        val snapshot = recorder.snapshot(nowMs = baseTimestamp + 10)
        assertThat(snapshot.ttffSampleCount).isEqualTo(5)
        // Median of [100,200,300,400,500] sorted by ceiling-index = sorted[(5-1) * 0.5] = sorted[2] = 300
        assertThat(snapshot.medianTtffMs).isEqualTo(300L)
        // p95 = sorted[(5-1) * 0.95] = sorted[3] = 400
        assertThat(snapshot.p95TtffMs).isEqualTo(400L)
    }

    @Test
    fun `percentile returns null when below min samples`() {
        val recorder = AdaptivePlaybackRecorder()
        // Min samples = 3, we record 2
        recorder.record(AdaptivePlaybackEvent.FirstFrame(100L, "m1", 1L))
        recorder.record(AdaptivePlaybackEvent.FirstFrame(200L, "m2", 2L))
        val snapshot = recorder.snapshot(nowMs = 10L)
        assertThat(snapshot.ttffSampleCount).isEqualTo(2)
        assertThat(snapshot.medianTtffMs).isNull()
        assertThat(snapshot.p95TtffMs).isNull()
    }

    @Test
    fun `rebuffer rate projects per minute from window`() {
        val recorder = AdaptivePlaybackRecorder()
        recorder.configure(windowMs = 60_000L) // 1-minute window for easy math
        // 3 rebuffer-starts in the last minute = 3 per minute
        repeat(3) { i ->
            recorder.record(
                AdaptivePlaybackEvent.RebufferStart(
                    mediaId = "m$i",
                    bufferedDurationMs = 0L,
                    timestampMs = (i + 1) * 1_000L,
                )
            )
        }
        val snapshot = recorder.snapshot(nowMs = 60_000L)
        assertThat(snapshot.rebufferCount).isEqualTo(3)
        assertThat(snapshot.rebufferRatePerMinute).isWithin(0.01).of(3.0)
    }

    @Test
    fun `window pruning drops events older than windowMs`() {
        val recorder = AdaptivePlaybackRecorder()
        recorder.configure(windowMs = 5_000L)
        // Three events spread across 10 seconds. The first should be pruned.
        recorder.record(AdaptivePlaybackEvent.FirstFrame(100L, "m1", timestampMs = 0L))
        recorder.record(AdaptivePlaybackEvent.FirstFrame(200L, "m2", timestampMs = 6_000L))
        recorder.record(AdaptivePlaybackEvent.FirstFrame(300L, "m3", timestampMs = 9_000L))
        val snapshot = recorder.snapshot(nowMs = 10_000L)
        // Only events with timestampMs >= (10_000 - 5_000) = 5_000 survive.
        assertThat(snapshot.eventCount).isEqualTo(2)
        assertThat(snapshot.ttffSampleCount).isEqualTo(2)
    }

    @Test
    fun `bandwidth EMA weighs recent samples more heavily`() {
        val recorder = AdaptivePlaybackRecorder()
        // Stable at 1 Mbps, then jumps to 10 Mbps for 4 samples.
        // With alpha=0.3, EMA should be much closer to 10 Mbps than 1 Mbps.
        listOf(1_000_000L, 1_000_000L, 1_000_000L, 10_000_000L, 10_000_000L, 10_000_000L, 10_000_000L)
            .forEachIndexed { i, bps ->
                recorder.record(
                    AdaptivePlaybackEvent.BandwidthSample(bitsPerSecond = bps, timestampMs = i.toLong())
                )
            }
        val snapshot = recorder.snapshot(nowMs = 100L)
        assertThat(snapshot.bandwidthSampleCount).isEqualTo(7)
        assertThat(snapshot.lastBitsPerSecond).isEqualTo(10_000_000L)
        // EMA should be > 5 Mbps (more than halfway to the new value after 4 samples at the new rate)
        assertThat(snapshot.emaBitsPerSecond!!).isGreaterThan(5_000_000L)
        assertThat(snapshot.emaBitsPerSecond!!).isLessThan(10_000_000L)
    }

    @Test
    fun `rebuffer recovery duration captured from start to end`() {
        val recorder = AdaptivePlaybackRecorder()
        recorder.record(AdaptivePlaybackEvent.RebufferStart("m", 1000L, timestampMs = 100L))
        recorder.record(AdaptivePlaybackEvent.RebufferEnd("m", recoveryMs = 4_200L, timestampMs = 4_300L))
        recorder.record(AdaptivePlaybackEvent.RebufferStart("m", 1000L, timestampMs = 5_000L))
        recorder.record(AdaptivePlaybackEvent.RebufferEnd("m", recoveryMs = 1_800L, timestampMs = 6_800L))
        recorder.record(AdaptivePlaybackEvent.RebufferStart("m", 1000L, timestampMs = 8_000L))
        recorder.record(AdaptivePlaybackEvent.RebufferEnd("m", recoveryMs = 600L, timestampMs = 8_600L))
        val snapshot = recorder.snapshot(nowMs = 9_000L)
        assertThat(snapshot.rebufferCount).isEqualTo(3)
        // Recoveries: [4200, 1800, 600] sorted = [600, 1800, 4200]
        // Median index = (3-1) * 0.5 = 1 → 1800
        assertThat(snapshot.medianRecoveryMs).isEqualTo(1_800L)
        // p95 index = (3-1) * 0.95 = 1 → 1800
        assertThat(snapshot.p95RecoveryMs).isEqualTo(1_800L)
    }

    @Test
    fun `codec init events tracked with hardware flag`() {
        val recorder = AdaptivePlaybackRecorder()
        recorder.record(AdaptivePlaybackEvent.CodecInit("OMX.qcom.video.decoder.avc", 120L, isHardware = true, mimeType = "video/avc", timestampMs = 1L))
        recorder.record(AdaptivePlaybackEvent.CodecInit("c2.android.avc.decoder", 380L, isHardware = false, mimeType = "video/avc", timestampMs = 2L))
        recorder.record(AdaptivePlaybackEvent.CodecInit("OMX.qcom.video.decoder.hevc", 95L, isHardware = true, mimeType = "video/hevc", timestampMs = 3L))
        val snapshot = recorder.snapshot(nowMs = 100L)
        assertThat(snapshot.codecInitsCount).isEqualTo(3)
        // Inits: [120, 380, 95] sorted = [95, 120, 380]; median index = 1 → 120
        assertThat(snapshot.medianCodecInitMs).isEqualTo(120L)
        assertThat(snapshot.recentDecoderNames).containsExactly(
            "OMX.qcom.video.decoder.avc",
            "c2.android.avc.decoder",
            "OMX.qcom.video.decoder.hevc",
        ).inOrder()
    }

    @Test
    fun `reset clears the window`() {
        val recorder = AdaptivePlaybackRecorder()
        recorder.record(AdaptivePlaybackEvent.FirstFrame(100L, "m1", timestampMs = 1L))
        recorder.record(AdaptivePlaybackEvent.FirstFrame(200L, "m2", timestampMs = 2L))
        recorder.reset()
        val snapshot = recorder.snapshot(nowMs = 10L)
        assertThat(snapshot.eventCount).isEqualTo(0)
    }

    @Test
    fun `max events cap prevents unbounded growth`() {
        val recorder = AdaptivePlaybackRecorder()
        recorder.configure(windowMs = 60_000L, maxEvents = 10)
        // Record 15 events; oldest 5 should be dropped by the maxEvents cap.
        repeat(15) { i ->
            recorder.record(AdaptivePlaybackEvent.FirstFrame(100L, "m$i", timestampMs = (i + 1).toLong()))
        }
        val snapshot = recorder.snapshot(nowMs = 100L)
        assertThat(snapshot.eventCount).isEqualTo(10)
    }
}
