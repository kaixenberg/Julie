package our.bunny.julie.ui.screens.timeline

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import our.bunny.julie.domain.model.TimelineEvent
import our.bunny.julie.domain.repository.TrackerRepository
import javax.inject.Inject

data class TimelineUiState(
    val events: List<TimelineEvent> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val trackerRepository: TrackerRepository
) : ViewModel() {

    val petId: Long = savedStateHandle.get<Long>("petId") ?: -1L

    val uiState: StateFlow<TimelineUiState> = trackerRepository
        .getTimelineEventsForPet(petId)
        .map { TimelineUiState(events = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TimelineUiState(isLoading = true)
        )
}
