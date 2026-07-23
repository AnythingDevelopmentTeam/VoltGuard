package com.example.voltguard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

class BatteryReceiver(
    private val onBatteryChanged: (BatteryInfo) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BATTERY_CHANGED) return
        onBatteryChanged(parseBatteryIntent(intent))
    }

    companion object {

        fun getCurrentBatteryInfo(context: Context): BatteryInfo {
            val intent = context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            return intent?.let { parseBatteryIntent(it) } ?: BatteryInfo()
        }

        private fun parseBatteryIntent(intent: Intent): BatteryInfo {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            val percentage = if (scale > 0) (level * 100) / scale else 0

            val status = when (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                BatteryManager.BATTERY_STATUS_FULL -> "Full"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
                else -> "Unknown"
            }

            val plugType = when (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)) {
                BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                else -> "None"
            }

            val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
            val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)

            val health = when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                else -> "Unknown"
            }

            val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
            val capacity = intent.getIntExtra("capacity", 0)
            val chargeCounter = intent.getIntExtra("charge_counter", 0)
            val currentNow = intent.getIntExtra("current_now", 0)
            val cycleCount = intent.getIntExtra("cycle_count", 0)

            return BatteryInfo(
                level = percentage,
                status = status,
                plugType = plugType,
                temperature = temperature,
                voltage = voltage,
                health = health,
                technology = technology,
                capacity = capacity,
                chargeCounter = chargeCounter,
                currentNow = currentNow,
                cycleCount = cycleCount
            )
        }
    }
}
