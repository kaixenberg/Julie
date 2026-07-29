package our.bunny.julie.util

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import our.bunny.julie.domain.model.Medication
import our.bunny.julie.worker.MedicationWorker
import java.time.LocalTime
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object ReminderManager {
    
    fun scheduleMedicationReminder(context: Context, medication: Medication) {
        if (!medication.isActive) {
            cancelMedicationReminder(context, medication.id)
            return
        }

        val timeParts = medication.timeOfDay.split(":")
        if (timeParts.size != 2) return
        
        val hour = timeParts[0].toIntOrNull() ?: return
        val minute = timeParts[1].toIntOrNull() ?: return

        val now = LocalDateTime.now()
        var scheduledTime = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        
        if (scheduledTime.isBefore(now)) {
            scheduledTime = scheduledTime.plusDays(1)
        }

        val initialDelay = Duration.between(now, scheduledTime).toMillis()

        // Based on frequency, determine repeat interval
        val repeatIntervalHours = when (medication.frequency) {
            "Twice a day" -> 12L
            "Daily" -> 24L
            "Weekly" -> 7 * 24L
            else -> 24L // Default to daily if unknown
        }

        val inputData = Data.Builder()
            .putLong("medicationId", medication.id)
            .putLong("petId", medication.petId)
            .putString("medicationName", medication.name)
            .putString("dosage", medication.dosage)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<MedicationWorker>(repeatIntervalHours, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "Medication_${medication.id}",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelMedicationReminder(context: Context, medicationId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("Medication_$medicationId")
    }
}
