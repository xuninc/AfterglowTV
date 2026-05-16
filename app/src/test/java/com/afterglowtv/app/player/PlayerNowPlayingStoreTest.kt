package com.afterglowtv.app.player

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PlayerNowPlayingStoreTest {

    @Test
    fun `update publishes active live now playing state`() {
        val store = PlayerNowPlayingStore()

        store.update(
            PlayerNowPlayingState(
                active = true,
                isLive = true,
                title = "News 24",
                subtitle = "Evening Report",
                channelNumber = 230
            )
        )

        assertThat(store.state.value).isEqualTo(
            PlayerNowPlayingState(
                active = true,
                isLive = true,
                title = "News 24",
                subtitle = "Evening Report",
                channelNumber = 230
            )
        )
    }

    @Test
    fun `clear removes sidecar playback state`() {
        val store = PlayerNowPlayingStore()
        store.update(PlayerNowPlayingState(active = true, isLive = true, title = "News 24"))

        store.clear()

        assertThat(store.state.value).isEqualTo(PlayerNowPlayingState())
    }
}
