package our.bunny.julie.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import our.bunny.julie.domain.model.ThemeConfig
import our.bunny.julie.ui.theme.PaletteStyle
import our.bunny.julie.util.WaterUnit
import our.bunny.julie.util.WeightUnit
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.platform.LocalContext
import our.bunny.julie.JulieApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    paddingValues: PaddingValues,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    appearanceViewModel: AppearanceSettingsViewModel = hiltViewModel(),
    notificationViewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val appearanceUiState by appearanceViewModel.uiState.collectAsState()
    val notificationUiState by notificationViewModel.uiState.collectAsState()
    
    val context = LocalContext.current
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            notificationViewModel.updateNotificationsEnabled(true)
        } else {
            notificationViewModel.updateNotificationsEnabled(false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // --- APPEARANCE ---
        Text("Appearance", style = MaterialTheme.typography.titleLarge)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Theme
                Column {
                    Text("Theme", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val options = listOf(ThemeConfig.SYSTEM, ThemeConfig.LIGHT, ThemeConfig.DARK)
                        options.forEachIndexed { index, option ->
                            SegmentedButton(
                                selected = appearanceUiState.themeConfig == option,
                                onClick = { appearanceViewModel.updateThemeConfig(option) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                            ) {
                                Text(
                                    when (option) {
                                        ThemeConfig.SYSTEM -> "System"
                                        ThemeConfig.LIGHT -> "Light"
                                        ThemeConfig.DARK -> "Dark"
                                    }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Dynamic Color
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dynamic Color", style = MaterialTheme.typography.titleMedium)
                        Text("Use wallpaper colors", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = appearanceUiState.dynamicColor,
                        onCheckedChange = { appearanceViewModel.updateDynamicColor(it) }
                    )
                }

                HorizontalDivider()

                // Palette Style
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { 
                        if (!appearanceUiState.dynamicColor) {
                            expanded = !expanded 
                        }
                    }
                ) {
                    OutlinedTextField(
                        value = appearanceUiState.paletteStyle.name.replace(Regex("([a-z])([A-Z]+)"), "$1 $2"),
                        onValueChange = {},
                        readOnly = true,
                        enabled = !appearanceUiState.dynamicColor,
                        label = { Text("Palette Style") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        PaletteStyle.entries.forEach { style ->
                            DropdownMenuItem(
                                text = { Text(style.name.replace(Regex("([a-z])([A-Z]+)"), "$1 $2")) },
                                onClick = {
                                    appearanceViewModel.updatePaletteStyle(style)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                if (appearanceUiState.dynamicColor) {
                    Text(
                        "Palette style is overridden when Dynamic Color is active.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()

                // Predictive Back
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Predictive Back", style = MaterialTheme.typography.titleMedium)
                        Text("Enable predictive back animations", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = appearanceUiState.predictiveBack,
                        onCheckedChange = { appearanceViewModel.updatePredictiveBack(it) }
                    )
                }

                HorizontalDivider()

                // Blur Effects
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Blur Effects", style = MaterialTheme.typography.titleMedium)
                        Text("Enable UI blur rendering", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = appearanceUiState.blurEffects,
                        onCheckedChange = { appearanceViewModel.updateBlurEffects(it) }
                    )
                }
            }
        }

        // --- UNITS ---
        Text("Units", style = MaterialTheme.typography.titleLarge)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Weight Unit", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    WeightUnit.entries.forEach { unit ->
                        val label = when (unit) {
                            WeightUnit.KG -> "Metric (kg)"
                            WeightUnit.LBS -> "Imperial (lbs)"
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = settingsUiState.weightUnit == unit,
                                onClick = { settingsViewModel.updateWeightUnit(unit) }
                            )
                            Text(text = label, modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Water Unit", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    WaterUnit.entries.forEach { unit ->
                        val label = when (unit) {
                            WaterUnit.ML -> "Metric (ml)"
                            WaterUnit.OZ -> "Imperial (oz)"
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = settingsUiState.waterUnit == unit,
                                onClick = { settingsViewModel.updateWaterUnit(unit) }
                            )
                            Text(text = label, modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }
        }

        // --- NOTIFICATIONS ---
        Text("Notifications", style = MaterialTheme.typography.titleLarge)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Master Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Notifications", style = MaterialTheme.typography.titleMedium)
                        Text("Receive reminders for your pets", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = notificationUiState.notificationsEnabled,
                        onCheckedChange = { enabled -> 
                            if (enabled) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    notificationViewModel.updateNotificationsEnabled(true)
                                }
                            } else {
                                notificationViewModel.updateNotificationsEnabled(false)
                            }
                        }
                    )
                }

                AnimatedVisibility(visible = notificationUiState.notificationsEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Weight Reminders", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = notificationUiState.remindersWeight,
                                onCheckedChange = { notificationViewModel.updateRemindersWeight(it) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Water Reminders", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = notificationUiState.remindersWater,
                                onCheckedChange = { notificationViewModel.updateRemindersWater(it) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Feeding Reminders", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = notificationUiState.remindersFeeding,
                                onCheckedChange = { notificationViewModel.updateRemindersFeeding(it) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Medication Reminders", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = notificationUiState.remindersMedication,
                                onCheckedChange = { notificationViewModel.updateRemindersMedication(it) }
                            )
                        }

                        HorizontalDivider()

                        TextButton(
                            onClick = {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Customize sound & vibration")
                        }
                    }
                }
            }
        }

        Text("Data & Export", style = MaterialTheme.typography.titleLarge)
        Card(modifier = Modifier.fillMaxWidth(), onClick = { /* TODO */ }) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Manage App Data", style = MaterialTheme.typography.titleMedium)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }

        Text("About", style = MaterialTheme.typography.titleLarge)
        Card(modifier = Modifier.fillMaxWidth(), onClick = { /* TODO */ }) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("About Pet Health App", style = MaterialTheme.typography.titleMedium)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
