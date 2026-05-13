package com.afterglowtv.app.ui.model

import com.afterglowtv.domain.model.Channel

fun List<Channel>.associateByAnyRawId(): Map<Long, Channel> = buildMap {
    this@associateByAnyRawId.forEach { channel ->
        channel.allVariantRawIds().forEach { rawId ->
            putIfAbsent(rawId, channel)
        }
    }
}

fun List<Channel>.orderedByRequestedRawIds(
    requestedIds: List<Long>,
    requiredProviderId: Long? = null
): List<Channel> {
    val filtered = requiredProviderId?.let { providerId ->
        filter { it.providerId == providerId }
    } ?: this
    val byRawId = filtered.associateByAnyRawId()
    val seenKeys = linkedSetOf<String>()
    return requestedIds.mapNotNull { rawId ->
        byRawId[rawId]
    }.filter { channel ->
        seenKeys.add(channel.logicalGroupId.ifBlank { channel.id.toString() })
    }
}
