package com.afterglowtv.domain.manager

data class ParentalControlSessionState(
    val unlockedCategoryIdsByProvider: Map<Long, Set<Long>> = emptyMap()
)

interface ParentalControlSessionStore {
    fun writeSessionState(state: ParentalControlSessionState)
}
