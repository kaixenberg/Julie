package our.bunny.julie.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.widget.RemoteViews
import kotlinx.coroutines.flow.firstOrNull
import our.bunny.julie.R
import our.bunny.julie.data.local.WidgetSlotConfig
import our.bunny.julie.domain.model.Pet
import our.bunny.julie.domain.model.ThemeConfig
import our.bunny.julie.ui.theme.PaletteStyle
import our.bunny.julie.ui.theme.buildColorScheme
import our.bunny.julie.util.UnitFormatter
import our.bunny.julie.util.WaterUnit
import our.bunny.julie.util.WeightUnit
import androidx.compose.ui.graphics.toArgb
import android.graphics.Color

object WidgetUIBuilder {

    suspend fun buildWidgetUI(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        is4x2: Boolean,
        ep: WidgetEntryPoint
    ) {
        val store = ep.widgetConfigStore()
        val configs = store.getWidgetConfig(appWidgetId)
        
        // Base layout is just an empty LinearLayout with a rounded background
        val rootLayoutId = if (is4x2) R.layout.widget_pet_stat_4x2 else R.layout.widget_pet_stat_2x2
        val views = RemoteViews(context.packageName, rootLayoutId)
        
        // Setup configuration click intent on the root (fallback if empty)
        val configIntent = Intent(context, our.bunny.julie.ui.screens.widget.WidgetConfigureActivity::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(
            context, appWidgetId, configIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pi)
        
        // Clear previous dynamic views
        views.removeAllViews(R.id.widget_root)

        // Build Theme Colors
        val settingsRepo = ep.settingsRepository()
        val themeConfig = settingsRepo.themeConfigFlow.firstOrNull() ?: ThemeConfig.SYSTEM
        val isSystemDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val isDark = when (themeConfig) {
            ThemeConfig.LIGHT -> false
            ThemeConfig.DARK -> true
            ThemeConfig.SYSTEM -> isSystemDark
        }
        val dynamicColor = settingsRepo.dynamicColorFlow.firstOrNull() ?: false
        val paletteStyle = settingsRepo.paletteStyleFlow.firstOrNull() ?: PaletteStyle.Julie
        val oledBlack = settingsRepo.oledBlackFlow.firstOrNull() ?: false
        val useSystemFont = settingsRepo.useSystemFontFlow.firstOrNull() ?: false

        // Determine seed and effective style
        val seedColorArgb = if (dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            runCatching {
                val wm = android.app.WallpaperManager.getInstance(context)
                wm.getWallpaperColors(android.app.WallpaperManager.FLAG_SYSTEM)?.primaryColor?.toArgb() ?: 0xFF6750A4.toInt()
            }.getOrElse { 0xFF6750A4.toInt() }
        } else {
            0xFF6750A4.toInt()
        }

        val effectiveStyle = if (dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            PaletteStyle.TonalSpot
        } else {
            paletteStyle
        }

        val colorScheme = buildColorScheme(
            seedColorArgb = seedColorArgb,
            isDark = isDark,
            style = effectiveStyle
        )
        val bgColor = if (isDark && oledBlack) Color.BLACK else colorScheme.surface.toArgb()
        val primaryTextColor = colorScheme.onSurface.toArgb()
        val secondaryTextColor = colorScheme.onSurfaceVariant.toArgb()
        val dividerColor = colorScheme.outlineVariant.toArgb()

        // Apply background color to root
        views.setInt(R.id.widget_root, "setBackgroundColor", bgColor)

        val emptyLayoutId = if (useSystemFont) R.layout.widget_stat_cell_sysfont else R.layout.widget_stat_cell
        if (configs == null || configs.isEmpty()) {
            val emptyView = RemoteViews(context.packageName, emptyLayoutId)
            setWidgetText(context, emptyView, R.id.widget_stat_label, "Setup Needed", false, useSystemFont)
            setWidgetText(context, emptyView, R.id.widget_stat_value, "--", true, useSystemFont)
            setWidgetText(context, emptyView, R.id.widget_stat_context, "Tap to configure", false, useSystemFont)
            emptyView.setTextColor(R.id.widget_stat_label, secondaryTextColor)
            emptyView.setTextColor(R.id.widget_stat_value, primaryTextColor)
            emptyView.setTextColor(R.id.widget_stat_context, secondaryTextColor)
            views.addView(R.id.widget_root, emptyView)
            appWidgetManager.updateAppWidget(appWidgetId, views)
            return
        }

        // Group configs by petId to deduplicate headers
        val petsById = mutableMapOf<Long, Pet?>()
        val groups = configs.groupBy { it.petId }

        for (petId in groups.keys) {
            val pet = ep.petRepository().getPetByIdStream(petId).firstOrNull()
            petsById[petId] = pet
        }

        // We use a master layout to arrange the groups
        val masterContainer = RemoteViews(context.packageName, R.layout.widget_container_linear)
        
        var isFirstGroup = true
        for ((petId, petConfigs) in groups) {
            if (!isFirstGroup) {
                // Add horizontal divider between different pet groups if stacking vertically
                val divider = RemoteViews(context.packageName, R.layout.widget_horizontal_divider)
                divider.setInt(R.id.widget_divider, "setBackgroundColor", dividerColor)
                masterContainer.addView(R.id.widget_container_root, divider)
            }
            isFirstGroup = false

            val pet = petsById[petId]
            
            // Add Pet Header
            val headerLayoutId = if (useSystemFont) R.layout.widget_pet_header_sysfont else R.layout.widget_pet_header
            val headerView = RemoteViews(context.packageName, headerLayoutId)
            if (pet != null) {
                setWidgetText(context, headerView, R.id.widget_pet_header_emoji, getSpeciesEmoji(pet.species), false, useSystemFont)
                setWidgetText(context, headerView, R.id.widget_pet_header_name, pet.name, true, useSystemFont)
            } else {
                setWidgetText(context, headerView, R.id.widget_pet_header_emoji, "❌", false, useSystemFont)
                setWidgetText(context, headerView, R.id.widget_pet_header_name, "Pet Not Found", true, useSystemFont)
            }
            headerView.setTextColor(R.id.widget_pet_header_name, primaryTextColor)
            masterContainer.addView(R.id.widget_container_root, headerView)

            // Render stats for this pet
            if (is4x2) {
                val chunkedConfigs = petConfigs.chunked(2)
                chunkedConfigs.forEachIndexed { rowIndex, rowConfigs ->
                    if (rowIndex > 0) {
                        val hDivider = RemoteViews(context.packageName, R.layout.widget_horizontal_divider)
                        hDivider.setInt(R.id.widget_divider, "setBackgroundColor", dividerColor)
                        masterContainer.addView(R.id.widget_container_root, hDivider)
                    }
                    
                    val rowView = RemoteViews(context.packageName, R.layout.widget_container_horizontal)
                    rowConfigs.forEachIndexed { index, config ->
                        if (index > 0) {
                            val vDivider = RemoteViews(context.packageName, R.layout.widget_vertical_divider)
                            vDivider.setInt(R.id.widget_divider, "setBackgroundColor", dividerColor)
                            rowView.addView(R.id.widget_container_root, vDivider)
                        }
                        
                        val cellView = buildStatCell(context, ep, config, pet, appWidgetId, index, primaryTextColor, secondaryTextColor, useSystemFont)
                        val weightContainer = RemoteViews(context.packageName, R.layout.widget_container_weight)
                        weightContainer.addView(R.id.widget_container_root, cellView)
                        rowView.addView(R.id.widget_container_root, weightContainer)
                    }
                    masterContainer.addView(R.id.widget_container_root, rowView)
                }
            } else {
                // Vertical stack for 2x2
                petConfigs.forEachIndexed { index, config ->
                    if (index > 0) {
                        val hDivider = RemoteViews(context.packageName, R.layout.widget_horizontal_divider)
                        hDivider.setInt(R.id.widget_divider, "setBackgroundColor", dividerColor)
                        masterContainer.addView(R.id.widget_container_root, hDivider)
                    }
                    val cellView = buildStatCell(context, ep, config, pet, appWidgetId, index, primaryTextColor, secondaryTextColor, useSystemFont)
                    masterContainer.addView(R.id.widget_container_root, cellView)
                }
            }
        }

        views.addView(R.id.widget_root, masterContainer)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private suspend fun buildStatCell(
        context: Context,
        ep: WidgetEntryPoint,
        config: WidgetSlotConfig,
        pet: Pet?,
        appWidgetId: Int,
        slotIndex: Int,
        primaryColor: Int,
        secondaryColor: Int,
        useSystemFont: Boolean
    ): RemoteViews {
        val cellLayoutId = if (useSystemFont) R.layout.widget_stat_cell_sysfont else R.layout.widget_stat_cell
        val views = RemoteViews(context.packageName, cellLayoutId)
        
        views.setTextColor(R.id.widget_stat_label, secondaryColor)
        views.setTextColor(R.id.widget_stat_value, primaryColor)
        views.setTextColor(R.id.widget_stat_context, secondaryColor)

        if (pet == null) {
            setWidgetText(context, views, R.id.widget_stat_label, "Error", false, useSystemFont)
            setWidgetText(context, views, R.id.widget_stat_value, "--", true, useSystemFont)
            setWidgetText(context, views, R.id.widget_stat_context, "Tap to reconfigure", false, useSystemFont)
            return views
        }

        val effectiveMode = if (config.statMode == "Auto") "Weight" else config.statMode
        val trackerRepo = ep.trackerRepository()
        val settingsRepo = ep.settingsRepository()

        when (effectiveMode) {
            "Weight" -> {
                setWidgetText(context, views, R.id.widget_stat_label, if (config.statMode == "Auto") "Weight (Auto)" else "Weight", false, useSystemFont)
                val latest = trackerRepo.getLatestWeightEntry(pet.id).firstOrNull()
                val unit = settingsRepo.weightUnitFlow.firstOrNull() ?: WeightUnit.KG
                setWidgetText(context, views, R.id.widget_stat_value, latest?.let { UnitFormatter.formatWeight(it.weight, unit) } ?: "--", true, useSystemFont)
                setWidgetText(context, views, R.id.widget_stat_context, "Latest", false, useSystemFont)
                setDeepLinkIntent(context, views, appWidgetId, slotIndex, pet.id, "weight")
            }
            "Water" -> {
                setWidgetText(context, views, R.id.widget_stat_label, "Water", false, useSystemFont)
                val total = trackerRepo.getTodayWaterTotal(pet.id).firstOrNull() ?: 0f
                val unit = settingsRepo.waterUnitFlow.firstOrNull() ?: WaterUnit.ML
                setWidgetText(context, views, R.id.widget_stat_value, UnitFormatter.formatWater(total, unit), true, useSystemFont)
                setWidgetText(context, views, R.id.widget_stat_context, "Today", false, useSystemFont)
                setDeepLinkIntent(context, views, appWidgetId, slotIndex, pet.id, "water")
            }
            "Feeding" -> {
                setWidgetText(context, views, R.id.widget_stat_label, "Feeding", false, useSystemFont)
                val latest = trackerRepo.getLatestFeedingLog(pet.id).firstOrNull()
                setWidgetText(context, views, R.id.widget_stat_value, latest?.let { "${it.quantity} ${it.unit}" } ?: "--", true, useSystemFont)
                setWidgetText(context, views, R.id.widget_stat_context, latest?.food ?: "No data", false, useSystemFont)
                setDeepLinkIntent(context, views, appWidgetId, slotIndex, pet.id, "feeding")
            }
            "Medication" -> {
                setWidgetText(context, views, R.id.widget_stat_label, "Medication", false, useSystemFont)
                val meds = trackerRepo.getMedicationsForPet(pet.id).firstOrNull() ?: emptyList()
                setWidgetText(context, views, R.id.widget_stat_value, "${meds.count { it.isActive }}", true, useSystemFont)
                setWidgetText(context, views, R.id.widget_stat_context, "Active", false, useSystemFont)
                setDeepLinkIntent(context, views, appWidgetId, slotIndex, pet.id, "medication")
            }
        }
        return views
    }

    private fun setWidgetText(
        context: Context,
        views: RemoteViews,
        viewId: Int,
        text: String,
        isBold: Boolean,
        useSystemFont: Boolean
    ) {
        if (useSystemFont) {
            views.setTextViewText(viewId, text)
        } else {
            val spannable = android.text.SpannableString(text)
            val styleId = if (isBold) R.style.WidgetTextBold else R.style.WidgetTextNormal
            spannable.setSpan(
                android.text.style.TextAppearanceSpan(context, styleId),
                0,
                text.length,
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            views.setTextViewText(viewId, spannable)
        }
    }

    private fun setDeepLinkIntent(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        slotIndex: Int,
        petId: Long,
        trackerType: String
    ) {
        val uri = Uri.parse("julie://pet/$petId/$trackerType")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(
            context, appWidgetId * 10 + slotIndex, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_stat_cell_root, pi)
    }

    private fun getSpeciesEmoji(species: String) = when (species.lowercase()) {
        "rabbit" -> "🐰"; "dog" -> "🐶"; "cat" -> "🐱"; "bird" -> "🐦"
        "guinea pig", "hamster" -> "🐹"; "mouse" -> "🐭"; "rat" -> "🐀"
        "reptile" -> "🦎"; "fish" -> "🐟"
        else -> "🐾"
    }
}
