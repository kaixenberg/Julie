package our.bunny.julie.domain.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import our.bunny.julie.domain.model.ThemeConfig
import our.bunny.julie.ui.theme.PaletteStyle
import our.bunny.julie.util.WaterUnit
import our.bunny.julie.util.WeightUnit
import androidx.datastore.preferences.core.booleanPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val WATER_UNIT = stringPreferencesKey("water_unit")
        val THEME_CONFIG = stringPreferencesKey("theme_config")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val PALETTE_STYLE = stringPreferencesKey("palette_style")
        val PREDICTIVE_BACK = booleanPreferencesKey("predictive_back")
        val BLUR_EFFECTS = booleanPreferencesKey("blur_effects")
    }

    val weightUnitFlow: Flow<WeightUnit> = context.dataStore.data.map { preferences ->
        val unitString = preferences[PreferencesKeys.WEIGHT_UNIT] ?: WeightUnit.KG.name
        try {
            WeightUnit.valueOf(unitString)
        } catch (e: Exception) {
            WeightUnit.KG
        }
    }

    val waterUnitFlow: Flow<WaterUnit> = context.dataStore.data.map { preferences ->
        val unitString = preferences[PreferencesKeys.WATER_UNIT] ?: WaterUnit.ML.name
        try {
            WaterUnit.valueOf(unitString)
        } catch (e: Exception) {
            WaterUnit.ML
        }
    }

    suspend fun updateWeightUnit(unit: WeightUnit) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEIGHT_UNIT] = unit.name
        }
    }

    suspend fun updateWaterUnit(unit: WaterUnit) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WATER_UNIT] = unit.name
        }
    }

    val themeConfigFlow: Flow<ThemeConfig> = context.dataStore.data.map { preferences ->
        val configStr = preferences[PreferencesKeys.THEME_CONFIG] ?: ThemeConfig.SYSTEM.name
        try {
            ThemeConfig.valueOf(configStr)
        } catch (e: Exception) {
            ThemeConfig.SYSTEM
        }
    }

    val dynamicColorFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DYNAMIC_COLOR] ?: true
    }

    val paletteStyleFlow: Flow<PaletteStyle> = context.dataStore.data.map { preferences ->
        val styleStr = preferences[PreferencesKeys.PALETTE_STYLE] ?: PaletteStyle.TonalSpot.name
        try {
            PaletteStyle.valueOf(styleStr)
        } catch (e: Exception) {
            PaletteStyle.TonalSpot
        }
    }

    val predictiveBackFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PREDICTIVE_BACK] ?: true
    }

    val blurEffectsFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BLUR_EFFECTS] ?: false
    }

    suspend fun updateThemeConfig(config: ThemeConfig) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_CONFIG] = config.name
        }
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun updatePaletteStyle(style: PaletteStyle) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PALETTE_STYLE] = style.name
        }
    }

    suspend fun updatePredictiveBack(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PREDICTIVE_BACK] = enabled
        }
    }

    suspend fun updateBlurEffects(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BLUR_EFFECTS] = enabled
        }
    }
}

