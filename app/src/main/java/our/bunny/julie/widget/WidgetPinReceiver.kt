package our.bunny.julie.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import our.bunny.julie.data.local.WidgetConfigStore
import our.bunny.julie.data.local.WidgetSlotConfig
import javax.inject.Inject

@AndroidEntryPoint
class WidgetPinReceiver : BroadcastReceiver() {

    @Inject
    lateinit var widgetConfigStore: WidgetConfigStore

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == "our.bunny.julie.ACTION_WIDGET_PINNED") {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val petId = intent.getLongExtra("EXTRA_PET_ID", -1L)
            val statMode = intent.getStringExtra("EXTRA_STAT_MODE")

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && petId != -1L && statMode != null) {
                Log.d("WidgetPinReceiver", "Widget $appWidgetId pinned successfully. Configuring for pet $petId, stat $statMode")
                
                // Save the configuration for the new widget
                val slots = listOf(WidgetSlotConfig(petId, statMode))
                widgetConfigStore.saveWidgetConfig(appWidgetId, slots)

                // Force an update to render the newly pinned and configured widget
                val providerClassName = intent.getStringExtra("EXTRA_PROVIDER_CLASS") ?: "our.bunny.julie.widget.PetStatWidget2x2Provider"
                val clazz = Class.forName(providerClassName)
                
                val updateIntent = Intent(context, clazz).apply {
                    this.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                }
                context.sendBroadcast(updateIntent)
            }
        }
    }
}
