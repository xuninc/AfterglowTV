package com.afterglowtv.app.ui.screens.settings

import com.afterglowtv.app.BuildConfig
import com.afterglowtv.app.update.AppUpdateDownloadState
import com.afterglowtv.app.update.AppUpdateDownloadStatus
import com.afterglowtv.app.update.GitHubReleaseInfo
import kotlin.math.max

data class AppUpdateUiModel(
    val latestVersionName: String? = null,
    val latestVersionCode: Int? = null,
    val releaseUrl: String? = null,
    val downloadUrl: String? = null,
    val releaseNotes: String = "",
    val publishedAt: String? = null,
    val isUpdateAvailable: Boolean = false,
    val lastCheckedAt: Long? = null,
    val errorMessage: String? = null,
    val downloadStatus: AppUpdateDownloadStatus = AppUpdateDownloadStatus.Idle,
    val downloadedVersionName: String? = null
)

internal fun AppUpdateUiModel.toReleaseInfoOrNull(): GitHubReleaseInfo? {
    val versionName = latestVersionName ?: return null
    val releaseUrl = releaseUrl ?: return null
    return GitHubReleaseInfo(
        versionName = versionName,
        versionCode = latestVersionCode,
        releaseUrl = releaseUrl,
        downloadUrl = downloadUrl,
        releaseNotes = releaseNotes,
        publishedAt = publishedAt
    )
}

internal fun AppUpdateUiModel.withDownloadState(downloadState: AppUpdateDownloadState): AppUpdateUiModel {
    return copy(
        downloadStatus = downloadState.status,
        downloadedVersionName = downloadState.versionName
    )
}

internal fun AppUpdateUiModel.toDownloadState(): AppUpdateDownloadState {
    return AppUpdateDownloadState(
        status = downloadStatus,
        versionName = downloadedVersionName
    )
}

internal fun SettingsPreferenceSnapshot.toCachedAppUpdateUiModel(): AppUpdateUiModel {
    val versionName = cachedAppUpdateVersionName
    return AppUpdateUiModel(
        latestVersionName = versionName,
        latestVersionCode = cachedAppUpdateVersionCode,
        releaseUrl = cachedAppUpdateReleaseUrl,
        downloadUrl = cachedAppUpdateDownloadUrl,
        releaseNotes = cachedAppUpdateReleaseNotes,
        publishedAt = cachedAppUpdatePublishedAt,
        isUpdateAvailable = versionName?.let {
            isRemoteVersionNewer(cachedAppUpdateVersionCode, it)
        } ?: false,
        lastCheckedAt = lastAppUpdateCheckAt
    )
}

internal fun isRemoteVersionNewer(remoteVersionCode: Int?, remoteVersionName: String): Boolean {
    if (remoteVersionCode != null && remoteVersionCode > BuildConfig.VERSION_CODE) {
        return true
    }
    return compareVersionNamesStatic(remoteVersionName, BuildConfig.VERSION_NAME) > 0
}

internal fun compareVersionNamesStatic(left: String, right: String): Int {
    val leftParts = left.removePrefix("v").split('.')
    val rightParts = right.removePrefix("v").split('.')
    val length = max(leftParts.size, rightParts.size)
    for (index in 0 until length) {
        val leftValue = leftParts.getOrNull(index)?.toIntOrNull() ?: 0
        val rightValue = rightParts.getOrNull(index)?.toIntOrNull() ?: 0
        if (leftValue != rightValue) {
            return leftValue.compareTo(rightValue)
        }
    }
    return 0
}