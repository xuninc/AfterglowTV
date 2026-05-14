package com.afterglowtv.app.di

import android.util.Log
import okhttp3.Dns
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap

/**
 * OkHttp [Dns] wrapper that caches successful resolutions for a bounded
 * TTL. The system resolver re-queries on every new connection by default
 * — for an IPTV app where every channel switch fires a fresh connection
 * to the same provider host, that's 10-50 ms of needless latency per zap.
 *
 * Cache strategy:
 *  - Successful lookups cached for [POSITIVE_TTL_MS] (5 minutes).
 *  - Failed lookups NOT cached (we want immediate retry on next request).
 *  - Cache bounded at [MAX_ENTRIES] hosts. LRU is approximated by
 *    insertion-order eviction — sufficient for a typical IPTV deployment
 *    where the user hits at most 2-5 provider hosts in a session.
 *
 * This is the same pattern Android's `LegacyInetAddressCache` uses for
 * connection-level caching, just at the application layer and with our
 * own TTL (the system cache is ~30 s on most devices).
 */
class CachingDns(
    private val delegate: Dns = Dns.SYSTEM,
) : Dns {
    private data class Entry(val addresses: List<InetAddress>, val expiresAtMs: Long)

    private val cache = ConcurrentHashMap<String, Entry>()
    private val insertOrder = java.util.LinkedHashMap<String, Boolean>(16, 0.75f, true)
    private val insertLock = Any()

    override fun lookup(hostname: String): List<InetAddress> {
        val now = System.currentTimeMillis()
        cache[hostname]?.let { entry ->
            if (entry.expiresAtMs > now) {
                return entry.addresses
            } else {
                cache.remove(hostname)
            }
        }

        // Real DNS hit — defer to the delegate (system resolver by default).
        val resolved = delegate.lookup(hostname)
        if (resolved.isNotEmpty()) {
            cache[hostname] = Entry(resolved, now + POSITIVE_TTL_MS)
            recordInsertion(hostname)
        }
        return resolved
    }

    private fun recordInsertion(hostname: String) {
        synchronized(insertLock) {
            insertOrder[hostname] = true
            while (insertOrder.size > MAX_ENTRIES) {
                val oldest = insertOrder.keys.iterator().next()
                insertOrder.remove(oldest)
                cache.remove(oldest)
            }
        }
    }

    /** Clear all cached entries — useful on network class change. */
    fun clear() {
        cache.clear()
        synchronized(insertLock) { insertOrder.clear() }
        Log.d(TAG, "cleared")
    }

    companion object {
        private const val TAG = "CachingDns"
        private const val POSITIVE_TTL_MS = 5L * 60_000L // 5 minutes
        private const val MAX_ENTRIES = 64
    }
}
