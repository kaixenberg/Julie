package our.bunny.julie.ui.screens.water

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import our.bunny.julie.domain.model.WaterLog
import our.bunny.julie.domain.repository.SettingsRepository
import our.bunny.julie.domain.repository.TrackerRepository
import our.bunny.julie.util.UnitFormatter
import our.bunny.julie.util.WaterUnit
import java.time.LocalDateTime
import javax.inject.Inject

data class WaterTrackerUiState(
    val entries: List<WaterLog> = emptyList(),
    val isLoading: Boolean = false,
    val waterUnit: WaterUnit = WaterUnit.ML
)

@HiltViewModel
class WaterTrackerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val trackerRepository: TrackerRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val petId: Long = savedStateHandle.get<Long>("petId") ?: -1L

    val uiState: StateFlow<WaterTrackerUiState> = combine(
        trackerRepository.getWaterLogsForPet(petId),
        settingsRepository.waterUnitFlow
    ) { entries, unit ->
        WaterTrackerUiState(
            entries = entries,
            isLoading = false,
            waterUnit = unit
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WaterTrackerUiState(isLoading = true)
    )

    fun addWaterEntry(amount: Float, unit: WaterUnit) {
        viewModelScope.launch {
            val canonicalAmount = UnitFormatter.parseWaterToCanonical(amount, unit)
            val entry = WaterLog(
                petId = petId,
                amount = canonicalAmount,
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
