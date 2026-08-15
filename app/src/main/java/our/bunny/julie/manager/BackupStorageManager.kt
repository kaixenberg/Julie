package our.bunny.julie.manager

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Creates a new backup file in the configured SAF folder or falls back to MediaStore Downloads.
     * Returns a pair of (OutputStream?, String) where String is the status/fallback message.
     */
    fun createBackupOutputStream(
        configuredUriString: String?,
        isEncrypted: Boolean
    ): Pair<OutputStream?, String> {
        val extension = if (isEncrypted) "juliebak" else "json"
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val filename = "julie_backup_$timestamp.$extension"
        val mimeType = "application/octet-stream"

        if (!configuredUriString.isNullOrEmpty()) {
            try {
                val treeUri = Uri.parse(configuredUriString)
                val documentFile = DocumentFile.fromTreeUri(context, treeUri)
                
                if (documentFile != null && documentFile.canWrite()) {
                    val newFile = documentFile.createFile(mimeType, filename)
                    if (newFile != null) {
                        val outputStream = context.contentResolver.openOutputStream(newFile.uri)
                        if (outputStream != null) {
                            return Pair(outputStream, "Saved to configured folder")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback to MediaStore Downloads
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
            }

            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: return Pair(null, "Failed to create file in Downloads fallback")
                
            val outputStream = context.contentResolver.openOutputStream(uri)
            Pair(outputStream, "Saved to Downloads (fallback)")
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(null, "Fallback to Downloads failed: ${e.message}")
        }
    }
}
