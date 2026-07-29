package our.bunny.julie.ui.screens.water

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import our.bunny.julie.domain.model.WaterLog
import our.bunny.julie.domain.repository.TrackerRepository
import java.time.LocalDateTime
import javax.inject.Inject

data class WaterTrackerUiState(
    val entries: List<WaterLog> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class WaterTrackerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val trackerRepository: TrackerRepository
) : ViewModel() {

    val petId: Long = savedStateHandle.get<Long>("petId") ?: -1L

    val uiState: StateFlow<WaterTrackerUiState> = trackerRepository
        .getWaterLogsForPet(petId)
        .map { WaterTrackerUiState(entries = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WaterTrackerUiState(isLoading = true)
        )

    fun addWaterEntry(amount: Float, unit: String) {
        viewModelScope.launch {
            val entry = WaterLog(
                petId = petId,
                amount = amount,
                unit = unit,
                time = LocalDateTime.now()
            )
            trackerRepository.insertWaterLog(entry)
        }
    }

    fun deleteWaterEntry(entry: WaterLog) {
        viewModelScope.launch {
            trackerRepository.deleteWaterLog(entry)
        }
    }
}
