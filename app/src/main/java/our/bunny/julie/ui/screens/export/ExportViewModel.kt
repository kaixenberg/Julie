package our.bunny.julie.ui.screens.export

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import our.bunny.julie.domain.repository.ExportRepository
import our.bunny.julie.domain.repository.SettingsRepository
import our.bunny.julie.util.PdfGenerator
import java.io.File
import javax.inject.Inject

sealed class ExportState {
    object Idle : ExportState()
    object Generating : ExportState()
    data class Success(val file: File) : ExportState()
    data class Error(val message: String) : ExportState()
}

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val exportRepository: ExportRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    fun generatePdf(context: Context, petId: Long) {
        viewModelScope.launch {
            _exportState.value = ExportState.Generating
            try {
                val report = exportRepository.getPetHealthReport(petId).firstOrNull()
                val weightUnit = settingsRepository.weightUnitFlow.firstOrNull() ?: our.bunny.julie.util.WeightUnit.KG
                val waterUnit = settingsRepository.waterUnitFlow.firstOrNull() ?: our.bunny.julie.util.WaterUnit.ML
                if (report != null) {
                    val file = PdfGenerator.generateReport(context, report, weightUnit, waterUnit)
                    _exportState.value = ExportState.Success(file)
                } else {
                    _exportState.value = ExportState.Error("Pet data not found.")
                }
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Failed to generate PDF")
            }
        }
    }

    fun resetState() {
        _exportState.value = ExportState.Idle
    }
}
