package our.bunny.julie.receiver

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import our.bunny.julie.R
import our.bunny.julie.util.UpdateManager
import java.io.File

class DownloadReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadId == -1L) return

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)

            if (cursor != null && cursor.moveToFirst()) {
                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                
                if (statusIndex >= 0 && uriIndex >= 0) {
                    val status = cursor.getInt(statusIndex)
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        val localUriString = cursor.getString(uriIndex)
                        if (localUriString != null) {
                            val uri = android.net.Uri.parse(localUriString)
                            val file = File(uri.path ?: "")
                            if (file.exists()) {
                                // Automatically attempt installation
                                UpdateManager.installApk(context, file)
                            }
                        }
                    }
                }
            }
            cursor?.close()
        }
    }    companion object {
        const val NOTIFICATION_ID = 5001
    }
}
