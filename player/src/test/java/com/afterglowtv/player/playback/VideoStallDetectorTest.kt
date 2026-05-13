package com.afterglowtv.player.playback

import com.google.common.truth.Truth.assertThat
import com.afterglowtv.player.PlaybackState
import org.junit.Test

class VideoStallDetectorTest {
    private var nowMs = 0L

    @Test
    fun `does not report before initial grace`() {
        val detector = detector()
        detector.reset()
        detector.onVideoFrameRendered()
        nowMs = 3_000L

        assertThat(
            detector.shouldReportStall(
                playbackState = PlaybackState.READY,
                isPlaying = true,
                playbackStarted = true,
                currentPositionMs = 3_000L,
                bufferedDurationMs = 5_000L
            )
        ).isFalse()
    }

    @Test
    fun `reports once when position advances and frames stop`() {
        val detector = detector()
        detector.reset()
        nowMs = 1_000L
        detector.onVideoFrameRendered(currentPositionMs = 500L)
        nowMs = 9_000L

        assertThat(
            detector.shouldReportStall(
                playbackState = PlaybackState.READY,
                isPlaying = true,
                playbackStarted = true,
                currentPositionMs = 3_000L,
                bufferedDurationMs = 5_000L
            )
        ).isTrue()
        assertThat(
            detector.shouldReportStall(
                playbackState = PlaybackState.READY,
                isPlaying = true,
                playbackStarted = true,
                currentPositionMs = 5_000L,
                bufferedDurationMs = 5_000L
            )
        ).isFalse()
    }

    @Test
    fun `does not report while buffering paused or empty buffered`() {
        val detector = detector()
        detector.reset()
        nowMs = 1_000L
        detector.onVideoFrameRendered(currentPositionMs = 500L)
        nowMs = 9_000L

        assertThat(detector.shouldReportStall(PlaybackState.BUFFERING, true, true, 4_000L, 5_000L)).isFalse()
        assertThat(detector.shouldReportStall(PlaybackState.READY, false, true, 4_000L, 5_000L)).isFalse()
        assertThat(detector.shouldReportStall(PlaybackState.READY, true, false, 4_000L, 5_000L)).isFalse()
        assertThat(detector.shouldReportStall(PlaybackState.READY, true, true, 4_000L, 0L)).isFalse()
    }

    @Test
    fun `new rendered frame clears stalled state`() {
        val detector = detector()
        detector.reset()
        nowMs = 1_000L
        detector.onVideoFrameRendered(currentPositionMs = 500L)
        nowMs = 9_000L
        assertThat(detector.shouldReportStall(PlaybackState.READY, true, true, 3_000L, 5_000L)).isTrue()

        nowMs = 9_100L
        detector.onVideoFrameRendered(currentPositionMs = 3_000L)
        nowMs = 15_000L
        assertThat(detector.shouldReportStall(PlaybackState.READY, true, true, 6_000L, 5_000L)).isTrue()
    }

    @Test
    fun `does not report before playback advances beyond first frame`() {
        val detector = detector()
        detector.reset()
        nowMs = 1_000L
        detector.onVideoFrameRendered(currentPositionMs = 2_500L)
        nowMs = 9_000L

        assertThat(
            detector.shouldReportStall(
                playbackState = PlaybackState.READY,
                isPlaying = true,
                playbackStarted = true,
                currentPositionMs = 3_000L,
                bufferedDurationMs = 5_000L
            )
        ).isFalse()
    }

    private fun detector() = VideoStallDetector(
        nowMs = { nowMs },
        initialGraceMs = 5_000L,
        stallThresholdMs = 4_000L,
        minPositionAdvanceMs = 1_000L
    )
}

