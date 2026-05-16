package com.afterglowtv.app.ui.screens.player

import androidx.lifecycle.viewModelScope
import com.afterglowtv.domain.model.Channel
import com.afterglowtv.domain.model.ChannelNumberingMode
import com.afterglowtv.domain.model.ContentType
import com.afterglowtv.domain.model.PlaybackHistory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal const val MAX_NUMERIC_CHANNEL_INPUT_DIGITS = 6

internal fun appendNumericChannelDigit(currentBuffer: String, digit: Int): String {
    val nextDigit = digit.toString()
    return if (currentBuffer.length >= MAX_NUMERIC_CHANNEL_INPUT_DIGITS) {
        nextDigit
    } else {
        currentBuffer + nextDigit
    }
}

internal fun isLinearLiveChannelZapAllowed(
    currentContentType: ContentType,
    isCatchUpPlayback: Boolean,
    hasChannels: Boolean
): Boolean = currentContentType == ContentType.LIVE && !isCatchUpPlayback && hasChannels

internal data class LivePlaybackRecordCandidate(
    val playbackKey: Pair<Long, Long>,
    val history: PlaybackHistory
)

internal fun buildLivePlaybackRecordCandidate(
    currentProviderId: Long,
    currentContentType: ContentType,
    currentContentId: Long,
    currentTitle: String,
    currentResolvedPlaybackUrl: String,
    currentStreamUrl: String,
    channel: Channel?
): LivePlaybackRecordCandidate? {
    if (currentProviderId <= 0L || currentContentType != ContentType.LIVE) return null

    channel?.let {
        return LivePlaybackRecordCandidate(
            playbackKey = currentProviderId to it.id,
            history = PlaybackHistory(
                contentId = it.id,
                contentType = ContentType.LIVE,
                providerId = currentProviderId,
                title = it.name,
                streamUrl = it.streamUrl,
                lastWatchedAt = System.currentTimeMillis()
            )
        )
    }

    val contentId = currentContentId.takeIf { it > 0L } ?: return null
    val streamUrl = currentResolvedPlaybackUrl.ifBlank { currentStreamUrl }.takeIf { it.isNotBlank() } ?: return null
    return LivePlaybackRecordCandidate(
        playbackKey = currentProviderId to contentId,
        history = PlaybackHistory(
            contentId = contentId,
            contentType = ContentType.LIVE,
            providerId = currentProviderId,
            title = currentTitle,
            streamUrl = streamUrl,
            lastWatchedAt = System.currentTimeMillis()
        )
    )
}

internal suspend fun <T> withScopedScrubbingMode(
    setScrubbingMode: (Boolean) -> Unit,
    block: suspend () -> T
): T {
    setScrubbingMode(true)
    return try {
        block()
    } finally {
        setScrubbingMode(false)
    }
}

fun PlayerViewModel.playNext() {
    clearNumericChannelInput()
    if (!isLinearLiveChannelZapAllowed(currentContentType, isCatchUpPlayback(), channelList.isNotEmpty())) return
    val nextIndex = wrappedChannelIndex(1)
    if (nextIndex == -1) return
    changeChannel(nextIndex)
}

fun PlayerViewModel.playPrevious() {
    clearNumericChannelInput()
    if (!isLinearLiveChannelZapAllowed(currentContentType, isCatchUpPlayback(), channelList.isNotEmpty())) return
    val prevIndex = wrappedChannelIndex(-1)
    if (prevIndex == -1) return
    changeChannel(prevIndex)
}

fun PlayerViewModel.zapToChannel(channelId: Long) {
    clearNumericChannelInput()
    if (!isLinearLiveChannelZapAllowed(currentContentType, isCatchUpPlayback(), channelList.isNotEmpty())) return
    val index = channelList.indexOfFirst { it.id == channelId }
    if (index != -1) {
        changeChannel(index)
        closeOverlays()
    }
}

