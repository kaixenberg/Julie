package our.bunny.julie.ui.screens.pet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import our.bunny.julie.domain.model.FeedingLog
import our.bunny.julie.domain.model.Pet
import our.bunny.julie.domain.model.WeightEntry
import our.bunny.julie.domain.repository.PetRepository
import our.bunny.julie.domain.repository.SettingsRepository
import our.bunny.julie.domain.repository.TrackerRepository
import our.bunny.julie.util.WaterUnit
import our.bunny.julie.util.WeightUnit
import javax.inject.Inject

data class PetDetailUiState(
    val isLoading: Boolean = true,
    val pet: Pet? = null,
    val latestWeight: WeightEntry? = null,
    val latestFeeding: FeedingLog? = null,
    val todayWater: Float = 0f,
    val activeMedicationsCount: Int = 0,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val waterUnit: WaterUnit = WaterUnit.ML
)

@HiltViewModel
class PetDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val petRepository: PetRepository,
    private val trackerRepository: TrackerRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val petId: Long = savedStateHandle.get<Long>("petId") ?: -1L

    val uiState: StateFlow<PetDetailUiState> = combine(
        petRepository.getPetByIdStream(petId),
        trackerRepository.getLatestWeightEntry(petId),
        trackerRepository.getLatestFeedingLog(petId),
        trackerRepository.getTodayWaterTotal(petId),
        trackerRepository.getMedicationsForPet(petId),
        settingsRepository.weightUnitFlow,
        settingsRepository.waterUnitFlow
    ) { args ->
        val pet = args[0] as Pet?
        val weight = args[1] as WeightEntry?
        val feeding = args[2] as FeedingLog?
        val water = args[3] as Float?
        val medications = args[4] as List<our.bunny.julie.domain.model.Medication>
        val weightUnit = args[5] as WeightUnit
        val waterUnit = args[6] as WaterUnit

        PetDetailUiState(
            isLoading = false,
            pet = pet,
            latestWeight = weight,
            latestFeeding = feeding,
            todayWater = water ?: 0f,
            activeMedicationsCount = medications.count { it.isActive },
            weightUnit = weightUnit,
            waterUnit = waterUnit
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PetDetailUiState(isLoading = true)
    )

    fun deletePet(petId: Long) {
        viewModelScope.launch {
            val pet = petRepository.getPetById(petId)
            if (pet != null) {
                petRepository.deletePet(pet)
            }
        }
    }
}
