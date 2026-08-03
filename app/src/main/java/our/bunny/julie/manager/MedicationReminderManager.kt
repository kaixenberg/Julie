package our.bunny.julie.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import our.bunny.julie.domain.model.Medication
import our.bunny.julie.domain.model.MedicationSchedule
import our.bunny.julie.domain.repository.SettingsRepository
import our.bunny.julie.domain.repository.TrackerRepository
import our.bunny.julie.receiver.MedicationAlarmReceiver
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationReminderManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackerRepository: TrackerRepository,
    private val settingsRepository: SettingsRepository
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun rescheduleAll() {
        val notificationsEnabled = settingsRepository.notificationsEnabledFlow.first()
        val medicationRemindersEnabled = settingsRepository.remindersMedicationFlow.first()

        val medications = trackerRepository.getAllMedications()

        // Cancel all existing alarms first to avoid orphans if a schedule is deleted
        cancelAllAlarms(medications)

        if (!notificationsEnabled || !medicationRemindersEnabled) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return // Cannot schedule exact alarms without permission
        }

        medications.filter { it.isActive }.forEach { medication ->
            medication.schedules.forEach { schedule ->
                scheduleNextAlarm(medication, schedule)
            }
        }
    }

    private fun cancelAllAlarms(medications: List<Medication>) {
        medications.forEach { medication ->
            medication.schedules.forEach { schedule ->
                val pendingIntent = getPendingIntent(medication, schedule, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                }
            }
        }
    }

    private fun scheduleNextAlarm(medication: Medication, schedule: MedicationSchedule) {
        if (schedule.daysOfWeek.isEmpty()) return

        val now = LocalDateTime.now()
        val nowTruncated = now.withSecond(0).withNano(0)
        var nextAlarmTime: LocalDateTime? = null

        // Find the next occurrence
        for (i in 0..7) {
            val candidateDate = now.plusDays(i.toLong())
            if (schedule.daysOfWeek.contains(candidateDate.dayOfWeek)) {
                val candidateTime = candidateDate.withHour(schedule.timeOfDay.hour).withMinute(schedule.timeOfDay.minute).withSecond(0).withNano(0)
                if (!candidateTime.isBefore(nowTruncated)) {
                    nextAlarmTime = candidateTime
                    break
                }
            }
        }

        if (nextAlarmTime != null) {
            val triggerTimeMillis = nextAlarmTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val pendingIntent = getPendingIntent(medication, schedule, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            if (pendingIntent != null) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        }
    }

    private fun getPendingIntent(medication: Medication, schedule: MedicationSchedule, flags: Int): PendingIntent? {
        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_ID, medication.id)
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_NAME, medication.name)
            putExtra(MedicationAlarmReceiver.EXTRA_DOSAGE, medication.dosage)
            putExtra(MedicationAlarmReceiver.EXTRA_PET_ID, medication.petId)
            putExtra(MedicationAlarmReceiver.EXTRA_SCHEDULE_ID, schedule.id)
        }
        
        // We use schedule.id as a unique request code so each schedule has its own alarm
        return PendingIntent.getBroadcast(
            context,
            schedule.id.toInt(),
            intent,
            flags
        )
    }
}
