package our.bunny.julie.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import our.bunny.julie.domain.repository.SettingsRepository
import our.bunny.julie.manager.BackupAlarmManager
import our.bunny.julie.manager.BackupRestoreManager
import javax.inject.Inject

data class BackupRestoreUiState(
    val backupFolderUri: String? = null,
    val isEncrypted: Boolean = false,
    val isAutoBackupEnabled: Boolean = false,
    val autoBackupTimeMinutes: Int = 120,
    val lastAutoBackupTimestamp: String? = null,
    val lastAutoBackupStatus: String? = null
)

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val backupRestoreManager: BackupRestoreManager,
    private val settingsRepository: SettingsRepository,
    private val backupAlarmManager: BackupAlarmManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _events = MutableSharedFlow<BackupRestoreEvent>()
    val events: SharedFlow<BackupRestoreEvent> = _events

    val uiState: StateFlow<BackupRestoreUiState> = combine(
        settingsRepository.backupFolderUriFlow,
        settingsRepository.encryptedBackupEnabledFlow,
        settingsRepository.autoBackupEnabledFlow,
        settingsRepository.autoBackupTimeMinutesFlow,
        settingsRepository.lastAutoBackupTimestampFlow,
        settingsRepository.lastAutoBackupStatusFlow
    ) { flowArray ->
        BackupRestoreUiState(
            backupFolderUri = flowArray[0] as String?,
            isEncrypted = flowArray[1] as Boolean,
            isAutoBackupEnabled = flowArray[2] as Boolean,
            autoBackupTimeMinutes = flowArray[3] as Int,
            lastAutoBackupTimestamp = flowArray[4] as String?,
            lastAutoBackupStatus = flowArray[5] as String?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BackupRestoreUiState()
    )

    fun updateBackupFolderUri(uri: String?) {
        viewModelScope.launch {
            settingsRepository.updateBackupFolderUri(uri)
        }
    }

    fun updateEncryptedBackupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateEncryptedBackupEnabled(enabled)
            // Option A: Disable auto backup if encryption is turned on
            if (enabled) {
                settingsRepository.updateAutoBackupEnabled(false)
                backupAlarmManager.cancelAutoBackup()
            }
        }
    }

    fun updateAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateAutoBackupEnabled(enabled)
            if (enabled) {
                backupAlarmManager.scheduleNextAutoBackup()
            } else {
                backupAlarmManager.cancelAutoBackup()
            }
        }
    }

    fun updateAutoBackupTimeMinutes(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.updateAutoBackupTimeMinutes(minutes)
            backupAlarmManager.scheduleNextAutoBackup()
        }
    }

    fun exportData(passphrase: CharArray? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            val isEncrypted = uiState.value.isEncrypted
            val folderUri = uiState.value.backupFolderUri
            val result = backupRestoreManager.exportData(folderUri, isEncrypted, passphrase)
            _isLoading.value = false
            
            if (result.isSuccess) {
                _events.emit(BackupRestoreEvent.ExportSuccess(result.getOrNull() ?: "Success"))
            } else {
                _events.emit(BackupRestoreEvent.ExportError(result.exceptionOrNull()?.message ?: "Unknown error"))
            }
        }
    }

    fun importData(uri: Uri, passphrase: CharArray? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = backupRestoreManager.importData(uri, passphrase)
            _isLoading.value = false
            
            if (result.isSuccess) {
                _events.emit(BackupRestoreEvent.ImportSuccess)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                if (errorMsg == "PASSPHRASE_REQUIRED") {
                    _events.emit(BackupRestoreEvent.PassphraseRequired(uri))
                } else {
                    _events.emit(BackupRestoreEvent.ImportError(errorMsg))
                }
            }
        }
    }
}

sealed class BackupRestoreEvent {
    data class ExportSuccess(val message: String) : BackupRestoreEvent()
    data class ExportError(val message: String) : BackupRestoreEvent()
    object ImportSuccess : BackupRestoreEvent()
    data class ImportError(val message: String) : BackupRestoreEvent()
    data class PassphraseRequired(val uri: Uri) : BackupRestoreEvent()
}
