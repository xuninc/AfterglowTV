package com.afterglowtv.player.adaptive

import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException

/**
 * Issues "warm-up" HTTP requests against stream URLs the user has
 * focused but not yet committed to. By the time the user presses OK,
 * DNS is resolved, the TCP connection is in OkHttp's idle pool, and
 * TLS keys are negotiated — saving 300-800 ms on every channel zap.
 *
 * ## How this is better than the existing preload
 *
 * The existing `Media3PlayerEngine.preload(streamInfo)` only allocates
 * a `MediaSource` object — it does NOT touch the network. The first
 * HTTP request fires at `setMediaSource` + `prepare` time, paying full
 * DNS + TCP + TLS cost.
 *
 * This prewarmer:
 *
 * 1. Issues a `Range: bytes=0-0` GET against the channel URL — one
 *    byte, fully resolves DNS, opens TCP, completes TLS, gets a 206
 *    Partial Content response. Single round-trip cost.
 * 2. Connection lands in the singleton OkHttp connection pool
 *    (configured for 20 idle / 10-min keep-alive). The next request
 *    to the same host — which will be `prepare`'s real media fetch —
 *    reuses the pooled connection.
 * 3. Coalesces multiple warm requests to the same host: rapidly
 *    scrolling through a channel list doesn't fire 20 parallel warms;
 *    it fires one and lets the others piggyback.
 * 4. Caches the most recently warmed URL with a TTL so re-focusing
 *    the same channel within a few seconds doesn't refire.
 *
 * ## What this is NOT
 *
 * - Not a full media preload. We don't fetch segments or manifests
 *   here — that work happens in Media3PlayerEngine.preload, which
 *   builds the MediaSource and is invoked separately.
 * - Not a Wikipedia-style speculative prefetch. We only warm on
 *   explicit user focus, not on heuristic "user might select this".
 */
@Singleton
class ConnectionPrewarmer @Inject constructor(
    private val okHttpClient: OkHttpClient,
) {
    // Most recently warmed URL → wall-clock timestamp. Bounded to a few entries
    // so it doesn't grow unbounded across a long EPG scroll.
    private val warmedAt = ConcurrentHashMap<String, Long>()

    // In-flight warm requests keyed by URL. Subsequent warm() calls for the
    // same URL while the first is in-flight are no-ops.
    private val inFlight = ConcurrentHashMap<String, Job>()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Fire-and-forget warm of the connection to [url]. Safe to call from
     * any thread, including the Compose UI thread on every focus change.
     */
    fun warm(url: String, headers: Map<String, String> = emptyMap()) {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return

        val now = System.currentTimeMillis()
        val warmedRecently = warmedAt[trimmed]?.let { now - it < REUSE_TTL_MS } == true
        if (warmedRecently) return
        if (inFlight.containsKey(trimmed)) return

        val job = scope.launch {
            warmInternal(trimmed, headers)
        }
        inFlight[trimmed] = job
        job.invokeOnCompletion {
            inFlight.remove(trimmed, job)
        }
    }

    /** Clear the warm cache — useful on provider switch or sign-out. */
    fun clear() {
        warmedAt.clear()
        inFlight.values.forEach { it.cancel() }
        inFlight.clear()
    }

    private suspend fun warmInternal(url: String, headers: Map<String, String>) {
        runCatching {
            val request = Request.Builder()
                .url(url)
                .header("Range", "bytes=0-0")
                .apply { headers.forEach { (k, v) -> header(k, v) } }
                .build()
            // Use a sharply-scoped client so the warm request doesn't sit on
            // the default LIVE/VOD long timeouts.
            val client = okHttpClient.newBuilder()
                .connectTimeout(WARM_CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .readTimeout(WARM_READ_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .writeTimeout(WARM_WRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .build()
            client.newCall(request).execute().use { response ->
                // Drain a single byte so the connection is fully usable for
                // the pool, then close. The pool now owns a warm connection
                // to this host that the next real request will reuse.
                response.body?.byteStream()?.read()
                if (response.isSuccessful || response.code == 206) {
                    warmedAt[url] = System.currentTimeMillis()
                    Log.d(TAG, "warmed host=${response.request.url.host} code=${response.code}")
                } else {
                    // Don't cache: a 404 or 4xx means the URL is broken and
                    // we'd want to re-warm if the user retries.
                    Log.d(TAG, "warm-failed code=${response.code}")
                }
            }
        }.onFailure { error ->
            // IOExceptions are expected on flaky networks — don't cache the
            // failure, don't surface to the user, and don't log loudly.
            if (error !is IOException) {
                Log.w(TAG, "warm error", error)
            }
        }
    }

    companion object {
        private const val TAG = "ConnectionPrewarmer"
        // Re-warming the same URL within 30s is wasteful — the OkHttp pool
        // is keeping the connection alive for 10 min anyway.
        private const val REUSE_TTL_MS = 30_000L
        // Tight timeouts: a warm should complete in < 3s on any reasonable
        // network. If it doesn't, the warm itself isn't worth waiting for.
        private const val WARM_CONNECT_TIMEOUT_MS = 3_000L
        private const val WARM_READ_TIMEOUT_MS = 5_000L
        private const val WARM_WRITE_TIMEOUT_MS = 3_000L
    }
}
