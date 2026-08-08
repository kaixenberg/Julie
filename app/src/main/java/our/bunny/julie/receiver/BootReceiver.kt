package our.bunny.julie.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import our.bunny.julie.manager.MedicationReminderManager
import our.bunny.julie.manager.StatReminderManager
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderManager: MedicationReminderManager

    @Inject
    lateinit var statReminderManager: StatReminderManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                reminderManager.rescheduleAll()
                statReminderManager.rescheduleAll()
            }
        }
    }
}
