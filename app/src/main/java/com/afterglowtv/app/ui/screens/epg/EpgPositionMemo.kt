package com.afterglowtv.app.ui.screens.epg

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Process-scope remember-the-spot store for the EPG. When the user navigates
 * away from the guide (e.g. to play a channel) and back, Compose's
 * `remember`-scoped state is gone — focused channel, focused program, and
 * scroll position all reset to defaults.
 *
 * This singleton lives for the lifetime of the app process and holds the
 * last known guide position. [EpgViewModel] writes to it on every focus
 * change (the screen calls back) and reads from it at init so the screen
 * can immediately restore focus.
 *
 * In-memory only — does NOT persist to disk. "Where I left off" survives
 * back-button navigation within the same launch, not a process kill. (If
 * cross-launch persistence becomes wanted, swap [snapshot]/[remember] to
 * route through [PreferencesRepository].)
 */
@Singleton
class EpgPositionMemo @Inject constructor() {
    @Volatile private var lastChannelId: Long? = null
    @Volatile private var lastProgramStartMs: Long? = null
    @Volatile private var lastCategoryId: Long? = null

    fun remember(channelId: Long, programStartMs: Long, categoryId: Long?) {
        lastChannelId = channelId
        lastProgramStartMs = programStartMs
        if (categoryId != null) lastCategoryId = categoryId
    }

    /** Returns null when there's nothing to restore (first visit / cold start). */
    fun snapshot(): Snapshot? {
        val channel = lastChannelId ?: return null
        val program = lastProgramStartMs ?: return null
        return Snapshot(channelId = channel, programStartMs = program, categoryId = lastCategoryId)
    }

    fun clear() {
        lastChannelId = null
        lastProgramStartMs = null
        lastCategoryId = null
    }

    data class Snapshot(
        val channelId: Long,
        val programStartMs: Long,
        val categoryId: Long?,
    )
}
