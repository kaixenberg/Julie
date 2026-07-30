package our.bunny.julie.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import our.bunny.julie.domain.repository.SettingsRepository
import our.bunny.julie.util.WaterUnit
import our.bunny.julie.util.WeightUnit
import javax.inject.Inject

data class SettingsUiState(
    val weightUnit: WeightUnit = WeightUnit.KG,
    val waterUnit: WaterUnit = WaterUnit.ML
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.weightUnitFlow,
        settingsRepository.waterUnitFlow
    ) { weightUnit, waterUnit ->
        SettingsUiState(weightUnit = weightUnit, waterUnit = waterUnit)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun updateWeightUnit(unit: WeightUnit) {
        viewModelScope.launch {
            settingsRepository.updateWeightUnit(unit)
        }
    }

    fun updateWaterUnit(unit: WaterUnit) {
        viewModelScope.launch {
            settingsRepository.updateWaterUnit(unit)
        }
    }
}
