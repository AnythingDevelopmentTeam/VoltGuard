package com.example.voltguard

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class BatteryWidgetProviderSmall : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateSmallWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET_SMALL) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, BatteryWidgetProviderSmall::class.java)
            )
            for (id in ids) {
                updateSmallWidget(context, manager, id)
            }
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET_SMALL = "com.example.voltguard.UPDATE_WIDGET_SMALL"

        fun updateWidget(context: Context, info: BatteryInfo) {
            val prefs = context.getSharedPreferences("voltguard_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putInt("widget_small_level", info.level)
                .putString("widget_small_status", info.status)
                .putBoolean("widget_small_plugged", info.status == "Charging" || info.status == "Full")
                .apply()
            val intent = Intent(context, BatteryWidgetProviderSmall::class.java).apply {
                action = ACTION_UPDATE_WIDGET_SMALL
            }
            context.sendBroadcast(intent)
        }
    }
}

private fun updateSmallWidget(context: Context, manager: AppWidgetManager, id: Int) {
    val prefs = context.getSharedPreferences("voltguard_prefs", Context.MODE_PRIVATE)
    val level = prefs.getInt("widget_small_level", 50)
    val isCharging = prefs.getBoolean("widget_small_plugged", false)
    val colorSetting = prefs.getString("widget_color", "auto") ?: "auto"

    val (bgColor, textColor) = widgetColors(colorSetting, level)

    val views = RemoteViews(context.packageName, R.layout.battery_widget_small)
    views.setInt(R.id.widget_root, "setBackgroundColor", bgColor)
    views.setTextColor(R.id.widget_level, textColor)
    views.setTextColor(R.id.widget_unit, textColor)

    views.setTextViewText(R.id.widget_level, "$level")

    val unitText = if (isCharging) {
        "${context.getString(R.string.widget_percent)} ⚡"
    } else {
        context.getString(R.string.widget_percent)
    }
    views.setTextViewText(R.id.widget_unit, unitText)

    manager.updateAppWidget(id, views)
}