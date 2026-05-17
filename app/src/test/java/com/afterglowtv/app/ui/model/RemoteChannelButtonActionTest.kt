package com.afterglowtv.app.ui.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RemoteChannelButtonActionTest {

    @Test
    fun `fromStorage keeps saved channel button mapping`() {
        assertThat(RemoteChannelButtonAction.fromStorage("open_guide"))
            .isEqualTo(RemoteChannelButtonAction.OPEN_GUIDE)
        assertThat(RemoteChannelButtonAction.fromStorage("open_channel_list"))
            .isEqualTo(RemoteChannelButtonAction.OPEN_CHANNEL_LIST)
    }

    @Test
    fun `fromStorage defaults physical channel buttons to channel changing`() {
        assertThat(RemoteChannelButtonAction.fromStorage(null))
            .isEqualTo(RemoteChannelButtonAction.CHANGE_CHANNELS)
        assertThat(RemoteChannelButtonAction.fromStorage("bogus"))
            .isEqualTo(RemoteChannelButtonAction.CHANGE_CHANNELS)
    }
}
