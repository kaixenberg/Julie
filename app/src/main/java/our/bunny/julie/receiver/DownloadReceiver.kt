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
                                UpdateManager.installApkSession(context, file)
                                
                                // Show persistent notification so user can retry if it fails or they cancel
                                showInstallNotification(context, file.absolutePath)
                            }
                        }
                    }
                }
            }
            cursor?.close()
        } else if (intent.action == ACTION_RETRY_INSTALL) {
            val apkPath = intent.getStringExtra(EXTRA_APK_PATH)
            if (apkPath != null) {
                val file = File(apkPath)
                if (file.exists()) {
                    UpdateManager.installApkSession(context, file)
                }
            }
        }
    }

    private fun showInstallNotification(context: Context, apkPath: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "update_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "App Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for app updates"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val retryIntent = Intent(context, DownloadReceiver::class.java).apply {
            action = ACTION_RETRY_INSTALL
            putExtra(EXTRA_APK_PATH, apkPath)
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(context, 1001, retryIntent, pendingIntentFlags)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Julie Update Ready")
            .setContentText("The update has been downloaded. Tap to install.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Persistent until updated or cleared manually
            .setAutoCancel(false)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val ACTION_RETRY_INSTALL = "our.bunny.julie.ACTION_RETRY_INSTALL"
        const val EXTRA_APK_PATH = "extra_apk_path"
        const val NOTIFICATION_ID = 5001
    }
}
