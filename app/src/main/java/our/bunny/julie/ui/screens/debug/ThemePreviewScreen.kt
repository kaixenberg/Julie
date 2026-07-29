package our.bunny.julie.ui.screens.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import our.bunny.julie.ui.theme.DefaultSeedColor
import our.bunny.julie.ui.theme.JulieTheme
import our.bunny.julie.ui.theme.PaletteStyle

/**
 * Debug-only screen to visually validate M3 Expressive theming.
 *
 * Shows:
 *  • Dark/Light toggle
 *  • Dynamic colour toggle (wallpaper seed vs static seed)
 *  • Palette style selector
 *  • Sample components: buttons, chips, cards, FAB, colour swatches
 *
 * NOT wired into the NavGraph — launch directly from MainActivity
 * with the DEBUG_THEME_PREVIEW flag for visual QA.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemePreviewContent(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    dynamicColor: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
    paletteStyle: PaletteStyle,
    onPaletteStyleChange: (PaletteStyle) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🎨 Theme Preview",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {},
                icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                text = { Text("Log Health") },
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            // ── Dark / Dynamic toggles ────────────────────────────────────────
            item {
                SectionCard(title = "Theme Controls") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Dark Mode", style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = darkTheme, onCheckedChange = onDarkThemeChange)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Dynamic Color", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "(wallpaper seed, API 27+)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(checked = dynamicColor, onCheckedChange = onDynamicColorChange)
                    }
                }
            }

            // ── Palette style selector ────────────────────────────────────────
            item {
                SectionCard(title = "Palette Style") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                    ) {
                        items(PaletteStyle.entries) { style ->
                            FilterChip(
                                selected = paletteStyle == style,
                                onClick = { onPaletteStyleChange(style) },
                                label = { Text(style.label()) },
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Current: ${paletteStyle.label()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // ── Color role swatches ───────────────────────────────────────────
            item {
                SectionCard(title = "Color Roles") {
                    val roles = listOf(
                        "Primary"              to MaterialTheme.colorScheme.primary,
                        "Secondary"            to MaterialTheme.colorScheme.secondary,
                        "Tertiary"             to MaterialTheme.colorScheme.tertiary,
                        "PrimaryContainer"     to MaterialTheme.colorScheme.primaryContainer,
                        "SecondaryContainer"   to MaterialTheme.colorScheme.secondaryContainer,
                        "TertiaryContainer"    to MaterialTheme.colorScheme.tertiaryContainer,
                        "Surface"              to MaterialTheme.colorScheme.surface,
                        "SurfaceVariant"       to MaterialTheme.colorScheme.surfaceVariant,
                        "Error"                to MaterialTheme.colorScheme.error,
                        "Outline"              to MaterialTheme.colorScheme.outline,
                    )
                    roles.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            pair.forEach { (label, color) ->
                                ColorSwatch(
                                    label = label,
                                    color = color,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // ── Typography scale ─────────────────────────────────────────────
            item {
                SectionCard(title = "Expressive Typography") {
                    listOf(
                        "Display Small"  to MaterialTheme.typography.displaySmall,
                        "Headline Large" to MaterialTheme.typography.headlineLarge,
                        "Headline Medium" to MaterialTheme.typography.headlineMedium,
                        "Title Large"    to MaterialTheme.typography.titleLarge,
                        "Body Large"     to MaterialTheme.typography.bodyLarge,
                        "Body Medium"    to MaterialTheme.typography.bodyMedium,
                        "Label Large"    to MaterialTheme.typography.labelLarge,
                        "Label Small"    to MaterialTheme.typography.labelSmall,
                    ).forEach { (label, style) ->
                        Text(text = label, style = style)
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }

            // ── Shape scale ──────────────────────────────────────────────────
            item {
                SectionCard(title = "Expressive Shape Scale") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(
                            "XS\n4dp"  to MaterialTheme.shapes.extraSmall,
                            "S\n8dp"   to MaterialTheme.shapes.small,
                            "M\n16dp"  to MaterialTheme.shapes.medium,
                            "L\n24dp"  to MaterialTheme.shapes.large,
                            "XL\n28dp" to MaterialTheme.shapes.extraLarge,
                        ).forEach { (label, shape) ->
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(shape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    }
                }
            }

            // ── Buttons ──────────────────────────────────────────────────────
            item {
                SectionCard(title = "Components") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {}) { Text("Filled") }
                        OutlinedButton(onClick = {}) { Text("Outlined") }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = true,  onClick = {}, label = { Text("Selected") })
                        FilterChip(selected = false, onClick = {}, label = { Text("Chip") })
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) } // FAB clearance
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ColorSwatch(label: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Self-contained stateful wrapper — use this from MainActivity for debug QA.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemePreviewScreen() {
    var darkTheme    by remember { mutableStateOf(false) }
    var dynamicColor by remember { mutableStateOf(false) }
    var palette      by remember { mutableStateOf(PaletteStyle.TonalSpot) }

    JulieTheme(
        darkTheme    = darkTheme,
        dynamicColor = dynamicColor,
        paletteStyle = palette,
        seedColor    = DefaultSeedColor,
    ) {
        ThemePreviewContent(
            darkTheme            = darkTheme,
            onDarkThemeChange    = { darkTheme = it },
            dynamicColor         = dynamicColor,
            onDynamicColorChange = { dynamicColor = it },
            paletteStyle         = palette,
            onPaletteStyleChange = { palette = it },
        )
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "Light — TonalSpot", showBackground = true)
@Composable
private fun PreviewLight() {
    JulieTheme(darkTheme = false, dynamicColor = false, paletteStyle = PaletteStyle.TonalSpot) {
        ThemePreviewContent(
            darkTheme = false, onDarkThemeChange = {},
            dynamicColor = false, onDynamicColorChange = {},
            paletteStyle = PaletteStyle.TonalSpot, onPaletteStyleChange = {},
        )
    }
}

@Preview(name = "Dark — Expressive", showBackground = true)
@Composable
private fun PreviewDarkExpressive() {
    JulieTheme(darkTheme = true, dynamicColor = false, paletteStyle = PaletteStyle.Expressive) {
        ThemePreviewContent(
            darkTheme = true, onDarkThemeChange = {},
            dynamicColor = false, onDynamicColorChange = {},
            paletteStyle = PaletteStyle.Expressive, onPaletteStyleChange = {},
        )
    }
}

@Preview(name = "Light — Vibrant", showBackground = true)
@Composable
private fun PreviewLightVibrant() {
    JulieTheme(darkTheme = false, dynamicColor = false, paletteStyle = PaletteStyle.Vibrant) {
        ThemePreviewContent(
            darkTheme = false, onDarkThemeChange = {},
            dynamicColor = false, onDynamicColorChange = {},
            paletteStyle = PaletteStyle.Vibrant, onPaletteStyleChange = {},
        )
    }
}
