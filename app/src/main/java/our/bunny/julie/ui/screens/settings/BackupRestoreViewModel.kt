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
import our.bunny.julie.manager.BackupRestoreManager
import javax.inject.Inject

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val backupRestoreManager: BackupRestoreManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _events = MutableSharedFlow<BackupRestoreEvent>()
    val events: SharedFlow<BackupRestoreEvent> = _events

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = backupRestoreManager.exportData(uri)
            _isLoading.value = false
            
            if (result.isSuccess) {
                _events.emit(BackupRestoreEvent.ExportSuccess)
            } else {
                _events.emit(BackupRestoreEvent.ExportError(result.exceptionOrNull()?.message ?: "Unknown error"))
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = backupRestoreManager.importData(uri)
            _isLoading.value = false
            
            if (result.isSuccess) {
                _events.emit(BackupRestoreEvent.ImportSuccess)
            } else {
                _events.emit(BackupRestoreEvent.ImportError(result.exceptionOrNull()?.message ?: "Unknown error"))
            }
        }
    }
}

sealed class BackupRestoreEvent {
    object ExportSuccess : BackupRestoreEvent()
    data class ExportError(val message: String) : BackupRestoreEvent()
    object ImportSuccess : BackupRestoreEvent()
    data class ImportError(val message: String) : BackupRestoreEvent()
}
