package our.bunny.julie.ui.screens.water

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
import our.bunny.julie.domain.model.WaterLog
import our.bunny.julie.domain.repository.SettingsRepository
import our.bunny.julie.domain.repository.TrackerRepository
import our.bunny.julie.util.UnitFormatter
import our.bunny.julie.util.WaterUnit
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import our.bunny.julie.manager.StatReminderManager

enum class WaterSort { DATE_NEWEST, DATE_OLDEST, AMOUNT_HIGH, AMOUNT_LOW }
enum class WaterFilter { ALL_TIME, LAST_7_DAYS, LAST_30_DAYS, LAST_6_MONTHS, LAST_YEAR }

data class WaterTrackerUiState(
    val entries: List<WaterLog> = emptyList(),
    val isLoading: Boolean = false,
    val waterUnit: WaterUnit = WaterUnit.ML
)

@HiltViewModel
class WaterTrackerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val trackerRepository: TrackerRepository,
    private val settingsRepository: SettingsRepository,
    private val statReminderManager: StatReminderManager
) : ViewModel() {

    val petId: Long = savedStateHandle.get<Long>("petId") ?: -1L

    val currentSort = MutableStateFlow(WaterSort.DATE_NEWEST)
    val currentFilter = MutableStateFlow(WaterFilter.ALL_TIME)
    val selectedIds = MutableStateFlow<Set<Long>>(emptySet())

    val uiState: StateFlow<WaterTrackerUiState> = combine(
        trackerRepository.getWaterLogsForPet(petId),
        settingsRepository.waterUnitFlow,
        currentSort,
        currentFilter
    ) { entries, unit, sort, filter ->
        var filtered = entries

        // Filter
        val now = LocalDateTime.now()
        filtered = when (filter) {
            WaterFilter.ALL_TIME -> filtered
            WaterFilter.LAST_7_DAYS -> {
                val cutoff = LocalDateTime.now().minusDays(7)
                filtered.filter { it.time.isAfter(cutoff) }
            }
            WaterFilter.LAST_30_DAYS -> {
                val cutoff = LocalDateTime.now().minusDays(30)
                filtered.filter { it.time.isAfter(cutoff) }
            }
            WaterFilter.LAST_6_MONTHS -> {
                val cutoff = LocalDateTime.now().minusMonths(6)
                filtered.filter { it.time.isAfter(cutoff) }
            }
            WaterFilter.LAST_YEAR -> {
                val cutoff = LocalDateTime.now().minusYears(1)
                filtered.filter { it.time.isAfter(cutoff) }
            }
        }

        // Sort
        filtered = when (sort) {
            WaterSort.DATE_NEWEST -> filtered.sortedByDescending { it.time }
            WaterSort.DATE_OLDEST -> filtered.sortedBy { it.time }
            WaterSort.AMOUNT_HIGH -> filtered.sortedByDescending { it.amount }
            WaterSort.AMOUNT_LOW -> filtered.sortedBy { it.amount }
        }

        WaterTrackerUiState(
            entries = filtered,
            isLoading = false,
            waterUnit = unit
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WaterTrackerUiState(isLoading = true)
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

    fun deleteSelected(entries: List<WaterLog>) {
        viewModelScope.launch {
            val toDelete = entries.filter { selectedIds.value.contains(it.id) }
            toDelete.forEach { entry ->
                trackerRepository.deleteWaterLog(entry)
            }
            clearSelection()
            statReminderManager.rescheduleWater(petId)
        }
    }

    fun addOrUpdateWaterEntry(id: Long = 0, amount: Float, unit: WaterUnit, time: LocalDateTime = LocalDateTime.now()) {
        viewModelScope.launch {
            val canonicalAmount = UnitFormatter.parseWaterToCanonical(amount, unit)
            val entry = WaterLog(
                id = id,
                petId = petId,
                amount = canonicalAmount,
                time = time
            )
            trackerRepository.insertWaterLog(entry)
            statReminderManager.rescheduleWater(petId)
        }
    }
}
