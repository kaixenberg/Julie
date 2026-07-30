package our.bunny.julie.ui.screens.feeding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import our.bunny.julie.domain.model.FeedingLog
import our.bunny.julie.ui.components.SelectableEntryCard
import our.bunny.julie.ui.components.TrackerListScaffold
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedingLogScreen(
    onNavigateUp: () -> Unit,
    viewModel: FeedingLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentSort by viewModel.currentSort.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<FeedingLog?>(null) }

    TrackerListScaffold(
        title = "Feeding Log",
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
        onSortClick = {
            val nextSort = when (currentSort) {
                FeedingLogSort.DATE_NEWEST -> FeedingLogSort.DATE_OLDEST
                FeedingLogSort.DATE_OLDEST -> FeedingLogSort.CALORIES_HIGH
                FeedingLogSort.CALORIES_HIGH -> FeedingLogSort.CALORIES_LOW
                FeedingLogSort.CALORIES_LOW -> FeedingLogSort.DATE_NEWEST
            }
            viewModel.currentSort.value = nextSort
        },
        onFilterClick = {
            val nextFilter = when (currentFilter) {
                FeedingLogFilter.ALL -> FeedingLogFilter.BREAKFAST
                FeedingLogFilter.BREAKFAST -> FeedingLogFilter.LUNCH
                FeedingLogFilter.LUNCH -> FeedingLogFilter.DINNER
                FeedingLogFilter.DINNER -> FeedingLogFilter.SNACK
                FeedingLogFilter.SNACK -> FeedingLogFilter.CUSTOM
                FeedingLogFilter.CUSTOM -> FeedingLogFilter.ALL
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
                Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Add Meal")
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
        } else if (uiState.entries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No meals found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
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
                        FeedingEntryCardContent(entry = entry)
                    }
                }
            }
        }

        if (showAddDialog) {
            AddFeedingDialog(
                editingEntry = editingEntry,
                onDismiss = {
                    showAddDialog = false
                    editingEntry = null
                },
                onAdd = { food, quantity, unit, type, calories, notes ->
                    val timeToUse = editingEntry?.time ?: java.time.LocalDateTime.now()
                    viewModel.addOrUpdateFeedingEntry(editingEntry?.id ?: 0L, food, quantity, unit, type, calories, notes, timeToUse)
                    showAddDialog = false
                    editingEntry = null
                }
            )
        }
    }
}

@Composable
fun FeedingEntryCardContent(entry: FeedingLog) {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
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
                    text = entry.food,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = entry.time.format(formatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${entry.quantity} ${entry.unit} • ${entry.type}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (entry.calories != null) {
                    Text(
                        text = "${entry.calories} kcal",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFeedingDialog(
    editingEntry: FeedingLog? = null,
    onDismiss: () -> Unit,
    onAdd: (food: String, quantity: String, unit: String, type: String, calories: Int?, notes: String) -> Unit
) {
    var foodText by remember { mutableStateOf(editingEntry?.food ?: "") }
    var quantityText by remember { mutableStateOf(editingEntry?.quantity ?: "") }
    var notesText by remember { mutableStateOf(editingEntry?.notes ?: "") }
    var caloriesText by remember { mutableStateOf(editingEntry?.calories?.toString() ?: "") }
    
    var expandedType by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(editingEntry?.type ?: "Breakfast") }
    val types = listOf("Breakfast", "Lunch", "Dinner", "Snack", "Custom")
    
    var expandedUnit by remember { mutableStateOf(false) }
    var selectedUnit by remember { mutableStateOf(editingEntry?.unit ?: "cups") }
    val units = listOf("cups", "grams", "oz", "tbsp", "pieces", "cans")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editingEntry != null) "Edit Meal" else "Add Meal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = foodText,
                    onValueChange = { foodText = it },
                    label = { Text("Food") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = expandedUnit,
                    onExpandedChange = { expandedUnit = !expandedUnit }
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
                        units.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    selectedUnit = selectionOption
                                    expandedUnit = false
                                }
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = !expandedType }
                ) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Meal Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        types.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    selectedType = selectionOption
                                    expandedType = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = caloriesText,
                    onValueChange = { caloriesText = it },
                    label = { Text("Calories (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    if (foodText.isNotBlank() && quantityText.isNotBlank()) {
                        val cal = caloriesText.toIntOrNull()
                        onAdd(foodText, quantityText, selectedUnit, selectedType, cal, notesText)
                    }
                },
                enabled = foodText.isNotBlank() && quantityText.isNotBlank()
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
