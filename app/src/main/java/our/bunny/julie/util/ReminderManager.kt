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
        // TODO: Implement complex alarm scheduling in Part 6 based on medication.schedules
    }

    fun cancelMedicationReminder(context: Context, medicationId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("Medication_$medicationId")
    }
}
