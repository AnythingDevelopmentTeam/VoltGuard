package com.example.voltguard

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class BatteryWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                android.content.ComponentName(context, BatteryWidgetProvider::class.java)
            )
            for (id in ids) {
                updateAppWidget(context, manager, id)
            }
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.example.voltguard.UPDATE_WIDGET"

        fun updateWidget(context: Context, info: BatteryInfo) {
            val prefs = context.getSharedPreferences("voltguard_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putInt("widget_battery_level", info.level)
                .putBoolean("widget_battery_plugged", info.status == "Charging" || info.status == "Full")
                .apply()
            val intent = Intent(context, BatteryWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            context.sendBroadcast(intent)
        }
    }
}

private fun updateAppWidget(context: Context, manager: AppWidgetManager, id: Int) {
    val prefs = context.getSharedPreferences("voltguard_prefs", Context.MODE_PRIVATE)
    val level = prefs.getInt("widget_battery_level", 50)
    val isCharging = prefs.getBoolean("widget_battery_plugged", false)

    val views = RemoteViews(context.packageName, R.layout.battery_widget_remote)

    val bgColor = when {
        level >= 80 -> 0xFF1B5E20.toInt()
        level >= 30 -> 0xFFF57F17.toInt()
        else -> 0xFFB71C1C.toInt()
    }
    views.setInt(R.id.widget_root, "setBackgroundColor", bgColor)

    views.setTextViewText(R.id.widget_level, "$level")

    val unitText = if (isCharging) "${context.getString(R.string.widget_percent)} ⚡" else context.getString(R.string.widget_percent)
    views.setTextViewText(R.id.widget_unit, unitText)

    manager.updateAppWidget(id, views)
}
