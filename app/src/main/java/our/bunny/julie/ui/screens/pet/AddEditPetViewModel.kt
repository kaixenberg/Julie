package our.bunny.julie.ui.screens.pet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import our.bunny.julie.domain.model.Pet
import our.bunny.julie.domain.repository.PetRepository
import javax.inject.Inject

@HiltViewModel
class AddEditPetViewModel @Inject constructor(
    private val petRepository: PetRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val petId: Long = savedStateHandle.get<Long>("petId") ?: -1L

    private val _uiState = MutableStateFlow(AddEditPetUiState())
    val uiState: StateFlow<AddEditPetUiState> = _uiState.asStateFlow()

    init {
        if (petId != -1L) {
            viewModelScope.launch {
                petRepository.getPetById(petId)?.let { pet ->
                    _uiState.update {
                        it.copy(
                            name = pet.name,
                            species = pet.species,
                            breed = pet.breed,
                            sex = pet.sex,
                            weightUnit = pet.weightUnit,
                            color = pet.color,
                            notes = pet.notes,
                            microchipId = pet.microchipId,
                            favoriteVet = pet.favoriteVet,
                            favoriteFood = pet.favoriteFood
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: AddEditPetEvent) {
        when (event) {
            is AddEditPetEvent.EnteredName -> _uiState.update { it.copy(name = event.value) }
            is AddEditPetEvent.EnteredSpecies -> _uiState.update { it.copy(species = event.value) }
            is AddEditPetEvent.EnteredBreed -> _uiState.update { it.copy(breed = event.value) }
            is AddEditPetEvent.EnteredSex -> _uiState.update { it.copy(sex = event.value) }
            is AddEditPetEvent.SavePet -> {
                viewModelScope.launch {
                    val pet = Pet(
                        id = if (petId != -1L) petId else 0,
                        name = uiState.value.name,
                        species = uiState.value.species,
                        breed = uiState.value.breed,
                        sex = uiState.value.sex,
                        birthday = null, // TODO: date picker
                        adoptionDate = null,
                        weightUnit = uiState.value.weightUnit,
                        color = uiState.value.color,
                        notes = uiState.value.notes,
                        microchipId = uiState.value.microchipId,
                        photoUri = null,
                        favoriteVet = uiState.value.favoriteVet,
                        favoriteFood = uiState.value.favoriteFood
                    )
                    if (petId == -1L) {
                        petRepository.insertPet(pet)
                    } else {
                        petRepository.updatePet(pet)
                    }
                }
            }
        }
    }
}

data class AddEditPetUiState(
    val name: String = "",
    val species: String = "",
    val breed: String = "",
    val sex: String = "",
    val weightUnit: String = "kg",
    val color: String = "",
    val notes: String = "",
    val microchipId: String = "",
    val favoriteVet: String = "",
    val favoriteFood: String = ""
)

sealed class AddEditPetEvent {
    data class EnteredName(val value: String) : AddEditPetEvent()
    data class EnteredSpecies(val value: String) : AddEditPetEvent()
    data class EnteredBreed(val value: String) : AddEditPetEvent()
    data class EnteredSex(val value: String) : AddEditPetEvent()
    object SavePet : AddEditPetEvent()
}
