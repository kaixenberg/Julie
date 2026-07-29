package our.bunny.julie.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import our.bunny.julie.domain.repository.PetRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val petRepository: PetRepository
) : ViewModel() {

    val pets = petRepository.getAllPets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
