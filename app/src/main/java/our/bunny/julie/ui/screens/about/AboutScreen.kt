package our.bunny.julie.ui.screens.about

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import our.bunny.julie.BuildConfig
import our.bunny.julie.R
import our.bunny.julie.util.UpdateManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLicenses: () -> Unit
) {
    val context = LocalContext.current
    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }
    var showEasterEgg by remember { mutableStateOf(false) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun handleIconTap() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTapTime > 1500) {
            tapCount = 1
        } else {
            tapCount++
            if (tapCount >= 7) {
                showEasterEgg = true
                Toast.makeText(context, "Fly High 🕊️", Toast.LENGTH_LONG).show()
                tapCount = 0
            }
        }
        lastTapTime = currentTime
    }

    var showImageCreditsDialog by remember { mutableStateOf(false) }

    if (showImageCreditsDialog) {
        AlertDialog(
            onDismissRequest = { showImageCreditsDialog = false },
            title = { Text("Image Credits") },
            text = {
                Text(
                    "The beautiful animal photography in the Fun Facts section is provided by talented artists on Unsplash:\n\n" +
                    "• Dog: Charles Deluvio\n" +
                    "• Cat: Manja Vitolic\n" +
                    "• Rabbit: Satyabratasm\n" +
                    "• Bird: David Clode\n" +
                    "• Guinea Pig: Bonnie Kittle\n" +
                    "• Mouse: Ricky Kharawala"
                )
            },
            confirmButton = {
                TextButton(onClick = { showImageCreditsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showEasterEgg) {
        Dialog(
            onDismissRequest = { showEasterEgg = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
            ) {
                // Back Button
                IconButton(
                    onClick = { showEasterEgg = false },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                // Assuming R.drawable.julie is the easter egg image
                val imageRes = context.resources.getIdentifier("julie", "drawable", context.packageName)
                if (imageRes != 0) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = "Julie Easter Egg",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Image 'julie' not found in drawable", color = Color.White)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // HEADER CARD
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Try to get ic_launcher
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                            .clickable { handleIconTap() },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.mipmap.ic_launcher_background),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Image(
                            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                            contentDescription = "App Icon",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val packageInfo = try {
                        context.packageManager.getPackageInfo(context.packageName, 0)
                    } catch (e: Exception) { null }
                    
                    val versionName = packageInfo?.versionName ?: BuildConfig.VERSION_NAME
                    val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        packageInfo?.longVersionCode?.toString() ?: BuildConfig.VERSION_CODE.toString()
                    } else {
                        packageInfo?.versionCode?.toString() ?: BuildConfig.VERSION_CODE.toString()
                    }

                    Text(
                        text = "v$versionName ($versionCode)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // DEDICATION CARD
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "I had the idea of making this app, when our pet rabbit Julie passed away; I wanted to honor her loss.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Privacy Focused Local Pet Stats Tracker for Android",
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // CREDITS SECTION
            Text("Credits", style = MaterialTheme.typography.titleLarge)
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsActionRow(
                        title = "Smarajit",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/kaixenberg"))
                            context.startActivity(intent)
                        }
                    )
                    HorizontalDivider()
                    SettingsActionRow(
                        title = "Shramana",
                        onClick = {
                            Toast.makeText(context, "My loving partner ❤️", Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider()
                    SettingsActionRow(
                        title = "Julie",
                        onClick = {
                            Toast.makeText(context, "🐰🕊️❤️", Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider()
                    SettingsActionRow(
                        title = "Source Code",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/kaixenberg/Julie"))
                            context.startActivity(intent)
                        }
                    )
                    HorizontalDivider()
                    SettingsActionRow(
                        title = "Fun Facts Image Credits",
                        onClick = {
                            showImageCreditsDialog = true
                        }
                    )
                    HorizontalDivider()
                    SettingsActionRow(
                        title = "Open Source Licenses",
                        onClick = onNavigateToLicenses
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            if (!BuildConfig.DEBUG) {
                // Check for Updates Button
                Button(
                    onClick = {
                        if (isCheckingUpdate) return@Button
                        isCheckingUpdate = true
                        coroutineScope.launch {
                            val updateInfo = UpdateManager.checkForUpdates()
                            when {
                                updateInfo == null -> Toast.makeText(context, "Could not reach update server. Check your internet connection.", Toast.LENGTH_LONG).show()
                                updateInfo.isUpdateAvailable -> UpdateManager.downloadAndInstallUpdate(context, updateInfo)
                                else -> Toast.makeText(context, "You are on the latest version", Toast.LENGTH_SHORT).show()
                            }
                            isCheckingUpdate = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    if (isCheckingUpdate) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Checking for updates...")
                    } else {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Check for Updates")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingsActionRow(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
    }
}
