package our.bunny.julie.ui.screens.medication

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import our.bunny.julie.domain.model.Medication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen(
    onNavigateUp: () -> Unit,
    viewModel: MedicationListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medications") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Medication")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.medications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No medications added yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.medications) { med ->
                    MedicationCard(
                        medication = med,
                        onToggleStatus = { viewModel.toggleMedicationStatus(med) },
                        onDelete = { viewModel.deleteMedication(med) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddMedicationDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, dosage, frequency, timeOfDay, notes ->
                    viewModel.addMedication(name, dosage, frequency, timeOfDay, notes)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun MedicationCard(medication: Medication, onToggleStatus: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${medication.dosage} • ${medication.frequency} at ${medication.timeOfDay}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (medication.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = medication.notes,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Switch(
                checked = medication.isActive,
                onCheckedChange = { onToggleStatus() }
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, dosage: String, frequency: String, timeOfDay: String, notes: String) -> Unit
) {
    var nameText by remember { mutableStateOf("") }
    var dosageText by remember { mutableStateOf("") }
    var timeOfDayText by remember { mutableStateOf("08:00") }
    var notesText by remember { mutableStateOf("") }
    
    var expandedFreq by remember { mutableStateOf(false) }
    var selectedFreq by remember { mutableStateOf("Daily") }
    val frequencies = listOf("Daily", "Twice a day", "Weekly")
    
    var expandedUnit by remember { mutableStateOf(false) }
    var selectedUnit by remember { mutableStateOf("pill(s)") }
    val units = listOf("pill(s)", "capsule(s)", "drops", "ml", "mg", "g", "tsp", "tbsp")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Medication") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Medication Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dosageText,
                        onValueChange = { dosageText = it },
                        label = { Text("Amount") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    ExposedDropdownMenuBox(
                        expanded = expandedUnit,
                        onExpandedChange = { expandedUnit = !expandedUnit },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedUnit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedUnit,
                            onDismissRequest = { expandedUnit = false }
                        ) {
                            units.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        selectedUnit = unit
                                        expandedUnit = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                ExposedDropdownMenuBox(
                    expanded = expandedFreq,
                    onExpandedChange = { expandedFreq = !expandedFreq }
                ) {
                    OutlinedTextField(
                        value = selectedFreq,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFreq) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedFreq,
                        onDismissRequest = { expandedFreq = false }
                    ) {
                        frequencies.forEach { freq ->
                            DropdownMenuItem(
                                text = { Text(freq) },
                                onClick = {
                                    selectedFreq = freq
                                    expandedFreq = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = timeOfDayText,
                    onValueChange = { timeOfDayText = it },
                    label = { Text("Time (HH:MM)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameText.isNotBlank() && dosageText.isNotBlank() && timeOfDayText.matches(Regex("\\d{2}:\\d{2}"))) {
                        val combinedDosage = "$dosageText $selectedUnit".trim()
                        onAdd(nameText, combinedDosage, selectedFreq, timeOfDayText, notesText)
                    }
                },
                enabled = nameText.isNotBlank() && dosageText.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
