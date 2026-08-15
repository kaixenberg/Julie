package our.bunny.julie.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import our.bunny.julie.R
import our.bunny.julie.data.local.WidgetSlotConfig
import our.bunny.julie.domain.model.Pet
import our.bunny.julie.util.WeightUnit
import our.bunny.julie.util.WaterUnit
import our.bunny.julie.util.UnitFormatter

class PetStatWidget4x2Provider : AppWidgetProvider() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun entryPoint(context: Context): WidgetEntryPoint =
        EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        scope.launch {
            try {
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val store = entryPoint(context).widgetConfigStore()
        for (appWidgetId in appWidgetIds) {
            store.deleteWidgetConfig(appWidgetId)
        }
    }

    private suspend fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val ep = entryPoint(context)
        val store = ep.widgetConfigStore()
        val views = RemoteViews(context.packageName, R.layout.widget_pet_stat_4x2)

        val slot1 = store.getSlotConfig(appWidgetId, 1)
        val slot2 = store.getSlotConfig(appWidgetId, 2)

        // Populate each cell independently — a missing pet in one slot doesn't break the other
        populateCell(context, ep, views, appWidgetId, slot = 1, config = slot1)
        populateCell(context, ep, views, appWidgetId, slot = 2, config = slot2)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private suspend fun populateCell(
        context: Context,
        ep: WidgetEntryPoint,
        views: RemoteViews,
        appWidgetId: Int,
        slot: Int,
        config: WidgetSlotConfig?
    ) {
        val emojiId = if (slot == 1) R.id.widget4x2_cell1_emoji else R.id.widget4x2_cell2_emoji
        val nameId  = if (slot == 1) R.id.widget4x2_cell1_name  else R.id.widget4x2_cell2_name
        val labelId = if (slot == 1) R.id.widget4x2_cell1_label else R.id.widget4x2_cell2_label
        val valueId = if (slot == 1) R.id.widget4x2_cell1_value else R.id.widget4x2_cell2_value
        val ctxId   = if (slot == 1) R.id.widget4x2_cell1_context else R.id.widget4x2_cell2_context
        val cellId  = if (slot == 1) R.id.widget4x2_cell1 else R.id.widget4x2_cell2

        fun setConfigTap() {
            val configIntent = Intent(context, our.bunny.julie.ui.screens.widget.WidgetConfigureActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pi = PendingIntent.getActivity(
                context, appWidgetId * 10 + slot, configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(cellId, pi)
        }

        if (config == null) {
            views.setTextViewText(emojiId, "❓")
            views.setTextViewText(nameId, "Slot $slot")
            views.setTextViewText(labelId, "Not set")
            views.setTextViewText(valueId, "--")
            views.setTextViewText(ctxId, "Tap to configure")
            setConfigTap()
            return
        }

        val pet = ep.petRepository().getPetByIdStream(config.petId).firstOrNull()
        if (pet == null) {
            views.setTextViewText(emojiId, "❌")
            views.setTextViewText(nameId, "Pet not found")
            views.setTextViewText(labelId, "Error")
            views.setTextViewText(valueId, "--")
            views.setTextViewText(ctxId, "Tap to reconfigure")
            setConfigTap()
            return
        }

        views.setTextViewText(emojiId, getSpeciesEmoji(pet.species))
        views.setTextViewText(nameId, pet.name)

        val trackerRepo = ep.trackerRepository()
        val settingsRepo = ep.settingsRepository()
        val effectiveMode = if (config.statMode == "Auto") "Weight" else config.statMode

        when (effectiveMode) {
            "Weight" -> {
                views.setTextViewText(labelId, if (config.statMode == "Auto") "Weight (Auto)" else "Weight")
                val latest = trackerRepo.getLatestWeightEntry(config.petId).firstOrNull()
                val unit = settingsRepo.weightUnitFlow.firstOrNull() ?: WeightUnit.KG
                views.setTextViewText(valueId, latest?.let { UnitFormatter.formatWeight(it.weight, unit) } ?: "--")
                views.setTextViewText(ctxId, "Latest")
                setDeepLinkTap(context, views, cellId, appWidgetId, slot, config.petId, "weight")
            }
            "Water" -> {
                views.setTextViewText(labelId, "Water")
                val total = trackerRepo.getTodayWaterTotal(config.petId).firstOrNull() ?: 0f
                val unit = settingsRepo.waterUnitFlow.firstOrNull() ?: WaterUnit.ML
                views.setTextViewText(valueId, UnitFormatter.formatWater(total, unit))
                views.setTextViewText(ctxId, "Today")
                setDeepLinkTap(context, views, cellId, appWidgetId, slot, config.petId, "water")
            }
            "Feeding" -> {
                views.setTextViewText(labelId, "Feeding")
                val latest = trackerRepo.getLatestFeedingLog(config.petId).firstOrNull()
                views.setTextViewText(valueId, latest?.let { "${it.quantity} ${it.unit}" } ?: "--")
                views.setTextViewText(ctxId, latest?.food ?: "No data")
                setDeepLinkTap(context, views, cellId, appWidgetId, slot, config.petId, "feeding")
            }
            "Medication" -> {
                views.setTextViewText(labelId, "Medication")
                val meds = trackerRepo.getMedicationsForPet(config.petId).firstOrNull() ?: emptyList()
                views.setTextViewText(valueId, "${meds.count { it.isActive }}")
                views.setTextViewText(ctxId, "Active")
                setDeepLinkTap(context, views, cellId, appWidgetId, slot, config.petId, "medication")
            }
        }
    }

    private fun setDeepLinkTap(
        context: Context,
        views: RemoteViews,
        viewId: Int,
        appWidgetId: Int,
        slot: Int,
        petId: Long,
        trackerType: String
    ) {
        val uri = Uri.parse("julie://pet/$petId/$trackerType")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        // Use appWidgetId * 10 + slot as request code to ensure unique PendingIntents per cell
        val pi = PendingIntent.getActivity(
            context, appWidgetId * 10 + slot, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(viewId, pi)
    }

    private fun getSpeciesEmoji(species: String) = when (species.lowercase()) {
        "rabbit" -> "🐰"
        "dog" -> "🐶"
        "cat" -> "🐱"
        "bird" -> "🐦"
        "guinea pig", "hamster" -> "🐹"
        "mouse" -> "🐭"
        "rat" -> "🐀"
        "reptile" -> "🦎"
        "fish" -> "🐟"
        else -> "🐾"
    }
}
