package com.afterglowtv.data.manager.recording

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit

class RecordingReconcileWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface RecordingWorkerEntryPoint {
        fun recordingManager(): com.afterglowtv.domain.manager.RecordingManager
    }

    override suspend fun doWork(): Result {
        val manager = EntryPointAccessors.fromApplication(
            applicationContext,
            RecordingWorkerEntryPoint::class.java
        ).recordingManager()
        return when (manager.reconcileRecordingState()) {
            is com.afterglowtv.domain.model.Result.Success -> Result.success()
            is com.afterglowtv.domain.model.Result.Error -> Result.retry()
            com.afterglowtv.domain.model.Result.Loading -> Result.retry()
        }
    }

    companion object {
        private const val PERIODIC_WORK_NAME = "RecordingReconcileWorker"
        private const val ONE_SHOT_WORK_NAME = "RecordingReconcileWorkerOneShot"
        private const val PERIODIC_INITIAL_DELAY_MINUTES = 15L
        private const val STARTUP_RECONCILE_DELAY_MINUTES = 3L

        fun enqueuePeriodic(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                createPeriodicRequest()
            )
        }

        fun enqueueOneShot(context: Context): Operation =
            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_SHOT_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                createOneShotRequest()
            )

        fun cancelStartupMaintenance(context: Context): List<Operation> {
            val workManager = WorkManager.getInstance(context)
            return listOf(
                workManager.cancelUniqueWork(PERIODIC_WORK_NAME),
                workManager.cancelUniqueWork(ONE_SHOT_WORK_NAME)
            )
        }

        internal fun createPeriodicRequest() =
            PeriodicWorkRequestBuilder<RecordingReconcileWorker>(6, TimeUnit.HOURS)
                .setInitialDelay(PERIODIC_INITIAL_DELAY_MINUTES, TimeUnit.MINUTES)
                .build()

        internal fun createOneShotRequest(initialDelayMinutes: Long = STARTUP_RECONCILE_DELAY_MINUTES) =
            OneTimeWorkRequestBuilder<RecordingReconcileWorker>()
                .setInitialDelay(initialDelayMinutes.coerceAtLeast(0L), TimeUnit.MINUTES)
                .build()
    }
}
