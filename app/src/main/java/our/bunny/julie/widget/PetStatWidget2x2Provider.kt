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
        WidgetUIBuilder.buildWidgetUI(context, appWidgetManager, appWidgetId, is4x2 = false, ep)
    }
}
