package our.bunny.julie.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import our.bunny.julie.MainActivity
import our.bunny.julie.JulieApplication
import our.bunny.julie.R

object NotificationHelper {
    fun showMedicationReminder(context: Context, medicationId: Long, medicationName: String, dosage: String, petId: Long, petName: String, speciesEmoji: String) {
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

        val builder = NotificationCompat.Builder(context, JulieApplication.CHANNEL_ID_MEDICATION)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$speciesEmoji $petName: $medicationName due")
            .setContentText("Give $dosage to your pet now.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(medicationId.toInt(), builder.build())
    }

    fun showWeightReminder(context: Context, petId: Long, petName: String, speciesEmoji: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, petId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, JulieApplication.CHANNEL_ID_WEIGHT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$speciesEmoji $petName: Weight Reminder")
            .setContentText("It's time to log your pet's weight.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = (petId.toInt() * 10) + 1
        notificationManager.notify(notificationId, builder.build())
    }

    fun showWaterReminder(context: Context, petId: Long, petName: String, speciesEmoji: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, petId.toInt() + 1000, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, JulieApplication.CHANNEL_ID_WATER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$speciesEmoji $petName: Water Check-in")
            .setContentText("Has your pet had enough water today?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = (petId.toInt() * 10) + 2
        notificationManager.notify(notificationId, builder.build())
    }

    fun showFeedingReminder(context: Context, petId: Long, petName: String, speciesEmoji: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, petId.toInt() + 2000, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, JulieApplication.CHANNEL_ID_FEEDING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$speciesEmoji $petName: Feeding Time")
            .setContentText("Time to feed your pet.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = (petId.toInt() * 10) + 3
        notificationManager.notify(notificationId, builder.build())
    }
}
