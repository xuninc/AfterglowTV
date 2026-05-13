package com.afterglowtv.player.playback

import com.afterglowtv.domain.model.StreamInfo

internal data class PlaybackPreparationPlan(
    val resolvedStreamType: ResolvedStreamType,
    val timeoutProfile: PlayerTimeoutProfile,
    val retryContext: PlaybackRetryContext,
    val retryPolicy: PlayerRetryPolicy
)

internal fun buildPlaybackPreparationPlan(
    streamInfo: StreamInfo,
    preload: Boolean,
    playbackStarted: () -> Boolean
): PlaybackPreparationPlan {
    val resolvedStreamType = StreamTypeResolver.resolve(streamInfo)
    val timeoutProfile = PlayerTimeoutProfile.resolve(streamInfo, resolvedStreamType, preload = preload)
    val retryContext = PlaybackRetryContext(resolvedStreamType, timeoutProfile)
    return PlaybackPreparationPlan(
        resolvedStreamType = resolvedStreamType,
        timeoutProfile = timeoutProfile,
        retryContext = retryContext,
        retryPolicy = PlayerRetryPolicy(retryContext) { playbackStarted() }
    )
}