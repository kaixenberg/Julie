package our.bunny.julie.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import our.bunny.julie.R

// ─── Font family ───────────────────────────────────────────────────────────────
// Uses the bundled Baloo 2 font for a friendly, rounded feel.
val Baloo2Family = FontFamily(
    Font(R.font.baloo2_regular, FontWeight.Normal),
    Font(R.font.baloo2_medium,  FontWeight.Medium),
    Font(R.font.baloo2_semibold, FontWeight.SemiBold),
    Font(R.font.baloo2_bold,    FontWeight.Bold),
)

// ─── M3 Expressive type scale ─────────────────────────────────────────────────
// Based on the Material 3 Expressive type scale (May 2025).
// Key deltas from the baseline M3 scale:
//   • displayLarge: 57 → 57sp (unchanged)
//   • headlineLarge: 32 → 34sp (+2sp for more presence)
//   • labelSmall: 11 → 10sp (tighter for dense data)
val ExpressiveTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Baloo2Family,
        fontWeight = FontWeight.Bold,
        fontSize   = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = Baloo2Family,
        fontWeight = FontWeight.Bold,
        fontSize   = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = Baloo2Family,
        fontWeight = FontWeight.Bold,
        fontSize   = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Baloo2Family,
        fontWeight = FontWeight.Bold,
        fontSize   = 34.sp,       // +2sp Expressive bump
        lineHeight = 42.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Baloo2Family,
        fontWeight = FontWeight.Bold,
        fontSize   = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Baloo2Family,
        fontWeight = FontWeight.Bold,
        fontSize   = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Baloo2Family,
        fontWeight = FontWeight.Bold,
        fontSize   = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Baloo2Family,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Baloo2Family,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Baloo2Family,
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Baloo2Family,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Baloo2Family,
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Baloo2Family,
        fontWeight = FontWeight.Medium,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Baloo2Family,
        fontWeight = FontWeight.Medium,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Baloo2Family,
        fontWeight = FontWeight.Normal,
        fontSize   = 10.sp,       // Expressive: -1sp for dense data labels
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

// Define SystemTypography using the exact same metrics but FontFamily.Default
val SystemTypography = Typography(
    displayLarge = ExpressiveTypography.displayLarge.copy(fontFamily = FontFamily.Default),
    displayMedium = ExpressiveTypography.displayMedium.copy(fontFamily = FontFamily.Default),
    displaySmall = ExpressiveTypography.displaySmall.copy(fontFamily = FontFamily.Default),
    headlineLarge = ExpressiveTypography.headlineLarge.copy(fontFamily = FontFamily.Default),
    headlineMedium = ExpressiveTypography.headlineMedium.copy(fontFamily = FontFamily.Default),
    headlineSmall = ExpressiveTypography.headlineSmall.copy(fontFamily = FontFamily.Default),
    titleLarge = ExpressiveTypography.titleLarge.copy(fontFamily = FontFamily.Default),
    titleMedium = ExpressiveTypography.titleMedium.copy(fontFamily = FontFamily.Default),
    titleSmall = ExpressiveTypography.titleSmall.copy(fontFamily = FontFamily.Default),
    bodyLarge = ExpressiveTypography.bodyLarge.copy(fontFamily = FontFamily.Default),
    bodyMedium = ExpressiveTypography.bodyMedium.copy(fontFamily = FontFamily.Default),
    bodySmall = ExpressiveTypography.bodySmall.copy(fontFamily = FontFamily.Default),
    labelLarge = ExpressiveTypography.labelLarge.copy(fontFamily = FontFamily.Default),
    labelMedium = ExpressiveTypography.labelMedium.copy(fontFamily = FontFamily.Default),
    labelSmall = ExpressiveTypography.labelSmall.copy(fontFamily = FontFamily.Default),
)

// Keep the old val as an alias so any existing references still compile.
val Typography = ExpressiveTypography
