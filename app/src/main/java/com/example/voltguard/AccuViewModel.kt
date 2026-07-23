package com.example.voltguard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccuViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionTracker = SessionTracker.getInstance(application)

    private val _stats = MutableStateFlow(ChargingStats())
    val stats: StateFlow<ChargingStats> = _stats.asStateFlow()

    private val _history = MutableStateFlow<List<BatterySession>>(emptyList())
    val history: StateFlow<List<BatterySession>> = _history.asStateFlow()

    private val _dailyUsage = MutableStateFlow<List<DailyUsage>>(emptyList())
    val dailyUsage: StateFlow<List<DailyUsage>> = _dailyUsage.asStateFlow()

    private val _activeSession = MutableStateFlow<BatterySession?>(null)
    val activeSession: StateFlow<BatterySession?> = _activeSession.asStateFlow()

    private val _timeToFull = MutableStateFlow(0L)
    val timeToFull: StateFlow<Long> = _timeToFull.asStateFlow()

    private val _timeToEmpty = MutableStateFlow(0L)
    val timeToEmpty: StateFlow<Long> = _timeToEmpty.asStateFlow()

    fun onBatteryChanged(info: BatteryInfo) {
        val completed = sessionTracker.onBatteryChanged(info)
        if (completed != null) {
            refresh()
        }
        _activeSession.value = sessionTracker.getActiveSession()
        calculateTimeEstimates(info)
    }

    fun refresh() {
        _stats.value = sessionTracker.getStats()
        _history.value = getRecentSessions()
        _dailyUsage.value = sessionTracker.getDailyUsage()
    }

    private fun calculateTimeEstimates(info: BatteryInfo) {
        val currentMa = kotlin.math.abs(info.currentNow / 1000)
        if (currentMa == 0) {
            _timeToFull.value = 0L
            _timeToEmpty.value = 0L
            return
        }

        val remainingPercent = 100 - info.level
        val isCharging = info.status == "Charging" || info.status == "Full"

        if (isCharging && currentMa > 0) {
            val hours = (remainingPercent * 40f) / currentMa
            _timeToFull.value = (hours * 3600000L).toLong()
            _timeToEmpty.value = 0L
        } else if (!isCharging && currentMa > 0) {
            val hours = (info.level * 40f) / currentMa
            _timeToEmpty.value = (hours * 3600000L).toLong()
            _timeToFull.value = 0L
        }
    }

    private fun getRecentSessions(): List<BatterySession> {
        val sessions = mutableListOf<BatterySession>()
        for (i in 0 until minOf(50, sessionTracker.getStats().totalSessions)) {
            sessions.add(sessionTracker.getActiveSession() ?: break)
        }
        return history.value
    }
}
