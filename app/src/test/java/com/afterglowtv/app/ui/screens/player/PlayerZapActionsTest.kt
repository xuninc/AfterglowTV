package com.afterglowtv.app.ui.screens.player

import com.google.common.truth.Truth.assertThat
import com.afterglowtv.domain.model.ContentType
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PlayerZapActionsTest {

    @Test
    fun `appendNumericChannelDigit keeps up to six digits`() {
        var buffer = ""

        (1..6).forEach { digit ->
            buffer = appendNumericChannelDigit(buffer, digit)
        }

        assertThat(buffer).isEqualTo("123456")
    }

    @Test
    fun `appendNumericChannelDigit restarts after sixth digit`() {
        var buffer = ""

        (1..6).forEach { digit ->
            buffer = appendNumericChannelDigit(buffer, digit)
        }
        buffer = appendNumericChannelDigit(buffer, 7)

        assertThat(buffer).isEqualTo("7")
    }

    @Test
    fun `linear live channel zapping is disabled during catch up playback`() {
        assertThat(
            isLinearLiveChannelZapAllowed(
                currentContentType = ContentType.LIVE,
                isCatchUpPlayback = true,
                hasChannels = true
            )
        ).isFalse()
    }

    @Test
    fun `linear live channel zapping requires live content with channels`() {
        assertThat(
            isLinearLiveChannelZapAllowed(
                currentContentType = ContentType.LIVE,
                isCatchUpPlayback = false,
                hasChannels = true
            )
        ).isTrue()
        assertThat(
            isLinearLiveChannelZapAllowed(
                currentContentType = ContentType.MOVIE,
                isCatchUpPlayback = false,
                hasChannels = true
            )
        ).isFalse()
        assertThat(
            isLinearLiveChannelZapAllowed(
                currentContentType = ContentType.LIVE,
                isCatchUpPlayback = false,
                hasChannels = false
            )
        ).isFalse()
    }

    @Test
    fun `withScopedScrubbingMode disables scrubbing after success`() = runTest {
        val states = mutableListOf<Boolean>()

        withScopedScrubbingMode(states::add) {
            "done"
        }

        assertThat(states).containsExactly(true, false).inOrder()
    }

    @Test
    fun `withScopedScrubbingMode disables scrubbing after failure`() = runTest {
        val states = mutableListOf<Boolean>()

        runCatching {
            withScopedScrubbingMode(states::add) {
                error("boom")
            }
        }

        assertThat(states).containsExactly(true, false).inOrder()
    }
}
