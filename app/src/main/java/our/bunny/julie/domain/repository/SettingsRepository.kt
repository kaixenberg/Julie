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
import our.bunny.julie.util.WaterUnit
import our.bunny.julie.util.WeightUnit
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
}
