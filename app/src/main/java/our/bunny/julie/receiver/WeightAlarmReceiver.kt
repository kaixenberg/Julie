package our.bunny.julie.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import our.bunny.julie.domain.repository.PetRepository
import our.bunny.julie.domain.repository.SettingsRepository
import our.bunny.julie.domain.repository.TrackerRepository
import our.bunny.julie.manager.StatReminderManager
import our.bunny.julie.util.NotificationHelper
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class WeightAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderManager: StatReminderManager

    @Inject
    lateinit var petRepository: PetRepository

    @Inject
    lateinit var trackerRepository: TrackerRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        val petId = intent.getLongExtra(EXTRA_PET_ID, -1L)
        if (petId == -1L) return

        CoroutineScope(Dispatchers.IO).launch {
            val intervalDays = settingsRepository.remindersWeightIntervalDaysFlow.first()
            val latestEntry = trackerRepository.getLatestWeightEntry(petId).first()
            val quietHoursEnabled = settingsRepository.quietHoursEnabledFlow.first()
            val now = LocalDateTime.now()

            var shouldSuppress = false
            
            // 1. Check if user logged an entry within the suppression window
            if (latestEntry != null) {
                val cutoff = now.minusDays(intervalDays.toLong())
                if (latestEntry.date.isAfter(cutoff)) {
                    shouldSuppress = true
                }
            }

            // 2. Check quiet hours
            if (quietHoursEnabled) {
                val hour = now.hour
                if (hour >= 22 || hour < 7) {
                    shouldSuppress = true
                }
            }

            if (!shouldSuppress) {
                val pet = petRepository.getPetById(petId)
                val petName = pet?.name ?: "Pet"
                val speciesEmoji = pet?.species?.let { our.bunny.julie.ui.screens.pet.PetData.getEmojiForSpecies(it) } ?: "🐾"

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    NotificationHelper.showWeightReminder(context, petId, petName, speciesEmoji)
                }
            }

            // Always reschedule the next one
            reminderManager.rescheduleWeight(petId)
        }
    }

    companion object {
        const val EXTRA_PET_ID = "extra_pet_id"
    }
}
