package our.bunny.julie.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import our.bunny.julie.manager.StatReminderManager
import our.bunny.julie.manager.MedicationReminderManager
import our.bunny.julie.domain.repository.SettingsRepository
import javax.inject.Inject

data class NotificationSettingsUiState(
    val notificationsEnabled: Boolean = false,
    val remindersWeight: Boolean = true,
    val remindersWater: Boolean = true,
    val remindersFeeding: Boolean = true,
    val remindersMedication: Boolean = true,
    val quietHoursEnabled: Boolean = true,
    val remindersWeightIntervalDays: Int = 1,
    val remindersWaterIntervalHours: Int = 4,
    val remindersFeedingTimes: Set<String> = setOf("08:00", "19:00")
)

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val medicationReminderManager: MedicationReminderManager,
    private val statReminderManager: StatReminderManager
) : ViewModel() {

    val uiState: StateFlow<NotificationSettingsUiState> = combine(
        settingsRepository.notificationsEnabledFlow,
        settingsRepository.remindersWeightFlow,
        settingsRepository.remindersWaterFlow,
        settingsRepository.remindersFeedingFlow,
        settingsRepository.remindersMedicationFlow,
        settingsRepository.quietHoursEnabledFlow,
        settingsRepository.remindersWeightIntervalDaysFlow,
        settingsRepository.remindersWaterIntervalHoursFlow,
        settingsRepository.remindersFeedingTimesFlow
    ) { args ->
        NotificationSettingsUiState(
            notificationsEnabled = args[0] as Boolean,
            remindersWeight = args[1] as Boolean,
            remindersWater = args[2] as Boolean,
            remindersFeeding = args[3] as Boolean,
            remindersMedication = args[4] as Boolean,
            quietHoursEnabled = args[5] as Boolean,
            remindersWeightIntervalDays = args[6] as Int,
            remindersWaterIntervalHours = args[7] as Int,
            remindersFeedingTimes = args[8] as Set<String>
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotificationSettingsUiState()
    )

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateNotificationsEnabled(enabled)
            medicationReminderManager.rescheduleAll()
            statReminderManager.rescheduleAll()
        }
    }

    fun updateRemindersWeight(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateRemindersWeight(enabled)
            statReminderManager.rescheduleAll()
        }
    }

    fun updateRemindersWater(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateRemindersWater(enabled)
            statReminderManager.rescheduleAll()
        }
    }

    fun updateRemindersFeeding(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateRemindersFeeding(enabled)
            statReminderManager.rescheduleAll()
        }
    }

    fun updateRemindersMedication(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateRemindersMedication(enabled)
            medicationReminderManager.rescheduleAll()
        }
    }

    fun updateQuietHoursEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateQuietHoursEnabled(enabled)
            statReminderManager.rescheduleAll()
        }
    }

    fun updateRemindersWeightIntervalDays(days: Int) {
        viewModelScope.launch {
            settingsRepository.updateRemindersWeightIntervalDays(days)
            statReminderManager.rescheduleAll()
        }
    }

    fun updateRemindersWaterIntervalHours(hours: Int) {
        viewModelScope.launch {
            settingsRepository.updateRemindersWaterIntervalHours(hours)
            statReminderManager.rescheduleAll()
        }
    }

    fun updateRemindersFeedingTimes(times: Set<String>) {
        viewModelScope.launch {
            settingsRepository.updateRemindersFeedingTimes(times)
            statReminderManager.rescheduleAll()
        }
    }
}
