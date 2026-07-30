package our.bunny.julie.ui.screens.timeline

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import our.bunny.julie.domain.model.TimelineEvent
import our.bunny.julie.domain.repository.SettingsRepository
import our.bunny.julie.domain.repository.TrackerRepository
import our.bunny.julie.util.WaterUnit
import our.bunny.julie.util.WeightUnit
import javax.inject.Inject

data class TimelineUiState(
    val events: List<TimelineEvent> = emptyList(),
    val isLoading: Boolean = false,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val waterUnit: WaterUnit = WaterUnit.ML
)

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val trackerRepository: TrackerRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val petId: Long = savedStateHandle.get<Long>("petId") ?: -1L

    val uiState: StateFlow<TimelineUiState> = combine(
        trackerRepository.getTimelineEventsForPet(petId),
        settingsRepository.weightUnitFlow,
        settingsRepository.waterUnitFlow
    ) { events, weightUnit, waterUnit ->
        TimelineUiState(
            events = events,
            isLoading = false,
            weightUnit = weightUnit,
            waterUnit = waterUnit
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TimelineUiState(isLoading = true)
    )
}
