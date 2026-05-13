package com.afterglowtv.player.playback

import com.afterglowtv.player.PlaybackState

class VideoStallDetector(
    private val nowMs: () -> Long = System::currentTimeMillis,
    // Cold-start window where we ignore stalls — HLS / MPEG-TS often take
    // 8-12s to deliver the first segment on weak Wi-Fi. Reporting a stall
    // during the warmup just triggers a redundant retry.
    private val initialGraceMs: Long = 12_000L,
    // How long the player can sit on a buffer-loading state before we flag a
    // real stall. Bumped from 5s — at 5s, transient HLS-segment fetches that
    // exceed a few seconds got misclassified as stalls.
    private val stallThresholdMs: Long = 7_000L,
    private val minPositionAdvanceMs: Long = 1_500L
) {
    private var startedAtMs: Long = 0L
    private var lastFrameAtMs: Long = 0L
    private var firstFramePositionMs: Long = 0L
    private var lastPositionMs: Long = 0L
    private var lastPositionCheckMs: Long = 0L
    private var stalled = false

    fun reset() {
        val now = nowMs()
        startedAtMs = now
        lastFrameAtMs = 0L
        firstFramePositionMs = 0L
        lastPositionMs = 0L
        lastPositionCheckMs = now
        stalled = false
    }

    fun onVideoFrameRendered(currentPositionMs: Long = 0L) {
        if (lastFrameAtMs <= 0L) {
            firstFramePositionMs = currentPositionMs.coerceAtLeast(0L)
        }
        lastFrameAtMs = nowMs()
        stalled = false
    }

    fun lastVideoFrameAgoMs(): Long {
        val frameAt = lastFrameAtMs
        return if (frameAt <= 0L) 0L else (nowMs() - frameAt).coerceAtLeast(0L)
    }

    fun shouldReportStall(
        playbackState: PlaybackState,
        isPlaying: Boolean,
        playbackStarted: Boolean,
        currentPositionMs: Long,
        bufferedDurationMs: Long
    ): Boolean {
        val now = nowMs()
        if (playbackState != PlaybackState.READY || !isPlaying || !playbackStarted || bufferedDurationMs <= 1_000L) {
            lastPositionMs = currentPositionMs
            lastPositionCheckMs = now
            stalled = false
            return false
        }
        if (now - startedAtMs < initialGraceMs || lastFrameAtMs <= 0L) return false
        if (currentPositionMs - firstFramePositionMs < minPositionAdvanceMs) return false

        val positionAdvanced = currentPositionMs - lastPositionMs >= minPositionAdvanceMs
        val checkWindowElapsed = now - lastPositionCheckMs >= minPositionAdvanceMs
        if (checkWindowElapsed) {
            lastPositionMs = currentPositionMs
            lastPositionCheckMs = now
        }

        val frameSilent = now - lastFrameAtMs >= stallThresholdMs
        val nextStalled = positionAdvanced && frameSilent
        if (nextStalled && !stalled) {
            stalled = true
            return true
        }
        if (!frameSilent) stalled = false
        return false
    }
}

