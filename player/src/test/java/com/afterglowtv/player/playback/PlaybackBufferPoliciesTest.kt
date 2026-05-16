package com.afterglowtv.player.playback

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PlaybackBufferPoliciesTest {

    @Test
    fun `normal live uses production live buffer baseline`() {
        val policy = PlaybackBufferPolicies.forPlayback(isLive = true, compatibilityMode = false)

        assertThat(policy.label).isEqualTo("stable-live")
        assertThat(policy.minBufferMs).isEqualTo(12_000)
        assertThat(policy.maxBufferMs).isEqualTo(60_000)
        assertThat(policy.playbackBufferMs).isEqualTo(2_500)
        assertThat(policy.rebufferMs).isEqualTo(8_000)
    }

    @Test
    fun `compatibility live uses larger live buffer`() {
        val policy = PlaybackBufferPolicies.forPlayback(isLive = true, compatibilityMode = true)

        assertThat(policy.label).isEqualTo("compat-live")
        assertThat(policy.minBufferMs).isEqualTo(20_000)
        assertThat(policy.maxBufferMs).isEqualTo(75_000)
        assertThat(policy.playbackBufferMs).isEqualTo(2_500)
        assertThat(policy.rebufferMs).isEqualTo(8_000)
    }

    @Test
    fun `vod uses larger stable buffer`() {
        val policy = PlaybackBufferPolicies.forPlayback(isLive = false, compatibilityMode = false)

        assertThat(policy.label).isEqualTo("stable-vod")
        assertThat(policy.minBufferMs).isEqualTo(50_000)
        assertThat(policy.maxBufferMs).isEqualTo(180_000)
        assertThat(policy.playbackBufferMs).isEqualTo(2_500)
        assertThat(policy.rebufferMs).isEqualTo(8_000)
    }
}
