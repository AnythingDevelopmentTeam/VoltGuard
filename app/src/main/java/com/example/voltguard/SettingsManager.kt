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

        @Volatile
        private var instance: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager =
            instance ?: synchronized(this) {
                instance ?: SettingsManager(context.applicationContext).also { instance = it }
            }
    }
}
