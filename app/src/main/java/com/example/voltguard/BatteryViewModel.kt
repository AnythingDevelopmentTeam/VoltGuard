package com.example.voltguard

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BatteryViewModel(application: Application) : AndroidViewModel(application) {

    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    private val _serviceRunning = MutableStateFlow(true)
    val serviceRunning: StateFlow<Boolean> = _serviceRunning.asStateFlow()

    private val _timeToFull = MutableStateFlow(0L)
    val timeToFull: StateFlow<Long> = _timeToFull.asStateFlow()

    private val _timeToEmpty = MutableStateFlow(0L)
    val timeToEmpty: StateFlow<Long> = _timeToEmpty.asStateFlow()

    private var receiver: BroadcastReceiver? = null

    init {
        _batteryInfo.value = BatteryReceiver.getCurrentBatteryInfo(application)
        BatteryService.start(application)
        startListening()
    }

    private fun startListening() {
        val context = getApplication<Application>()
        receiver = BatteryReceiver { info ->
            _batteryInfo.value = info
            calculateTimeEstimates(info)
        }
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        ContextCompat.registerReceiver(
            context,
            receiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
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
        val estimatedCapacity = 4000
        if (isCharging && currentMa > 0) {
            val capacityAh = estimatedCapacity / 1000f
            val hours = (remainingPercent * capacityAh * 10f) / currentMa
            _timeToFull.value = (hours * 3600000L).toLong()
            _timeToEmpty.value = 0L
        } else if (!isCharging && currentMa > 0) {
            val capacityAh = estimatedCapacity / 1000f
            val hours = (info.level * capacityAh * 10f) / currentMa
            _timeToEmpty.value = (hours * 3600000L).toLong()
            _timeToFull.value = 0L
        }
    }

    fun toggleService() {
        val context = getApplication<Application>()
        if (_serviceRunning.value) {
            BatteryService.stop(context)
            _serviceRunning.value = false
        } else {
            BatteryService.start(context)
            _serviceRunning.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        receiver?.let {
            getApplication<Application>().unregisterReceiver(it)
            receiver = null
        }
    }
}
