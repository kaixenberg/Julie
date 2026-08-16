package our.bunny.julie.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import our.bunny.julie.domain.model.ThemeConfig
import our.bunny.julie.ui.theme.PaletteStyle
import our.bunny.julie.util.WaterUnit
import our.bunny.julie.util.WeightUnit
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Schedule
import android.app.TimePickerDialog
import android.net.Uri

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.platform.LocalContext
import our.bunny.julie.JulieApplication
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import our.bunny.julie.util.BatteryOptimizationHelper
import our.bunny.julie.util.MiuiAutostartHelper
import our.bunny.julie.util.AutostartState
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ChevronRight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    paddingValues: PaddingValues,
    onNavigateBack: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    appearanceViewModel: AppearanceSettingsViewModel = hiltViewModel(),
    notificationViewModel: NotificationSettingsViewModel = hiltViewModel(),
    backupRestoreViewModel: BackupRestoreViewModel = hiltViewModel()
) {
    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val appearanceUiState by appearanceViewModel.uiState.collectAsState()
    val notificationUiState by notificationViewModel.uiState.collectAsState()
    
    val context = LocalContext.current
    val alarmManager = remember { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    val snackbarHostState = remember { SnackbarHostState() }
    
    val backupRestoreUiState by backupRestoreViewModel.uiState.collectAsState()

    // Battery optimization state
    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context))
    }
    
    var miuiAutostartState by remember {
        mutableStateOf(MiuiAutostartHelper.getAutostartState(context))
    }
    
    val lifecycleOwner = LocalLifecycleOwner.current
    val showMiuiAutostartWarning = BatteryOptimizationHelper.isXiaomiFamily() &&
                                   notificationUiState.notificationsEnabled &&
                                   miuiAutostartState == AutostartState.DISABLED
                                   
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val showNotificationWarning = !hasNotificationPermission

    var hasInstallPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.packageManager.canRequestPackageInstalls()
            } else {
                true
            }
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isIgnoringBatteryOptimizations = BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
                miuiAutostartState = MiuiAutostartHelper.getAutostartState(context)
                hasInstallPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.packageManager.canRequestPackageInstalls()
                } else {
                    true
                }
                hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
                } else true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val showBatteryWarning = BatteryOptimizationHelper.isAggressiveOem() && 
                             notificationUiState.notificationsEnabled && 
                             !isIgnoringBatteryOptimizations

    LaunchedEffect(Unit) {
        backupRestoreViewModel.events.collect { event ->
            when (event) {
                is BackupRestoreEvent.ExportSuccess -> snackbarHostState.showSnackbar("Backup exported successfully to ${event.message}")
                is BackupRestoreEvent.ExportError -> snackbarHostState.showSnackbar("Export failed: ${event.message}")
                is BackupRestoreEvent.ImportSuccess -> snackbarHostState.showSnackbar("Data restored successfully")
                is BackupRestoreEvent.ImportError -> snackbarHostState.showSnackbar("Restore failed: ${event.message}")
                is BackupRestoreEvent.PassphraseRequired -> {
                    // This event is handled via state below
                }
            }
        }
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            notificationViewModel.updateNotificationsEnabled(true)
        } else {
            notificationViewModel.updateNotificationsEnabled(false)
        }
    }

    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var restoreUri by remember { mutableStateOf<Uri?>(null) }
    
    var showBackupPassphraseDialog by remember { mutableStateOf(false) }
    var showRestorePassphraseDialog by remember { mutableStateOf(false) }
    var passphraseInput by remember { mutableStateOf("") }
    var passphraseConfirmInput by remember { mutableStateOf("") }
    var passphraseError by remember { mutableStateOf<String?>(null) }

    val folderPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
            backupRestoreViewModel.updateBackupFolderUri(uri.toString())
        }
    }
    
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            restoreUri = uri
            showRestoreConfirmDialog = true
        }
    }

    LaunchedEffect(backupRestoreViewModel.events) {
        backupRestoreViewModel.events.collect { event ->
            if (event is BackupRestoreEvent.PassphraseRequired) {
                showRestoreConfirmDialog = false
                showRestorePassphraseDialog = true
                restoreUri = event.uri
            }
        }
    }

    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { 
                showRestoreConfirmDialog = false 
                restoreUri = null
            },
            title = { Text("Restore Data") },
            text = { Text("This will overwrite existing data for matching pets. Are you sure you want to continue?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirmDialog = false
                        restoreUri?.let { backupRestoreViewModel.importData(it) }
                    }
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showRestoreConfirmDialog = false 
                    restoreUri = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBackupPassphraseDialog) {
        AlertDialog(
            onDismissRequest = {
                showBackupPassphraseDialog = false
                passphraseInput = ""
                passphraseConfirmInput = ""
                passphraseError = null
            },
            title = { Text("Encrypt Backup") },
            text = {
                Column {
                    Text("Enter a strong passphrase to encrypt your backup. If you lose this, you cannot restore your data.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = passphraseInput,
                        onValueChange = { passphraseInput = it; passphraseError = null },
                        label = { Text("Passphrase") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = passphraseConfirmInput,
                        onValueChange = { passphraseConfirmInput = it; passphraseError = null },
                        label = { Text("Confirm Passphrase") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        isError = passphraseError != null
                    )
                    if (passphraseError != null) {
                        Text(passphraseError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (passphraseInput.isEmpty()) {
                            passphraseError = "Passphrase cannot be empty"
                        } else if (passphraseInput != passphraseConfirmInput) {
                            passphraseError = "Passphrases do not match"
                        } else {
                            showBackupPassphraseDialog = false
                            backupRestoreViewModel.exportData(passphraseInput.toCharArray())
                            passphraseInput = ""
                            passphraseConfirmInput = ""
                            passphraseError = null
                        }
                    }
                ) {
                    Text("Backup")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showBackupPassphraseDialog = false
                    passphraseInput = ""
                    passphraseConfirmInput = ""
                    passphraseError = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRestorePassphraseDialog) {
        AlertDialog(
            onDismissRequest = {
                showRestorePassphraseDialog = false
                passphraseInput = ""
                passphraseError = null
                restoreUri = null
            },
            title = { Text("Encrypted Backup") },
            text = {
                Column {
                    Text("This backup is encrypted. Please enter the passphrase to restore.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = passphraseInput,
                        onValueChange = { passphraseInput = it },
                        label = { Text("Passphrase") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (passphraseInput.isNotEmpty()) {
                            showRestorePassphraseDialog = false
                            restoreUri?.let { backupRestoreViewModel.importData(it, passphraseInput.toCharArray()) }
                            passphraseInput = ""
                            restoreUri = null
                        }
                    }
                ) {
                    Text("Decrypt & Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showRestorePassphraseDialog = false
                    passphraseInput = ""
                    restoreUri = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(visible = !hasInstallPermission) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Provide Install Permissions",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Text(
                                text = "Required to seamlessly update the app in the background.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    context.startActivity(intent)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Settings"
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = showBatteryWarning) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Disable battery optimizations",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Text(
                                text = "Disabling battery optimization is required for reminders to work reliably on this device.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://dontkillmyapp.com/"))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Learn More"
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = showNotificationWarning) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(fallbackIntent)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Missing Notification Permission",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Text(
                                text = "Julie needs permission to send you background reminders for your pets.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open Settings"
                        )
                    }
                }
            }
            
            AnimatedVisibility(visible = showMiuiAutostartWarning) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = MiuiAutostartHelper.getAutostartSettingsIntent(context)
                            if (intent != null) {
                                context.startActivity(intent)
                            } else {
                                android.widget.Toast.makeText(context, "Please enable Autostart for Julie in App Settings", android.widget.Toast.LENGTH_LONG).show()
                                val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(fallbackIntent)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable MIUI Autostart",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Text(
                                text = "Xiaomi devices require Autostart to be enabled for background reminders.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://dontkillmyapp.com/xiaomi"))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Learn More"
                            )
                        }
                    }
                }
            }

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { appearanceViewModel.updateDynamicColor(!appearanceUiState.dynamicColor) },
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
                    val effectiveStyle = if (appearanceUiState.dynamicColor) PaletteStyle.TonalSpot else appearanceUiState.paletteStyle
                    OutlinedTextField(
                        value = effectiveStyle.name.replace(Regex("([a-z])([A-Z]+)"), "$1 $2"),
                        onValueChange = {},
                        readOnly = true,
                        enabled = !appearanceUiState.dynamicColor,
                        label = { Text("Palette Style") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.then(if (appearanceUiState.dynamicColor) Modifier else Modifier.menuAnchor()).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        PaletteStyle.entries.filter { it != PaletteStyle.TonalSpot }.forEach { style ->
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

                val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
                val isDark = when (appearanceUiState.themeConfig) {
                    our.bunny.julie.domain.model.ThemeConfig.DARK -> true
                    our.bunny.julie.domain.model.ThemeConfig.LIGHT -> false
                    our.bunny.julie.domain.model.ThemeConfig.SYSTEM -> isSystemDark
                }

                // OLED Black
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isDark) Modifier.clickable { appearanceViewModel.updateOledBlack(!appearanceUiState.oledBlack) } else Modifier),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "OLED Black", 
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                        Text(
                            "True black background for dark mode", 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    }
                    Switch(
                        checked = appearanceUiState.oledBlack && isDark,
                        onCheckedChange = { appearanceViewModel.updateOledBlack(it) },
                        enabled = isDark
                    )
                }

                HorizontalDivider()

                // Predictive Back
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { appearanceViewModel.updatePredictiveBack(!appearanceUiState.predictiveBack) },
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

                // System Font
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { appearanceViewModel.updateUseSystemFont(!appearanceUiState.useSystemFont) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Use System Font", style = MaterialTheme.typography.titleMedium)
                        Text("Use device's default font instead of custom typography", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = appearanceUiState.useSystemFont,
                        onCheckedChange = { appearanceViewModel.updateUseSystemFont(it) }
                    )
                }

                if (our.bunny.julie.BuildConfig.ENABLE_FANCY_UI) {
                    HorizontalDivider()

                    // Blur Effects
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { appearanceViewModel.updateBlurEffects(!appearanceUiState.blurEffects) },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            val enabled = !notificationUiState.notificationsEnabled
                            if (enabled) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    notificationViewModel.updateNotificationsEnabled(true)
                                }
                            } else {
                                notificationViewModel.updateNotificationsEnabled(false)
                            }
                        },
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { notificationViewModel.updateQuietHoursEnabled(!notificationUiState.quietHoursEnabled) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                                Text("Quiet Hours", style = MaterialTheme.typography.bodyLarge)
                                Text("Suppress reminders between 10 PM\nand 7 AM", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = notificationUiState.quietHoursEnabled,
                                onCheckedChange = { notificationViewModel.updateQuietHoursEnabled(it) }
                            )
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { notificationViewModel.updateRemindersWeight(!notificationUiState.remindersWeight) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Weight Reminders", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = notificationUiState.remindersWeight,
                                onCheckedChange = { notificationViewModel.updateRemindersWeight(it) }
                            )
                        }
                        
                        AnimatedVisibility(visible = notificationUiState.remindersWeight) {
                            Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)) {
                                Text("Recurrence", style = MaterialTheme.typography.labelLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                    val options = listOf(1 to "Daily", 3 to "Every 3 Days", 7 to "Weekly")
                                    options.forEachIndexed { index, (days, label) ->
                                        SegmentedButton(
                                            selected = notificationUiState.remindersWeightIntervalDays == days,
                                            onClick = { notificationViewModel.updateRemindersWeightIntervalDays(days) },
                                            shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                                        ) {
                                            Text(
                                                text = label, 
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { notificationViewModel.updateRemindersWater(!notificationUiState.remindersWater) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Water Reminders", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = notificationUiState.remindersWater,
                                onCheckedChange = { notificationViewModel.updateRemindersWater(it) }
                            )
                        }
                        
                        AnimatedVisibility(visible = notificationUiState.remindersWater) {
                            Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)) {
                                Text("Check-in Frequency: Every ${notificationUiState.remindersWaterIntervalHours} hours", style = MaterialTheme.typography.labelLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                Slider(
                                    value = notificationUiState.remindersWaterIntervalHours.toFloat(),
                                    onValueChange = { notificationViewModel.updateRemindersWaterIntervalHours(it.toInt()) },
                                    valueRange = 1f..12f,
                                    steps = 10
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { notificationViewModel.updateRemindersFeeding(!notificationUiState.remindersFeeding) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Feeding Reminders", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = notificationUiState.remindersFeeding,
                                onCheckedChange = { notificationViewModel.updateRemindersFeeding(it) }
                            )
                        }

                        AnimatedVisibility(visible = notificationUiState.remindersFeeding) {
                            Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)) {
                                Text("Scheduled Times", style = MaterialTheme.typography.labelLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                notificationUiState.remindersFeedingTimes.sorted().forEach { time ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(time)
                                        IconButton(onClick = { 
                                            notificationViewModel.updateRemindersFeedingTimes(notificationUiState.remindersFeedingTimes - time) 
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove")
                                        }
                                    }
                                }
                                TextButton(onClick = {
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            val newTime = String.format("%02d:%02d", hourOfDay, minute)
                                            notificationViewModel.updateRemindersFeedingTimes(notificationUiState.remindersFeedingTimes + newTime)
                                        },
                                        8, 0, false
                                    ).show()
                                }) {
                                    Text("Add Time")
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val enabled = !notificationUiState.remindersMedication
                                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        notificationViewModel.updateRemindersMedication(enabled)
                                    }
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Medication Reminders", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = notificationUiState.remindersMedication,
                                onCheckedChange = { enabled ->
                                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        notificationViewModel.updateRemindersMedication(enabled)
                                    }
                                }
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

        Text("Backup & Export", style = MaterialTheme.typography.titleLarge)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val locationName = if (backupRestoreUiState.backupFolderUri.isNullOrEmpty()) {
                    "Not set — using Downloads"
                } else {
                    try {
                        val uri = android.net.Uri.parse(backupRestoreUiState.backupFolderUri)
                        androidx.documentfile.provider.DocumentFile.fromTreeUri(context, uri)?.name ?: "Custom Folder"
                    } catch (e: Exception) {
                        "Custom Folder"
                    }
                }
                SettingsActionRow(
                    icon = Icons.Outlined.Folder,
                    title = "Backup Location",
                    subtitle = locationName,
                    onClick = { folderPickerLauncher.launch(null) }
                )
                HorizontalDivider()
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { backupRestoreViewModel.updateEncryptedBackupEnabled(!backupRestoreUiState.isEncrypted) }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Encrypt Backup", style = MaterialTheme.typography.titleMedium)
                        Text("Protect backup with a passphrase", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = backupRestoreUiState.isEncrypted,
                        onCheckedChange = { backupRestoreViewModel.updateEncryptedBackupEnabled(it) }
                    )
                }
                HorizontalDivider()

                val autoBackupEnabled = backupRestoreUiState.isAutoBackupEnabled
                val isEncrypted = backupRestoreUiState.isEncrypted
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isEncrypted) Modifier else Modifier.clickable { backupRestoreViewModel.updateAutoBackupEnabled(!autoBackupEnabled) })
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto Backup", style = MaterialTheme.typography.titleMedium, color = if (isEncrypted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface)
                        Text(if (isEncrypted) "Disabled when Encrypted Backup is ON" else "Automatically backup daily", style = MaterialTheme.typography.bodyMedium, color = if (isEncrypted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = autoBackupEnabled && !isEncrypted,
                        onCheckedChange = { backupRestoreViewModel.updateAutoBackupEnabled(it) },
                        enabled = !isEncrypted
                    )
                }

                AnimatedVisibility(visible = autoBackupEnabled && !isEncrypted) {
                    Column {
                        HorizontalDivider()
                        SettingsActionRow(
                            icon = Icons.Outlined.Schedule,
                            title = "Auto Backup Time",
                            subtitle = String.format("%02d:%02d", backupRestoreUiState.autoBackupTimeMinutes / 60, backupRestoreUiState.autoBackupTimeMinutes % 60),
                            onClick = {
                                val currentHour = backupRestoreUiState.autoBackupTimeMinutes / 60
                                val currentMinute = backupRestoreUiState.autoBackupTimeMinutes % 60
                                android.app.TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        backupRestoreViewModel.updateAutoBackupTimeMinutes(hourOfDay * 60 + minute)
                                    },
                                    currentHour,
                                    currentMinute,
                                    false
                                ).show()
                            }
                        )
                        if (backupRestoreUiState.lastAutoBackupTimestamp != null) {
                            Text(
                                text = "Last backup: ${backupRestoreUiState.lastAutoBackupTimestamp}\nStatus: ${backupRestoreUiState.lastAutoBackupStatus}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                HorizontalDivider()

                SettingsActionRow(
                    icon = Icons.Outlined.CloudUpload,
                    title = "Backup Now",
                    subtitle = "Export all pets and history",
                    onClick = { 
                        if (backupRestoreUiState.isEncrypted) {
                            showBackupPassphraseDialog = true
                        } else {
                            backupRestoreViewModel.exportData()
                        }
                    }
                )
                HorizontalDivider()
                SettingsActionRow(
                    icon = Icons.Outlined.CloudDownload,
                    title = "Restore Data",
                    subtitle = "Import data from a backup file",
                    onClick = { 
                        importLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
                    }
                )
            }
        }

        Text("About", style = MaterialTheme.typography.titleLarge)
        Card(modifier = Modifier.fillMaxWidth(), onClick = onNavigateToAbout) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("About Julie", style = MaterialTheme.typography.titleMedium)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun SettingsActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