fun PlayerViewModel.zapToLastChannel() {
    clearNumericChannelInput()
    if (!isLinearLiveChannelZapAllowed(currentContentType, isCatchUpPlayback(), channelList.isNotEmpty())) return
    if (previousChannelIndex in channelList.indices && previousChannelIndex != currentChannelIndex) {
        changeChannel(previousChannelIndex)
    }
}

fun PlayerViewModel.hasLastChannel(): Boolean {
    if (!isLinearLiveChannelZapAllowed(currentContentType, isCatchUpPlayback(), channelList.isNotEmpty())) return false
    val channels = channelList
    return previousChannelIndex in channels.indices && previousChannelIndex != currentChannelIndex
}

fun PlayerViewModel.hasPendingNumericChannelInput(): Boolean = numericInputBuffer.isNotBlank()

fun PlayerViewModel.inputNumericChannelDigit(digit: Int) {
    if (!isLinearLiveChannelZapAllowed(currentContentType, isCatchUpPlayback(), channelList.isNotEmpty())) return
    if (digit !in 0..9) return

    numericInputBuffer = appendNumericChannelDigit(numericInputBuffer, digit)
    val exactMatch = resolveChannelByNumber(numericInputBuffer.toIntOrNull())
    val previewMatch = exactMatch ?: resolveChannelByPrefix(numericInputBuffer)

    numericChannelInputFlow.value = NumericChannelInputState(
        input = numericInputBuffer,
        matchedChannelName = previewMatch?.name,
        invalid = false
    )

    scheduleNumericChannelCommit()
}

fun PlayerViewModel.commitNumericChannelInput() {
    numericInputCommitJob?.cancel()
    if (numericInputBuffer.isBlank()) return

    // "0" committed alone after timeout zaps to last channel.
    if (numericInputBuffer == "0" && hasLastChannel()) {
        clearNumericChannelInput()
        zapToLastChannel()
        return
    }

    val targetChannel = resolveChannelByNumber(numericInputBuffer.toIntOrNull())
    if (targetChannel != null) {
        val targetIndex = channelList.indexOfFirst { it.id == targetChannel.id }
        if (targetIndex != -1) {
            changeChannel(targetIndex)
        }
        clearNumericChannelInput()
        return
    }

    numericChannelInputFlow.value = NumericChannelInputState(
        input = numericInputBuffer,
        matchedChannelName = null,
        invalid = true
    )

    numericInputFeedbackJob?.cancel()
    numericInputFeedbackJob = viewModelScope.launch {
        delay(900)
        clearNumericChannelInput()
    }
}

fun PlayerViewModel.clearNumericChannelInput() {
    numericInputCommitJob?.cancel()
    numericInputFeedbackJob?.cancel()
    numericInputBuffer = ""
    numericChannelInputFlow.value = null
}

