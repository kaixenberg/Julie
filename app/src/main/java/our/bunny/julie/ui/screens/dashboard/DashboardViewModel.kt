package our.bunny.julie.ui.screens.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import our.bunny.julie.domain.model.FeedingLog
import our.bunny.julie.domain.model.Pet
import our.bunny.julie.domain.model.WaterLog
import our.bunny.julie.domain.model.WeightEntry
import our.bunny.julie.domain.repository.PetRepository
import our.bunny.julie.domain.repository.TrackerRepository
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val pet: Pet? = null,
    val latestWeight: WeightEntry? = null,
    val latestFeeding: FeedingLog? = null,
    val todayWater: Float = 0f,
    val activeMedicationsCount: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val petRepository: PetRepository,
    private val trackerRepository: TrackerRepository
) : ViewModel() {

    private val petId: Long = savedStateHandle.get<Long>("petId") ?: -1L

    val uiState: StateFlow<DashboardUiState> = combine(
        petRepository.getPetByIdStream(petId),
        trackerRepository.getLatestWeightEntry(petId),
        trackerRepository.getLatestFeedingLog(petId),
        trackerRepository.getTodayWaterTotal(petId),
        trackerRepository.getMedicationsForPet(petId)
    ) { pet, weight, feeding, water, medications ->
        DashboardUiState(
            isLoading = false,
            pet = pet,
            latestWeight = weight,
            latestFeeding = feeding,
            todayWater = water ?: 0f,
            activeMedicationsCount = medications.count { it.isActive }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )
}
