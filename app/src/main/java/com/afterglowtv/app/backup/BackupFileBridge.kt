package com.afterglowtv.app.backup

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.afterglowtv.app.BuildConfig
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object BackupFileBridge {
    const val MIME_TYPE_JSON = "application/json"
    private const val BACKUP_EXPORTS_DIR = "Backups"
    private const val BACKUP_IMPORTS_DIR = "backup_imports"
    private const val PUBLIC_BACKUP_EXPORTS_DIR = "AfterglowTV"
    private val exportNameFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    data class ExportDestination(
        val uri: Uri,
        val displayLocation: String
    )

    fun createDownloadsExport(context: Context): ExportDestination {
        val fileName = createExportFileName()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, MIME_TYPE_JSON)
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_DOWNLOADS}/$PUBLIC_BACKUP_EXPORTS_DIR"
                    )
                }
            )
            if (uri != null) {
                return ExportDestination(
                    uri = uri,
                    displayLocation = "${Environment.DIRECTORY_DOWNLOADS}/$PUBLIC_BACKUP_EXPORTS_DIR/$fileName"
                )
            }
        }

        val file = createExportFile(context, fileName)
        return ExportDestination(
            uri = providerUriForFile(context, file),
            displayLocation = file.absolutePath
        )
    }

    fun createExportFile(context: Context, fileName: String = createExportFileName()): File {
        val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val backupsDir = File(documentsDir, BACKUP_EXPORTS_DIR).apply { mkdirs() }
        return File(backupsDir, fileName)
            .also { file ->
                file.parentFile?.mkdirs()
                if (!file.exists()) file.createNewFile()
            }
    }

    fun copyToImportInbox(context: Context, sourceUri: Uri): Uri? {
        pruneImportInbox(context)
        val inboxDir = File(context.cacheDir, BACKUP_IMPORTS_DIR).apply { mkdirs() }
        val targetFile = File(inboxDir, "afterglowtv_import_${System.currentTimeMillis()}.json")
        return runCatching {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                targetFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            providerUriForFile(context, targetFile)
        }.getOrNull()
    }

    fun providerUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
    }

    fun buildShareIntent(uri: Uri): Intent {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = MIME_TYPE_JSON
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(sendIntent, "Share AfterglowTV backup")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    private fun pruneImportInbox(context: Context) {
        val cutoff = System.currentTimeMillis() - 7L * 24L * 60L * 60L * 1000L
        File(context.cacheDir, BACKUP_IMPORTS_DIR)
            .listFiles()
            ?.filter { it.isFile && it.lastModified() < cutoff }
            ?.forEach { it.delete() }
    }

    private fun createExportFileName(): String =
        "afterglowtv_backup_${LocalDateTime.now().format(exportNameFormatter)}.json"
}
