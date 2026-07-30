package our.bunny.julie.ui.screens.feeding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import our.bunny.julie.domain.model.FeedingLog
import our.bunny.julie.domain.repository.TrackerRepository
import java.time.LocalDateTime
import javax.inject.Inject

enum class FeedingLogSort { DATE_NEWEST, DATE_OLDEST, CALORIES_HIGH, CALORIES_LOW }
enum class FeedingLogFilter { ALL, BREAKFAST, LUNCH, DINNER, SNACK, CUSTOM }

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

    val searchQuery = MutableStateFlow("")
    val currentSort = MutableStateFlow(FeedingLogSort.DATE_NEWEST)
    val currentFilter = MutableStateFlow(FeedingLogFilter.ALL)
    val selectedIds = MutableStateFlow<Set<Long>>(emptySet())

    val uiState: StateFlow<FeedingLogUiState> = combine(
        trackerRepository.getFeedingLogsForPet(petId),
        searchQuery,
        currentSort,
        currentFilter
    ) { logs, query, sort, filter ->
        var filtered = logs

        // Filter
        filtered = when (filter) {
            FeedingLogFilter.ALL -> filtered
            FeedingLogFilter.BREAKFAST -> filtered.filter { it.type.equals("Breakfast", ignoreCase = true) }
            FeedingLogFilter.LUNCH -> filtered.filter { it.type.equals("Lunch", ignoreCase = true) }
            FeedingLogFilter.DINNER -> filtered.filter { it.type.equals("Dinner", ignoreCase = true) }
            FeedingLogFilter.SNACK -> filtered.filter { it.type.equals("Snack", ignoreCase = true) }
            FeedingLogFilter.CUSTOM -> filtered.filter { it.type.equals("Custom", ignoreCase = true) }
        }

        // Search
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.food.contains(query, ignoreCase = true) || it.notes.contains(query, ignoreCase = true)
            }
        }

        // Sort
        filtered = when (sort) {
            FeedingLogSort.DATE_NEWEST -> filtered.sortedByDescending { it.time }
            FeedingLogSort.DATE_OLDEST -> filtered.sortedBy { it.time }
            FeedingLogSort.CALORIES_HIGH -> filtered.sortedByDescending { it.calories ?: 0 }
            FeedingLogSort.CALORIES_LOW -> filtered.sortedBy { it.calories ?: 0 }
        }

        FeedingLogUiState(entries = filtered, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FeedingLogUiState(isLoading = true)
    )

    fun toggleSelection(id: Long) {
        selectedIds.update { if (it.contains(id)) it - id else it + id }
    }

    fun selectAll(ids: List<Long>) {
        selectedIds.update { current -> if (current.size == ids.size) emptySet() else ids.toSet() }
    }

    fun clearSelection() {
        selectedIds.value = emptySet()
    }

    fun deleteSelected(entries: List<FeedingLog>) {
        viewModelScope.launch {
            val toDelete = entries.filter { selectedIds.value.contains(it.id) }
            toDelete.forEach { entry ->
                trackerRepository.deleteFeedingLog(entry)
            }
            clearSelection()
        }
    }

    fun addOrUpdateFeedingEntry(id: Long = 0, food: String, quantity: String, unit: String, type: String, calories: Int?, notes: String, time: LocalDateTime = LocalDateTime.now()) {
        viewModelScope.launch {
            val entry = FeedingLog(
                id = id,
                petId = petId,
                food = food,
                quantity = quantity,
                unit = unit,
                time = time,
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
