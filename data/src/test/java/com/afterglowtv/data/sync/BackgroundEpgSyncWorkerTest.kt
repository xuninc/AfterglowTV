package com.afterglowtv.data.sync

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BackgroundEpgSyncWorkerTest {

    @Test
    fun `retryable partial epg failures stop retrying after capped worker attempts`() {
        assertThat(shouldRetryPartialEpgFailure(runAttemptCount = 0)).isTrue()
        assertThat(shouldRetryPartialEpgFailure(runAttemptCount = 1)).isTrue()
        assertThat(shouldRetryPartialEpgFailure(runAttemptCount = 2)).isFalse()
    }
}
