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
        val isFull = info.status == "Full"

        if (currentMa == 0 || isFull) {
            _timeToFull.value = 0L
            _timeToEmpty.value = 0L
            return
        }

        val remainingPercent = 100 - info.level
        val isCharging = info.status == "Charging"

        if (isCharging && currentMa > 0) {
            val capacityAh = _stats.value.estimatedCapacity / 1000f
            val hours = (remainingPercent * capacityAh * 10f) / currentMa
            _timeToFull.value = (hours * 3600000L).toLong()
            _timeToEmpty.value = 0L
        } else if (!isCharging && currentMa > 0) {
            val capacityAh = _stats.value.estimatedCapacity / 1000f
            val hours = (info.level * capacityAh * 10f) / currentMa
            _timeToEmpty.value = (hours * 3600000L).toLong()
            _timeToFull.value = 0L
        }
    }

    private fun getRecentSessions(): List<BatterySession> {
        return sessionTracker.getRecentSessions(50)
    }
}
