package our.bunny.julie.ui.screens.weight

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
import our.bunny.julie.domain.model.WeightEntry
import our.bunny.julie.domain.repository.SettingsRepository
import our.bunny.julie.domain.repository.TrackerRepository
import our.bunny.julie.util.SearchUtil
import our.bunny.julie.util.UnitFormatter
import our.bunny.julie.util.WeightUnit
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

enum class WeightSort { DATE_NEWEST, DATE_OLDEST, WEIGHT_HIGH, WEIGHT_LOW }
enum class WeightFilter { ALL_TIME, LAST_7_DAYS, LAST_30_DAYS, LAST_6_MONTHS, LAST_YEAR }

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

    val searchQuery = MutableStateFlow("")
    val currentSort = MutableStateFlow(WeightSort.DATE_NEWEST)
    val currentFilter = MutableStateFlow(WeightFilter.ALL_TIME)
    val selectedIds = MutableStateFlow<Set<Long>>(emptySet())

    val uiState: StateFlow<WeightTrackerUiState> = combine(
        trackerRepository.getWeightEntriesForPet(petId),
        settingsRepository.weightUnitFlow,
        searchQuery,
        currentSort,
        currentFilter
    ) { entries, unit, query, sort, filter ->
        var filtered = entries

        // Filter
        val now = LocalDateTime.now()
        filtered = when (filter) {
            WeightFilter.ALL_TIME -> filtered
            WeightFilter.LAST_7_DAYS -> filtered.filter { ChronoUnit.DAYS.between(it.date, now) <= 7 }
            WeightFilter.LAST_30_DAYS -> filtered.filter { ChronoUnit.DAYS.between(it.date, now) <= 30 }
            WeightFilter.LAST_6_MONTHS -> filtered.filter { ChronoUnit.MONTHS.between(it.date, now) <= 6 }
            WeightFilter.LAST_YEAR -> filtered.filter { ChronoUnit.YEARS.between(it.date, now) <= 1 }
        }

        // Search
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                SearchUtil.fuzzyMatches(query, it.notes)
            }
        }

        // Sort
        filtered = when (sort) {
            WeightSort.DATE_NEWEST -> filtered.sortedByDescending { it.date }
            WeightSort.DATE_OLDEST -> filtered.sortedBy { it.date }
            WeightSort.WEIGHT_HIGH -> filtered.sortedByDescending { it.weight }
            WeightSort.WEIGHT_LOW -> filtered.sortedBy { it.weight }
        }

        WeightTrackerUiState(
            entries = filtered,
            isLoading = false,
            weightUnit = unit
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WeightTrackerUiState(isLoading = true)
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

    fun deleteSelected(entries: List<WeightEntry>) {
        viewModelScope.launch {
            val toDelete = entries.filter { selectedIds.value.contains(it.id) }
            toDelete.forEach { entry ->
                trackerRepository.deleteWeightEntry(entry)
            }
            clearSelection()
        }
    }

    fun addOrUpdateWeightEntry(id: Long = 0, weight: Float, unit: WeightUnit, notes: String, time: LocalDateTime = LocalDateTime.now()) {
        viewModelScope.launch {
            val canonicalWeight = UnitFormatter.parseWeightToCanonical(weight, unit)
            val entry = WeightEntry(
                id = id,
                petId = petId,
                date = time,
                weight = canonicalWeight,
                notes = notes
            )
            trackerRepository.insertWeightEntry(entry)
        }
    }
}
