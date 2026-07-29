package our.bunny.julie.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import our.bunny.julie.util.NotificationHelper

@HiltWorker
class MedicationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val medicationId = inputData.getLong("medicationId", -1L)
        val petId = inputData.getLong("petId", -1L)
        val medicationName = inputData.getString("medicationName") ?: return Result.failure()
        val dosage = inputData.getString("dosage") ?: return Result.failure()

        NotificationHelper.createNotificationChannel(context)
        NotificationHelper.showMedicationReminder(context, medicationId, medicationName, dosage, petId)

        return Result.success()
    }
}
