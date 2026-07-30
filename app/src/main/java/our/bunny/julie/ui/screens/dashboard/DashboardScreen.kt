package our.bunny.julie.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import our.bunny.julie.domain.model.Pet
import our.bunny.julie.ui.screens.home.PetAvatar
import our.bunny.julie.util.UnitFormatter
import our.bunny.julie.util.WaterUnit
import our.bunny.julie.util.WeightUnit

@Composable
fun DashboardScreen(
    paddingValues: PaddingValues,
    onNavigateToAddPet: () -> Unit,
    onNavigateToPetDetail: (Long) -> Unit,
    onNavigateToPetStatDetail: (Long, our.bunny.julie.ui.navigation.StatType) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (uiState.petsData.isEmpty()) {
        // Zero pets empty state
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
    } else {
        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
        // List of all pets
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(uiState.petsData, key = { it.pet.id }) { petData ->
                PetSummarySection(
                    petData = petData,
                    weightUnit = uiState.weightUnit,
                    waterUnit = uiState.waterUnit,
                    onNavigateToPetDetail = onNavigateToPetDetail,
                    onNavigateToPetStatDetail = onNavigateToPetStatDetail
                )
                
                if (uiState.petsData.size >= 3) {
                    HorizontalDivider(modifier = Modifier.padding(top = 24.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun PetSummarySection(
    petData: PetDashboardData,
    weightUnit: WeightUnit,
    waterUnit: WaterUnit,
    onNavigateToPetDetail: (Long) -> Unit,
    onNavigateToPetStatDetail: (Long, our.bunny.julie.ui.navigation.StatType) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .clickable { onNavigateToPetDetail(petData.pet.id) }
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
