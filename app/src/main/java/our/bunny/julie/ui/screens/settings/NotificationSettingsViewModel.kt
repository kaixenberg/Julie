package our.bunny.julie.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import our.bunny.julie.domain.repository.SettingsRepository
import javax.inject.Inject

data class NotificationSettingsUiState(
    val notificationsEnabled: Boolean = false,
    val remindersWeight: Boolean = true,
    val remindersWater: Boolean = true,
    val remindersFeeding: Boolean = true,
    val remindersMedication: Boolean = true
)

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<NotificationSettingsUiState> = combine(
        settingsRepository.notificationsEnabledFlow,
        settingsRepository.remindersWeightFlow,
        settingsRepository.remindersWaterFlow,
        settingsRepository.remindersFeedingFlow,
        settingsRepository.remindersMedicationFlow
    ) { notifications, weight, water, feeding, medication ->
        NotificationSettingsUiState(
            notificationsEnabled = notifications,
            remindersWeight = weight,
            remindersWater = water,
            remindersFeeding = feeding,
            remindersMedication = medication
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotificationSettingsUiState()
    )

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateNotificationsEnabled(enabled)
        }
    }

    fun updateRemindersWeight(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateRemindersWeight(enabled)
        }
    }

    fun updateRemindersWater(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateRemindersWater(enabled)
        }
    }

    fun updateRemindersFeeding(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateRemindersFeeding(enabled)
        }
    }

    fun updateRemindersMedication(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateRemindersMedication(enabled)
        }
    }
}
