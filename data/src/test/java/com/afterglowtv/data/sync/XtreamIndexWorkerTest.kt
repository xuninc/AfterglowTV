package com.afterglowtv.data.sync

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.concurrent.TimeUnit

class XtreamIndexWorkerTest {
    @Test
    fun `periodic index work is delayed after app startup`() {
        val request = XtreamIndexWorker.createPeriodicRequest()

        assertThat(request.workSpec.initialDelay >= TimeUnit.MINUTES.toMillis(15)).isTrue()
    }

    @Test
    fun `empty background index work succeeds even when device is low on memory`() {
        val decision = decideXtreamIndexWorkReadiness(
            hasProviders = false,
            isLowOnMemory = true
        )

        assertThat(decision == XtreamIndexWorkReadiness.NO_WORK).isTrue()
    }

    @Test
    fun `background index work with providers defers when device is low on memory`() {
        val decision = decideXtreamIndexWorkReadiness(
            hasProviders = true,
            isLowOnMemory = true
        )

        assertThat(decision == XtreamIndexWorkReadiness.DEFER_LOW_MEMORY).isTrue()
    }

    @Test
    fun `launch stale index work is delayed so cold start can finish first`() {
        val request = XtreamIndexWorker.createLaunchStaleCheckRequest()

        assertThat(request.workSpec.initialDelay >= TimeUnit.MINUTES.toMillis(2)).isTrue()
    }

    @Test
    fun `targeted index work keeps caller supplied delay`() {
        val request = XtreamIndexWorker.createProviderIndexRequest(
            providerId = 7L,
            section = null,
            force = false,
            initialDelaySeconds = 3L
        )

        assertThat(request.workSpec.initialDelay == TimeUnit.SECONDS.toMillis(3)).isTrue()
    }
}
