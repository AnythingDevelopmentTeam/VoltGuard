package com.example.voltguard

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionTracker(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("voltguard_sessions", Context.MODE_PRIVATE)

    private var currentSession: BatterySession? = null
    private var lastLevel: Int = -1
    private var sampleCount: Int = 0
    private var cumulativeCurrent: Long = 0L

    fun onBatteryChanged(info: BatteryInfo): BatterySession? {
        val isCharging = info.status == "Charging" || info.status == "Full"
        val level = info.level
        val now = System.currentTimeMillis()

        if (lastLevel == -1) {
            lastLevel = level
            return null
        }

        val session = currentSession
        val sessionType = if (isCharging) SessionType.CHARGE else SessionType.DISCHARGE

        if (session == null || session.type != sessionType) {
            val completed = currentSession?.let { finishSession(it, info) }
            startNewSession(sessionType, level, info, now)
            lastLevel = level
            return completed
        }

        if (level != lastLevel || sampleCount % 5 == 0) {
            cumulativeCurrent += kotlin.math.abs(info.currentNow / 1000)
            sampleCount++
        }

        lastLevel = level
        return null
    }

    private fun startNewSession(
        type: SessionType,
        level: Int,
        info: BatteryInfo,
        now: Long
    ) {
        cumulativeCurrent = 0
        sampleCount = 1
        cumulativeCurrent = kotlin.math.abs(info.currentNow / 1000).toLong()

        currentSession = BatterySession(
            type = type,
            startLevel = level,
            endLevel = level,
            startTime = now,
            endTime = now,
            startVoltage = info.voltage,
            endVoltage = info.voltage,
            avgCurrent = info.currentNow / 1000,
            avgTemperature = info.temperature,
            chargeAdded = 0,
            duration = 0L
        )
    }

    private fun finishSession(session: BatterySession, info: BatteryInfo): BatterySession {
        val now = System.currentTimeMillis()
        val avgCurrent = if (sampleCount > 0) (cumulativeCurrent / sampleCount).toInt() else 0

        val levelDiff = kotlin.math.abs(session.startLevel - info.level)
        val completed = session.copy(
            endLevel = info.level,
            endTime = now,
            endVoltage = info.voltage,
            avgCurrent = avgCurrent,
            chargeAdded = levelDiff,
            duration = now - session.startTime
        )

        saveSession(completed)
        currentSession = null
        return completed
    }

    fun finishCurrentSession(info: BatteryInfo): BatterySession? {
        val session = currentSession ?: return null
        val completed = finishSession(session, info)
        lastLevel = -1
        return completed
    }

    fun getActiveSession(): BatterySession? = currentSession

    fun getRecentSessions(limit: Int = 50): List<BatterySession> {
        val all = loadSessions()
        return all.takeLast(limit).reversed()
    }

    fun getStats(): ChargingStats {
        val sessions = loadSessions()
        val chargeSessions = sessions.filter { it.type == SessionType.CHARGE }
        val dischargeSessions = sessions.filter { it.type == SessionType.DISCHARGE }

        val totalChargeAdded = chargeSessions.sumOf { it.chargeAdded }
        val totalDischarge = dischargeSessions.sumOf { it.chargeAdded }

        val avgChargeSpeed = if (chargeSessions.isNotEmpty()) {
            chargeSessions.map { session ->
                val hours = session.duration / 3600000f
                if (hours > 0f) session.chargeAdded / hours else 0f
            }.average().toFloat()
        } else 0f

        val avgDischargeSpeed = if (dischargeSessions.isNotEmpty()) {
            dischargeSessions.map { session ->
                val hours = session.duration / 3600000f
                if (hours > 0f) session.chargeAdded / hours else 0f
            }.average().toFloat()
        } else 0f

        val avgCapacity = if (chargeSessions.isNotEmpty()) {
            chargeSessions.takeLast(10).map { it.endLevel - it.startLevel }.average().toInt()
        } else 0

        val designCapacity = 4000
        val estimatedCapacity = if (avgCapacity > 0) {
            (designCapacity * avgCapacity / 100.0).toInt()
        } else designCapacity

        val healthPercent = (estimatedCapacity * 100f / designCapacity).coerceIn(0f, 100f)

        val chargeCycles = totalChargeAdded / 100

        val totalScreenOn = sessions.filter { it.type == SessionType.DISCHARGE }
            .sumOf { it.duration }

        return ChargingStats(
            totalSessions = sessions.size,
            totalChargeAdded = totalChargeAdded,
            totalDischarge = totalDischarge,
            avgChargeSpeed = avgChargeSpeed,
            avgDischargeSpeed = avgDischargeSpeed,
            estimatedCapacity = estimatedCapacity,
            designCapacity = designCapacity,
            healthPercent = healthPercent,
            chargeCycles = chargeCycles,
            totalScreenOnTime = totalScreenOn
        )
    }

    fun getDailyUsage(): List<DailyUsage> {
        val sessions = loadSessions()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val grouped = sessions.groupBy { dateFormat.format(Date(it.startTime)) }

        return grouped.map { (date, daySessions) ->
            val charge = daySessions.filter { it.type == SessionType.CHARGE }
            val discharge = daySessions.filter { it.type == SessionType.DISCHARGE }

            DailyUsage(
                date = date,
                chargeTime = charge.sumOf { it.duration },
                screenOnTime = discharge.sumOf { it.duration },
                chargePercent = charge.sumOf { it.chargeAdded },
                dischargePercent = discharge.sumOf { it.chargeAdded },
                avgTemperature = if (daySessions.isNotEmpty()) {
                    daySessions.map { it.avgTemperature }.average().toFloat()
                } else 0f
            )
        }.sortedByDescending { it.date }
    }

    fun generateCsv(): String {
        val sessions = loadSessions()
        val sb = StringBuilder()
        sb.appendLine("ID,Type,StartLevel,EndLevel,StartTime,EndTime,StartVoltage,EndVoltage,AvgCurrent(mA),AvgTemperature(°C),ChargeAdded(%),Duration(ms)")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        for (s in sessions) {
            sb.appendLine(
                "${s.id},${s.type.name},${s.startLevel},${s.endLevel}," +
                "${dateFormat.format(Date(s.startTime))},${dateFormat.format(Date(s.endTime))}," +
                "${s.startVoltage},${s.endVoltage},${s.avgCurrent},${s.avgTemperature}," +
                "${s.chargeAdded},${s.duration}"
            )
        }
        return sb.toString()
    }

    fun clearAllSessions() {
        prefs.edit().putString(KEY_SESSIONS, "[]").apply()
        currentSession = null
        lastLevel = -1
    }

    private fun saveSession(session: BatterySession) {
        val json = sessionToJson(session)
        val array = loadSessionArray()
        array.put(json)
        if (array.length() > MAX_SESSIONS) {
            val trimmed = JSONArray()
            for (i in array.length() - MAX_SESSIONS until array.length()) {
                trimmed.put(array.get(i))
            }
            prefs.edit().putString(KEY_SESSIONS, trimmed.toString()).apply()
        } else {
            prefs.edit().putString(KEY_SESSIONS, array.toString()).apply()
        }
    }

    private fun loadSessions(): List<BatterySession> {
        return loadSessionArray().let { array ->
            (0 until array.length()).map { i -> jsonToSession(array.getJSONObject(i)) }
        }
    }

    private fun loadSessionArray(): JSONArray {
        val str = prefs.getString(KEY_SESSIONS, "[]") ?: "[]"
        return try { JSONArray(str) } catch (_: Exception) { JSONArray() }
    }

    private fun sessionToJson(s: BatterySession): JSONObject = JSONObject().apply {
        put("id", s.id)
        put("type", s.type.name)
        put("startLevel", s.startLevel)
        put("endLevel", s.endLevel)
        put("startTime", s.startTime)
        put("endTime", s.endTime)
        put("startVoltage", s.startVoltage)
        put("endVoltage", s.endVoltage)
        put("avgCurrent", s.avgCurrent)
        put("avgTemperature", s.avgTemperature.toDouble())
        put("chargeAdded", s.chargeAdded)
        put("duration", s.duration)
    }

    private fun jsonToSession(j: JSONObject): BatterySession = BatterySession(
        id = j.optLong("id", 0),
        type = try { SessionType.valueOf(j.getString("type")) } catch (_: Exception) { SessionType.DISCHARGE },
        startLevel = j.optInt("startLevel"),
        endLevel = j.optInt("endLevel"),
        startTime = j.optLong("startTime"),
        endTime = j.optLong("endTime"),
        startVoltage = j.optInt("startVoltage"),
        endVoltage = j.optInt("endVoltage"),
        avgCurrent = j.optInt("avgCurrent"),
        avgTemperature = j.optDouble("avgTemperature", 0.0).toFloat(),
        chargeAdded = j.optInt("chargeAdded"),
        duration = j.optLong("duration")
    )

    companion object {
        private const val KEY_SESSIONS = "sessions"
        private const val MAX_SESSIONS = 200

        @Volatile
        private var instance: SessionTracker? = null

        fun getInstance(context: Context): SessionTracker =
            instance ?: synchronized(this) {
                instance ?: SessionTracker(context.applicationContext).also { instance = it }
            }
    }
}
