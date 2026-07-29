package our.bunny.julie.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import our.bunny.julie.MainActivity
import our.bunny.julie.R

object NotificationHelper {
    private const val MEDICATION_CHANNEL_ID = "medication_reminders"
    private const val MEDICATION_CHANNEL_NAME = "Medication Reminders"
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MEDICATION_CHANNEL_ID,
                MEDICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to give your pet their medication"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showMedicationReminder(context: Context, medicationId: Long, medicationName: String, dosage: String, petId: Long) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // We can pass petId to navigate directly if we want
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            medicationId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, MEDICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Fallback icon for now
            .setContentTitle("Time for Medication: $medicationName")
            .setContentText("Give $dosage to your pet now.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(medicationId.toInt(), builder.build())
    }
}
