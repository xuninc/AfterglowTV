package com.afterglowtv.domain.util

/**
 * Central policy for user-protected content visibility across all surfaces.
 *
 * Levels:
 *   0 = OFF      – no restrictions
 *   1 = LOCKED   – categories visible + PIN-gated; content shown in all surfaces
 *   2 = PRIVATE  – categories visible + PIN-gated; content excluded from aggregated surfaces
 *   3 = HIDDEN   – user-protected categories hidden everywhere; content excluded from all surfaces
 *
 * "Aggregated surfaces" are views that mix content from multiple categories:
 * All Channels, Recent (Home + Dashboard), Search, and EPG guide.
 */
object AdultContentVisibilityPolicy {
    const val LEVEL_OFF = 0
    const val LEVEL_LOCKED = 1
    const val LEVEL_PRIVATE = 2
    const val LEVEL_HIDDEN = 3

    /**
     * True when protected content may appear in aggregated surfaces (All Channels,
     * Recent, Search, EPG). Only OFF and LOCKED allow this.
     */
    fun showInAggregatedSurfaces(level: Int): Boolean = level <= LEVEL_LOCKED

    /**
     * True when protected categories appear in navigation/sidebar at all.
     * OFF, LOCKED, and PRIVATE show categories (with lock for the latter two).
     */
    fun showCategories(level: Int): Boolean = level <= LEVEL_PRIVATE

    /**
     * True when accessing a protected category requires a PIN.
     */
    fun requiresPin(level: Int): Boolean = level in LEVEL_LOCKED..LEVEL_PRIVATE

    /**
     * True when repositories should remove protected content from ALL queries,
     * including category-specific browsing. Only applies at HIDDEN level.
     */
    fun hideAllContent(level: Int): Boolean = level >= LEVEL_HIDDEN

    /**
     * Filters [items] for an aggregated surface, removing protected items
     * when the current [level] requires it.
     */
    fun <T> filterForAggregatedSurface(
        items: List<T>,
        level: Int,
        isProtected: T.() -> Boolean
    ): List<T> = if (showInAggregatedSurfaces(level)) items else items.filterNot { it.isProtected() }
}
