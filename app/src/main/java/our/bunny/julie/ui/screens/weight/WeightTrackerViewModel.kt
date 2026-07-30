package our.bunny.julie.ui.screens.weight

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import our.bunny.julie.domain.model.WeightEntry
import our.bunny.julie.domain.repository.SettingsRepository
import our.bunny.julie.domain.repository.TrackerRepository
import our.bunny.julie.util.UnitFormatter
import our.bunny.julie.util.WeightUnit
import java.time.LocalDateTime
import javax.inject.Inject

data class WeightTrackerUiState(
    val entries: List<WeightEntry> = emptyList(),
    val isLoading: Boolean = false,
    val weightUnit: WeightUnit = WeightUnit.KG
)

@HiltViewModel
class WeightTrackerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val trackerRepository: TrackerRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val petId: Long = savedStateHandle.get<Long>("petId") ?: -1L

    val uiState: StateFlow<WeightTrackerUiState> = combine(
        trackerRepository.getWeightEntriesForPet(petId),
        settingsRepository.weightUnitFlow
    ) { entries, unit ->
        WeightTrackerUiState(
            entries = entries,
            isLoading = false,
            weightUnit = unit
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WeightTrackerUiState(isLoading = true)
    )

    fun addWeightEntry(weight: Float, unit: WeightUnit, notes: String) {
        viewModelScope.launch {
            val canonicalWeight = UnitFormatter.parseWeightToCanonical(weight, unit)
            val entry = WeightEntry(
                petId = petId,
                date = LocalDateTime.now(),
                weight = canonicalWeight,
                notes = notes
            )
            trackerRepository.insertWeightEntry(entry)
        }
    }

    fun deleteWeightEntry(entry: WeightEntry) {
        viewModelScope.launch {
            trackerRepository.deleteWeightEntry(entry)
        }
    }
}
