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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import our.bunny.julie.data.local.WidgetConfigStore
import our.bunny.julie.domain.model.Pet
import our.bunny.julie.domain.repository.PetRepository
import our.bunny.julie.ui.theme.JulieTheme
import our.bunny.julie.widget.PetStatWidget2x2Provider
import our.bunny.julie.widget.PetStatWidget4x2Provider
import javax.inject.Inject

/**
 * Single configuration Activity for both 2x2 and 4x2 widgets.
 *
 * Design choice: One Activity, one Composable tree. We detect the widget type by inspecting
 * AppWidgetManager.getAppWidgetInfo(appWidgetId).provider.className. If it's the 4x2 provider,
 * we render two StatSlotSelector groups stacked vertically; otherwise just one. This avoids
 * duplicating any UI code — the shared [StatSlotSelector] composable does all the heavy lifting.
 */
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

        // Detect whether this is a 4x2 widget by checking the provider class name
        val is4x2 = AppWidgetManager.getInstance(this)
            .getAppWidgetInfo(appWidgetId)
            ?.provider?.className
            ?.contains("4x2") == true

        setContent {
            JulieTheme {
                val pets by petRepository.getAllPets().collectAsState(initial = emptyList())

                // Slot 1 state
                var slot1Pet  by remember { mutableStateOf<Pet?>(null) }
                var slot1Mode by remember { mutableStateOf("Auto") }
                // Slot 2 state (only used for 4x2)
                var slot2Pet  by remember { mutableStateOf<Pet?>(null) }
                var slot2Mode by remember { mutableStateOf("Water") }

                LaunchedEffect(pets) {
                    if (pets.isNotEmpty()) {
                        if (slot1Pet == null) slot1Pet = pets.first()
                        if (slot2Pet == null) slot2Pet = pets.first()
                    }
                }

                val canSave = slot1Pet != null && (!is4x2 || slot2Pet != null)

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
                        // Slot 1
                        StatSlotSelector(
                            slotLabel = if (is4x2) "Left Stat" else null,
                            pets = pets,
                            selectedPet = slot1Pet,
                            selectedMode = slot1Mode,
                            onPetSelected = { slot1Pet = it },
                            onModeSelected = { slot1Mode = it }
                        )

                        // Slot 2 only for 4x2
                        if (is4x2) {
                            HorizontalDivider()
                            StatSlotSelector(
                                slotLabel = "Right Stat",
                                pets = pets,
                                selectedPet = slot2Pet,
                                selectedMode = slot2Mode,
                                onPetSelected = { slot2Pet = it },
                                onModeSelected = { slot2Mode = it }
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                val p1 = slot1Pet ?: return@Button
                                // Save slot 1
                                widgetConfigStore.saveSlotConfig(appWidgetId, 1, p1.id, slot1Mode)
                                // Save slot 2 only for 4x2
                                if (is4x2) {
                                    val p2 = slot2Pet ?: return@Button
                                    widgetConfigStore.saveSlotConfig(appWidgetId, 2, p2.id, slot2Mode)
                                }
                                // Trigger the right provider to update
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
