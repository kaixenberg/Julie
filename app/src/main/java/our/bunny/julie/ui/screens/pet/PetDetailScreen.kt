package our.bunny.julie.ui.screens.pet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import our.bunny.julie.ui.screens.home.PetAvatar
import our.bunny.julie.util.UnitFormatter
import android.content.Intent
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import our.bunny.julie.ui.screens.export.ExportState
import our.bunny.julie.ui.screens.export.ExportViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    onNavigateUp: () -> Unit,
    onNavigateToEditPet: (Long) -> Unit,
    onNavigateToWeightTracker: (Long) -> Unit,
    onNavigateToWaterTracker: (Long) -> Unit,
    onNavigateToFeedingLog: (Long) -> Unit,
    onNavigateToMedicationList: (Long) -> Unit,
    onNavigateToTimeline: (Long) -> Unit,
    viewModel: PetDetailViewModel = hiltViewModel(),
    exportViewModel: ExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val exportState by exportViewModel.exportState.collectAsState()
    val context = LocalContext.current
    var exportAction by remember { mutableStateOf<String?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let { destUri ->
            if (exportState is ExportState.Success) {
                val file = (exportState as ExportState.Success).file
                context.contentResolver.openOutputStream(destUri)?.use { out ->
                    file.inputStream().use { inStream ->
                        inStream.copyTo(out)
                    }
                }
            }
        }
        exportViewModel.resetState()
        exportAction = null
    }

    LaunchedEffect(exportState) {
        if (exportState is ExportState.Success) {
            val file = (exportState as ExportState.Success).file
            if (exportAction == "SHARE") {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share Health Report"))
                exportViewModel.resetState()
                exportAction = null
            } else if (exportAction == "SAVE") {
                val date = java.time.LocalDate.now().toString()
                createDocumentLauncher.launch("Julie_Health_Report_${uiState.pet?.name}_$date.pdf")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.pet?.name ?: "Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    uiState.pet?.let { pet ->
                        if (exportState is ExportState.Generating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp).padding(end = 8.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            IconButton(onClick = { 
                                exportAction = "SHARE"
                                exportViewModel.generatePdf(context, pet.id) 
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "Share PDF")
                            }
                            IconButton(onClick = { 
                                exportAction = "SAVE"
                                exportViewModel.generatePdf(context, pet.id) 
                            }) {
                                Icon(Icons.Default.Done, contentDescription = "Save PDF")
                            }
                        }
                        IconButton(onClick = { onNavigateToEditPet(pet.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Pet")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.pet != null) {
            val pet = uiState.pet!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PetAvatar(species = pet.species)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = pet.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${pet.species} • ${pet.breed}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tracker Cards
                TrackerCard(
                    title = "Weight",
                    value = uiState.latestWeight?.let { UnitFormatter.formatWeight(it.weight, uiState.weightUnit) } ?: "No data",
                    subtitle = uiState.latestWeight?.date?.toLocalDate()?.toString() ?: "Tap to add",
                    onClick = { onNavigateToWeightTracker(pet.id) }
                )

                TrackerCard(
                    title = "Feeding Log",
                    value = uiState.latestFeeding?.food ?: "No data",
                    subtitle = uiState.latestFeeding?.time?.toLocalDate()?.toString() ?: "Tap to add",
                    onClick = { onNavigateToFeedingLog(pet.id) }
                )

                TrackerCard(
                    title = "Water",
                    value = UnitFormatter.formatWater(uiState.todayWater, uiState.waterUnit),
                    subtitle = "Today",
                    onClick = { onNavigateToWaterTracker(pet.id) }
                )

                TrackerCard(
                    title = "Medications",
                    value = if (uiState.activeMedicationsCount > 0) "${uiState.activeMedicationsCount} Active" else "None",
                    subtitle = "Tap to manage",
                    onClick = { onNavigateToMedicationList(pet.id) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onNavigateToTimeline(pet.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("View Health Timeline")
                }
            }
        }
    }
}

@Composable
fun TrackerCard(
    title: String,
    value: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
