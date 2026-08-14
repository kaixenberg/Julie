package our.bunny.julie.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import our.bunny.julie.domain.model.ThemeConfig
import our.bunny.julie.domain.repository.SettingsRepository
import our.bunny.julie.ui.theme.PaletteStyle
import javax.inject.Inject

data class AppearanceSettingsUiState(
    val themeConfig: ThemeConfig = ThemeConfig.SYSTEM,
    val dynamicColor: Boolean = false,
    val paletteStyle: PaletteStyle = PaletteStyle.Julie,
    val predictiveBack: Boolean = true,
    val blurEffects: Boolean = false,
    val useSystemFont: Boolean = false
)

@HiltViewModel
class AppearanceSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<AppearanceSettingsUiState> = combine(
        settingsRepository.themeConfigFlow,
        settingsRepository.dynamicColorFlow,
        settingsRepository.paletteStyleFlow,
        settingsRepository.predictiveBackFlow,
        settingsRepository.blurEffectsFlow,
        settingsRepository.useSystemFontFlow
    ) { flowArray ->
        AppearanceSettingsUiState(
            themeConfig = flowArray[0] as ThemeConfig,
            dynamicColor = flowArray[1] as Boolean,
            paletteStyle = flowArray[2] as PaletteStyle,
            predictiveBack = flowArray[3] as Boolean,
            blurEffects = flowArray[4] as Boolean,
            useSystemFont = flowArray[5] as Boolean
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppearanceSettingsUiState()
    )

    fun updateThemeConfig(config: ThemeConfig) {
        viewModelScope.launch {
            settingsRepository.updateThemeConfig(config)
        }
    }

    fun updateDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateDynamicColor(enabled)
        }
    }

    fun updatePaletteStyle(style: PaletteStyle) {
        viewModelScope.launch {
            settingsRepository.updatePaletteStyle(style)
        }
    }

    fun updatePredictiveBack(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updatePredictiveBack(enabled)
        }
    }

    fun updateBlurEffects(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateBlurEffects(enabled)
        }
    }

    fun updateUseSystemFont(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateUseSystemFont(enabled)
        }
    }
}
