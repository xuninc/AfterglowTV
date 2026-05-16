package com.afterglowtv.app

import android.app.Application
import android.os.Handler
import android.os.Looper
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import com.afterglowtv.app.diagnostics.CrashReportStore
import com.afterglowtv.app.diagnostics.RuntimeDiagnosticsManager
import com.afterglowtv.app.update.GitHubReleaseChecker
import com.afterglowtv.app.ui.accessibility.isReducedMotionEnabled
import com.afterglowtv.app.ui.design.AppColors
import com.afterglowtv.app.ui.design.AppPalette
import com.afterglowtv.data.preferences.PreferencesRepository
import com.afterglowtv.domain.model.Result
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okio.Path.Companion.toOkioPath

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.Operation
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.afterglowtv.data.manager.recording.RecordingReconcileWorker
import com.afterglowtv.data.sync.ProviderSyncWorker
import com.afterglowtv.data.sync.XtreamIndexWorker
import com.afterglowtv.player.timeshift.TimeshiftDiskManager
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class AfterglowTVApp : Application(), SingletonImageLoader.Factory {
    private val runtimeDiagnosticsManager by lazy { RuntimeDiagnosticsManager(this) }
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    override fun onCreate() {
        super.onCreate()
        CrashReportStore.install(this)
        applicationScope.launch {
            cancelColdStartMaintenanceWork()
        }
        scheduleDeferredStartupWork()
    }

    private fun scheduleDeferredStartupWork() {
        val startupDelayMs = if (BuildConfig.DEBUG) {
            DEBUG_STARTUP_BACKGROUND_WORK_DELAY_MS
        } else {
            STARTUP_BACKGROUND_WORK_DELAY_MS
        }
        mainHandler.postDelayed({
            runtimeDiagnosticsManager.start()
            applicationScope.launch {
                runDeferredStartupWork()
            }
        }, startupDelayMs)
    }

    private suspend fun runDeferredStartupWork() {
        val entryPoint = startupEntryPoint()
        val preferencesRepository = entryPoint.preferencesRepository()
        TimeshiftDiskManager(applicationContext).cleanupStaleDirectories(activeSessionDir = null)
        applySavedVisualPreferences(preferencesRepository)
        refreshCachedAppUpdateIfNeeded(preferencesRepository, entryPoint.gitHubReleaseChecker())
        enqueueMaintenanceWorkers()
    }

    private suspend fun applySavedVisualPreferences(preferencesRepository: PreferencesRepository) {
        val storedId = preferencesRepository.themePalette.first()
        AppColors.applyPalette(AppPalette.byId(storedId))

        val shapeSetId = preferencesRepository.themeShapeSet.first()
        com.afterglowtv.app.ui.design.AppStyles.apply(
            com.afterglowtv.app.ui.design.AppShapeSet.byId(shapeSetId)
        )
        applyPerAxisStyleOverrides(preferencesRepository)

        val intensity = preferencesRepository.glowIntensity.first()
        com.afterglowtv.app.ui.design.Glows.applyIntensity(intensity)

        preferencesRepository.glowFocusSpecs.first()
            .let { com.afterglowtv.app.ui.design.GlowSerialization.deserialize(it) }
            ?.let { com.afterglowtv.app.ui.design.Glows.overrideFocus(it) }
        preferencesRepository.glowLiveSpecs.first()
            .let { com.afterglowtv.app.ui.design.GlowSerialization.deserialize(it) }
            ?.let { com.afterglowtv.app.ui.design.Glows.overrideLive(it) }
        preferencesRepository.glowAmbientSpecs.first()
            .let { com.afterglowtv.app.ui.design.GlowSerialization.deserialize(it) }
            ?.let { com.afterglowtv.app.ui.design.Glows.overrideAmbient(it) }
    }

    private fun enqueueMaintenanceWorkers() {
        // Schedule daily data maintenance: EPG pruning, stale-favorite cleanup, and DB compaction checks.
        // BLD-H02: Require network + device idle so the worker doesn't drain battery.
        val gcConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .setRequiresDeviceIdle(true)
            .build()

        val gcWorkRequest = PeriodicWorkRequestBuilder<com.afterglowtv.data.sync.SyncWorker>(24, java.util.concurrent.TimeUnit.HOURS)
            .setConstraints(gcConstraints)
            .setInitialDelay(MAINTENANCE_WORK_INITIAL_DELAY_MINUTES, java.util.concurrent.TimeUnit.MINUTES)
            .build()
            
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            DATA_MAINTENANCE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            gcWorkRequest
        )

        ProviderSyncWorker.enqueuePeriodic(this)
        ProviderSyncWorker.enqueueLaunchStaleCheck(this)
        XtreamIndexWorker.enqueuePeriodic(this)
        XtreamIndexWorker.enqueueLaunchStaleCheck(this)
        RecordingReconcileWorker.enqueuePeriodic(this)
        RecordingReconcileWorker.enqueueOneShot(this)
    }

    private fun cancelColdStartMaintenanceWork() {
        val operations = buildList {
            add(WorkManager.getInstance(this@AfterglowTVApp).cancelUniqueWork(DATA_MAINTENANCE_WORK_NAME))
            addAll(ProviderSyncWorker.cancelStartupMaintenance(this@AfterglowTVApp))
            addAll(XtreamIndexWorker.cancelStartupMaintenance(this@AfterglowTVApp))
            addAll(RecordingReconcileWorker.cancelStartupMaintenance(this@AfterglowTVApp))
        }
        operations.awaitStartupMaintenance()
    }

    private fun List<Operation>.awaitStartupMaintenance() {
        forEach { operation ->
            runCatching {
                operation.result.get(5, TimeUnit.SECONDS)
            }
        }
    }

    override fun onTerminate() {
        runtimeDiagnosticsManager.stop()
        super.onTerminate()
    }

    private suspend fun applyPerAxisStyleOverrides(preferencesRepository: PreferencesRepository) {
        preferencesRepository.styleButton.first()?.let { saved ->
            runCatching { com.afterglowtv.app.ui.design.AppShapeSet.ButtonStyle.valueOf(saved) }
                .getOrNull()?.let(com.afterglowtv.app.ui.design.AppStyles::setButton)
        }
        preferencesRepository.styleEpgCell.first()?.let { saved ->
            runCatching { com.afterglowtv.app.ui.design.AppShapeSet.EpgCellStyle.valueOf(saved) }
                .getOrNull()?.let(com.afterglowtv.app.ui.design.AppStyles::setEpgCell)
        }
        preferencesRepository.styleEpgLiveCell.first()?.let { saved ->
            runCatching { com.afterglowtv.app.ui.design.AppShapeSet.EpgLiveCellStyle.valueOf(saved) }
                .getOrNull()?.let(com.afterglowtv.app.ui.design.AppStyles::setEpgLiveCell)
        }
        preferencesRepository.styleTextField.first()?.let { saved ->
            runCatching { com.afterglowtv.app.ui.design.AppShapeSet.TextFieldStyle.valueOf(saved) }
                .getOrNull()?.let(com.afterglowtv.app.ui.design.AppStyles::setTextField)
        }
        preferencesRepository.styleChannelRow.first()?.let { saved ->
            runCatching { com.afterglowtv.app.ui.design.AppShapeSet.ChannelRowStyle.valueOf(saved) }
                .getOrNull()?.let(com.afterglowtv.app.ui.design.AppStyles::setChannelRow)
        }
        preferencesRepository.stylePill.first()?.let { saved ->
            runCatching { com.afterglowtv.app.ui.design.AppShapeSet.PillStyle.valueOf(saved) }
                .getOrNull()?.let(com.afterglowtv.app.ui.design.AppStyles::setPill)
        }
        preferencesRepository.styleFocus.first()?.let { saved ->
            runCatching { com.afterglowtv.app.ui.design.AppShapeSet.FocusStyle.valueOf(saved) }
                .getOrNull()?.let(com.afterglowtv.app.ui.design.AppStyles::setFocus)
        }
        preferencesRepository.styleProgress.first()?.let { saved ->
            runCatching { com.afterglowtv.app.ui.design.AppShapeSet.ProgressStyle.valueOf(saved) }
                .getOrNull()?.let(com.afterglowtv.app.ui.design.AppStyles::setProgress)
        }
    }

    private suspend fun refreshCachedAppUpdateIfNeeded(
        preferencesRepository: PreferencesRepository,
        gitHubReleaseChecker: GitHubReleaseChecker
    ) {
        val autoCheckEnabled = preferencesRepository.autoCheckAppUpdates.first()
        if (!autoCheckEnabled) {
            return
        }

        val lastCheckedAt = preferencesRepository.lastAppUpdateCheckTimestamp.first()
        val now = System.currentTimeMillis()
        val checkIntervalMs = 24L * 60L * 60L * 1000L
        if (lastCheckedAt != null && now - lastCheckedAt < checkIntervalMs) {
            return
        }

        preferencesRepository.setLastAppUpdateCheckTimestamp(now)
        when (val result = gitHubReleaseChecker.fetchLatestRelease()) {
            is Result.Success -> {
                preferencesRepository.setCachedAppUpdateRelease(
                    versionName = result.data.versionName,
                    versionCode = result.data.versionCode,
                    releaseUrl = result.data.releaseUrl,
                    downloadUrl = result.data.downloadUrl,
                    releaseNotes = result.data.releaseNotes,
                    publishedAt = result.data.publishedAt
                )
            }
            else -> Unit
        }
    }

    private fun startupEntryPoint(): StartupEntryPoint =
        EntryPointAccessors.fromApplication(
            applicationContext,
            StartupEntryPoint::class.java
        )

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.15) // Conservative TV memory cache
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizeBytes(1024L * 1024L * 100L) // 100MB disk cache
                    .build()
            }
            // Limit concurrent decoding and fetching to 6 for TV hardware constraints
            .fetcherCoroutineContext(Dispatchers.IO.limitedParallelism(6))
            .decoderCoroutineContext(Dispatchers.Default.limitedParallelism(4))
            .crossfade(!isReducedMotionEnabled(context))
            .build()
    }

    companion object {
        private const val DATA_MAINTENANCE_WORK_NAME = "DataMaintenanceWorker"
        private const val MAINTENANCE_WORK_INITIAL_DELAY_MINUTES = 15L
        private const val STARTUP_BACKGROUND_WORK_DELAY_MS = 5_000L
        private const val DEBUG_STARTUP_BACKGROUND_WORK_DELAY_MS = 60_000L
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface StartupEntryPoint {
        fun preferencesRepository(): PreferencesRepository
        fun gitHubReleaseChecker(): GitHubReleaseChecker
    }
}
