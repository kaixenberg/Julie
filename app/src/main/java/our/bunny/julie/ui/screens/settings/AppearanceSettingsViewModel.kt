package our.bunny.julie.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val useSystemFont: Boolean = false,
    val oledBlack: Boolean = false
)

@HiltViewModel
class AppearanceSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<AppearanceSettingsUiState> = combine(
        settingsRepository.themeConfigFlow,
        settingsRepository.dynamicColorFlow,
        settingsRepository.paletteStyleFlow,
        settingsRepository.predictiveBackFlow,
        settingsRepository.blurEffectsFlow,
        settingsRepository.useSystemFontFlow,
        settingsRepository.oledBlackFlow
    ) { flowArray ->
        AppearanceSettingsUiState(
            themeConfig = flowArray[0] as ThemeConfig,
            dynamicColor = flowArray[1] as Boolean,
            paletteStyle = flowArray[2] as PaletteStyle,
            predictiveBack = flowArray[3] as Boolean,
            blurEffects = flowArray[4] as Boolean,
            useSystemFont = flowArray[5] as Boolean,
            oledBlack = flowArray[6] as Boolean
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppearanceSettingsUiState()
    )

    fun updateThemeConfig(config: ThemeConfig) {
        viewModelScope.launch {
            settingsRepository.updateThemeConfig(config)
            broadcastWidgetUpdate()
        }
    }

    fun updateDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateDynamicColor(enabled)
            broadcastWidgetUpdate()
        }
    }

    fun updatePaletteStyle(style: PaletteStyle) {
        viewModelScope.launch {
            settingsRepository.updatePaletteStyle(style)
            broadcastWidgetUpdate()
        }
    }

    fun updatePredictiveBack(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updatePredictiveBack(enabled)
            // No widget visual impact for this one, but safe to broadcast
        }
    }

    fun updateBlurEffects(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateBlurEffects(enabled)
            broadcastWidgetUpdate()
        }
    }

    fun updateUseSystemFont(useSystem: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateUseSystemFont(useSystem)
            broadcastWidgetUpdate()
        }
    }

    fun updateOledBlack(oledBlack: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateOledBlack(oledBlack)
            broadcastWidgetUpdate()
        }
    }

    private fun broadcastWidgetUpdate() {
        val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
        
        val intent2x2 = android.content.Intent(context, our.bunny.julie.widget.PetStatWidget2x2Provider::class.java).apply {
            action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = appWidgetManager.getAppWidgetIds(android.content.ComponentName(context, our.bunny.julie.widget.PetStatWidget2x2Provider::class.java))
            putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(intent2x2)

        val intent4x2 = android.content.Intent(context, our.bunny.julie.widget.PetStatWidget4x2Provider::class.java).apply {
            action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = appWidgetManager.getAppWidgetIds(android.content.ComponentName(context, our.bunny.julie.widget.PetStatWidget4x2Provider::class.java))
            putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(intent4x2)
    }
}
