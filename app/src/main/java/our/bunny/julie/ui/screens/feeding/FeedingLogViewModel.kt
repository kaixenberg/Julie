package our.bunny.julie.ui.screens.feeding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import our.bunny.julie.domain.model.FeedingLog
import our.bunny.julie.domain.repository.TrackerRepository
import java.time.LocalDateTime
import javax.inject.Inject

data class FeedingLogUiState(
    val entries: List<FeedingLog> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class FeedingLogViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val trackerRepository: TrackerRepository
) : ViewModel() {

    val petId: Long = savedStateHandle.get<Long>("petId") ?: -1L

    val uiState: StateFlow<FeedingLogUiState> = trackerRepository
        .getFeedingLogsForPet(petId)
        .map { FeedingLogUiState(entries = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FeedingLogUiState(isLoading = true)
        )

    fun addFeedingEntry(food: String, quantity: String, unit: String, type: String, calories: Int?, notes: String) {
        viewModelScope.launch {
            val entry = FeedingLog(
                petId = petId,
                food = food,
                quantity = quantity,
                unit = unit,
                time = LocalDateTime.now(),
                calories = calories,
                notes = notes,
                type = type
            )
            trackerRepository.insertFeedingLog(entry)
        }
    }

    fun deleteFeedingEntry(entry: FeedingLog) {
        viewModelScope.launch {
            trackerRepository.deleteFeedingLog(entry)
        }
    }
}
