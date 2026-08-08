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
    private fun getDeepLinkPendingIntent(context: Context, petId: Long, statType: String, requestCode: Int): PendingIntent {
        val intent = Intent(
            Intent.ACTION_VIEW,
            android.net.Uri.parse("julieapp://pet_stat_detail/$petId/$statType"),
            context,
            MainActivity::class.java
        )
        return androidx.core.app.TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } ?: PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun showMedicationReminder(context: Context, medicationId: Long, medicationName: String, dosage: String, petId: Long, petName: String, speciesEmoji: String) {
        val pendingIntent = getDeepLinkPendingIntent(context, petId, "Medication", medicationId.toInt())

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
        val pendingIntent = getDeepLinkPendingIntent(context, petId, "Weight", petId.toInt())

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
        val pendingIntent = getDeepLinkPendingIntent(context, petId, "Water", petId.toInt() + 1000)

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
        val pendingIntent = getDeepLinkPendingIntent(context, petId, "Feeding", petId.toInt() + 2000)

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
