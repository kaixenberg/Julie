package our.bunny.julie.ui.screens.weight

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import our.bunny.julie.domain.model.WeightEntry
import our.bunny.julie.domain.repository.TrackerRepository
import java.time.LocalDateTime
import javax.inject.Inject

data class WeightTrackerUiState(
    val entries: List<WeightEntry> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class WeightTrackerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val trackerRepository: TrackerRepository
) : ViewModel() {

    val petId: Long = savedStateHandle.get<Long>("petId") ?: -1L

    val uiState: StateFlow<WeightTrackerUiState> = trackerRepository
        .getWeightEntriesForPet(petId)
        .map { WeightTrackerUiState(entries = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeightTrackerUiState(isLoading = true)
        )

    fun addWeightEntry(weight: Float, unit: String, notes: String) {
        viewModelScope.launch {
            val entry = WeightEntry(
                petId = petId,
                date = LocalDateTime.now(),
                weight = weight,
                unit = unit,
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
