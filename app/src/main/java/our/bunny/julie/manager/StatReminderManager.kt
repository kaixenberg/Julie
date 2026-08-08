package our.bunny.julie.manager

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import our.bunny.julie.domain.model.FeedingReminderTemplate
import our.bunny.julie.domain.model.WaterReminderTemplate
import our.bunny.julie.domain.model.WeightReminderTemplate
import our.bunny.julie.domain.repository.PetRepository
import our.bunny.julie.domain.repository.SettingsRepository
import our.bunny.julie.domain.repository.TrackerRepository
import our.bunny.julie.worker.FeedingReminderWorker
import our.bunny.julie.worker.WaterReminderWorker
import our.bunny.julie.worker.WeightReminderWorker
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatReminderManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val petRepository: PetRepository,
    private val trackerRepository: TrackerRepository,
    private val settingsRepository: SettingsRepository
) {
    private val workManager = WorkManager.getInstance(context)

    fun rescheduleAll() {
        CoroutineScope(Dispatchers.IO).launch {
            val pets = petRepository.getAllPets().first()
            pets.forEach { pet ->
                rescheduleWeight(pet.id)
                rescheduleWater(pet.id)
                rescheduleFeeding(pet.id)
            }
        }
    }

    suspend fun rescheduleWeight(petId: Long) {
        val notificationsEnabled = settingsRepository.notificationsEnabledFlow.first()
        val weightEnabled = settingsRepository.remindersWeightFlow.first()
        
        if (!notificationsEnabled || !weightEnabled) {
            workManager.cancelUniqueWork("WeightReminder_$petId")
            return
        }

        val quietHours = settingsRepository.quietHoursEnabledFlow.first()
        val intervalDays = settingsRepository.remindersWeightIntervalDaysFlow.first()

        val template = WeightReminderTemplate(true, quietHours, intervalDays)
        val lastEntry = trackerRepository.getLatestWeightEntry(petId).first()

        val now = LocalDateTime.now()
        var nextFireTime: LocalDateTime
        
        if (lastEntry == null) {
            nextFireTime = now.plusMinutes(1)
        } else {
            nextFireTime = lastEntry.date.plusDays(template.intervalDays.toLong())
            if (nextFireTime.isBefore(now)) {
                nextFireTime = now.plusMinutes(1)
            }
        }

        nextFireTime = adjustForQuietHours(nextFireTime, template.quietHoursEnabled)

        val delayMillis = nextFireTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis()
        scheduleWork("WeightReminder_$petId", WeightReminderWorker::class.java, delayMillis, petId)
    }

    suspend fun rescheduleWater(petId: Long) {
        val notificationsEnabled = settingsRepository.notificationsEnabledFlow.first()
        val waterEnabled = settingsRepository.remindersWaterFlow.first()

        if (!notificationsEnabled || !waterEnabled) {
            workManager.cancelUniqueWork("WaterReminder_$petId")
            return
        }

        val quietHours = settingsRepository.quietHoursEnabledFlow.first()
        val intervalHours = settingsRepository.remindersWaterIntervalHoursFlow.first()
        val template = WaterReminderTemplate(true, quietHours, intervalHours)

        val waterLogs = trackerRepository.getWaterLogsForPet(petId).first()
        val latestLog = waterLogs.maxByOrNull { it.time }

        val now = LocalDateTime.now()
        var nextFireTime: LocalDateTime

        if (latestLog == null) {
            nextFireTime = now.plusMinutes(1)
        } else {
            nextFireTime = latestLog.time.plusHours(template.intervalHours.toLong())
            if (nextFireTime.isBefore(now)) {
                nextFireTime = now.plusMinutes(1)
            }
        }

        nextFireTime = adjustForQuietHours(nextFireTime, template.quietHoursEnabled)

        val delayMillis = nextFireTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis()
        scheduleWork("WaterReminder_$petId", WaterReminderWorker::class.java, delayMillis, petId)
    }

    suspend fun rescheduleFeeding(petId: Long) {
        val notificationsEnabled = settingsRepository.notificationsEnabledFlow.first()
        val feedingEnabled = settingsRepository.remindersFeedingFlow.first()

        if (!notificationsEnabled || !feedingEnabled) {
            workManager.cancelUniqueWork("FeedingReminder_$petId")
            return
        }

        val quietHours = settingsRepository.quietHoursEnabledFlow.first()
        val timesSet = settingsRepository.remindersFeedingTimesFlow.first()
        val scheduledTimes = timesSet.map { LocalTime.parse(it) }.sorted()
        
        if (scheduledTimes.isEmpty()) {
            workManager.cancelUniqueWork("FeedingReminder_$petId")
            return
        }

        val template = FeedingReminderTemplate(true, quietHours, scheduledTimes)
        val now = LocalDateTime.now()
        var nextFireTime: LocalDateTime? = null

        // Find the next scheduled time today that is strictly in the future
        for (time in template.scheduledTimes) {
            val candidate = LocalDateTime.of(now.toLocalDate(), time)
            if (candidate.isAfter(now)) {
                nextFireTime = candidate
                break
            }
        }

        // If no times left today, schedule for the first time tomorrow
        if (nextFireTime == null) {
            nextFireTime = LocalDateTime.of(now.toLocalDate().plusDays(1), template.scheduledTimes.first())
        }

        nextFireTime = adjustForQuietHours(nextFireTime, template.quietHoursEnabled)
        val delayMillis = nextFireTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis()
        scheduleWork("FeedingReminder_$petId", FeedingReminderWorker::class.java, delayMillis, petId)
    }

    private fun adjustForQuietHours(time: LocalDateTime, quietHoursEnabled: Boolean): LocalDateTime {
        if (!quietHoursEnabled) return time
        
        val hour = time.hour
        // Quiet hours: 10 PM (22:00) to 7 AM (07:00)
        return if (hour >= 22) {
            // Shift to 7 AM next day
            time.plusDays(1).withHour(7).withMinute(0).withSecond(0).withNano(0)
        } else if (hour < 7) {
            // Shift to 7 AM today
            time.withHour(7).withMinute(0).withSecond(0).withNano(0)
        } else {
            time
        }
    }

    private fun scheduleWork(uniqueWorkName: String, workerClass: Class<out androidx.work.ListenableWorker>, delayMillis: Long, petId: Long) {
        val data = Data.Builder().putLong("petId", petId).build()
        val request = androidx.work.OneTimeWorkRequest.Builder(workerClass)
            .setInputData(data)
            .setInitialDelay(maxOf(delayMillis, 0L), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
