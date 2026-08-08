package our.bunny.julie.util

import android.content.Context
import android.os.Build
import android.os.PowerManager
import java.util.Locale

object BatteryOptimizationHelper {

    // Known aggressive OEMs from dontkillmyapp.com
    private val AGGRESSIVE_OEMS = setOf(
        "xiaomi",
        "redmi",
        "poco",
        "huawei",
        "honor",
        "oneplus",
        "oppo",
        "vivo",
        "realme",
        "samsung",
        "asus",
        "meizu"
    )

    fun isAggressiveOem(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.ROOT)
        val brand = Build.BRAND.lowercase(Locale.ROOT)

        // Explicitly exclude Google/Pixel devices
        if (manufacturer == "google" || brand == "google") {
            return false
        }

        return AGGRESSIVE_OEMS.contains(manufacturer) || AGGRESSIVE_OEMS.contains(brand)
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }
}
