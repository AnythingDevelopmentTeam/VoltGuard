package com.example.voltguard

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
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
                ComponentName(context, BatteryWidgetProvider::class.java)
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
                .putString("widget_battery_status", info.status)
                .putFloat("widget_battery_temp", info.temperature)
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
    val status = prefs.getString("widget_battery_status", "Discharging") ?: "Discharging"
    val temperature = prefs.getFloat("widget_battery_temp", 0f)
    val showStatus = prefs.getBoolean("widget_status", true)
    val showTemp = prefs.getBoolean("widget_temp", true)
    val colorSetting = prefs.getString("widget_color", "auto") ?: "auto"

    val (bgColor, textColor) = widgetColors(colorSetting, level)

    val views = RemoteViews(context.packageName, R.layout.battery_widget_remote)
    views.setInt(R.id.widget_root, "setBackgroundColor", bgColor)
    views.setTextColor(R.id.widget_level, textColor)
    views.setTextColor(R.id.widget_unit, textColor)

    views.setTextViewText(R.id.widget_level, "$level")

    val unitText = if (prefs.getBoolean("widget_battery_plugged", false)) {
        "${context.getString(R.string.widget_percent)} ⚡"
    } else {
        context.getString(R.string.widget_percent)
    }
    views.setTextViewText(R.id.widget_unit, unitText)

    val statusText = buildWidgetStatusText(context, status, temperature, showStatus, showTemp)
    views.setTextViewText(R.id.widget_status, statusText)
    views.setViewVisibility(
        R.id.widget_status,
        if (statusText.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
    )
    views.setInt(
        R.id.widget_status,
        "setBackgroundColor",
        bgColor or 0x33000000.toInt()
    )

    manager.updateAppWidget(id, views)
}

fun buildWidgetStatusText(
    context: Context,
    status: String,
    temperature: Float,
    showStatus: Boolean,
    showTemp: Boolean
): String {
    val statusLabel = when {
        !showStatus -> null
        status == "Full" -> context.getString(R.string.full)
        status == "Charging" -> context.getString(R.string.charging)
        else -> context.getString(R.string.discharging)
    }
    val tempLabel = if (showTemp) "${temperature.toInt()}°" else null
    return listOfNotNull(statusLabel, tempLabel).joinToString(" · ")
}

data class WidgetColors(val background: Int, val text: Int)

fun widgetColors(colorSetting: String, level: Int): WidgetColors {
    val lightText = AndroidColor.WHITE
    val darkText = 0xFF1F1F1F.toInt()
    return when (colorSetting) {
        "green" -> WidgetColors(0xFF1B5E20.toInt(), lightText)
        "blue" -> WidgetColors(0xFF0D47A1.toInt(), lightText)
        "dark" -> WidgetColors(0xFF212121.toInt(), lightText)
        "white" -> WidgetColors(AndroidColor.WHITE, darkText)
        "purple" -> WidgetColors(0xFF4A148C.toInt(), lightText)
        "red" -> WidgetColors(0xFFB71C1C.toInt(), lightText)
        "teal" -> WidgetColors(0xFF00695C.toInt(), lightText)
        else -> when {
            level >= 80 -> WidgetColors(0xFF1B5E20.toInt(), lightText)
            level >= 30 -> WidgetColors(0xFFF57F17.toInt(), lightText)
            else -> WidgetColors(0xFFB71C1C.toInt(), lightText)
        }
    }
}