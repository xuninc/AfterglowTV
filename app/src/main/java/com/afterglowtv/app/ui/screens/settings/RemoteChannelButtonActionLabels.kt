package com.afterglowtv.app.ui.screens.settings

import com.afterglowtv.app.R
import com.afterglowtv.app.ui.model.RemoteChannelButtonAction

internal fun RemoteChannelButtonAction.labelResId(): Int = when (this) {
    RemoteChannelButtonAction.CHANGE_CHANNELS -> R.string.settings_remote_channel_button_change_channels
    RemoteChannelButtonAction.OPEN_GUIDE -> R.string.settings_remote_channel_button_open_guide
    RemoteChannelButtonAction.OPEN_CHANNEL_LIST -> R.string.settings_remote_channel_button_open_channel_list
    RemoteChannelButtonAction.SHOW_INFO -> R.string.settings_remote_channel_button_show_info
    RemoteChannelButtonAction.DISABLED -> R.string.settings_remote_channel_button_disabled
}
