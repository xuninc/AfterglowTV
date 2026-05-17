package com.afterglowtv.app.ui.screens.player

import com.afterglowtv.domain.model.Program
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PlayerViewModelActionsTest {

    @Test
    fun `manual recording gives placeholder guide slots a usable end time`() {
        val now = 30_000L
        val endMs = resolveManualRecordingEndMs(
            nowMs = now,
            currentProgram = program(
                startTime = 1_000L,
                endTime = 61_000L,
                isPlaceholder = true
            )
        )

        assertThat(endMs).isEqualTo(now + 60 * 60_000L)
    }

    private fun program(
        startTime: Long,
        endTime: Long,
        isPlaceholder: Boolean
    ) = Program(
        id = 1L,
        channelId = "11",
        title = "Channel 11",
        startTime = startTime,
        endTime = endTime,
        providerId = 7L,
        isPlaceholder = isPlaceholder
    )
}
