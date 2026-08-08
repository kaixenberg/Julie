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
import our.bunny.julie.manager.StatReminderManager
import our.bunny.julie.util.NotificationHelper
import javax.inject.Inject

@AndroidEntryPoint
class FeedingAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderManager: StatReminderManager

    @Inject
    lateinit var petRepository: our.bunny.julie.domain.repository.PetRepository

    override fun onReceive(context: Context, intent: Intent) {
        val petId = intent.getLongExtra(EXTRA_PET_ID, -1L)
        if (petId == -1L) return

        CoroutineScope(Dispatchers.IO).launch {
            // 1. Fetch pet details
            val pet = petRepository.getPetById(petId)
            val petName = pet?.name ?: "Pet"
            val speciesEmoji = pet?.species?.let { our.bunny.julie.ui.screens.pet.PetData.getEmojiForSpecies(it) } ?: "🐾"

            // 2. Show the notification if we have permission
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationHelper.showFeedingReminder(context, petId, petName, speciesEmoji)
            }

            // 3. Reschedule next alarms
            reminderManager.rescheduleFeeding(petId)
        }
    }

    companion object {
        const val EXTRA_PET_ID = "extra_pet_id"
    }
}
