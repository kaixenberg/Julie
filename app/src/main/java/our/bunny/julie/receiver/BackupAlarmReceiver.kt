package our.bunny.julie.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import our.bunny.julie.domain.repository.SettingsRepository
import our.bunny.julie.manager.BackupAlarmManager
import our.bunny.julie.manager.BackupRestoreManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class BackupAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var backupRestoreManager: BackupRestoreManager

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var backupAlarmManager: BackupAlarmManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        
        scope.launch {
            try {
                val isAutoBackupEnabled = settingsRepository.autoBackupEnabledFlow.first()
                val isEncrypted = settingsRepository.encryptedBackupEnabledFlow.first()

                if (isAutoBackupEnabled && !isEncrypted) {
                    val configuredUriString = settingsRepository.backupFolderUriFlow.first()
                    
                    val result = backupRestoreManager.exportData(configuredUriString, false, null)
                    
                    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                    val status = if (result.isSuccess) {
                        "Success (${result.getOrNull()})"
                    } else {
                        "Failed: ${result.exceptionOrNull()?.message}"
                    }
                    
                    settingsRepository.updateLastAutoBackupInfo(timestamp, status)
                }

                // Reschedule for next day
                backupAlarmManager.scheduleNextAutoBackup()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val REQUEST_CODE = 8000
    }
}
