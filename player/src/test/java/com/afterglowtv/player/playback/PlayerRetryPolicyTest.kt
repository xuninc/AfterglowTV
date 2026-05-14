package com.afterglowtv.player.playback

import com.google.common.truth.Truth.assertThat
import androidx.media3.exoplayer.source.BehindLiveWindowException
import java.io.IOException
import javax.net.ssl.SSLHandshakeException
import org.junit.Test

class PlayerRetryPolicyTest {

    private val liveContext = PlaybackRetryContext(
        resolvedStreamType = ResolvedStreamType.HLS,
        timeoutProfile = PlayerTimeoutProfile.LIVE
    )

    private val policy = PlayerRetryPolicy(liveContext) { false }

    @Test
    fun `500 before first frame retries 3 times with jittered backoff`() {
        val error = IOException("HTTP 500")
        assertThat(policy.shouldRetry(error, liveContext, playbackStarted = false, attempt = 1)).isTrue()
        assertThat(policy.shouldRetry(error, liveContext, playbackStarted = false, attempt = 2)).isTrue()
        assertThat(policy.shouldRetry(error, liveContext, playbackStarted = false, attempt = 3)).isTrue()
        assertThat(policy.shouldRetry(error, liveContext, playbackStarted = false, attempt = 4)).isFalse()
        // AWS-style full jitter: each delay is uniform in [0, ceiling].
        // Sample 50 times per attempt and assert the spread is bounded by
        // the expected ceiling for that attempt.
        repeat(50) {
            assertThat(policy.retryDelayMs(error, 1)).isAtMost(2_000L)
            assertThat(policy.retryDelayMs(error, 2)).isAtMost(4_000L)
            assertThat(policy.retryDelayMs(error, 3)).isAtMost(5_000L)
        }
        // Sanity: at least one sample exceeds 0 — otherwise the jitter is broken.
        val anyNonZeroAt2 = (1..50).any { policy.retryDelayMs(error, 2) > 0L }
        assertThat(anyNonZeroAt2).isTrue()
    }

    @Test
    fun `403 never retries`() {
        assertThat(policy.shouldRetry(IOException("HTTP 403"), liveContext, playbackStarted = false, attempt = 1))
            .isFalse()
    }

    @Test
    fun `ssl error never retries`() {
        assertThat(policy.shouldRetry(SSLHandshakeException("bad cert"), liveContext, playbackStarted = false, attempt = 1))
            .isFalse()
    }

    @Test
    fun `behind live window retries once`() {
        val error = BehindLiveWindowException()
        assertThat(policy.shouldRetry(error, liveContext, playbackStarted = false, attempt = 1)).isTrue()
        assertThat(policy.shouldRetry(error, liveContext, playbackStarted = false, attempt = 2)).isFalse()
        assertThat(policy.retryDelayMs(error, 1)).isEqualTo(500L)
    }

    @Test
    fun `decoder init failure does not go through network retry policy`() {
        val error = IllegalStateException("decoder init failed")
        assertThat(policy.shouldRetry(error, liveContext, playbackStarted = false, attempt = 1)).isFalse()
    }

    @Test
    fun `format unsupported after playback start retries once`() {
        val error = IllegalStateException("video format unsupported")
        assertThat(policy.shouldRetry(error, liveContext, playbackStarted = true, attempt = 1)).isTrue()
        assertThat(policy.shouldRetry(error, liveContext, playbackStarted = true, attempt = 2)).isFalse()
        assertThat(policy.maxAttempts(error, playbackStarted = true)).isEqualTo(1)
    }

    @Test
    fun `decoder init failure after playback start still does not retry`() {
        val error = IllegalStateException("decoder init failed")
        assertThat(policy.shouldRetry(error, liveContext, playbackStarted = true, attempt = 1)).isFalse()
    }
}
