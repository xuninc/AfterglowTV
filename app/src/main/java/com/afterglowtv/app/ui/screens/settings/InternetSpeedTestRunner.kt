package com.afterglowtv.app.ui.screens.settings

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.SystemClock
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

enum class InternetSpeedTestTransport {
    WIFI,
    ETHERNET,
    CELLULAR,
    OTHER,
    UNKNOWN
}

data class InternetSpeedTestSnapshot(
    val megabitsPerSecond: Double,
    val recommendedMaxVideoHeight: Int?,
    val measuredAtMs: Long,
    val transport: InternetSpeedTestTransport,
    val isEstimated: Boolean
)

sealed interface InternetSpeedTestResult {
    data class Success(val snapshot: InternetSpeedTestSnapshot) : InternetSpeedTestResult
    data class Error(val message: String) : InternetSpeedTestResult
}

@Singleton
class InternetSpeedTestRunner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    suspend fun run(): InternetSpeedTestResult = withContext(Dispatchers.IO) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return@withContext InternetSpeedTestResult.Error("Network service unavailable")
        val network = connectivityManager.activeNetwork
            ?: return@withContext InternetSpeedTestResult.Error("No active network connection")
        val capabilities = connectivityManager.getNetworkCapabilities(network)
            ?: return@withContext InternetSpeedTestResult.Error("Unable to inspect active network")

        val transport = capabilities.toTransport()
        val measuredAtMs = System.currentTimeMillis()

        runCatching {
            val bytesToDownload = 4_000_000L
            val request = Request.Builder()
                .url("https://speed.cloudflare.com/__down?bytes=$bytesToDownload&seed=${SystemClock.elapsedRealtime()}")
                .cacheControl(CacheControl.Builder().noCache().noStore().build())
                .build()
            val startedAtNs = SystemClock.elapsedRealtimeNanos()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    error("Speed test failed with HTTP ${response.code}")
                }
                val body = response.body ?: error("Speed test returned no data")
                val bytesRead = body.source().use { source ->
                    var total = 0L
                    while (!source.exhausted()) {
                        total += source.read(source.buffer, 8_192)
                            .takeIf { it > 0 }
                            ?: 0L
                    }
                    total
                }
                val elapsedSeconds = (SystemClock.elapsedRealtimeNanos() - startedAtNs)
                    .coerceAtLeast(TimeUnit.MILLISECONDS.toNanos(1)) / 1_000_000_000.0
                val megabitsPerSecond = ((bytesRead * 8.0) / elapsedSeconds) / 1_000_000.0
                InternetSpeedTestResult.Success(
                    InternetSpeedTestSnapshot(
                        megabitsPerSecond = megabitsPerSecond,
                        recommendedMaxVideoHeight = recommendMaxVideoHeight(megabitsPerSecond),
                        measuredAtMs = measuredAtMs,
                        transport = transport,
                        isEstimated = false
                    )
                )
            }
        }.getOrElse {
            val fallbackMbps = capabilities.linkDownstreamBandwidthKbps
                .takeIf { it > 0 }
                ?.div(1000.0)
            if (fallbackMbps != null) {
                InternetSpeedTestResult.Success(
                    InternetSpeedTestSnapshot(
                        megabitsPerSecond = fallbackMbps,
                        recommendedMaxVideoHeight = recommendMaxVideoHeight(fallbackMbps),
                        measuredAtMs = measuredAtMs,
                        transport = transport,
                        isEstimated = true
                    )
                )
            } else {
                InternetSpeedTestResult.Error(it.message ?: "Speed test failed")
            }
        }
    }

    private fun recommendMaxVideoHeight(megabitsPerSecond: Double): Int? {
        return when {
            megabitsPerSecond < 6.0 -> 480
            megabitsPerSecond < 12.0 -> 720
            megabitsPerSecond < 25.0 -> 1080
            megabitsPerSecond < 40.0 -> 2160
            else -> null
        }
    }

    private fun NetworkCapabilities.toTransport(): InternetSpeedTestTransport {
        return when {
            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> InternetSpeedTestTransport.WIFI
            hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> InternetSpeedTestTransport.ETHERNET
            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> InternetSpeedTestTransport.CELLULAR
            else -> InternetSpeedTestTransport.OTHER
        }
    }
}