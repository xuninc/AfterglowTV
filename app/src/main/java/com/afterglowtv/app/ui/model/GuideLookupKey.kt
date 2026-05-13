package com.afterglowtv.app.ui.model

import com.afterglowtv.domain.model.Channel

fun Channel.guideLookupKey(): String? {
    return epgChannelId?.trim()?.takeIf { it.isNotEmpty() }
        ?: streamId.takeIf { it > 0L }?.toString()
}
