package our.bunny.julie.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.materialkolor.hct.Hct
import com.materialkolor.scheme.SchemeExpressive
import com.materialkolor.scheme.SchemeFruitSalad
import com.materialkolor.scheme.SchemeMonochrome
import com.materialkolor.scheme.SchemeNeutral
import com.materialkolor.scheme.SchemeRainbow
import com.materialkolor.scheme.SchemeTonalSpot
import com.materialkolor.scheme.SchemeVibrant
import com.materialkolor.dynamiccolor.MaterialDynamicColors

/**
 * The set of supported palette styles for the Julie theme.
 * Each maps to a MaterialColorUtilities DynamicScheme variant.
 */
enum class PaletteStyle {
    TonalSpot,   // Calm, balanced — M3 default character
    Neutral,     // Minimal chroma, subtle background tones
    Vibrant,     // Bold, saturated colours
    Expressive,  // Playful, offset hue shift — M3 Expressive default
    FruitSalad,  // Split-complementary palette
    Rainbow,     // Multiple hue sources
    Monochrome,  // Greyscale (no chroma)
    Julie;       // Julie Brand colors

    fun label(): String = when (this) {
        TonalSpot   -> "Tonal Spot"
        Neutral     -> "Neutral"
        Vibrant     -> "Vibrant"
        Expressive  -> "Expressive"
        FruitSalad  -> "Fruit Salad"
        Rainbow     -> "Rainbow"
        Monochrome  -> "Monochrome"
        Julie       -> "Julie"
    }
}

/**
 * Builds a Material 3 [ColorScheme] from a seed color ARGB integer,
 * a chosen [PaletteStyle], and dark/light mode flag.
 *
 * Uses the MaterialKolor material-color-utilities library (HCT + DynamicScheme)
 * to generate the full 40-token palette — the same algorithm used by Android's
 * Monet engine.
 */
