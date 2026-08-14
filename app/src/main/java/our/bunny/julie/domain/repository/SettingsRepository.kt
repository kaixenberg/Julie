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
        val USE_SYSTEM_FONT = booleanPreferencesKey("use_system_font")
        val OLED_BLACK = booleanPreferencesKey("oled_black")
        val BLUR_EFFECTS = booleanPreferencesKey("blur_effects")
        val HAS_REQUESTED_NOTIFICATION_PERMISSION = booleanPreferencesKey("has_requested_notification_permission")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val REMINDERS_WEIGHT = booleanPreferencesKey("reminders_weight")
        val REMINDERS_WATER = booleanPreferencesKey("reminders_water")
        val REMINDERS_FEEDING = booleanPreferencesKey("reminders_feeding")
        val REMINDERS_MEDICATION = booleanPreferencesKey("reminders_medication")
        val QUIET_HOURS_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        val REMINDERS_WEIGHT_INTERVAL_DAYS = androidx.datastore.preferences.core.intPreferencesKey("reminders_weight_interval_days")
        val REMINDERS_WATER_INTERVAL_HOURS = androidx.datastore.preferences.core.intPreferencesKey("reminders_water_interval_hours")
        val REMINDERS_FEEDING_TIMES = androidx.datastore.preferences.core.stringSetPreferencesKey("reminders_feeding_times")
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
        preferences[PreferencesKeys.DYNAMIC_COLOR] ?: false
    }

    val paletteStyleFlow: Flow<PaletteStyle> = context.dataStore.data.map { preferences ->
        val styleStr = preferences[PreferencesKeys.PALETTE_STYLE] ?: PaletteStyle.Julie.name
        try {
            PaletteStyle.valueOf(styleStr)
        } catch (e: Exception) {
            PaletteStyle.Julie
        }
    }

    val predictiveBackFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PREDICTIVE_BACK] ?: true
    }

    val useSystemFontFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USE_SYSTEM_FONT] ?: false
    }

    val oledBlackFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.OLED_BLACK] ?: false
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

    suspend fun updateUseSystemFont(useSystemFont: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_SYSTEM_FONT] = useSystemFont
        }
    }

    suspend fun updateOledBlack(oledBlack: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OLED_BLACK] = oledBlack
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

    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
    }

    val hasRequestedNotificationPermissionFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAS_REQUESTED_NOTIFICATION_PERMISSION] ?: false
    }

    suspend fun setHasRequestedNotificationPermission(hasRequested: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_REQUESTED_NOTIFICATION_PERMISSION] = hasRequested
        }
    }

    val remindersWeightFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMINDERS_WEIGHT] ?: true
    }

    val remindersWaterFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMINDERS_WATER] ?: true
    }

    val remindersFeedingFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMINDERS_FEEDING] ?: true
    }

    val remindersMedicationFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMINDERS_MEDICATION] ?: true
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun updateRemindersWeight(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDERS_WEIGHT] = enabled
        }
    }

    suspend fun updateRemindersWater(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDERS_WATER] = enabled
        }
    }

    suspend fun updateRemindersFeeding(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDERS_FEEDING] = enabled
        }
    }

    suspend fun updateRemindersMedication(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDERS_MEDICATION] = enabled
        }
    }

    val quietHoursEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.QUIET_HOURS_ENABLED] ?: true
    }

    val remindersWeightIntervalDaysFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMINDERS_WEIGHT_INTERVAL_DAYS] ?: 1
    }

    val remindersWaterIntervalHoursFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMINDERS_WATER_INTERVAL_HOURS] ?: 4
    }

    val remindersFeedingTimesFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMINDERS_FEEDING_TIMES] ?: setOf("08:00", "19:00")
    }

    suspend fun updateQuietHoursEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.QUIET_HOURS_ENABLED] = enabled
        }
    }

    suspend fun updateRemindersWeightIntervalDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDERS_WEIGHT_INTERVAL_DAYS] = days
        }
    }

    suspend fun updateRemindersWaterIntervalHours(hours: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDERS_WATER_INTERVAL_HOURS] = hours
        }
    }

    suspend fun updateRemindersFeedingTimes(times: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDERS_FEEDING_TIMES] = times
        }
    }
}

