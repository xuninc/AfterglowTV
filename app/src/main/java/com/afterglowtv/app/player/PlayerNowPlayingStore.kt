package com.afterglowtv.app.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class PlayerNowPlayingState(
    val active: Boolean = false,
    val isLive: Boolean = false,
    val title: String = "",
    val subtitle: String = "",
    val channelNumber: Int? = null
)

@Singleton
class PlayerNowPlayingStore @Inject constructor() {
    private val _state = MutableStateFlow(PlayerNowPlayingState())
    val state: StateFlow<PlayerNowPlayingState> = _state.asStateFlow()

    fun update(state: PlayerNowPlayingState) {
        _state.value = state
    }

    fun clear() {
        _state.value = PlayerNowPlayingState()
    }
}