internal fun PlayerViewModel.changeChannel(index: Int, isAutoFallback: Boolean = false) {
    check(index in channelList.indices) {
        "changeChannel index=$index out of channelList bounds (size=${channelList.size})"
    }
    clearNumericChannelInput()
    if (currentChannelIndex != -1 && currentChannelIndex != index) {
        previousChannelIndex = currentChannelIndex
    }
    val requestVersion = beginPlaybackSession()
    val channel = channelList[index]
    currentChannelIndex = index
    currentContentId = channel.id
    currentTitle = channel.name
    playbackTitleFlow.value = currentTitle
    currentStreamUrl = channel.streamUrl
    pendingCatchUpUrls = emptyList()
    updateStreamClass("Primary")
    currentChannelFlow.value = channel
    refreshCurrentChannelRecording()
    displayChannelNumberFlow.value = resolveChannelNumber(channel, index)
    recentChannelsFlow.update { channels -> channels.filterNot { it.id == channel.id } }

    viewModelScope.launch {
        withScopedScrubbingMode(playerEngine::setScrubbingMode) {
            val streamInfo = resolvePlaybackStreamInfo(channel.streamUrl, channel.id, channel.providerId, ContentType.LIVE)
                ?: return@withScopedScrubbingMode
            if (!isActivePlaybackSession(requestVersion, channel.streamUrl)) return@withScopedScrubbingMode
            if (!preparePlayer(streamInfo, requestVersion)) return@withScopedScrubbingMode
            playerEngine.play()

            playerEngine.playbackState
                .filter {
                    it == com.afterglowtv.player.PlaybackState.READY ||
                        !isActivePlaybackSession(requestVersion, channel.streamUrl)
                }
                .first()
        }
    }

    preloadAdjacentChannel(index)

    requestEpg(
        providerId = currentProviderId,
        epgChannelId = channel.epgChannelId,
        streamId = channel.streamId,
        internalChannelId = channel.id
    )

    showZapOverlayFlow.value = false
    showControlsFlow.value = false
    openChannelInfoOverlay()

    triedAlternativeStreams.clear()
    triedAlternativeStreams.add(channel.streamUrl)
    if (currentContentType == ContentType.LIVE && !isAutoFallback) scheduleZapBufferWatchdog(index)
}

internal fun PlayerViewModel.preloadAdjacentChannel(currentIndex: Int) {
    if (channelList.size < 2) return
    val nextIndex = (currentIndex + 1) % channelList.size
    val nextChannel = channelList[nextIndex]
    viewModelScope.launch {
        val streamInfo = resolvePlaybackStreamInfo(
            nextChannel.streamUrl,
            nextChannel.id,
            nextChannel.providerId,
            ContentType.LIVE
        ) ?: return@launch
        playerEngine.preload(streamInfo)
    }
}

internal fun PlayerViewModel.recordLivePlayback(channel: com.afterglowtv.domain.model.Channel) {
    recordActiveLivePlayback(channel)
}

internal fun PlayerViewModel.recordActiveLivePlayback(channel: Channel? = currentChannelFlow.value?.sanitizedForPlayer()) {
    val candidate = buildLivePlaybackRecordCandidate(
        currentProviderId = currentProviderId,
        currentContentType = currentContentType,
        currentContentId = currentContentId,
        currentTitle = currentTitle,
        currentResolvedPlaybackUrl = currentResolvedPlaybackUrl,
        currentStreamUrl = currentStreamUrl,
        channel = channel
    ) ?: return

    if (lastRecordedLivePlaybackKey == candidate.playbackKey) return
    lastRecordedLivePlaybackKey = candidate.playbackKey

    viewModelScope.launch {
        logRepositoryFailure(
            operation = "Record live playback",
            result = playbackHistoryRepository.recordPlayback(candidate.history)
        )
    }
}

internal fun PlayerViewModel.scheduleNumericChannelCommit() {
    numericInputCommitJob?.cancel()
    numericInputCommitJob = viewModelScope.launch {
        delay(1300)
        commitNumericChannelInput()
    }
}

internal fun PlayerViewModel.resolveChannelByNumber(number: Int?): com.afterglowtv.domain.model.Channel? {
    if (number == null) return null
    return channelNumberIndex[number]
}

internal fun PlayerViewModel.resolveChannelByPrefix(prefix: String): com.afterglowtv.domain.model.Channel? {
    return channelNumberIndex.entries
        .firstOrNull { (key, _) -> key.toString().startsWith(prefix) }
        ?.value
}

internal fun PlayerViewModel.resolveChannelNumber(
    channel: com.afterglowtv.domain.model.Channel,
    index: Int
): Int = when (channelNumberingMode) {
    ChannelNumberingMode.GROUP -> if (index >= 0) index + 1 else channel.number.takeIf { it > 0 } ?: 0
    ChannelNumberingMode.PROVIDER -> channel.number.takeIf { it > 0 } ?: if (index >= 0) index + 1 else 0
    ChannelNumberingMode.HIDDEN -> 0
}
