package our.bunny.julie.ui.screens.weight

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import our.bunny.julie.domain.model.WeightEntry
import our.bunny.julie.util.UnitFormatter
import our.bunny.julie.util.WeightUnit
import our.bunny.julie.ui.components.SelectableEntryCard
import our.bunny.julie.ui.components.TrackerListScaffold
import our.bunny.julie.ui.components.MenuOption
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightTrackerScreen(
    onNavigateUp: () -> Unit,
    viewModel: WeightTrackerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentSort by viewModel.currentSort.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<WeightEntry?>(null) }

    TrackerListScaffold(
        title = "Weight Tracker",
        onNavigateUp = onNavigateUp,
        isSelectionMode = selectedIds.isNotEmpty(),
        selectedCount = selectedIds.size,
        onClearSelection = { viewModel.clearSelection() },
        onSelectAll = { viewModel.selectAll(uiState.entries.map { it.id }) },
        onDeleteSelected = {
            viewModel.deleteSelected(uiState.entries)
        },
        showSearchOption = true,
        onSearchClick = { isSearchActive = true },
        showFilterOption = false,
        sortOptions = listOf(
            MenuOption("Date: Newest first", currentSort == WeightSort.DATE_NEWEST) { viewModel.currentSort.value = WeightSort.DATE_NEWEST },
            MenuOption("Date: Oldest first", currentSort == WeightSort.DATE_OLDEST) { viewModel.currentSort.value = WeightSort.DATE_OLDEST },
            MenuOption("Weight: High to Low", currentSort == WeightSort.WEIGHT_HIGH) { viewModel.currentSort.value = WeightSort.WEIGHT_HIGH },
            MenuOption("Weight: Low to High", currentSort == WeightSort.WEIGHT_LOW) { viewModel.currentSort.value = WeightSort.WEIGHT_LOW }
        ),
        isSearchActive = isSearchActive,
        searchQuery = searchQuery,
        onSearchQueryChange = { viewModel.searchQuery.value = it },
        onSearchClose = {
            isSearchActive = false
            viewModel.searchQuery.value = ""
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Weight")
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
        } else if (uiState.entries.isEmpty() && currentFilter == WeightFilter.ALL_TIME && searchQuery.isBlank()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No weight logged yet.")
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (uiState.entries.size >= 2) {
                    Text(
                        "Weight Trend",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp)
                    )
                    WeightChart(
                        entries = uiState.entries.sortedBy { it.date },
                        weightUnit = uiState.weightUnit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(16.dp)
                    )
                }
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.entries) { entry ->
                        val isSelected = selectedIds.contains(entry.id)
                        SelectableEntryCard(
                            isSelected = isSelected,
                            isSelectionMode = selectedIds.isNotEmpty(),
                            onClick = {
                                if (selectedIds.isNotEmpty()) {
                                    viewModel.toggleSelection(entry.id)
                                } else {
                                    editingEntry = entry
                                    showAddDialog = true
                                }
                            },
                            onLongClick = { viewModel.toggleSelection(entry.id) },
                            onEditClick = {
                                editingEntry = entry
                                showAddDialog = true
                            }
                        ) {
                            WeightEntryCardContent(
                                entry = entry,
                                weightUnit = uiState.weightUnit
                            )
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddWeightDialog(
                editingEntry = editingEntry,
                weightUnit = uiState.weightUnit,
                onDismiss = {
                    showAddDialog = false
                    editingEntry = null
                },
                onAdd = { weight, notes ->
                    val timeToUse = editingEntry?.date ?: java.time.LocalDateTime.now()
                    viewModel.addOrUpdateWeightEntry(editingEntry?.id ?: 0L, weight, uiState.weightUnit, notes, timeToUse)
                    showAddDialog = false
                    editingEntry = null
                }
            )
        }
    }
}

@Composable
fun WeightEntryCardContent(entry: WeightEntry, weightUnit: WeightUnit) {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
    val displayWeight = UnitFormatter.formatWeight(entry.weight, weightUnit)
    Row(
        modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayWeight,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = entry.date.format(formatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (entry.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddWeightDialog(
    editingEntry: WeightEntry? = null,
    weightUnit: WeightUnit,
    onDismiss: () -> Unit,
    onAdd: (weight: Float, notes: String) -> Unit
) {
    val initialAmountText = if (editingEntry != null) {
        UnitFormatter.getWeightInDisplayUnit(editingEntry.weight, weightUnit).toString()
    } else {
        ""
    }
    var weightText by remember { mutableStateOf(initialAmountText) }
    var notesText by remember { mutableStateOf(editingEntry?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            our.bunny.julie.ui.components.BlurDialogWindow()
            Text(if (editingEntry != null) "Edit Weight" else "Add Weight") 
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Weight (${if (weightUnit == WeightUnit.KG) "kg" else "lbs"})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                    val w = weightText.toFloatOrNull()
                    if (w != null) {
                        onAdd(w, notesText)
                    }
                }
            ) {
                Text(if (editingEntry != null) "Save" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun WeightChart(entries: List<WeightEntry>, weightUnit: WeightUnit, modifier: Modifier = Modifier) {
    if (entries.isEmpty()) return
    
    val displayValues = entries.map { UnitFormatter.getWeightInDisplayUnit(it.weight, weightUnit) }
    
    val maxWeight = displayValues.maxOrNull() ?: return
    val minWeight = displayValues.minOrNull() ?: return
    val range = (maxWeight - minWeight).coerceAtLeast(1f) // Avoid divide by zero
    
    val lineColor = MaterialTheme.colorScheme.primary
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stepX = width / (entries.size - 1).coerceAtLeast(1).toFloat()
        
        val path = Path()
        entries.forEachIndexed { index, _ ->
            val displayWeight = displayValues[index]
            val x = index * stepX
            val y = height - ((displayWeight - minWeight) / range) * height
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            // Draw points
            drawCircle(
                color = lineColor,
                radius = 6f,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
        
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 4f)
        )
    }
}
