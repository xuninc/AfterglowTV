package com.afterglowtv.app

import android.app.Application
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
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okio.Path.Companion.toOkioPath

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.afterglowtv.data.manager.recording.RecordingReconcileWorker
import com.afterglowtv.data.sync.ProviderSyncWorker
import com.afterglowtv.data.sync.XtreamIndexWorker
import com.afterglowtv.player.timeshift.TimeshiftDiskManager
import javax.inject.Inject

@HiltAndroidApp
class AfterglowTVApp : Application(), SingletonImageLoader.Factory {
    private val runtimeDiagnosticsManager by lazy { RuntimeDiagnosticsManager(this) }
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @Inject
    lateinit var gitHubReleaseChecker: GitHubReleaseChecker

    override fun onCreate() {
        super.onCreate()
        CrashReportStore.install(this)
        runtimeDiagnosticsManager.start()
        applicationScope.launch {
            // Clean up any timeshift temp directories left behind by crashes, OOM kills, or
            // force-stops from the previous run. activeSessionDir = null means wipe everything.
            TimeshiftDiskManager(applicationContext).cleanupStaleDirectories(activeSessionDir = null)
        }
        applicationScope.launch {
            // Load the user's saved theme palette ASAP so first-frame composition
            // renders in the chosen palette rather than the default Vaporwave.
            val storedId = preferencesRepository.themePalette.first()
            AppColors.applyPalette(AppPalette.byId(storedId))

            // Apply the bundled shape-set first, then layer any per-axis
            // overrides on top. Per-axis prefs are cleared whenever the user
            // switches the bundled set, so this composition order is right.
            val shapeSetId = preferencesRepository.themeShapeSet.first()
            com.afterglowtv.app.ui.design.AppStyles.apply(
                com.afterglowtv.app.ui.design.AppShapeSet.byId(shapeSetId)
            )
            applyPerAxisStyleOverrides()
        }
        applicationScope.launch {
            // Load saved Glow customization — intensity + per-role specs.
            // Empty serialized strings mean "use the in-code defaults".
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
        applicationScope.launch {
            refreshCachedAppUpdateIfNeeded()
        }
        
        // Schedule daily data maintenance: EPG pruning, stale-favorite cleanup, and DB compaction checks.
        // BLD-H02: Require network + device idle so the worker doesn't drain battery.
        val gcConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .setRequiresDeviceIdle(true)
            .build()

        val gcWorkRequest = PeriodicWorkRequestBuilder<com.afterglowtv.data.sync.SyncWorker>(24, java.util.concurrent.TimeUnit.HOURS)
            .setConstraints(gcConstraints)
            .build()
            
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DataMaintenanceWorker",
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

    override fun onTerminate() {
        runtimeDiagnosticsManager.stop()
        super.onTerminate()
    }

    private suspend fun applyPerAxisStyleOverrides() {
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

    private suspend fun refreshCachedAppUpdateIfNeeded() {
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
}
