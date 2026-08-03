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
import kotlinx.coroutines.launch
import our.bunny.julie.manager.MedicationReminderManager
import our.bunny.julie.util.NotificationHelper
import javax.inject.Inject

@AndroidEntryPoint
class MedicationAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderManager: MedicationReminderManager

    @Inject
    lateinit var petRepository: our.bunny.julie.domain.repository.PetRepository

    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1L)
        val petId = intent.getLongExtra(EXTRA_PET_ID, -1L)
        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: return
        val dosage = intent.getStringExtra(EXTRA_DOSAGE) ?: return

        CoroutineScope(Dispatchers.IO).launch {
            // 1. Fetch pet details
            val pet = petRepository.getPetById(petId)
            val petName = pet?.name ?: "Pet"
            val speciesEmoji = pet?.species?.let { our.bunny.julie.ui.screens.pet.PetData.getEmojiForSpecies(it) } ?: "🐾"

            // 2. Show the notification if we have permission
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationHelper.showMedicationReminder(context, medicationId, medicationName, dosage, petId, petName, speciesEmoji)
            }

            // 3. Reschedule next alarms (since AlarmManager setExact is one-shot, we must recalculate and set the next one)
            reminderManager.rescheduleAll()
        }
    }

    companion object {
        const val EXTRA_MEDICATION_ID = "extra_medication_id"
        const val EXTRA_PET_ID = "extra_pet_id"
        const val EXTRA_MEDICATION_NAME = "extra_medication_name"
        const val EXTRA_DOSAGE = "extra_dosage"
        const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
    }
}
