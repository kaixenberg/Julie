package our.bunny.julie.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import our.bunny.julie.domain.repository.PetRepository
import javax.inject.Inject

import our.bunny.julie.domain.repository.SettingsRepository
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val pets = petRepository.getAllPets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val notificationsEnabled = settingsRepository.notificationsEnabledFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val hasRequestedNotificationPermission = settingsRepository.hasRequestedNotificationPermissionFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun disableNotifications() {
        viewModelScope.launch {
            settingsRepository.updateNotificationsEnabled(false)
        }
    }

    fun setHasRequestedNotificationPermission(hasRequested: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHasRequestedNotificationPermission(hasRequested)
        }
    }
}
