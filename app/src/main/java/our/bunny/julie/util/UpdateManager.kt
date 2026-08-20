package our.bunny.julie.util

import android.app.DownloadManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import our.bunny.julie.BuildConfig
import our.bunny.julie.receiver.UpdateReceiver
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val isUpdateAvailable: Boolean,
    val latestVersion: String,
    val downloadUrl: String
)

object UpdateManager {

    private const val GITHUB_API_URL = "https://api.github.com/repos/kaixenberg/Julie/releases/latest"

    suspend fun checkForUpdates(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = Json.parseToJsonElement(response).jsonObject
                
                val tagName = json["tag_name"]?.jsonPrimitive?.content ?: return@withContext null
                val assets = json["assets"]?.jsonArray
                val downloadUrl = assets?.firstOrNull()?.jsonObject?.get("browser_download_url")?.jsonPrimitive?.content
                
                if (downloadUrl != null) {
                    val remoteVersion = tagName.removePrefix("v")
                    val currentVersion = BuildConfig.VERSION_NAME.removePrefix("v")
                    
                    if (isVersionGreater(remoteVersion, currentVersion)) {
                        return@withContext UpdateInfo(true, remoteVersion, downloadUrl)
                    } else {
                        return@withContext UpdateInfo(false, remoteVersion, "")
                    }
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun isVersionGreater(remote: String, local: String): Boolean {
        val remoteParts = remote.split(".").mapNotNull { it.toIntOrNull() }
        val localParts = local.split(".").mapNotNull { it.toIntOrNull() }
        
        val maxLength = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until maxLength) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }
        return false
    }

    fun downloadAndInstallUpdate(context: Context, updateInfo: UpdateInfo) {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val apkFile = File(dir, "Julie-update-${updateInfo.latestVersion}.apk")
        
        // 24H rule check
        if (apkFile.exists()) {
            val lastModified = apkFile.lastModified()
            val twentyFourHours = 24 * 60 * 60 * 1000L
            if (System.currentTimeMillis() - lastModified < twentyFourHours) {
                Toast.makeText(context, "Installing cached update...", Toast.LENGTH_SHORT).show()
                installApkSession(context, apkFile)
                return
            }
        }
        
        // Clear old downloads
        dir?.listFiles()?.forEach { if (it.name.startsWith("Julie-update") && it.extension == "apk") it.delete() }
        
        // Storage check
        if (!checkStorageSpace(context)) {
            Toast.makeText(context, "Not enough storage to download update (Need 150MB+)", Toast.LENGTH_LONG).show()
            return
        }

        try {
            val request = DownloadManager.Request(Uri.parse(updateInfo.downloadUrl))
                .setTitle("Julie Update")
                .setDescription("Downloading version ${updateInfo.latestVersion}")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "Julie-update-${updateInfo.latestVersion}.apk")
            
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            Toast.makeText(context, "Downloading update...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to start download.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkStorageSpace(context: Context): Boolean {
        val path = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return true
        val stat = android.os.StatFs(path.path)
        val bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
        val megAvailable = bytesAvailable / (1024 * 1024)
        return megAvailable >= 150
    }

    fun installApkSession(context: Context, apkFile: File) {
        val packageInstaller = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        var sessionId = -1
        try {
            sessionId = packageInstaller.createSession(params)
            val session = packageInstaller.openSession(sessionId)
            
            val out = session.openWrite("package", 0, -1)
            apkFile.inputStream().use { input ->
                input.copyTo(out)
            }
            session.fsync(out)
            out.close()

            val intent = Intent(context, UpdateReceiver::class.java)
            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            
            val pendingIntent = PendingIntent.getBroadcast(context, 345, intent, pendingIntentFlags)
            session.commit(pendingIntent.intentSender)
            
        } catch (e: Exception) {
            e.printStackTrace()
            if (sessionId != -1) {
                packageInstaller.abandonSession(sessionId)
            }
        }
    }
}
