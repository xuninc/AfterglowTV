package com.afterglowtv.app.ui.screens.player

import com.afterglowtv.domain.model.LiveChannelVariant

internal fun buildPlayerRecoveryActions(
    hasAlternateStream: Boolean,
    hasLastChannel: Boolean,
    shouldOfferGuide: Boolean
): List<PlayerNoticeAction> {
    val actions = mutableListOf(PlayerNoticeAction.RETRY)
    if (hasAlternateStream) {
        actions += PlayerNoticeAction.ALTERNATE_STREAM
    }
    if (hasLastChannel) {
        actions += PlayerNoticeAction.LAST_CHANNEL
    }
    if (shouldOfferGuide) {
        actions += PlayerNoticeAction.OPEN_GUIDE
    }
    return actions
}

internal fun selectNextAlternateUrl(
    candidateUrls: List<String>,
    currentStreamUrl: String,
    triedAlternativeStreams: Set<String>,
    failedStreamsThisSession: Map<String, Int>
): String? {
    return candidateUrls.firstOrNull { altUrl ->
        altUrl != currentStreamUrl &&
            altUrl !in triedAlternativeStreams &&
            (failedStreamsThisSession[altUrl] ?: 0) == 0
    } ?: candidateUrls.firstOrNull { altUrl ->
        altUrl != currentStreamUrl && altUrl !in triedAlternativeStreams
    }
}

internal fun selectNextLiveVariant(
    variants: List<LiveChannelVariant>,
    currentVariantId: Long,
    currentStreamUrl: String,
    triedAlternativeStreams: Set<String>,
    failedStreamsThisSession: Map<String, Int>
): LiveChannelVariant? {
    return variants.firstOrNull { variant ->
        variant.rawChannelId != currentVariantId &&
            variant.streamUrl.isNotBlank() &&
            variant.streamUrl != currentStreamUrl &&
            variant.streamUrl !in triedAlternativeStreams &&
            (failedStreamsThisSession[variant.streamUrl] ?: 0) == 0
    } ?: variants.firstOrNull { variant ->
        variant.rawChannelId != currentVariantId &&
            variant.streamUrl.isNotBlank() &&
            variant.streamUrl != currentStreamUrl &&
            variant.streamUrl !in triedAlternativeStreams
    }
}