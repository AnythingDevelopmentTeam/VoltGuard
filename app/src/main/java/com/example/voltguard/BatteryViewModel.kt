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
        }
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        ContextCompat.registerReceiver(
            context,
            receiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
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
