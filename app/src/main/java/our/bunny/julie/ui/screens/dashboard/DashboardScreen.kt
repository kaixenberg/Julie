package our.bunny.julie.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.hilt.navigation.compose.hiltViewModel
import our.bunny.julie.domain.model.Pet
import our.bunny.julie.ui.screens.home.PetAvatar
import our.bunny.julie.util.UnitFormatter
import our.bunny.julie.util.WaterUnit
import our.bunny.julie.util.WeightUnit

@Composable
fun DashboardScreen(
    paddingValues: PaddingValues, // Ignored, as DashboardScreen now owns its scaffold
    onOpenDrawer: () -> Unit,
    onNavigateToAddPet: () -> Unit,
    onNavigateToEditPet: (Long) -> Unit,
    onNavigateToPetDetail: (Long) -> Unit,
    onNavigateToPetStatDetail: (Long, our.bunny.julie.ui.navigation.StatType) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedSpecies by viewModel.selectedSpecies.collectAsState()
    val availableSpecies by viewModel.availableSpecies.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    var isFilterExpanded by remember { mutableStateOf(false) }
    var selectedPetId by remember { mutableStateOf<Long?>(null) }

    our.bunny.julie.ui.navigation.JulieAppScaffold(
        title = if (selectedPetId != null) "1 Selected" else "Dashboard",
        onOpenDrawer = onOpenDrawer,
        isSearchActive = isSearchActive && selectedPetId == null,
        searchQuery = searchQuery,
        onSearchQueryChange = { viewModel.searchQuery.value = it },
        onSearchClose = {
            isSearchActive = false
            viewModel.searchQuery.value = ""
        },
        navigationIcon = {
            if (selectedPetId != null) {
                IconButton(onClick = { selectedPetId = null }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear Selection")
                }
            } else {
                IconButton(onClick = onOpenDrawer) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open navigation drawer"
                    )
                }
            }
        },
        actions = {
            if (selectedPetId != null) {
                IconButton(onClick = {
                    onNavigateToEditPet(selectedPetId!!)
                    selectedPetId = null
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Pet")
                }
                IconButton(onClick = {
                    viewModel.deletePet(selectedPetId!!)
                    selectedPetId = null
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Pet")
                }
            } else {
                IconButton(onClick = { isSearchActive = true }) {
                    Icon(Icons.Default.Search, contentDescription = "Search Pets")
                }
                IconButton(onClick = { isFilterExpanded = !isFilterExpanded }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter by Species")
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddPet) {
                Icon(Icons.Default.Add, contentDescription = "Add Pet")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            AnimatedVisibility(visible = isFilterExpanded && availableSpecies.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedSpecies.isEmpty(),
                        onClick = { viewModel.selectedSpecies.value = emptySet() },
                        label = { Text("All") }
                    )
                    availableSpecies.forEach { species ->
                        FilterChip(
                            selected = selectedSpecies.contains(species),
                            onClick = {
                                val current = selectedSpecies.toMutableSet()
                                if (current.contains(species)) current.remove(species) else current.add(species)
                                viewModel.selectedSpecies.value = current
                            },
                            label = { Text(species) }
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (!uiState.hasAnyPets) {
                // Zero pets at all in DB empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Welcome to Julie!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Let's get started by adding your first pet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onNavigateToAddPet,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Your First Pet")
                    }
                }
            } else if (uiState.petsData.isEmpty()) {
                // Search/Filter returned no results
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No pets found matching your criteria",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(uiState.petsData, key = { it.pet.id }) { petData ->
                        PetSummarySection(
                            petData = petData,
                            weightUnit = uiState.weightUnit,
                            waterUnit = uiState.waterUnit,
                            isSelected = selectedPetId == petData.pet.id,
                            isSelectionModeActive = selectedPetId != null,
                            onNavigateToPetDetail = onNavigateToPetDetail,
                            onNavigateToPetStatDetail = onNavigateToPetStatDetail,
                            onToggleSelection = {
                                selectedPetId = if (selectedPetId == petData.pet.id) null else petData.pet.id
                            },
                            onLongPress = { selectedPetId = petData.pet.id }
                        )
                        
                        if (uiState.petsData.size >= 3) {
                            HorizontalDivider(modifier = Modifier.padding(top = 24.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PetSummarySection(
    petData: PetDashboardData,
    weightUnit: WeightUnit,
    waterUnit: WaterUnit,
    isSelected: Boolean,
    isSelectionModeActive: Boolean,
    onNavigateToPetDetail: (Long) -> Unit,
    onNavigateToPetStatDetail: (Long, our.bunny.julie.ui.navigation.StatType) -> Unit,
    onToggleSelection: () -> Unit,
    onLongPress: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .combinedClickable(
                    onClick = {
                        if (isSelectionModeActive) {
                            onToggleSelection()
                        } else {
                            onNavigateToPetDetail(petData.pet.id)
                        }
                    },
                    onLongClick = onLongPress
                )
                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent)
                .padding(4.dp) // extra touch target padding
        ) {
            PetAvatar(species = petData.pet.species)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = petData.pet.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${petData.pet.species} • ${petData.pet.breed}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Expressive Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExpressiveStatCard(
                title = "Weight",
                value = petData.latestWeight?.let { UnitFormatter.formatWeight(it.weight, weightUnit) } ?: "--",
                subtitle = "Latest",
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToPetStatDetail(petData.pet.id, our.bunny.julie.ui.navigation.StatType.Weight) }
            )
            ExpressiveStatCard(
                title = "Water",
                value = if (petData.todayWater > 0f) UnitFormatter.formatWater(petData.todayWater, waterUnit) else "--",
                subtitle = "Today",
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToPetStatDetail(petData.pet.id, our.bunny.julie.ui.navigation.StatType.Water) }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExpressiveStatCard(
                title = "Feeding",
                value = petData.latestFeeding?.food ?: "--",
                subtitle = "Recent",
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToPetStatDetail(petData.pet.id, our.bunny.julie.ui.navigation.StatType.Feeding) }
            )
            ExpressiveStatCard(
                title = "Medications",
                value = petData.activeMedicationsCount.toString(),
                subtitle = "Active",
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToPetStatDetail(petData.pet.id, our.bunny.julie.ui.navigation.StatType.Medication) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveStatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}
