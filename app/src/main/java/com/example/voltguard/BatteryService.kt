package com.example.voltguard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlin.math.abs

class BatteryService : Service() {

    private var batteryReceiver: BatteryReceiver? = null
    private var lastNotifiedLevel = -1
    private var lastWasCharging = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        registerBatteryReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val batteryInfo = BatteryReceiver.getCurrentBatteryInfo(this)
        startForeground(NOTIFICATION_ID_STICKY, buildStickyNotification(batteryInfo.level))
        updateStickyNotification(batteryInfo.level)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterBatteryReceiver()
    }

    private fun registerBatteryReceiver() {
        val tracker = SessionTracker.getInstance(this)
        batteryReceiver = BatteryReceiver { info ->
            updateStickyNotification(info.level)
            checkThresholdAndNotify(info)
            tracker.onBatteryChanged(info)
        }
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, intentFilter)
    }

    private fun unregisterBatteryReceiver() {
        batteryReceiver?.let {
            unregisterReceiver(it)
            batteryReceiver = null
        }
    }

    private fun checkThresholdAndNotify(info: BatteryInfo) {
        val settings = SettingsManager.getInstance(this)
        if (!settings.alertsEnabled.value) return

        val lowThreshold = settings.lowThreshold.value
        val highThreshold = settings.highThreshold.value
        val isCharging = info.status == "Charging" || info.status == "Full"

        if (info.level >= highThreshold && isCharging && !lastWasCharging) {
            sendThresholdNotification(
                title = getString(R.string.battery_high_title),
                message = getString(R.string.battery_high_body, info.level)
            )
        }

        if (info.level <= lowThreshold && !isCharging && lastWasCharging) {
            sendThresholdNotification(
                title = getString(R.string.battery_low_title),
                message = getString(R.string.battery_low_body, info.level)
            )
        }

        lastWasCharging = isCharging
        lastNotifiedLevel = info.level
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val stickyChannel = NotificationChannel(
            CHANNEL_STICKY,
            getString(R.string.channel_status_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.channel_status_desc)
        }

        val alertChannel = NotificationChannel(
            CHANNEL_ALERT,
            getString(R.string.channel_alerts_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.channel_alerts_desc)
        }

        manager.createNotificationChannel(stickyChannel)
        manager.createNotificationChannel(alertChannel)
    }

    private fun buildStickyNotification(level: Int) =
        NotificationCompat.Builder(this, CHANNEL_STICKY)
            .setSmallIcon(R.drawable.ic_notification_battery)
            .setContentTitle(getString(R.string.service_notif_title))
            .setContentText(getString(R.string.service_notif_body, level))
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(buildMainActivityPendingIntent())
            .build()

    private fun updateStickyNotification(level: Int) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID_STICKY, buildStickyNotification(level))
    }

    private fun sendThresholdNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ALERT)
            .setSmallIcon(R.drawable.ic_notification_battery)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(buildMainActivityPendingIntent())
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(abs(message.hashCode()), notification)
    }

    private fun buildMainActivityPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val CHANNEL_STICKY = "battery_sticky"
        private const val CHANNEL_ALERT = "battery_alert"
        private const val NOTIFICATION_ID_STICKY = 1001

        fun start(context: Context) {
            val intent = Intent(context, BatteryService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, BatteryService::class.java))
        }
    }
}
