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
        WidgetUIBuilder.buildWidgetUI(context, appWidgetManager, appWidgetId, is4x2 = true, ep)
    }
}
