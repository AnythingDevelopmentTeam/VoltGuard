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
import org.koin.android.ext.android.inject

class BatteryService : Service() {

    private val settings: SettingsManager by inject()
    private val tracker: SessionTracker by inject()
    private val history: BatteryHistoryManager by inject()

    private var batteryReceiver: BatteryReceiver? = null
    private var notifiedHigh = false
    private var notifiedLow = false
    private var lastFullReminderTime = 0L
    private var lastSpeedNotified: String? = null
    private var lastSaverNotified = false

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
        batteryReceiver = BatteryReceiver { info ->
            updateStickyNotification(info.level)
            checkThresholdAndNotify(info)
            tracker.onBatteryChanged(info)
            history.addPoint(info.level)
            history.addTempPoint(info.temperature)
            applyDnd()
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
        if (!settings.alertsEnabled.value) return
        if (settings.isInQuietHours() && !settings.dndQuietHours.value) return

        val lowThreshold = settings.lowThreshold.value
        val highThreshold = settings.highThreshold.value
        val isCharging = info.status == "Charging" || info.status == "Full"

        if (info.level >= highThreshold && !notifiedHigh) {
            notifiedHigh = true
            notifiedLow = false
            if (isCharging) {
                sendThresholdNotification(
                    title = getString(R.string.battery_high_title),
                    message = getString(R.string.battery_high_body, info.level)
                )
            }
        }

        if (info.level <= lowThreshold && !notifiedLow) {
            notifiedLow = true
            notifiedHigh = false
            if (!isCharging) {
                sendThresholdNotification(
                    title = getString(R.string.battery_low_title),
                    message = getString(R.string.battery_low_body, info.level)
                )
            }
        }

        if (info.level in (lowThreshold + 1) until highThreshold) {
            notifiedHigh = false
            notifiedLow = false
        }

        checkFullChargeReminder(info, isCharging)
        checkChargeSpeed(info, isCharging, settings)
        checkBatterySaver(info, isCharging, settings)
    }

    private fun checkChargeSpeed(info: BatteryInfo, isCharging: Boolean, settings: SettingsManager) {
        if (!settings.chargeSpeedAlert.value || !isCharging) {
            lastSpeedNotified = null
            return
        }
        val currentMa = info.currentNow / 1000
        val speed = when {
            currentMa < 0 -> null
            currentMa < 500 -> "slow"
            currentMa > 2000 -> "fast"
            else -> null
        }
        if (speed != null && speed != lastSpeedNotified) {
            lastSpeedNotified = speed
            val title = if (speed == "slow") "Slow charging" else "Fast charging"
            val body = if (speed == "slow") "Charging at ${currentMa}mA — consider using a different charger"
                       else "Charging at ${currentMa}mA"
            sendThresholdNotification(
                title = title,
                message = body,
                channelId = CHANNEL_REMINDER
            )
        }
    }

    private fun checkBatterySaver(info: BatteryInfo, isCharging: Boolean, settings: SettingsManager) {
        val threshold = settings.batterySaverThreshold.value
        if (!isCharging && info.level <= threshold && !lastSaverNotified) {
            lastSaverNotified = true
            val intent = Intent(android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            val notification = NotificationCompat.Builder(this, CHANNEL_ALERT)
                .setSmallIcon(R.drawable.ic_notification_battery)
                .setContentTitle("Battery saver")
                .setContentText("Battery is at ${info.level}% — consider enabling battery saver")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pending)
                .build()
            val manager = getSystemService(NotificationManager::class.java)
            manager.notify(NOTIFICATION_BATTERY_SAVER, notification)
        } else if (isCharging || info.level > threshold + 5) {
            lastSaverNotified = false
        }
    }

    private fun checkFullChargeReminder(info: BatteryInfo, isCharging: Boolean) {
        if (!settings.fullChargeReminder.value) return

        val now = System.currentTimeMillis()
        if (info.level >= 100 && isCharging) {
            if (lastFullReminderTime == 0L) {
                lastFullReminderTime = now
            } else if (now - lastFullReminderTime > FULL_REMINDER_INTERVAL) {
                sendThresholdNotification(
                    title = getString(R.string.full_charge_reminder_title),
                    message = getString(R.string.full_charge_reminder_body),
                    channelId = CHANNEL_REMINDER
                )
                lastFullReminderTime = now
            }
        } else {
            lastFullReminderTime = 0L
        }
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

        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDER,
            getString(R.string.channel_reminder_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.channel_reminder_desc)
        }
        manager.createNotificationChannel(reminderChannel)
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

    private fun sendThresholdNotification(title: String, message: String, channelId: String = CHANNEL_ALERT) {
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification_battery)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(buildMainActivityPendingIntent())

        if (!settings.alertSound.value) builder.setSound(null)
        if (!settings.alertVibrate.value) builder.setVibrate(null)

        val notification = builder.build()
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(message.hashCode() and 0x7FFFFFFF, notification)
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
        private const val CHANNEL_REMINDER = "battery_reminder"
        private const val NOTIFICATION_ID_STICKY = 1001
        private const val NOTIFICATION_BATTERY_SAVER = 1003
        private const val FULL_REMINDER_INTERVAL = 15 * 60 * 1000L

        fun start(context: Context) {
            val intent = Intent(context, BatteryService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, BatteryService::class.java))
        }
    }

    private fun applyDnd() {
        val manager = getSystemService(NotificationManager::class.java)
        if (!manager.isNotificationPolicyAccessGranted) return
        if (settings.dndQuietHours.value && settings.quietHoursEnabled.value && settings.isInQuietHours()) {
            manager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
        } else {
            manager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }
}
