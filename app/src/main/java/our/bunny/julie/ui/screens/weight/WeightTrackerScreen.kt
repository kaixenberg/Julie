package our.bunny.julie.ui.screens.weight

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightTrackerScreen(
    onNavigateUp: () -> Unit,
    viewModel: WeightTrackerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weight Tracker") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
        } else if (uiState.entries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No weight entries yet.")
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
                        entries = uiState.entries.sortedBy { it.date }, // chronological order for chart
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
                        WeightEntryCard(
                            entry = entry,
                            weightUnit = uiState.weightUnit,
                            onDelete = { viewModel.deleteWeightEntry(entry) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddWeightDialog(
                defaultUnit = uiState.weightUnit,
                onDismiss = { showAddDialog = false },
                onAdd = { weight, unit, notes ->
                    viewModel.addWeightEntry(weight, unit, notes)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun WeightEntryCard(entry: WeightEntry, weightUnit: WeightUnit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = UnitFormatter.formatWeight(entry.weight, weightUnit),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = entry.date.toLocalDate().toString(),
                    style = MaterialTheme.typography.bodySmall
                )
                if (entry.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = entry.notes,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddWeightDialog(
    defaultUnit: WeightUnit,
    onDismiss: () -> Unit,
    onAdd: (weight: Float, unit: WeightUnit, notes: String) -> Unit
) {
    var weightText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(defaultUnit) } // default
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Weight") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Weight") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Unit:")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = unit == WeightUnit.KG, onClick = { unit = WeightUnit.KG })
                        Text("kg")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = unit == WeightUnit.LBS, onClick = { unit = WeightUnit.LBS })
                        Text("lbs")
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
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
                        onAdd(w, unit, notes)
                    }
                }
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
