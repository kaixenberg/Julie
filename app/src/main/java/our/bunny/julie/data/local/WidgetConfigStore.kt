package our.bunny.julie.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class WidgetSlotConfig(val petId: Long, val statMode: String)

@Singleton
class WidgetConfigStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

    fun saveWidgetConfig(appWidgetId: Int, slots: List<WidgetSlotConfig>) {
        prefs.edit().apply {
            putInt("slotCount_$appWidgetId", slots.size)
            slots.forEachIndexed { index, slot ->
                putLong("petId_slot${index}_$appWidgetId", slot.petId)
                putString("statMode_slot${index}_$appWidgetId", slot.statMode)
            }
            apply()
        }
    }

    fun getWidgetConfig(appWidgetId: Int): List<WidgetSlotConfig>? {
        // Try new list-based API
        val count = prefs.getInt("slotCount_$appWidgetId", -1)
        if (count > 0) {
            val list = mutableListOf<WidgetSlotConfig>()
            for (i in 0 until count) {
                val petId = prefs.getLong("petId_slot${i}_$appWidgetId", -1L)
                val statMode = prefs.getString("statMode_slot${i}_$appWidgetId", null)
                if (petId != -1L && statMode != null) {
                    list.add(WidgetSlotConfig(petId, statMode))
                }
            }
            if (list.isNotEmpty()) return list
        }

        // Fallback migration: try legacy slot API
        val slot1Id = prefs.getLong("petId_slot1_$appWidgetId", -1L)
        val slot1Mode = prefs.getString("statMode_slot1_$appWidgetId", null)
        val slot2Id = prefs.getLong("petId_slot2_$appWidgetId", -1L)
        val slot2Mode = prefs.getString("statMode_slot2_$appWidgetId", null)
        
        val legacyList = mutableListOf<WidgetSlotConfig>()
        if (slot1Id != -1L && slot1Mode != null) {
            legacyList.add(WidgetSlotConfig(slot1Id, slot1Mode))
        }
        if (slot2Id != -1L && slot2Mode != null) {
            legacyList.add(WidgetSlotConfig(slot2Id, slot2Mode))
        }
        if (legacyList.isNotEmpty()) {
            return legacyList
        }
        
        // Final fallback: original single slot API
        val origId = prefs.getLong("petId_$appWidgetId", -1L)
        val origMode = prefs.getString("statMode_$appWidgetId", null)
        if (origId != -1L && origMode != null) {
            return listOf(WidgetSlotConfig(origId, origMode))
        }

        return null
    }

    fun deleteWidgetConfig(appWidgetId: Int) {
        val count = prefs.getInt("slotCount_$appWidgetId", 0)
        prefs.edit().apply {
            remove("slotCount_$appWidgetId")
            for (i in 0 until Math.max(count, 4)) {
                remove("petId_slot${i}_$appWidgetId")
                remove("statMode_slot${i}_$appWidgetId")
            }
            // Also cleanup legacy slot1/slot2 (1-indexed)
            remove("petId_slot1_$appWidgetId")
            remove("statMode_slot1_$appWidgetId")
            remove("petId_slot2_$appWidgetId")
            remove("statMode_slot2_$appWidgetId")
            // Legacy 2x2 keys
            remove("petId_$appWidgetId")
            remove("statMode_$appWidgetId")
            apply()
        }
    }
}
