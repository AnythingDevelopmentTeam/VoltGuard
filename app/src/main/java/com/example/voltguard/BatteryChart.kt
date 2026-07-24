package com.example.voltguard

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

data class BatteryLevelPoint(
    val timestamp: Long,
    val level: Int
)

class BatteryHistoryManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("voltguard_history", Context.MODE_PRIVATE)

    fun addPoint(level: Int) {
        val now = System.currentTimeMillis()
        val points = loadPoints()
        if (points.isNotEmpty()) {
            val last = points.last()
            if (now - last.timestamp < 300_000L && level == last.level) return
        }
        points.add(BatteryLevelPoint(now, level))
        val trimmed = points.takeLast(MAX_POINTS)
        savePoints(trimmed)
    }

    fun getPoints(): List<BatteryLevelPoint> {
        return loadPoints().takeLast(MAX_POINTS)
    }

    private fun loadPoints(): MutableList<BatteryLevelPoint> {
        val str = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
        val array = try { JSONArray(str) } catch (_: Exception) { JSONArray() }
        val list = mutableListOf<BatteryLevelPoint>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(BatteryLevelPoint(
                timestamp = obj.optLong("t"),
                level = obj.optInt("l")
            ))
        }
        return list
    }

    private fun savePoints(points: List<BatteryLevelPoint>) {
        val array = JSONArray()
        for (p in points) {
            array.put(JSONObject().apply {
                put("t", p.timestamp)
                put("l", p.level)
            })
        }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    companion object {
        private const val KEY_HISTORY = "battery_history"
        private const val MAX_POINTS = 288

        @Volatile
        private var instance: BatteryHistoryManager? = null

        fun getInstance(context: Context): BatteryHistoryManager =
            instance ?: synchronized(this) {
                instance ?: BatteryHistoryManager(context.applicationContext).also { instance = it }
            }
    }
}

@Composable
fun BatteryChart(
    points: List<BatteryLevelPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val density = LocalDensity.current

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(start = 24.dp, end = 8.dp, bottom = 16.dp)
    ) {
        val textPaint = android.graphics.Paint().apply {
            color = labelColor.hashCode()
            textSize = with(density) { 10.dp.toPx() }
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val levelPaint = android.graphics.Paint().apply {
            color = labelColor.copy(alpha = 0.6f).hashCode()
            textSize = with(density) { 9.dp.toPx() }
            textAlign = android.graphics.Paint.Align.RIGHT
        }

        val minLevel = 0f
        val maxLevel = 100f
        val chartWidth = size.width
        val chartHeight = size.height
        val paddingTop = 4f
        val drawHeight = chartHeight - paddingTop

        val timeStart = points.first().timestamp
        val timeEnd = points.last().timestamp
        val timeRange = (timeEnd - timeStart).coerceAtLeast(1L).toFloat()

        // horizontal grid lines + level labels
        val gridLevels = listOf(0f, 25f, 50f, 75f, 100f)
        for (level in gridLevels) {
            val y = paddingTop + drawHeight * (1f - (level - minLevel) / (maxLevel - minLevel))
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(chartWidth, y),
                strokeWidth = 1f
            )
            drawContext.canvas.nativeCanvas.drawText(
                "${level.toInt()}",
                -4f, y + 4f, levelPaint
            )
        }

        // time labels
        val intervalHours = if (timeRange > 24 * 3600_000L) 6 else 3
        val intervalMs = intervalHours * 3600_000L
        val firstHour = (timeStart / intervalMs) * intervalMs
        var labelTime = firstHour + intervalMs
        while (labelTime < timeEnd) {
            val x = ((labelTime - timeStart) / timeRange) * chartWidth
            drawLine(
                color = gridColor.copy(alpha = 0.15f),
                start = Offset(x, paddingTop),
                end = Offset(x, chartHeight),
                strokeWidth = 1f
            )
            val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            drawContext.canvas.nativeCanvas.drawText(
                sdf.format(java.util.Date(labelTime)),
                x, chartHeight + with(density) { 12.dp.toPx() }, textPaint
            )
            labelTime += intervalMs
        }

        // line path
        if (points.size >= 2) {
            val path = Path()
            points.forEachIndexed { index, point ->
                val x = ((point.timestamp - timeStart) / timeRange) * chartWidth
                val y = paddingTop + drawHeight * (1f - (point.level - minLevel) / (maxLevel - minLevel))
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(
                    width = 3f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            val last = points.last()
            val lastX = ((last.timestamp - timeStart) / timeRange) * chartWidth
            val lastY = paddingTop + drawHeight * (1f - (last.level - minLevel) / (maxLevel - minLevel))
            drawCircle(
                color = lineColor,
                radius = 6f,
                center = Offset(lastX, lastY)
            )
        }
    }
}
