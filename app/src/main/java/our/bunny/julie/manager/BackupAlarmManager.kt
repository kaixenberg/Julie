package our.bunny.julie.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import our.bunny.julie.domain.repository.SettingsRepository
import our.bunny.julie.receiver.BackupAlarmReceiver
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupAlarmManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleNextAutoBackup() {
        runBlocking {
            val isEnabled = settingsRepository.autoBackupEnabledFlow.first()
            if (!isEnabled) {
                cancelAutoBackup()
                return@runBlocking
            }
            
            // Note: the logic explicitly cancels auto-backup if encryption is enabled.
            val isEncrypted = settingsRepository.encryptedBackupEnabledFlow.first()
            if (isEncrypted) {
                cancelAutoBackup()
                return@runBlocking
            }

            val timeMinutes = settingsRepository.autoBackupTimeMinutesFlow.first()
            val hour = timeMinutes / 60
            val minute = timeMinutes % 60

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // If time is in the past for today, schedule for tomorrow
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val intent = Intent(context, BackupAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                BackupAlarmReceiver.REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                // If permission is missing on Android 12+, we could fallback to set(), but standard behavior is required
                return@runBlocking
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelAutoBackup() {
        val intent = Intent(context, BackupAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            BackupAlarmReceiver.REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
