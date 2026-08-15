package our.bunny.julie.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import our.bunny.julie.R
import our.bunny.julie.data.local.WidgetConfigStore
import our.bunny.julie.domain.repository.PetRepository
import our.bunny.julie.domain.repository.SettingsRepository
import our.bunny.julie.domain.repository.TrackerRepository
import our.bunny.julie.util.UnitFormatter

/**
 * Hilt EntryPoint to manually retrieve dependencies from the Singleton component.
 * This is the correct approach for AppWidgetProvider (a BroadcastReceiver subclass)
 * because @AndroidEntryPoint on BroadcastReceivers requires the app process to already
 * be running - which is NOT guaranteed when the OS calls onUpdate() after a reboot.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun widgetConfigStore(): WidgetConfigStore
    fun petRepository(): PetRepository
    fun trackerRepository(): TrackerRepository
    fun settingsRepository(): SettingsRepository
}

class PetStatWidget2x2Provider : AppWidgetProvider() {

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
        val config = ep.widgetConfigStore().getWidgetConfig(appWidgetId)
        val views = RemoteViews(context.packageName, R.layout.widget_pet_stat_2x2)

        // Helper to set a tap to open config activity
        fun setConfigIntent() {
            val configIntent = Intent(context, our.bunny.julie.ui.screens.widget.WidgetConfigureActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pi = PendingIntent.getActivity(
                context, appWidgetId, configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pi)
        }

        if (config == null) {
            views.setTextViewText(R.id.widget_pet_emoji, "❓")
            views.setTextViewText(R.id.widget_pet_name, "Not Configured")
            views.setTextViewText(R.id.widget_stat_label, "Setup Needed")
            views.setTextViewText(R.id.widget_stat_value, "--")
            views.setTextViewText(R.id.widget_stat_context, "Tap to configure")
            setConfigIntent()
            appWidgetManager.updateAppWidget(appWidgetId, views)
            return
        }

        val (petId, statMode) = config
        val pet = ep.petRepository().getPetByIdStream(petId).firstOrNull()

        if (pet == null) {
            views.setTextViewText(R.id.widget_pet_emoji, "❌")
            views.setTextViewText(R.id.widget_pet_name, "Pet Not Found")
            views.setTextViewText(R.id.widget_stat_label, "Error")
            views.setTextViewText(R.id.widget_stat_value, "--")
            views.setTextViewText(R.id.widget_stat_context, "Tap to reconfigure")
            setConfigIntent()
            appWidgetManager.updateAppWidget(appWidgetId, views)
            return
        }

        views.setTextViewText(R.id.widget_pet_name, pet.name)
        views.setTextViewText(R.id.widget_pet_emoji, getSpeciesEmoji(pet.species))

        // Auto = Weight placeholder (TODO: implement real auto-selection logic)
        val effectiveMode = if (statMode == "Auto") "Weight" else statMode

        val trackerRepo = ep.trackerRepository()
        val settingsRepo = ep.settingsRepository()

        when (effectiveMode) {
            "Weight" -> {
                views.setTextViewText(R.id.widget_stat_label, if (statMode == "Auto") "Weight (Auto)" else "Weight")
                val latest = trackerRepo.getLatestWeightEntry(petId).firstOrNull()
                val unit = settingsRepo.weightUnitFlow.firstOrNull() ?: our.bunny.julie.util.WeightUnit.KG
                views.setTextViewText(R.id.widget_stat_value, latest?.let { UnitFormatter.formatWeight(it.weight, unit) } ?: "--")
                views.setTextViewText(R.id.widget_stat_context, "Latest")
                setDeepLinkIntent(context, views, appWidgetId, petId, "weight")
            }
            "Water" -> {
                views.setTextViewText(R.id.widget_stat_label, "Water")
                val total = trackerRepo.getTodayWaterTotal(petId).firstOrNull() ?: 0f
                val unit = settingsRepo.waterUnitFlow.firstOrNull() ?: our.bunny.julie.util.WaterUnit.ML
                views.setTextViewText(R.id.widget_stat_value, UnitFormatter.formatWater(total, unit))
                views.setTextViewText(R.id.widget_stat_context, "Today")
                setDeepLinkIntent(context, views, appWidgetId, petId, "water")
            }
            "Feeding" -> {
                views.setTextViewText(R.id.widget_stat_label, "Feeding")
                val latest = trackerRepo.getLatestFeedingLog(petId).firstOrNull()
                views.setTextViewText(R.id.widget_stat_value, latest?.let { "${it.quantity} ${it.unit}" } ?: "--")
                views.setTextViewText(R.id.widget_stat_context, latest?.food ?: "No data")
                setDeepLinkIntent(context, views, appWidgetId, petId, "feeding")
            }
            "Medication" -> {
                views.setTextViewText(R.id.widget_stat_label, "Medication")
                val meds = trackerRepo.getMedicationsForPet(petId).firstOrNull() ?: emptyList()
                val activeCount = meds.count { it.isActive }
                views.setTextViewText(R.id.widget_stat_value, "$activeCount")
                views.setTextViewText(R.id.widget_stat_context, "Active")
                setDeepLinkIntent(context, views, appWidgetId, petId, "medication")
            }
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun setDeepLinkIntent(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        petId: Long,
        trackerType: String
    ) {
        val uri = Uri.parse("julie://pet/$petId/$trackerType")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(
            context, appWidgetId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pi)
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
