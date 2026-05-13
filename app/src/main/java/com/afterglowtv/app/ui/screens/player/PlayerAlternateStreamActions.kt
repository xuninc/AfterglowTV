package com.afterglowtv.app.ui.screens.player

import androidx.lifecycle.viewModelScope
import com.afterglowtv.domain.model.Channel
import com.afterglowtv.domain.model.ContentType
import com.afterglowtv.domain.model.LiveChannelVariant
import kotlinx.coroutines.launch

fun PlayerViewModel.hasAlternateStream(): Boolean {
    if (isCatchUpPlayback()) {
        return nextCatchUpVariant() != null
    }
    if (currentContentType != ContentType.LIVE) return false
    val channel = currentChannelFlow.value?.sanitizedForPlayer() ?: return false
    return nextLiveVariant(channel) != null ||
        selectNextAlternateUrl(
            candidateUrls = channel.alternativeStreams,
            currentStreamUrl = currentStreamUrl,
            triedAlternativeStreams = triedAlternativeStreams,
            failedStreamsThisSession = failedStreamsThisSession
        ) != null
}

fun PlayerViewModel.tryAlternateStream(): Boolean {
    if (isCatchUpPlayback()) {
        return tryNextCatchUpVariantInternal()
    }
    if (currentContentType != ContentType.LIVE) return false
    val channel = currentChannelFlow.value?.sanitizedForPlayer() ?: return false
    return tryAlternateStreamInternal(channel)
}

internal fun PlayerViewModel.tryAlternateStreamInternal(channel: Channel): Boolean {
    nextLiveVariant(channel)?.let { nextVariant ->
        val updatedChannel = channel.withSelectedVariant(nextVariant.rawChannelId)?.sanitizedForPlayer() ?: return@let
        val requestVersion = beginPlaybackSession()
        triedAlternativeStreams.add(nextVariant.streamUrl)
        currentContentId = updatedChannel.id
        currentStreamUrl = updatedChannel.streamUrl
        currentTitle = nextVariant.originalName.ifBlank { updatedChannel.name }
        playbackTitleFlow.value = currentTitle
        currentChannelFlow.value = updatedChannel
        if (currentChannelIndex in channelList.indices) {
            channelList = channelList.mapIndexed { index, existing ->
                if (index == currentChannelIndex || existing.logicalGroupId == updatedChannel.logicalGroupId) {
                    updatedChannel
                } else {
                    existing
                }
            }
            currentChannelFlowList.value = channelList
        }
        if (currentChannelIndex >= 0) {
            displayChannelNumberFlow.value = resolveChannelNumber(updatedChannel, currentChannelIndex)
        }
        refreshCurrentChannelRecording()
        updateChannelDiagnostics(updatedChannel)
        updateStreamClass("Variant")
        viewModelScope.launch {
            preferencesRepository.setPreferredLiveVariant(
                providerId = updatedChannel.providerId,
                logicalGroupId = updatedChannel.logicalGroupId,
                rawChannelId = nextVariant.rawChannelId
            )
            val streamInfo = resolvePlaybackStreamInfo(
                logicalUrl = nextVariant.streamUrl,
                internalContentId = updatedChannel.id,
                providerId = updatedChannel.providerId,
                contentType = ContentType.LIVE
            ) ?: return@launch
            if (!isActivePlaybackSession(requestVersion, nextVariant.streamUrl)) return@launch
            requestEpg(
                providerId = updatedChannel.providerId,
                epgChannelId = updatedChannel.epgChannelId,
                streamId = updatedChannel.streamId,
                internalChannelId = updatedChannel.id
            )
            if (!preparePlayer(streamInfo.copy(title = streamInfo.title ?: currentTitle), requestVersion)) return@launch
            playerEngine.play()
        }
        return true
    }

    val nextStream = selectNextAlternateUrl(
        candidateUrls = channel.alternativeStreams,
        currentStreamUrl = currentStreamUrl,
        triedAlternativeStreams = triedAlternativeStreams,
        failedStreamsThisSession = failedStreamsThisSession
    ) ?: return false

    val requestVersion = beginPlaybackSession()
    triedAlternativeStreams.add(nextStream)
    currentStreamUrl = nextStream
    updateStreamClass("Alternate")
    viewModelScope.launch {
        val streamInfo = resolvePlaybackStreamInfo(nextStream, channel.id, channel.providerId, ContentType.LIVE)
            ?: return@launch
        if (!isActivePlaybackSession(requestVersion, nextStream)) return@launch
        if (!preparePlayer(streamInfo.copy(title = streamInfo.title ?: currentTitle), requestVersion)) return@launch
        playerEngine.play()
    }
    return true
}

private fun PlayerViewModel.nextLiveVariant(channel: Channel): LiveChannelVariant? {
    return selectNextLiveVariant(
        variants = channel.variants,
        currentVariantId = channel.selectedVariantId.takeIf { it > 0 } ?: channel.id,
        currentStreamUrl = currentStreamUrl,
        triedAlternativeStreams = triedAlternativeStreams,
        failedStreamsThisSession = failedStreamsThisSession
    )
}

internal fun PlayerViewModel.isCatchUpPlayback(): Boolean = isCatchUpPlayback.value

private fun PlayerViewModel.nextCatchUpVariant(): String? {
    return selectNextAlternateUrl(
        candidateUrls = pendingCatchUpUrls,
        currentStreamUrl = currentStreamUrl,
        triedAlternativeStreams = triedAlternativeStreams,
        failedStreamsThisSession = failedStreamsThisSession
    )
}

internal fun PlayerViewModel.tryNextCatchUpVariantInternal(): Boolean {
    val nextStream = nextCatchUpVariant() ?: return false
    val requestVersion = beginPlaybackSession()
    triedAlternativeStreams.add(nextStream)
    currentStreamUrl = nextStream
    updateStreamClass("Catch-up")
    viewModelScope.launch {
        val streamInfo = resolveCatchUpStreamInfo(
            candidateUrl = nextStream,
            title = currentTitle,
            currentContentId = currentContentId,
            currentProviderId = currentProviderId,
            resolveStreamInfo = ::resolvePlaybackStreamInfo
        )
            ?: return@launch
        if (!isActivePlaybackSession(requestVersion, nextStream)) return@launch
        if (!preparePlayer(streamInfo, requestVersion)) return@launch
        playerEngine.play()
    }
    return true
}