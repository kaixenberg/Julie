package our.bunny.julie.ui.screens.medication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import our.bunny.julie.domain.model.Medication
import our.bunny.julie.domain.repository.TrackerRepository
import our.bunny.julie.util.ReminderManager
import javax.inject.Inject

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

    val uiState: StateFlow<MedicationListUiState> = trackerRepository
        .getMedicationsForPet(petId)
        .map { MedicationListUiState(medications = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MedicationListUiState(isLoading = true)
        )

    fun addMedication(name: String, dosage: String, schedules: List<our.bunny.julie.domain.model.MedicationSchedule>, notes: String) {
        viewModelScope.launch {
            val med = Medication(
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
