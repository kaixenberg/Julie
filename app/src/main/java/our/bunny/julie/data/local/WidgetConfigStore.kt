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

    // ── Slot-based API (used by both 2x2 and 4x2) ────────────────────────────

    fun saveSlotConfig(appWidgetId: Int, slot: Int, petId: Long, statMode: String) {
        prefs.edit().apply {
            putLong("petId_slot${slot}_$appWidgetId", petId)
            putString("statMode_slot${slot}_$appWidgetId", statMode)
            apply()
        }
    }

    fun getSlotConfig(appWidgetId: Int, slot: Int): WidgetSlotConfig? {
        val petId = prefs.getLong("petId_slot${slot}_$appWidgetId", -1L)
        val statMode = prefs.getString("statMode_slot${slot}_$appWidgetId", null)
        if (petId == -1L || statMode == null) return null
        return WidgetSlotConfig(petId, statMode)
    }

    fun deleteWidgetConfig(appWidgetId: Int) {
        prefs.edit().apply {
            // Remove both slot keys and legacy single-slot keys
            remove("petId_slot1_$appWidgetId")
            remove("statMode_slot1_$appWidgetId")
            remove("petId_slot2_$appWidgetId")
            remove("statMode_slot2_$appWidgetId")
            // Legacy 2x2 keys (before slot migration)
            remove("petId_$appWidgetId")
            remove("statMode_$appWidgetId")
            apply()
        }
    }

    // ── Legacy single-slot shims (2x2 backward compat) ───────────────────────
    // Reads old key first, falls back to slot1. Writes to slot1.

    fun saveWidgetConfig(appWidgetId: Int, petId: Long, statMode: String) =
        saveSlotConfig(appWidgetId, 1, petId, statMode)

    fun getWidgetConfig(appWidgetId: Int): Pair<Long, String>? {
        // Try new slot1 key first, fall back to legacy key
        val slotCfg = getSlotConfig(appWidgetId, 1)
        if (slotCfg != null) return Pair(slotCfg.petId, slotCfg.statMode)
        val legacyPetId = prefs.getLong("petId_$appWidgetId", -1L)
        val legacyMode = prefs.getString("statMode_$appWidgetId", null)
        if (legacyPetId == -1L || legacyMode == null) return null
        return Pair(legacyPetId, legacyMode)
    }
}
