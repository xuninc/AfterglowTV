package com.afterglowtv.data.manager.recording

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.concurrent.TimeUnit

class RecordingReconcileWorkerTest {
    @Test
    fun `periodic reconcile work is delayed after app startup`() {
        val request = RecordingReconcileWorker.createPeriodicRequest()

        assertThat(request.workSpec.initialDelay >= TimeUnit.MINUTES.toMillis(15)).isTrue()
    }

    @Test
    fun `startup reconcile work is delayed so cold start can finish first`() {
        val request = RecordingReconcileWorker.createOneShotRequest()

        assertThat(request.workSpec.initialDelay >= TimeUnit.MINUTES.toMillis(2)).isTrue()
    }
}
