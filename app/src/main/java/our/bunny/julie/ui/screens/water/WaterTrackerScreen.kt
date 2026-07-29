package our.bunny.julie.ui.screens.water

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTrackerScreen(
    onNavigateUp: () -> Unit,
    viewModel: WaterTrackerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Water Tracker") },
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
                Icon(Icons.Default.Add, contentDescription = "Add Water")
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
                        WaterEntryCard(
                            entry = entry,
                            onDelete = { viewModel.deleteWaterEntry(entry) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddWaterDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { amount, unit ->
                    viewModel.addWaterEntry(amount, unit)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun WaterEntryCard(entry: WaterLog, onDelete: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
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
                    text = "${entry.amount} ${entry.unit}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = entry.time.format(formatter),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddWaterDialog(
    onDismiss: () -> Unit,
    onAdd: (amount: Float, unit: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("ml") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Water") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Unit:")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = unit == "ml", onClick = { unit = "ml" })
                        Text("ml")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = unit == "oz", onClick = { unit = "oz" })
                        Text("oz")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toFloatOrNull()
                    if (amt != null) {
                        onAdd(amt, unit)
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
fun WaterBarChart(entries: List<WaterLog>, modifier: Modifier = Modifier) {
    if (entries.isEmpty()) return

    // Group by day of year for the last 7 days
    val now = java.time.LocalDateTime.now()
    val last7Days = (0..6).map { now.minusDays(it.toLong()).toLocalDate() }.reversed()
    
    val grouped = entries.groupBy { it.time.toLocalDate() }
    
    // Map to daily totals (converting oz to ml roughly for chart scale if mixed, but let's assume same unit for simplicity)
    val dailyTotals = last7Days.map { date ->
        grouped[date]?.sumOf { it.amount.toDouble() }?.toFloat() ?: 0f
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
