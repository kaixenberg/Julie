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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import our.bunny.julie.domain.model.Medication
import our.bunny.julie.domain.model.MedicationSchedule
import our.bunny.julie.util.MedicationScheduleFormatter
import our.bunny.julie.ui.components.TrackerListScaffold
import our.bunny.julie.ui.components.SelectableEntryCard
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen(
    onNavigateUp: () -> Unit,
    viewModel: MedicationListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentSort by viewModel.currentSort.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    var editingMedication by remember { mutableStateOf<Medication?>(null) }

    TrackerListScaffold(
        title = "Medications",
        onNavigateUp = onNavigateUp,
        isSelectionMode = selectedIds.isNotEmpty(),
        selectedCount = selectedIds.size,
        onClearSelection = { viewModel.clearSelection() },
        onSelectAll = { viewModel.selectAll(uiState.medications.map { it.id }) },
        onDeleteSelected = {
            viewModel.deleteSelected(uiState.medications)
        },
        showSearchOption = true,
        onSearchClick = { isSearchActive = true },
        onSortClick = {
            val nextSort = if (currentSort == MedicationSort.NAME) MedicationSort.STATUS else MedicationSort.NAME
            viewModel.currentSort.value = nextSort
        },
        onFilterClick = {
            val nextFilter = when (currentFilter) {
                MedicationFilter.ALL -> MedicationFilter.ACTIVE
                MedicationFilter.ACTIVE -> MedicationFilter.PAUSED
                MedicationFilter.PAUSED -> MedicationFilter.ALL
            }
            viewModel.currentFilter.value = nextFilter
        },
        isSearchActive = isSearchActive,
        searchQuery = searchQuery,
        onSearchQueryChange = { viewModel.searchQuery.value = it },
        onSearchClose = {
            isSearchActive = false
            viewModel.searchQuery.value = ""
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Add Medication")
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
                Text("No medications found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.medications) { med ->
                    val isSelected = selectedIds.contains(med.id)
                    SelectableEntryCard(
                        isSelected = isSelected,
                        isSelectionMode = selectedIds.isNotEmpty(),
                        onClick = {
                            if (selectedIds.isNotEmpty()) {
                                viewModel.toggleSelection(med.id)
                            } else {
                                editingMedication = med
                                showAddDialog = true
                            }
                        },
                        onLongClick = { viewModel.toggleSelection(med.id) },
                        onEditClick = {
                            editingMedication = med
                            showAddDialog = true
                        }
                    ) {
                        MedicationCardContent(
                            medication = med,
                            onToggleStatus = { viewModel.toggleMedicationStatus(med) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddMedicationDialog(
                editingMedication = editingMedication,
                onDismiss = {
                    showAddDialog = false
                    editingMedication = null
                },
                onAdd = { name, dosage, schedules, notes ->
                    // Since the current addMedication always inserts a new one or we can update it to take ID.
                    // Wait, MedicationListViewModel's addMedication currently doesn't take an ID.
                    // Let's pass the whole object or just ID. Wait, addMedication does not take ID.
                    // We'll update the viewModel in another step, or just pass editingMedication.id here if we have it, but wait, addMedication doesn't accept id.
                    // I'll update addMedication to take ID or just create an updateMedication.
                    // For now, I'll pass the ID to a new viewModel function or use insertMedication via a new param.
                    viewModel.addOrUpdateMedication(editingMedication?.id ?: 0L, name, dosage, schedules, notes)
                    showAddDialog = false
                    editingMedication = null
                }
            )
        }
    }
}

@Composable
fun MedicationCardContent(medication: Medication, onToggleStatus: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = medication.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = MedicationScheduleFormatter.format(medication.schedules),
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationDialog(
    editingMedication: Medication? = null,
    onDismiss: () -> Unit,
    onAdd: (name: String, dosage: String, schedules: List<MedicationSchedule>, notes: String) -> Unit
) {
    var nameText by remember { mutableStateOf(editingMedication?.name ?: "") }
    
    // Dosage text splitting logic (e.g. "1.5 pill(s)")
    val units = listOf("pill(s)", "capsule(s)", "drops", "ml", "mg", "g", "tsp", "tbsp")
    val defaultUnit = "pill(s)"
    
    val initialDosageParts = editingMedication?.dosage?.split(" ", limit = 2)
    val initialDosageAmt = initialDosageParts?.getOrNull(0) ?: ""
    val initialDosageUnit = initialDosageParts?.getOrNull(1)?.takeIf { units.contains(it) } ?: defaultUnit
    
    var dosageText by remember { mutableStateOf(initialDosageAmt) }
    var notesText by remember { mutableStateOf(editingMedication?.notes ?: "") }
    
    var expandedUnit by remember { mutableStateOf(false) }
    var selectedUnit by remember { mutableStateOf(initialDosageUnit) }

    var schedules by remember { mutableStateOf<List<MedicationSchedule>>(editingMedication?.schedules ?: emptyList()) }
    var showTimePicker by remember { mutableStateOf(false) }
    var timePickerState = rememberTimePickerState()
    
    // Day selection state for new/editing schedule
    val allDays = DayOfWeek.values().toSet()
    var selectedDays by remember { mutableStateOf(allDays) }
    
    // To handle editing an existing schedule
    var editingSchedule by remember { mutableStateOf<MedicationSchedule?>(null) }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(if (editingSchedule != null) "Edit Schedule" else "Add Schedule") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    TimePicker(state = timePickerState)
                    
                    Text("Repeat on:", style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val daysList = listOf(
                            DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
                        )
                        daysList.forEach { day ->
                            val isSelected = selectedDays.contains(day)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDays = if (isSelected) {
                                        selectedDays - day
                                    } else {
                                        selectedDays + day
                                    }
                                },
                                label = { Text(day.name.take(1)) },
                                modifier = Modifier.size(36.dp),
                                shape = MaterialTheme.shapes.small,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val newSched = MedicationSchedule(
                        timeOfDay = LocalTime.of(timePickerState.hour, timePickerState.minute),
                        daysOfWeek = selectedDays
                    )
                    schedules = if (editingSchedule != null) {
                        schedules.map { if (it == editingSchedule) newSched else it }
                    } else {
                        schedules + newSched
                    }
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editingMedication != null) "Edit Medication" else "Add Medication") },
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
                
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Schedule", style = MaterialTheme.typography.titleSmall)
                
                schedules.forEach { sched ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sched.timeOfDay.format(DateTimeFormatter.ofPattern("h:mm a")),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = MedicationScheduleFormatter.format(listOf(sched)).substringAfter("•").trim(),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = {
                                editingSchedule = sched
                                timePickerState = TimePickerState(sched.timeOfDay.hour, sched.timeOfDay.minute, false)
                                selectedDays = sched.daysOfWeek
                                showTimePicker = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Schedule")
                            }
                            IconButton(onClick = {
                                schedules = schedules - sched
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Schedule", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        editingSchedule = null
                        timePickerState = TimePickerState(8, 0, false)
                        selectedDays = allDays
                        showTimePicker = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add time")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameText.isNotBlank() && dosageText.isNotBlank() && schedules.isNotEmpty()) {
                        val combinedDosage = "$dosageText $selectedUnit".trim()
                        onAdd(nameText, combinedDosage, schedules, notesText)
                    }
                },
                enabled = nameText.isNotBlank() && dosageText.isNotBlank() && schedules.isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
