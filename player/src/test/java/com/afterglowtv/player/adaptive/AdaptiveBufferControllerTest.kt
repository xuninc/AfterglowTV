package com.afterglowtv.player.adaptive

import com.afterglowtv.player.playback.BufferClass
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests the pure classification logic in [AdaptiveBufferController.classify]
 * by constructing synthetic [AdaptivePlaybackSnapshot]s. No Android runtime
 * required.
 */
class AdaptiveBufferControllerTest {

    @Test
    fun `cold start on Wi-Fi classifies as STABLE`() {
        val snapshot = AdaptivePlaybackSnapshot.EMPTY
        assertThat(AdaptiveBufferController.classify(snapshot, NetworkClass.WIFI))
            .isEqualTo(BufferClass.STABLE)
    }

    @Test
    fun `cold start on 3G classifies as COMPAT`() {
        val snapshot = AdaptivePlaybackSnapshot.EMPTY
        assertThat(AdaptiveBufferController.classify(snapshot, NetworkClass.CELLULAR_3G))
            .isEqualTo(BufferClass.COMPAT)
    }

    @Test
    fun `cold start on 2G classifies as COMPAT`() {
        assertThat(AdaptiveBufferController.classify(AdaptivePlaybackSnapshot.EMPTY, NetworkClass.CELLULAR_2G))
            .isEqualTo(BufferClass.COMPAT)
    }

    @Test
    fun `cold start on Ethernet classifies as STABLE`() {
        assertThat(AdaptiveBufferController.classify(AdaptivePlaybackSnapshot.EMPTY, NetworkClass.ETHERNET))
            .isEqualTo(BufferClass.STABLE)
    }

    @Test
    fun `high rebuffer rate on Wi-Fi classifies as COMPAT`() {
        val snapshot = AdaptivePlaybackSnapshot.EMPTY.copy(
            eventCount = 50,
            rebufferCount = 10,
            rebufferRatePerMinute = 2.5,
            p95RecoveryMs = 3_000L,
        )
        assertThat(AdaptiveBufferController.classify(snapshot, NetworkClass.WIFI))
            .isEqualTo(BufferClass.COMPAT)
    }

    @Test
    fun `slow recovery alone classifies as COMPAT`() {
        val snapshot = AdaptivePlaybackSnapshot.EMPTY.copy(
            eventCount = 50,
            rebufferCount = 3,
            rebufferRatePerMinute = 0.6,
            p95RecoveryMs = 15_000L, // above STRESS_RECOVERY_THRESHOLD_MS
        )
        assertThat(AdaptiveBufferController.classify(snapshot, NetworkClass.WIFI))
            .isEqualTo(BufferClass.COMPAT)
    }

    @Test
    fun `clean playback on Wi-Fi with history classifies as AGGRESSIVE`() {
        val snapshot = AdaptivePlaybackSnapshot.EMPTY.copy(
            eventCount = 100,
            rebufferCount = 0,
            rebufferRatePerMinute = 0.0,
            p95RecoveryMs = null,
        )
        assertThat(AdaptiveBufferController.classify(snapshot, NetworkClass.WIFI))
            .isEqualTo(BufferClass.AGGRESSIVE)
    }

    @Test
    fun `clean playback on Ethernet classifies as AGGRESSIVE`() {
        val snapshot = AdaptivePlaybackSnapshot.EMPTY.copy(
            eventCount = 50,
            rebufferCount = 0,
            rebufferRatePerMinute = 0.0,
            p95RecoveryMs = null,
        )
        assertThat(AdaptiveBufferController.classify(snapshot, NetworkClass.ETHERNET))
            .isEqualTo(BufferClass.AGGRESSIVE)
    }

    @Test
    fun `clean playback on cellular stays at STABLE not AGGRESSIVE`() {
        val snapshot = AdaptivePlaybackSnapshot.EMPTY.copy(
            eventCount = 100,
            rebufferCount = 0,
            rebufferRatePerMinute = 0.0,
            p95RecoveryMs = null,
        )
        // Cellular is too prone to handover blips for AGGRESSIVE — must
        // anchor on STABLE even when recent samples look perfect.
        assertThat(AdaptiveBufferController.classify(snapshot, NetworkClass.CELLULAR_4G))
            .isEqualTo(BufferClass.STABLE)
        assertThat(AdaptiveBufferController.classify(snapshot, NetworkClass.CELLULAR_5G))
            .isEqualTo(BufferClass.STABLE)
    }

    @Test
    fun `mid-range rebuffer rate on Wi-Fi stays at STABLE`() {
        val snapshot = AdaptivePlaybackSnapshot.EMPTY.copy(
            eventCount = 50,
            rebufferCount = 3,
            rebufferRatePerMinute = 0.6, // above CLEAN (0.2), below STRESS (2.0)
            p95RecoveryMs = 3_000L,
        )
        assertThat(AdaptiveBufferController.classify(snapshot, NetworkClass.WIFI))
            .isEqualTo(BufferClass.STABLE)
    }

    @Test
    fun `boundary at stress rate threshold classifies as COMPAT`() {
        val snapshot = AdaptivePlaybackSnapshot.EMPTY.copy(
            eventCount = 50,
            rebufferCount = 10,
            rebufferRatePerMinute = 2.0, // exactly at threshold
            p95RecoveryMs = 3_000L,
        )
        assertThat(AdaptiveBufferController.classify(snapshot, NetworkClass.WIFI))
            .isEqualTo(BufferClass.COMPAT)
    }

    @Test
    fun `not enough history blocks AGGRESSIVE promotion`() {
        val snapshot = AdaptivePlaybackSnapshot.EMPTY.copy(
            eventCount = 1, // below COLD_START_MIN_EVENTS = 20
            rebufferCount = 1, // forces past cold-start gate
            rebufferRatePerMinute = 0.0,
            p95RecoveryMs = null,
        )
        assertThat(AdaptiveBufferController.classify(snapshot, NetworkClass.WIFI))
            .isEqualTo(BufferClass.STABLE)
    }
}
