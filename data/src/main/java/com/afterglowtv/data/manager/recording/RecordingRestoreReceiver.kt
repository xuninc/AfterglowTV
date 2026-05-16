package com.afterglowtv.data.manager.recording

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Operation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class RecordingRestoreReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                val pendingResult = goAsync()
                val appContext = context.applicationContext
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        val operations = RecordingReconcileWorker.cancelStartupMaintenance(appContext) +
                            RecordingReconcileWorker.enqueueOneShot(appContext)
                        operations.awaitQuietly()
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    private fun List<Operation>.awaitQuietly() {
        forEach { operation ->
            runCatching {
                operation.result.get(5, TimeUnit.SECONDS)
            }
        }
    }
}
