package com.afterglowtv.app.ui.model

enum class RemoteChannelButtonAction(val storageValue: String) {
    CHANGE_CHANNELS("change_channels"),
    OPEN_GUIDE("open_guide"),
    OPEN_CHANNEL_LIST("open_channel_list"),
    SHOW_INFO("show_info"),
    DISABLED("disabled");

    companion object {
        fun fromStorage(value: String?): RemoteChannelButtonAction =
            entries.firstOrNull { it.storageValue.equals(value, ignoreCase = true) }
                ?: CHANGE_CHANNELS
    }
}
