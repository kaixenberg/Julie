package our.bunny.julie.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import our.bunny.julie.domain.repository.SettingsRepository
import our.bunny.julie.domain.repository.TrackerRepository
import our.bunny.julie.manager.StatReminderManager
import our.bunny.julie.util.NotificationHelper
import java.time.LocalDateTime
import java.time.ZoneId

@HiltWorker
class WaterReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val trackerRepository: TrackerRepository,
    private val statReminderManager: StatReminderManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val petId = inputData.getLong("petId", -1L)
        if (petId == -1L) return Result.failure()

        // Get today's total water to see if we should suppress? 
        // Or check if there was a log recently. The prompt says:
        // "has a water entry been logged within the last N hours (e.g. 2 hours)? If yes, skip firing."
        // Wait, TrackerDao has getWaterLogsForPet but not getLatestWaterLog.
        // I will use getWaterLogsForPet and take the first one, or add getLatestWaterLog.
        // For now, I'll fetch the list and take the max date.
        val logs = trackerRepository.getWaterLogsForPet(petId).first()
        val latestLog = logs.maxByOrNull { it.time }

        var shouldSuppress = false
        if (latestLog != null) {
            val lastLogTime = latestLog.time
            val cutoff = LocalDateTime.now().minusHours(2)
            if (lastLogTime.isAfter(cutoff)) {
                shouldSuppress = true
            }
        }

        if (!shouldSuppress) {
            NotificationHelper.showWaterReminder(context, petId)
        }

        statReminderManager.rescheduleWater(petId)

        return Result.success()
    }
}
