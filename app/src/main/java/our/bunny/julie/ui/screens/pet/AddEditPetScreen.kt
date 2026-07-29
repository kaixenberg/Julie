package our.bunny.julie.ui.screens.pet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPetScreen(
    onNavigateUp: () -> Unit,
    viewModel: AddEditPetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val isFormValid = uiState.name.isNotBlank() && uiState.species.isNotBlank() && uiState.breed.isNotBlank() && uiState.sex.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pet Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isFormValid) {
                        viewModel.onEvent(AddEditPetEvent.SavePet)
                        onNavigateUp()
                    }
                },
                containerColor = if (isFormValid) FloatingActionButtonDefaults.containerColor else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isFormValid) contentColorFor(FloatingActionButtonDefaults.containerColor) else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save Pet")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onEvent(AddEditPetEvent.EnteredName(it)) },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            DropdownField(
                value = uiState.species,
                onValueChange = { 
                    viewModel.onEvent(AddEditPetEvent.EnteredSpecies(it)) 
                    // Reset breed if species changes to avoid invalid combinations
                    viewModel.onEvent(AddEditPetEvent.EnteredBreed(""))
                },
                label = "Species (e.g., Dog, Cat)",
                options = PetData.species,
                modifier = Modifier.fillMaxWidth()
            )
            DropdownField(
                value = uiState.breed,
                onValueChange = { viewModel.onEvent(AddEditPetEvent.EnteredBreed(it)) },
                label = "Breed",
                options = PetData.getBreedsForSpecies(uiState.species),
                modifier = Modifier.fillMaxWidth()
            )
            DropdownField(
                value = uiState.sex,
                onValueChange = { viewModel.onEvent(AddEditPetEvent.EnteredSex(it)) },
                label = "Sex",
                options = PetData.sexes,
                modifier = Modifier.fillMaxWidth()
            )
            // Add other fields (birthday, adoption date, weight unit, color, etc.) in later milestones
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onValueChange(selectionOption)
                        expanded = false
                    }
                )
            }
        }
    }
}
