package com.afterglowtv.app.di

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NetworkModuleTest {
    @Test
    fun `http cache directory is derived without touching Context cacheDir`() {
        val directory = httpCacheDirectory("/data/user/0/com.afterglowtv.app")

        assertThat(directory.path == "/data/user/0/com.afterglowtv.app/cache/afterglowtv_http_cache").isTrue()
    }
}
