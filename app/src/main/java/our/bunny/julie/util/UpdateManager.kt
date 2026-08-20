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
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import our.bunny.julie.BuildConfig
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
            connection.setRequestProperty("User-Agent", "Julie-App-Updater")
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = Json.parseToJsonElement(response).jsonObject

                val tagName = json["tag_name"]?.jsonPrimitive?.content ?: return@withContext null
                val remoteVersion = tagName.removePrefix("v")
                val currentVersion = BuildConfig.VERSION_NAME.removePrefix("v")
                Log.d("UpdateManager", "remoteVersion: $remoteVersion, currentVersion: $currentVersion")

                if (!isVersionGreater(remoteVersion, currentVersion)) {
                    Log.d("UpdateManager", "No update available")
                    return@withContext UpdateInfo(false, remoteVersion, "")
                }

                // Update is available — now look up the download URL
                val assets = json["assets"]?.jsonArray
                val downloadUrl = assets
                    ?.mapNotNull { it.jsonObject["browser_download_url"]?.jsonPrimitive?.content }
                    ?.firstOrNull { it.contains("arm64") }
                    ?: assets?.firstOrNull()?.jsonObject?.get("browser_download_url")?.jsonPrimitive?.content

                Log.d("UpdateManager", "Update available! downloadUrl: $downloadUrl")
                return@withContext UpdateInfo(true, remoteVersion, downloadUrl ?: "")
            } else {
                Log.e("UpdateManager", "Response Code: ${connection.responseCode}")
            }
            null
        } catch (e: Exception) {
            Log.e("UpdateManager", "checkForUpdates failed: ${e.message}")
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
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val apkFile = File(dir, "Julie-update-${updateInfo.latestVersion}.apk")

        // Use cached APK only if it already exists, is recent, AND the version is actually newer
        if (apkFile.exists() && isVersionGreater(updateInfo.latestVersion, BuildConfig.VERSION_NAME)) {
            val lastModified = apkFile.lastModified()
            val twentyFourHours = 24 * 60 * 60 * 1000L
            if (System.currentTimeMillis() - lastModified < twentyFourHours) {
                Toast.makeText(context, "Installing cached update...", Toast.LENGTH_SHORT).show()
                installApk(context, apkFile)
                return
            }
        }

        // Clear old Julie update APKs from public Downloads
        dir.listFiles()?.forEach { if (it.name.startsWith("Julie-update") && it.extension == "apk") it.delete() }

        // Storage check
        if (!checkStorageSpace()) {
            Toast.makeText(context, "Not enough storage to download update (Need 150MB+)", Toast.LENGTH_LONG).show()
            return
        }

        try {
            val request = DownloadManager.Request(Uri.parse(updateInfo.downloadUrl))
                .setTitle("Julie Update")
                .setDescription("Downloading version ${updateInfo.latestVersion}")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Julie-update-${updateInfo.latestVersion}.apk")

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            Toast.makeText(context, "Downloading update...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to start download.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkStorageSpace(): Boolean {
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val stat = android.os.StatFs(path.path)
        val bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
        val megAvailable = bytesAvailable / (1024 * 1024)
        return megAvailable >= 150
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to launch installer.", Toast.LENGTH_SHORT).show()
        }
    }
}
