package com.afterglowtv.player.playback

import com.google.common.truth.Truth.assertThat
import com.afterglowtv.domain.model.StreamInfo
import org.junit.Test

class PlayerTimeoutProfileTest {

    @Test
    fun `hls live selects live timeout profile`() {
        assertThat(
            PlayerTimeoutProfile.resolve(
                streamInfo = StreamInfo(url = "http://example.com/live/1.m3u8"),
                resolvedStreamType = ResolvedStreamType.HLS,
                preload = false
            )
        ).isEqualTo(PlayerTimeoutProfile.LIVE)
    }

    @Test
    fun `preload always selects preload profile`() {
        assertThat(
            PlayerTimeoutProfile.resolve(
                streamInfo = StreamInfo(url = "http://example.com/movie.mp4"),
                resolvedStreamType = ResolvedStreamType.PROGRESSIVE,
                preload = true
            )
        ).isEqualTo(PlayerTimeoutProfile.PRELOAD)
    }

    @Test
    fun `progressive file selects progressive timeout profile`() {
        assertThat(
            PlayerTimeoutProfile.resolve(
                streamInfo = StreamInfo(url = "http://example.com/movie.mp4"),
                resolvedStreamType = ResolvedStreamType.PROGRESSIVE,
                preload = false
            )
        ).isEqualTo(PlayerTimeoutProfile.PROGRESSIVE)
    }
}

