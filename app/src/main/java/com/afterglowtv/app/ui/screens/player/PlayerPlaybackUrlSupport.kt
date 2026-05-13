package com.afterglowtv.app.ui.screens.player

import com.afterglowtv.domain.model.StreamInfo

internal fun String?.safeTrimmedOrNull(): String? {
    val value = this ?: return null
    return value.trim().takeIf { it.isNotEmpty() }
}

internal fun resolveTimeshiftStreamInfo(
    streamInfoOverride: StreamInfo?,
    currentResolvedStreamInfo: StreamInfo?,
    currentResolvedPlaybackUrl: String,
    currentStreamUrl: String,
    playbackTitle: String,
    currentTitle: String
): StreamInfo? {
    val resolvedTitle = playbackTitle.ifBlank { currentTitle }
    streamInfoOverride?.let { override ->
        return override.copy(title = override.title ?: resolvedTitle)
    }
    currentResolvedStreamInfo?.let { resolved ->
        val resolvedUrl = resolved.url.safeTrimmedOrNull()
        if (resolvedUrl != null) {
            return resolved.copy(
                url = resolvedUrl,
                title = resolved.title ?: resolvedTitle
            )
        }
    }
    val fallbackUrl = currentResolvedPlaybackUrl.safeTrimmedOrNull()
        ?: currentStreamUrl.safeTrimmedOrNull()
        ?: return null
    return StreamInfo(url = fallbackUrl, title = resolvedTitle)
}

internal fun resolvePlaybackIdentityUrl(
    currentResolvedPlaybackUrl: String,
    currentStreamUrl: String
): String = currentResolvedPlaybackUrl.ifBlank { currentStreamUrl }

internal fun resolvePlaybackProbeCacheKey(
    currentStreamUrl: String,
    url: String
): String = currentStreamUrl.takeIf { it.isNotBlank() } ?: url

internal fun matchesActivePlaybackSession(
    requestVersion: Long,
    activeRequestVersion: Long,
    expectedLogicalUrl: String? = null,
    currentResolvedPlaybackUrl: String,
    currentStreamUrl: String
): Boolean {
    if (requestVersion != activeRequestVersion) return false
    val expectedUrl = expectedLogicalUrl?.takeIf { it.isNotBlank() } ?: return true
    val activeUrl = resolvePlaybackIdentityUrl(
        currentResolvedPlaybackUrl = currentResolvedPlaybackUrl,
        currentStreamUrl = currentStreamUrl
    )
    return activeUrl.isBlank() || activeUrl == expectedUrl || currentStreamUrl == expectedUrl
}