package com.example.voltguard

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("voltguard_prefs", Context.MODE_PRIVATE)

    private val _lowThreshold = MutableStateFlow(prefs.getInt(KEY_LOW, 20))
    val lowThreshold: StateFlow<Int> = _lowThreshold.asStateFlow()

    private val _highThreshold = MutableStateFlow(prefs.getInt(KEY_HIGH, 80))
    val highThreshold: StateFlow<Int> = _highThreshold.asStateFlow()

    private val _alertsEnabled = MutableStateFlow(prefs.getBoolean(KEY_ALERTS, true))
    val alertsEnabled: StateFlow<Boolean> = _alertsEnabled.asStateFlow()

    private val _navigationStyle = MutableStateFlow(prefs.getInt(KEY_NAV_STYLE, 0))
    val navigationStyle: StateFlow<Int> = _navigationStyle.asStateFlow()

    private val _language = MutableStateFlow(prefs.getString(KEY_LANG, "en") ?: "en")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _firstLaunchDone = MutableStateFlow(prefs.getBoolean(KEY_FIRST_LAUNCH, false))
    val firstLaunchDone: StateFlow<Boolean> = _firstLaunchDone.asStateFlow()

    private val _recommendationsEnabled = MutableStateFlow(prefs.getBoolean(KEY_RECOMMENDATIONS, true))
    val recommendationsEnabled: StateFlow<Boolean> = _recommendationsEnabled.asStateFlow()

    private val _fullChargeReminder = MutableStateFlow(prefs.getBoolean(KEY_FULL_CHARGE, false))
    val fullChargeReminder: StateFlow<Boolean> = _fullChargeReminder.asStateFlow()

    private val _darkTheme = MutableStateFlow(prefs.getBoolean(KEY_DARK_THEME, false))
    val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()

    private val _quietHoursEnabled = MutableStateFlow(prefs.getBoolean(KEY_QUIET_HOURS_ENABLED, false))
    val quietHoursEnabled: StateFlow<Boolean> = _quietHoursEnabled.asStateFlow()

    private val _quietHoursStart = MutableStateFlow(prefs.getString(KEY_QUIET_HOURS_START, "22:00") ?: "22:00")
    val quietHoursStart: StateFlow<String> = _quietHoursStart.asStateFlow()

    private val _quietHoursEnd = MutableStateFlow(prefs.getString(KEY_QUIET_HOURS_END, "08:00") ?: "08:00")
    val quietHoursEnd: StateFlow<String> = _quietHoursEnd.asStateFlow()

    private val _alertSound = MutableStateFlow(prefs.getBoolean(KEY_ALERT_SOUND, true))
    val alertSound: StateFlow<Boolean> = _alertSound.asStateFlow()

    private val _alertVibrate = MutableStateFlow(prefs.getBoolean(KEY_ALERT_VIBRATE, true))
    val alertVibrate: StateFlow<Boolean> = _alertVibrate.asStateFlow()

    private val _chargeSpeedAlert = MutableStateFlow(prefs.getBoolean(KEY_CHARGE_SPEED, false))
    val chargeSpeedAlert: StateFlow<Boolean> = _chargeSpeedAlert.asStateFlow()

    private val _widgetColor = MutableStateFlow(prefs.getString(KEY_WIDGET_COLOR, "auto") ?: "auto")
    val widgetColor: StateFlow<String> = _widgetColor.asStateFlow()

    private val _widgetStatus = MutableStateFlow(prefs.getBoolean(KEY_WIDGET_STATUS, true))
    val widgetStatus: StateFlow<Boolean> = _widgetStatus.asStateFlow()

    private val _widgetTemp = MutableStateFlow(prefs.getBoolean(KEY_WIDGET_TEMP, true))
    val widgetTemp: StateFlow<Boolean> = _widgetTemp.asStateFlow()

    private val _themeColor = MutableStateFlow(prefs.getString(KEY_THEME_COLOR, "green") ?: "green")
    val themeColor: StateFlow<String> = _themeColor.asStateFlow()

    private val _dynamicColor = MutableStateFlow(prefs.getBoolean(KEY_DYNAMIC_COLOR, true))
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    private val _batterySaverThreshold = MutableStateFlow(prefs.getInt(KEY_BATTERY_SAVER, 15))
    val batterySaverThreshold: StateFlow<Int> = _batterySaverThreshold.asStateFlow()

    private val _dndQuietHours = MutableStateFlow(prefs.getBoolean(KEY_DND_QUIET, false))
    val dndQuietHours: StateFlow<Boolean> = _dndQuietHours.asStateFlow()

    fun setLowThreshold(value: Int) {
        prefs.edit().putInt(KEY_LOW, value).apply()
        _lowThreshold.value = value
    }

    fun setHighThreshold(value: Int) {
        prefs.edit().putInt(KEY_HIGH, value).apply()
        _highThreshold.value = value
    }

    fun setAlertsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ALERTS, enabled).apply()
        _alertsEnabled.value = enabled
    }

    fun setNavigationStyle(style: Int) {
        prefs.edit().putInt(KEY_NAV_STYLE, style).apply()
        _navigationStyle.value = style
    }

    fun setLanguage(value: String) {
        prefs.edit().putString(KEY_LANG, value).apply()
        _language.value = value
    }

    fun setFirstLaunchDone() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, true).apply()
        _firstLaunchDone.value = true
    }

    fun setRecommendationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_RECOMMENDATIONS, enabled).apply()
        _recommendationsEnabled.value = enabled
    }

    fun setFullChargeReminder(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FULL_CHARGE, enabled).apply()
        _fullChargeReminder.value = enabled
    }

    fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
        _darkTheme.value = enabled
    }

    fun setQuietHoursEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_QUIET_HOURS_ENABLED, enabled).apply()
        _quietHoursEnabled.value = enabled
    }

    fun setQuietHoursStart(value: String) {
        prefs.edit().putString(KEY_QUIET_HOURS_START, value).apply()
        _quietHoursStart.value = value
    }

    fun setQuietHoursEnd(value: String) {
        prefs.edit().putString(KEY_QUIET_HOURS_END, value).apply()
        _quietHoursEnd.value = value
    }

    fun isInQuietHours(): Boolean {
        if (!_quietHoursEnabled.value) return false
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        val now = sdf.format(java.util.Date(System.currentTimeMillis()))
        return now >= _quietHoursStart.value && now < _quietHoursEnd.value
    }

    fun getPrefs(): SharedPreferences = prefs

    fun exportSettings(): String {
        val all = prefs.all
        val json = org.json.JSONObject()
        for ((key, value) in all) {
            when (value) {
                is String -> json.put(key, value)
                is Int -> json.put(key, value)
                is Boolean -> json.put(key, value)
                is Float -> json.put(key, value.toDouble())
                is Long -> json.put(key, value)
            }
        }
        return json.toString(2)
    }

    fun importSettings(jsonStr: String): Boolean {
        return try {
            val json = org.json.JSONObject(jsonStr)
            val editor = prefs.edit()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = json.get(key)
                when (value) {
                    is String -> editor.putString(key, value)
                    is Int -> editor.putInt(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Double -> editor.putFloat(key, value.toFloat())
                }
            }
            editor.apply()
            true
        } catch (_: Exception) { false }
    }

    fun setAlertSound(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ALERT_SOUND, enabled).apply()
        _alertSound.value = enabled
    }

    fun setAlertVibrate(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ALERT_VIBRATE, enabled).apply()
        _alertVibrate.value = enabled
    }

    fun setChargeSpeedAlert(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CHARGE_SPEED, enabled).apply()
        _chargeSpeedAlert.value = enabled
    }

    fun setWidgetColor(color: String) {
        prefs.edit().putString(KEY_WIDGET_COLOR, color).apply()
        _widgetColor.value = color
    }

    fun setWidgetStatus(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WIDGET_STATUS, enabled).apply()
        _widgetStatus.value = enabled
    }

    fun setWidgetTemp(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WIDGET_TEMP, enabled).apply()
        _widgetTemp.value = enabled
    }

    fun setThemeColor(color: String) {
        prefs.edit().putString(KEY_THEME_COLOR, color).apply()
        _themeColor.value = color
    }

    fun setDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
        _dynamicColor.value = enabled
    }

    fun setBatterySaverThreshold(value: Int) {
        prefs.edit().putInt(KEY_BATTERY_SAVER, value).apply()
        _batterySaverThreshold.value = value
    }

    fun setDndQuietHours(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DND_QUIET, enabled).apply()
        _dndQuietHours.value = enabled
    }

    companion object {
        private const val KEY_LOW = "low_threshold"
        private const val KEY_HIGH = "high_threshold"
        private const val KEY_ALERTS = "alerts_enabled"
        private const val KEY_NAV_STYLE = "navigation_style"
        private const val KEY_FIRST_LAUNCH = "first_launch_done"
        private const val KEY_LANG = "app_language"
        private const val KEY_RECOMMENDATIONS = "recommendations_enabled"
        private const val KEY_FULL_CHARGE = "full_charge_reminder"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_QUIET_HOURS_ENABLED = "quiet_hours_enabled"
        private const val KEY_QUIET_HOURS_START = "quiet_hours_start"
        private const val KEY_QUIET_HOURS_END = "quiet_hours_end"
        private const val KEY_ALERT_SOUND = "alert_sound"
        private const val KEY_ALERT_VIBRATE = "alert_vibrate"
        private const val KEY_CHARGE_SPEED = "charge_speed_alert"
        private const val KEY_WIDGET_COLOR = "widget_color"
        private const val KEY_WIDGET_STATUS = "widget_status"
        private const val KEY_WIDGET_TEMP = "widget_temp"
        private const val KEY_THEME_COLOR = "theme_color"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_BATTERY_SAVER = "battery_saver_threshold"
        private const val KEY_DND_QUIET = "dnd_quiet_hours"
    }
}
