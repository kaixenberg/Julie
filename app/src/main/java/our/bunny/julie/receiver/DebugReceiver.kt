package our.bunny.julie.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import our.bunny.julie.manager.StatReminderManager
import our.bunny.julie.domain.repository.PetRepository
import javax.inject.Inject

@AndroidEntryPoint
class DebugReceiver : BroadcastReceiver() {
    @Inject
    lateinit var statReminderManager: StatReminderManager

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("DebugReceiver", "onReceive triggered")
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("DebugReceiver", "Calling rescheduleAll")
            statReminderManager.rescheduleAll()
        }
    }
}