fun buildColorScheme(
    seedColorArgb: Int,
    isDark: Boolean,
    style: PaletteStyle,
    contrastLevel: Double = 0.0
): ColorScheme {
    val hct = if (style == PaletteStyle.Julie) Hct.fromInt(0xFF908373.toInt()) else Hct.fromInt(seedColorArgb)
    val scheme = when (style) {
        PaletteStyle.TonalSpot   -> SchemeTonalSpot(hct, isDark, contrastLevel)
        PaletteStyle.Neutral     -> SchemeNeutral(hct, isDark, contrastLevel)
        PaletteStyle.Vibrant     -> SchemeVibrant(hct, isDark, contrastLevel)
        PaletteStyle.Expressive  -> SchemeExpressive(hct, isDark, contrastLevel)
        PaletteStyle.FruitSalad  -> SchemeFruitSalad(hct, isDark, contrastLevel)
        PaletteStyle.Rainbow     -> SchemeRainbow(hct, isDark, contrastLevel)
        PaletteStyle.Monochrome  -> SchemeMonochrome(hct, isDark, contrastLevel)
        PaletteStyle.Julie       -> SchemeTonalSpot(hct, isDark, contrastLevel)
    }

    val c = MaterialDynamicColors()

    fun Int.toComposeColor() = Color(this)

    return if (isDark) {
        darkColorScheme(
            primary                 = if (style == PaletteStyle.Julie) Color(0xFF908373) else c.primary().getArgb(scheme).toComposeColor(),
            onPrimary               = if (style == PaletteStyle.Julie) Color(0xFF2F2D21) else c.onPrimary().getArgb(scheme).toComposeColor(),
            primaryContainer        = if (style == PaletteStyle.Julie) Color(0xFF2F2D21) else c.primaryContainer().getArgb(scheme).toComposeColor(),
            onPrimaryContainer      = if (style == PaletteStyle.Julie) Color(0xFF908373) else c.onPrimaryContainer().getArgb(scheme).toComposeColor(),
            inversePrimary          = c.inversePrimary().getArgb(scheme).toComposeColor(),
            secondary               = c.secondary().getArgb(scheme).toComposeColor(),
            onSecondary             = c.onSecondary().getArgb(scheme).toComposeColor(),
            secondaryContainer      = c.secondaryContainer().getArgb(scheme).toComposeColor(),
            onSecondaryContainer    = c.onSecondaryContainer().getArgb(scheme).toComposeColor(),
            tertiary                = c.tertiary().getArgb(scheme).toComposeColor(),
            onTertiary              = c.onTertiary().getArgb(scheme).toComposeColor(),
            tertiaryContainer       = c.tertiaryContainer().getArgb(scheme).toComposeColor(),
            onTertiaryContainer     = c.onTertiaryContainer().getArgb(scheme).toComposeColor(),
            background              = c.background().getArgb(scheme).toComposeColor(),
            onBackground            = c.onBackground().getArgb(scheme).toComposeColor(),
            surface                 = c.surface().getArgb(scheme).toComposeColor(),
            onSurface               = c.onSurface().getArgb(scheme).toComposeColor(),
            surfaceVariant          = c.surfaceVariant().getArgb(scheme).toComposeColor(),
            onSurfaceVariant        = c.onSurfaceVariant().getArgb(scheme).toComposeColor(),
            surfaceTint             = c.primary().getArgb(scheme).toComposeColor(),
            inverseSurface          = c.inverseSurface().getArgb(scheme).toComposeColor(),
            inverseOnSurface        = c.inverseOnSurface().getArgb(scheme).toComposeColor(),
            error                   = c.error().getArgb(scheme).toComposeColor(),
            onError                 = c.onError().getArgb(scheme).toComposeColor(),
            errorContainer          = c.errorContainer().getArgb(scheme).toComposeColor(),
            onErrorContainer        = c.onErrorContainer().getArgb(scheme).toComposeColor(),
            outline                 = c.outline().getArgb(scheme).toComposeColor(),
            outlineVariant          = c.outlineVariant().getArgb(scheme).toComposeColor(),
            scrim                   = c.scrim().getArgb(scheme).toComposeColor(),
        )
    } else {
        lightColorScheme(
            primary                 = if (style == PaletteStyle.Julie) Color(0xFF2F2D21) else c.primary().getArgb(scheme).toComposeColor(),
            onPrimary               = if (style == PaletteStyle.Julie) Color.White else c.onPrimary().getArgb(scheme).toComposeColor(),
            primaryContainer        = if (style == PaletteStyle.Julie) Color(0xFF908373) else c.primaryContainer().getArgb(scheme).toComposeColor(),
            onPrimaryContainer      = if (style == PaletteStyle.Julie) Color(0xFF2F2D21) else c.onPrimaryContainer().getArgb(scheme).toComposeColor(),
            inversePrimary          = c.inversePrimary().getArgb(scheme).toComposeColor(),
            secondary               = c.secondary().getArgb(scheme).toComposeColor(),
            onSecondary             = c.onSecondary().getArgb(scheme).toComposeColor(),
            secondaryContainer      = c.secondaryContainer().getArgb(scheme).toComposeColor(),
            onSecondaryContainer    = c.onSecondaryContainer().getArgb(scheme).toComposeColor(),
            tertiary                = c.tertiary().getArgb(scheme).toComposeColor(),
            onTertiary              = c.onTertiary().getArgb(scheme).toComposeColor(),
            tertiaryContainer       = c.tertiaryContainer().getArgb(scheme).toComposeColor(),
            onTertiaryContainer     = c.onTertiaryContainer().getArgb(scheme).toComposeColor(),
            background              = c.background().getArgb(scheme).toComposeColor(),
            onBackground            = c.onBackground().getArgb(scheme).toComposeColor(),
            surface                 = c.surface().getArgb(scheme).toComposeColor(),
            onSurface               = c.onSurface().getArgb(scheme).toComposeColor(),
            surfaceVariant          = c.surfaceVariant().getArgb(scheme).toComposeColor(),
            onSurfaceVariant        = c.onSurfaceVariant().getArgb(scheme).toComposeColor(),
            surfaceTint             = c.primary().getArgb(scheme).toComposeColor(),
            inverseSurface          = c.inverseSurface().getArgb(scheme).toComposeColor(),
            inverseOnSurface        = c.inverseOnSurface().getArgb(scheme).toComposeColor(),
            error                   = c.error().getArgb(scheme).toComposeColor(),
            onError                 = c.onError().getArgb(scheme).toComposeColor(),
            errorContainer          = c.errorContainer().getArgb(scheme).toComposeColor(),
            onErrorContainer        = c.onErrorContainer().getArgb(scheme).toComposeColor(),
            outline                 = c.outline().getArgb(scheme).toComposeColor(),
            outlineVariant          = c.outlineVariant().getArgb(scheme).toComposeColor(),
            scrim                   = c.scrim().getArgb(scheme).toComposeColor(),
        )
    }
}
