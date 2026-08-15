package our.bunny.julie.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
import our.bunny.julie.util.SearchUtil
import our.bunny.julie.util.WaterUnit
import our.bunny.julie.util.WeightUnit
import kotlinx.coroutines.flow.MutableStateFlow
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
    val hasAnyPets: Boolean = false,
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

    val searchQuery = MutableStateFlow("")
    val selectedSpecies = MutableStateFlow<Set<String>>(emptySet())

    val availableSpecies: StateFlow<Set<String>> = petRepository.getAllPets()
        .map { pets -> pets.map { it.species }.filter { it.isNotBlank() }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val uiState: StateFlow<DashboardUiState> = combine(
        petRepository.getAllPets(),
        searchQuery,
        selectedSpecies
    ) { pets, query, selected ->
        Triple(pets, query, selected)
    }.flatMapLatest { (pets, query, selected) ->
        val hasAnyPets = pets.isNotEmpty()
        if (!hasAnyPets) {
            combine(
                settingsRepository.weightUnitFlow,
                settingsRepository.waterUnitFlow
            ) { weightUnit, waterUnit ->
                DashboardUiState(isLoading = false, hasAnyPets = false, petsData = emptyList(), weightUnit = weightUnit, waterUnit = waterUnit)
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
                if (petDataFlows.isEmpty()) flowOf(emptyList()) else combine(petDataFlows) { it.toList() },
                settingsRepository.weightUnitFlow,
                settingsRepository.waterUnitFlow
            ) { petDataList, weightUnit, waterUnit ->
                val filteredPets = petDataList.filter { data ->
                    val matchesSearch = if (query.isBlank()) true else SearchUtil.fuzzyMatches(query, data.pet.name)
                    val matchesSpecies = if (selected.isEmpty()) true else selected.contains(data.pet.species)
                    matchesSearch && matchesSpecies
                }
                DashboardUiState(
                    isLoading = false,
                    hasAnyPets = true,
                    petsData = filteredPets,
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

    fun deletePet(petId: Long) {
        viewModelScope.launch {
            val pet = petRepository.getPetById(petId)
            if (pet != null) {
                petRepository.deletePet(pet)
            }
        }
    }
}
