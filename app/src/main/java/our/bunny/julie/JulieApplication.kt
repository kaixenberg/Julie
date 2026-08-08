package our.bunny.julie

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import our.bunny.julie.manager.StatReminderManager
import javax.inject.Inject

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class JulieApplication : Application() {

    @Inject
    lateinit var statReminderManager: StatReminderManager
    
    @Inject
    lateinit var medicationReminderManager: our.bunny.julie.manager.MedicationReminderManager

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        statReminderManager.rescheduleAll()
        
        CoroutineScope(Dispatchers.IO).launch {
            medicationReminderManager.rescheduleAll()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val weightChannel = NotificationChannel(
                CHANNEL_ID_WEIGHT,
                "Weight Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Reminders for weight tracking" }

            val waterChannel = NotificationChannel(
                CHANNEL_ID_WATER,
                "Water Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Reminders for water tracking" }

            val feedingChannel = NotificationChannel(
                CHANNEL_ID_FEEDING,
                "Feeding Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Reminders for feeding" }

            val medicationChannel = NotificationChannel(
                CHANNEL_ID_MEDICATION,
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Reminders for medication" }

            notificationManager.createNotificationChannels(
                listOf(weightChannel, waterChannel, feedingChannel, medicationChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_ID_WEIGHT = "channel_weight_reminders_high"
        const val CHANNEL_ID_WATER = "channel_water_reminders_high"
        const val CHANNEL_ID_FEEDING = "channel_feeding_reminders_high"
        const val CHANNEL_ID_MEDICATION = "channel_medication_reminders"
    }
}
