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
class WeightReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val trackerRepository: TrackerRepository,
    private val settingsRepository: SettingsRepository,
    private val statReminderManager: StatReminderManager,
    private val petRepository: our.bunny.julie.domain.repository.PetRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val petId = inputData.getLong("petId", -1L)
        if (petId == -1L) return Result.failure()

        val intervalDays = settingsRepository.remindersWeightIntervalDaysFlow.first()
        val latestEntry = trackerRepository.getLatestWeightEntry(petId).first()

        var shouldSuppress = false
        if (latestEntry != null) {
            val lastEntryTime = latestEntry.date
            val now = LocalDateTime.now()
            
            // If the latest entry is newer than (now - interval), suppress
            val cutoff = now.minusDays(intervalDays.toLong())
            if (lastEntryTime.isAfter(cutoff)) {
                shouldSuppress = true
            }
        }

        if (!shouldSuppress) {
            val pet = petRepository.getPetById(petId)
            val petName = pet?.name ?: "Pet"
            val speciesEmoji = pet?.species?.let { our.bunny.julie.ui.screens.pet.PetData.getEmojiForSpecies(it) } ?: "🐾"
            NotificationHelper.showWeightReminder(context, petId, petName, speciesEmoji)
        }

        statReminderManager.rescheduleWeight(petId)

        return Result.success()
    }
}
