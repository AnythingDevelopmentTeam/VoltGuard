package com.example.voltguard

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class BatteryWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        val context = LocalContext.current
        val prefs = context.getSharedPreferences("voltguard_prefs", Context.MODE_PRIVATE)
        val level = prefs.getInt("widget_battery_level", 50)
        val isCharging = prefs.getBoolean("widget_battery_plugged", false)

        val bgColor = when {
            level >= 80 -> ColorProvider(0xFF4CAF50.toInt())
            level >= 30 -> ColorProvider(0xFFFFC107.toInt())
            else -> ColorProvider(0xFFF44336.toInt())
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(bgColor),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$level",
                style = TextStyle(
                    color = ColorProvider(android.graphics.Color.WHITE),
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = if (isCharging) "% ⚡" else "%",
                style = TextStyle(
                    color = ColorProvider(android.graphics.Color.argb(204, 255, 255, 255)),
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }

    companion object {
        suspend fun updateWidget(context: Context, info: BatteryInfo) {
            val prefs = context.getSharedPreferences("voltguard_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putInt("widget_battery_level", info.level)
                .putBoolean("widget_battery_plugged", info.status == "Charging" || info.status == "Full")
                .apply()
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(BatteryWidget::class.java)
            glanceIds.forEach { BatteryWidget().update(context, it) }
        }
    }
}

class BatteryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = BatteryWidget()
}
