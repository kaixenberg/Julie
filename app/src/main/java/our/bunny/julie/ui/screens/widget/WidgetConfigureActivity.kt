package our.bunny.julie.ui.screens.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import our.bunny.julie.data.local.WidgetConfigStore
import our.bunny.julie.data.local.WidgetSlotConfig
import our.bunny.julie.domain.model.Pet
import our.bunny.julie.domain.repository.PetRepository
import our.bunny.julie.ui.theme.JulieTheme
import our.bunny.julie.widget.PetStatWidget2x2Provider
import our.bunny.julie.widget.PetStatWidget4x2Provider
import javax.inject.Inject

@AndroidEntryPoint
class WidgetConfigureActivity : ComponentActivity() {

    @Inject
    lateinit var widgetConfigStore: WidgetConfigStore

    @Inject
    lateinit var petRepository: PetRepository

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }

        val is4x2 = AppWidgetManager.getInstance(this)
            .getAppWidgetInfo(appWidgetId)
            ?.provider?.className
            ?.contains("4x2") == true
            
        val maxSlots = if (is4x2) 4 else 2

        setContent {
            JulieTheme {
                val pets by petRepository.getAllPets().collectAsState(initial = emptyList())

                // Dynamic list of slots
                var slots by remember { mutableStateOf(listOf(WidgetSlotConfig(-1L, "Auto"))) }

                LaunchedEffect(pets) {
                    if (pets.isNotEmpty() && slots.first().petId == -1L) {
                        slots = listOf(WidgetSlotConfig(pets.first().id, "Auto"))
                    }
                }

                val canSave = slots.all { it.petId != -1L }

                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text(if (is4x2) "Configure 4×2 Widget" else "Configure Widget") })
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        slots.forEachIndexed { index, slot ->
                            val selectedPet = pets.find { it.id == slot.petId }
                            
                            Row(verticalAlignment = Alignment.Top) {
                                Box(modifier = Modifier.weight(1f)) {
                                    StatSlotSelector(
                                        slotLabel = "Stat ${index + 1}",
                                        pets = pets,
                                        selectedPet = selectedPet,
                                        selectedMode = slot.statMode,
                                        onPetSelected = { pet ->
                                            val newSlots = slots.toMutableList()
                                            newSlots[index] = slot.copy(petId = pet.id)
                                            slots = newSlots
                                        },
                                        onModeSelected = { mode ->
                                            val newSlots = slots.toMutableList()
                                            newSlots[index] = slot.copy(statMode = mode)
                                            slots = newSlots
                                        }
                                    )
                                }
                                
                                if (slots.size > 1) {
                                    IconButton(
                                        onClick = {
                                            val newSlots = slots.toMutableList()
                                            newSlots.removeAt(index)
                                            slots = newSlots
                                        },
                                        modifier = Modifier.padding(top = 32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove Stat", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                            
                            if (index < slots.size - 1) {
                                HorizontalDivider()
                            }
                        }

                        if (slots.size < maxSlots) {
                            OutlinedButton(
                                onClick = {
                                    val newPetId = pets.firstOrNull()?.id ?: -1L
                                    slots = slots + WidgetSlotConfig(newPetId, "Auto")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Stat")
                                Spacer(Modifier.width(8.dp))
                                Text("Add Stat (${slots.size}/$maxSlots)")
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                widgetConfigStore.saveWidgetConfig(appWidgetId, slots)
                                
                                val providerClass = if (is4x2) PetStatWidget4x2Provider::class.java else PetStatWidget2x2Provider::class.java
                                val updateIntent = Intent(this@WidgetConfigureActivity, providerClass).apply {
                                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                                }
                                sendBroadcast(updateIntent)

                                setResult(Activity.RESULT_OK, Intent().apply {
                                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                })
                                finish()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = canSave
                        ) {
                            Text("Save Configuration")
                        }
                    }
                }
            }
        }
    }
}

// ─── Shared composable: one pet + stat mode selector group ───────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatSlotSelector(
    slotLabel: String?,
    pets: List<Pet>,
    selectedPet: Pet?,
    selectedMode: String,
    onPetSelected: (Pet) -> Unit,
    onModeSelected: (String) -> Unit
) {
    val modes = listOf("Auto", "Weight", "Water", "Feeding", "Medication")
    var petExpanded  by remember { mutableStateOf(false) }
    var modeExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (slotLabel != null) {
            Text(slotLabel, style = MaterialTheme.typography.titleMedium)
        }

        Text("Pet", style = MaterialTheme.typography.labelLarge)
        ExposedDropdownMenuBox(
            expanded = petExpanded,
            onExpandedChange = { petExpanded = !petExpanded }
        ) {
            OutlinedTextField(
                value = selectedPet?.let { "${getSpeciesEmoji(it.species)}  ${it.name}" } ?: "No pets available",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = petExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = petExpanded, onDismissRequest = { petExpanded = false }) {
                pets.forEach { pet ->
                    DropdownMenuItem(
                        text = { Text("${getSpeciesEmoji(pet.species)}  ${pet.name}") },
                        onClick = { onPetSelected(pet); petExpanded = false }
                    )
                }
            }
        }

        Text("Stat", style = MaterialTheme.typography.labelLarge)
        ExposedDropdownMenuBox(
            expanded = modeExpanded,
            onExpandedChange = { modeExpanded = !modeExpanded }
        ) {
            OutlinedTextField(
                value = selectedMode,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = modeExpanded, onDismissRequest = { modeExpanded = false }) {
                modes.forEach { mode ->
                    DropdownMenuItem(
                        text = { Text(mode) },
                        onClick = { onModeSelected(mode); modeExpanded = false }
                    )
                }
            }
        }
    }
}

private fun getSpeciesEmoji(species: String) = when (species.lowercase()) {
    "rabbit" -> "🐰"; "dog" -> "🐶"; "cat" -> "🐱"; "bird" -> "🐦"
    "guinea pig", "hamster" -> "🐹"; "mouse" -> "🐭"; "rat" -> "🐀"
    "reptile" -> "🦎"; "fish" -> "🐟"
    else -> "🐾"
}
