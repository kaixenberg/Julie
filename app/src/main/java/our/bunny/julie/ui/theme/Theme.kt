package our.bunny.julie.ui.theme

import android.app.WallpaperManager
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// ─── Default seed: a warm earthy green suited to a pet health app ──────────────
val DefaultSeedColor = Color(0xFF6B9E5E)

// ─── Expressive shape scale ────────────────────────────────────────────────────
// M3 Expressive bumps corners: ExtraSmall 4→4, Small 8→8, Medium 12→12,
// Large 16→16, ExtraLarge 28→28 (new), ExtraExtraLarge 48dp (new).
val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small      = RoundedCornerShape(8.dp),
    medium     = RoundedCornerShape(16.dp),       // slightly rounder than default 12dp
    large      = RoundedCornerShape(24.dp),       // rounder than default 16dp
    extraLarge = RoundedCornerShape(28.dp),       // M3 Expressive ExtraLarge
)

// ─── Theme ─────────────────────────────────────────────────────────────────────

/**
 * Main app theme for Julie.
 *
 * @param darkTheme       Follow system dark mode by default.
 * @param dynamicColor    When true and running on Android 12+, seeds the palette
 *                        from the device wallpaper's primary color instead of [seedColor].
 * @param paletteStyle    Which MaterialColorUtilities DynamicScheme variant to use.
 * @param seedColor       Fallback seed colour used when [dynamicColor] is false or
 *                        unavailable (API < 27).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun JulieTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    seedColor: Color = DefaultSeedColor,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = remember(darkTheme, dynamicColor, paletteStyle, seedColor) {
        // --- Resolve seed ARGB --------------------------------------------------
        val seedArgb: Int = when {
            // On API 27+ we can read the wallpaper's primary colour as a seed.
            // On API 31+ this is the same primary swatch the system's Monet engine sees.
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 -> {
                runCatching {
                    val wm = WallpaperManager.getInstance(context)
                    wm.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
                        ?.primaryColor
                        ?.toArgb()
                        ?: seedColor.toArgb()
                }.getOrElse { seedColor.toArgb() }
            }
            else -> seedColor.toArgb()
        }

        // --- Build palette from seed + style ------------------------------------
        buildColorScheme(
            seedColorArgb  = seedArgb,
            isDark         = darkTheme,
            style          = paletteStyle,
        )
    }

    MaterialTheme(
        colorScheme  = colorScheme,
        typography   = ExpressiveTypography,
        shapes       = ExpressiveShapes,
        motionScheme = MotionScheme.expressive(),
        content      = content,
    )
}
