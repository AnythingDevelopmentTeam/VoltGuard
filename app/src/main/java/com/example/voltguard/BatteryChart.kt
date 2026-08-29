package com.example.voltguard

import android.content.Context
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
import com.example.voltguard.data.AppDatabase
import com.example.voltguard.data.HistoryDao
import com.example.voltguard.data.LevelPointEntity
import com.example.voltguard.data.TempPointEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

data class BatteryLevelPoint(
    val timestamp: Long,
    val level: Int
)

data class TempPoint(
    val timestamp: Long,
    val temperature: Float
)

class BatteryHistoryManager(context: Context) {

    private val legacyContext: Context = context.applicationContext
    private val appContext: Context = context.applicationContext
    private val db: AppDatabase = AppDatabase.getInstance(appContext)
    private val historyDao: HistoryDao = db.historyDao()

    init {
        migrateLegacyIfNeeded()
    }

    fun addPoint(level: Int) {
        val now = System.currentTimeMillis()
        val points = getPoints()
        if (points.isNotEmpty()) {
            val last = points.last()
            if (now - last.timestamp < 300_000L && level == last.level) return
        }
        runBlocking(Dispatchers.IO) {
            historyDao.insertAllLevels(listOf(LevelPointEntity(now, level)))
            historyDao.trimLevels(MAX_POINTS)
        }
    }

    fun getPoints(): List<BatteryLevelPoint> =
        runBlocking(Dispatchers.IO) {
            historyDao.getLevelPoints().map { BatteryLevelPoint(it.timestamp, it.level) }
        }

    fun addTempPoint(temperature: Float) {
        val now = System.currentTimeMillis()
        val points = getTempPoints()
        if (points.isNotEmpty()) {
            val last = points.last()
            if (now - last.timestamp < 300_000L && kotlin.math.abs(temperature - last.temperature) < 2f) return
        }
        runBlocking(Dispatchers.IO) {
            historyDao.insertAllTemps(listOf(TempPointEntity(now, temperature)))
            historyDao.trimTemps(MAX_POINTS)
        }
    }

    fun getTempPoints(): List<TempPoint> =
        runBlocking(Dispatchers.IO) {
            historyDao.getTempPoints().map { TempPoint(it.timestamp, it.temperature) }
        }

    private fun migrateLegacyIfNeeded() {
        if (runBlocking(Dispatchers.IO) { historyDao.levelCount() } > 0 ||
            runBlocking(Dispatchers.IO) { historyDao.tempCount() } > 0) return

        val legacyLevels = loadLegacyLevels()
        val legacyTemps = loadLegacyTemps()
        if (legacyLevels.isEmpty() && legacyTemps.isEmpty()) return

        runBlocking(Dispatchers.IO) {
            if (legacyLevels.isNotEmpty()) historyDao.insertAllLevels(legacyLevels)
            if (legacyTemps.isNotEmpty()) historyDao.insertAllTemps(legacyTemps)
        }
    }

    private fun loadLegacyLevels(): List<LevelPointEntity> {
        val str = legacyPrefs.getString(KEY_HISTORY, "[]") ?: "[]"
        val array = try { org.json.JSONArray(str) } catch (_: Exception) { org.json.JSONArray() }
        val list = mutableListOf<LevelPointEntity>()
        for (i in 0 until array.length()) {
            val obj = try { array.getJSONObject(i) } catch (_: Exception) { continue }
            list.add(LevelPointEntity(
                timestamp = obj.optLong("t"),
                level = obj.optInt("l")
            ))
        }
        return list
    }

    private fun loadLegacyTemps(): List<TempPointEntity> {
        val str = legacyPrefs.getString(KEY_TEMP_HISTORY, "[]") ?: "[]"
        val array = try { org.json.JSONArray(str) } catch (_: Exception) { org.json.JSONArray() }
        val list = mutableListOf<TempPointEntity>()
        for (i in 0 until array.length()) {
            val obj = try { array.getJSONObject(i) } catch (_: Exception) { continue }
            list.add(TempPointEntity(
                timestamp = obj.optLong("t"),
                temperature = obj.optDouble("c", 0.0).toFloat()
            ))
        }
        return list
    }

    private val legacyPrefs: android.content.SharedPreferences
        get() = legacyContext.getSharedPreferences("voltguard_history", android.content.Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HISTORY = "battery_history"
        private const val KEY_TEMP_HISTORY = "temp_history"
        private const val MAX_POINTS = 288
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

@Composable
fun BatteryTempChart(
    points: List<TempPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val lineColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val density = LocalDensity.current

    val minTemp = (points.minOf { it.temperature } - 2f).coerceAtMost(20f).toInt()
    val maxTemp = (points.maxOf { it.temperature } + 2f).coerceAtLeast(50f).toInt()
    val range = (maxTemp - minTemp).coerceAtLeast(1)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
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

        val chartWidth = size.width
        val chartHeight = size.height
        val paddingTop = 4f
        val drawHeight = chartHeight - paddingTop

        val timeStart = points.first().timestamp
        val timeEnd = points.last().timestamp
        val timeRange = (timeEnd - timeStart).coerceAtLeast(1L).toFloat()

        val gridTemps = listOf(minTemp, minTemp + range / 2, maxTemp)
        for (t in gridTemps) {
            val y = paddingTop + drawHeight * (1f - (t - minTemp).toFloat() / range)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(chartWidth, y),
                strokeWidth = 1f
            )
            drawContext.canvas.nativeCanvas.drawText(
                "${t}°",
                -4f, y + 4f, levelPaint
            )
        }

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

        if (points.size >= 2) {
            val path = Path()
            points.forEachIndexed { index, point ->
                val x = ((point.timestamp - timeStart) / timeRange) * chartWidth
                val y = paddingTop + drawHeight * (1f - (point.temperature - minTemp) / range)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            val last = points.last()
            val lastX = ((last.timestamp - timeStart) / timeRange) * chartWidth
            val lastY = paddingTop + drawHeight * (1f - (last.temperature - minTemp) / range)
            drawCircle(color = lineColor, radius = 6f, center = Offset(lastX, lastY))
        }
    }
}
