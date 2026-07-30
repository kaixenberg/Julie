package our.bunny.julie.ui.screens.medication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import our.bunny.julie.domain.model.Medication
import our.bunny.julie.data.local.entity.MedicationWithSchedules
import our.bunny.julie.domain.repository.TrackerRepository
import our.bunny.julie.util.ReminderManager
import our.bunny.julie.util.SearchUtil
import javax.inject.Inject

enum class MedicationSort { NAME, STATUS }
enum class MedicationFilter { ALL, ACTIVE, PAUSED }

data class MedicationListUiState(
    val medications: List<Medication> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class MedicationListViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val trackerRepository: TrackerRepository,
    application: Application
) : AndroidViewModel(application) {

    val petId: Long = savedStateHandle.get<Long>("petId") ?: -1L

    val searchQuery = MutableStateFlow("")
    val currentSort = MutableStateFlow(MedicationSort.NAME)
    val currentFilter = MutableStateFlow(MedicationFilter.ALL)
    val selectedIds = MutableStateFlow<Set<Long>>(emptySet())

    val uiState: StateFlow<MedicationListUiState> = combine(
        trackerRepository.getMedicationsForPet(petId),
        searchQuery,
        currentSort,
        currentFilter
    ) { medications, query, sort, filter ->
        var filtered = medications

        // Filter
        filtered = when (filter) {
            MedicationFilter.ALL -> filtered
            MedicationFilter.ACTIVE -> filtered.filter { it.isActive }
            MedicationFilter.PAUSED -> filtered.filter { !it.isActive }
        }

        // Search
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                SearchUtil.fuzzyMatches(query, it.name) || SearchUtil.fuzzyMatches(query, it.notes)
            }
        }

        // Sort
        filtered = when (sort) {
            MedicationSort.NAME -> filtered.sortedBy { it.name.lowercase() }
            MedicationSort.STATUS -> filtered.sortedByDescending { it.isActive } // Active first
        }

        MedicationListUiState(medications = filtered, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MedicationListUiState(isLoading = true)
    )

    fun toggleSelection(medicationId: Long) {
        selectedIds.update {
            if (it.contains(medicationId)) it - medicationId else it + medicationId
        }
    }

    fun selectAll(medicationIds: List<Long>) {
        selectedIds.update { current ->
            if (current.size == medicationIds.size) emptySet() else medicationIds.toSet()
        }
    }

    fun clearSelection() {
        selectedIds.value = emptySet()
    }

    fun deleteSelected(medications: List<Medication>) {
        viewModelScope.launch {
            val toDelete = medications.filter { selectedIds.value.contains(it.id) }
            toDelete.forEach { med ->
                trackerRepository.deleteMedication(med)
                ReminderManager.cancelMedicationReminder(getApplication(), med.id)
            }
            clearSelection()
        }
    }

    fun addOrUpdateMedication(id: Long, name: String, dosage: String, schedules: List<our.bunny.julie.domain.model.MedicationSchedule>, notes: String) {
        viewModelScope.launch {
            val med = Medication(
                id = id,
                petId = petId,
                name = name,
                dosage = dosage,
                isActive = true,
                notes = notes,
                schedules = schedules
            )
            val medId = trackerRepository.insertMedication(med)
            ReminderManager.scheduleMedicationReminder(getApplication(), med.copy(id = medId))
        }
    }

    fun toggleMedicationStatus(medication: Medication) {
        viewModelScope.launch {
            val updatedMed = medication.copy(isActive = !medication.isActive)
            trackerRepository.insertMedication(updatedMed)
            ReminderManager.scheduleMedicationReminder(getApplication(), updatedMed)
        }
    }

    fun deleteMedication(medication: Medication) {
        viewModelScope.launch {
            trackerRepository.deleteMedication(medication)
            ReminderManager.cancelMedicationReminder(getApplication(), medication.id)
        }
    }
}
