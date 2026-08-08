package our.bunny.julie.util

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process

enum class AutostartState {
    ENABLED,
    DISABLED,
    NO_INFO
}

object MiuiAutostartHelper {
    // Hidden AppOpsManager operation code for Autostart on MIUI
    private const val OP_AUTO_START = 10008

    fun getAutostartState(context: Context): AutostartState {
        if (!BatteryOptimizationHelper.isXiaomiFamily()) {
            return AutostartState.NO_INFO
        }

        return try {
            val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val method = appOpsManager.javaClass.getDeclaredMethod(
                "checkOpNoThrow",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                String::class.java
            )
            val result = method.invoke(
                appOpsManager,
                OP_AUTO_START,
                Process.myUid(),
                context.packageName
            ) as Int

            if (result == AppOpsManager.MODE_ALLOWED) {
                AutostartState.ENABLED
            } else {
                AutostartState.DISABLED
            }
        } catch (e: Exception) {
            // Fails safe on non-MIUI or updated MIUI breaking reflection
            AutostartState.NO_INFO
        }
    }

    fun getAutostartSettingsIntent(context: Context): Intent? {
        // Try the direct AutoStartManagementActivity first
        val directIntent = Intent().apply {
            component = android.content.ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
        }
        
        if (directIntent.resolveActivity(context.packageManager) != null) {
            return directIntent
        }

        // Fallback to PermissionsEditorActivity
        val fallbackIntent = Intent().apply {
            component = android.content.ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.permissions.PermissionsEditorActivity"
            )
            putExtra("extra_pkgname", context.packageName)
        }

        if (fallbackIntent.resolveActivity(context.packageManager) != null) {
            return fallbackIntent
        }

        return null
    }
}
