package our.bunny.julie.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import our.bunny.julie.domain.model.Pet
import our.bunny.julie.domain.model.WeightEntry
import our.bunny.julie.domain.model.FeedingLog
import our.bunny.julie.domain.repository.PetRepository
import our.bunny.julie.domain.repository.SettingsRepository
import our.bunny.julie.domain.repository.TrackerRepository
import our.bunny.julie.util.WaterUnit
import our.bunny.julie.util.WeightUnit
import javax.inject.Inject

data class PetDashboardData(
    val pet: Pet,
    val latestWeight: WeightEntry?,
    val latestFeeding: FeedingLog?,
    val todayWater: Float,
    val activeMedicationsCount: Int
)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val petsData: List<PetDashboardData> = emptyList(),
    val weightUnit: WeightUnit = WeightUnit.KG,
    val waterUnit: WaterUnit = WaterUnit.ML
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val trackerRepository: TrackerRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = petRepository.getAllPets()
        .flatMapLatest { pets ->
            if (pets.isEmpty()) {
                combine(
                    settingsRepository.weightUnitFlow,
                    settingsRepository.waterUnitFlow
                ) { weightUnit, waterUnit ->
                    DashboardUiState(isLoading = false, petsData = emptyList(), weightUnit = weightUnit, waterUnit = waterUnit)
                }
            } else {
                val petDataFlows = pets.map { pet ->
                    combine(
                        trackerRepository.getLatestWeightEntry(pet.id),
                        trackerRepository.getLatestFeedingLog(pet.id),
                        trackerRepository.getTodayWaterTotal(pet.id),
                        trackerRepository.getMedicationsForPet(pet.id)
                    ) { weight, feeding, water, medications ->
                        PetDashboardData(
                            pet = pet,
                            latestWeight = weight,
                            latestFeeding = feeding,
                            todayWater = water ?: 0f,
                            activeMedicationsCount = medications.count { it.isActive }
                        )
                    }
                }
                combine(
                    combine(petDataFlows) { it.toList() },
                    settingsRepository.weightUnitFlow,
                    settingsRepository.waterUnitFlow
                ) { petDataList, weightUnit, waterUnit ->
                    DashboardUiState(
                        isLoading = false,
                        petsData = petDataList,
                        weightUnit = weightUnit,
                        waterUnit = waterUnit
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState(isLoading = true)
        )
}
