package com.afterglowtv.app.util

import com.afterglowtv.domain.util.isPlaybackComplete as domainIsPlaybackComplete

fun isPlaybackComplete(
    progressMs: Long,
    totalDurationMs: Long,
    threshold: Float = com.afterglowtv.domain.util.DEFAULT_PLAYBACK_COMPLETION_THRESHOLD
): Boolean = domainIsPlaybackComplete(progressMs, totalDurationMs, threshold)
