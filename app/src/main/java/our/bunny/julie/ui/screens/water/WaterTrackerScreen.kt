package our.bunny.julie.ui.screens.water

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
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import our.bunny.julie.domain.model.WaterLog
import our.bunny.julie.util.UnitFormatter
import our.bunny.julie.util.WaterUnit
import our.bunny.julie.ui.components.SelectableEntryCard
import our.bunny.julie.ui.components.TrackerListScaffold
import our.bunny.julie.ui.components.MenuOption
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTrackerScreen(
    onNavigateUp: () -> Unit,
    viewModel: WaterTrackerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val currentSort by viewModel.currentSort.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    var editingEntry by remember { mutableStateOf<WaterLog?>(null) }

    TrackerListScaffold(
        title = "Water Tracker",
        onNavigateUp = onNavigateUp,
        isSelectionMode = selectedIds.isNotEmpty(),
        selectedCount = selectedIds.size,
        onClearSelection = { viewModel.clearSelection() },
        onSelectAll = { viewModel.selectAll(uiState.entries.map { it.id }) },
        onDeleteSelected = {
            viewModel.deleteSelected(uiState.entries)
        },
        showSearchOption = false,
        onSearchClick = { }, // No-op
        showFilterOption = false,
        sortOptions = listOf(
            MenuOption("Date: Newest first", currentSort == WaterSort.DATE_NEWEST) { viewModel.currentSort.value = WaterSort.DATE_NEWEST },
            MenuOption("Date: Oldest first", currentSort == WaterSort.DATE_OLDEST) { viewModel.currentSort.value = WaterSort.DATE_OLDEST },
            MenuOption("Amount: High to Low", currentSort == WaterSort.AMOUNT_HIGH) { viewModel.currentSort.value = WaterSort.AMOUNT_HIGH },
            MenuOption("Amount: Low to High", currentSort == WaterSort.AMOUNT_LOW) { viewModel.currentSort.value = WaterSort.AMOUNT_LOW }
        ),
        isSearchActive = false,
        searchQuery = "",
        onSearchQueryChange = { },
        onSearchClose = { },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Add Water")
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
        } else if (uiState.entries.isEmpty() && currentFilter == WaterFilter.ALL_TIME) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No water logged yet.")
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (uiState.entries.isNotEmpty()) {
                    Text(
                        "Water Consumption (Last 7 Days)",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp)
                    )
                    WaterBarChart(
                        entries = uiState.entries,
                        waterUnit = uiState.waterUnit,
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
                            WaterEntryCardContent(
                                entry = entry,
                                waterUnit = uiState.waterUnit
                            )
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddWaterDialog(
                editingEntry = editingEntry,
                waterUnit = uiState.waterUnit,
                onDismiss = {
                    showAddDialog = false
                    editingEntry = null
                },
                onAdd = { amount ->
                    val timeToUse = editingEntry?.time ?: java.time.LocalDateTime.now()
                    viewModel.addOrUpdateWaterEntry(editingEntry?.id ?: 0L, amount, uiState.waterUnit, timeToUse)
                    showAddDialog = false
                    editingEntry = null
                }
            )
        }
    }
}

@Composable
fun WaterEntryCardContent(entry: WaterLog, waterUnit: WaterUnit) {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
    val displayAmount = UnitFormatter.formatWater(entry.amount, waterUnit)
    Row(
        modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayAmount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = entry.time.format(formatter),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AddWaterDialog(
    editingEntry: WaterLog? = null,
    waterUnit: WaterUnit,
    onDismiss: () -> Unit,
    onAdd: (amount: Float) -> Unit
) {
    val initialAmountText = if (editingEntry != null) {
        UnitFormatter.getWaterInDisplayUnit(editingEntry.amount, waterUnit).toString()
    } else {
        ""
    }
    var amountText by remember { mutableStateOf(initialAmountText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editingEntry != null) "Edit Water" else "Add Water") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (${if (waterUnit == WaterUnit.ML) "ml" else "oz"})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toFloatOrNull()
                    if (amt != null) {
                        onAdd(amt)
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
fun WaterBarChart(entries: List<WaterLog>, waterUnit: WaterUnit, modifier: Modifier = Modifier) {
    if (entries.isEmpty()) return

    // Group by day of year for the last 7 days
    val now = java.time.LocalDateTime.now()
    val last7Days = (0..6).map { now.minusDays(it.toLong()).toLocalDate() }.reversed()
    
    val grouped = entries.groupBy { it.time.toLocalDate() }
    
    // Map to daily totals correctly converting to display unit
    val dailyTotals = last7Days.map { date ->
        val sumCanonical = grouped[date]?.sumOf { it.amount.toDouble() }?.toFloat() ?: 0f
        UnitFormatter.getWaterInDisplayUnit(sumCanonical, waterUnit)
    }
    
    val maxAmount = dailyTotals.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    val barColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barWidth = width / (dailyTotals.size * 2f)
        val spacing = width / dailyTotals.size
        
        dailyTotals.forEachIndexed { index, total ->
            val barHeight = (total / maxAmount) * height
            val x = (index * spacing) + (spacing / 2f) - (barWidth / 2f)
            val y = height - barHeight
            
            drawRect(
                color = barColor,
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }
    }
}
