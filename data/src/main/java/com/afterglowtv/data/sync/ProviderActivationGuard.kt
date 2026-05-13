package com.afterglowtv.data.sync

import com.afterglowtv.data.local.dao.ChannelDao
import com.afterglowtv.domain.model.ProviderType
import kotlinx.coroutines.flow.first

internal suspend fun hasUsableLiveCatalogForActivation(
    providerId: Long,
    providerType: ProviderType,
    channelDao: ChannelDao
): Boolean {
    if (providerType != ProviderType.XTREAM_CODES) {
        return true
    }
    return channelDao.getCount(providerId).first() > 0
}