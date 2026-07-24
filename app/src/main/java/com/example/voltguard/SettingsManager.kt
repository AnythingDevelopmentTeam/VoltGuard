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

    companion object {
        private const val KEY_LOW = "low_threshold"
        private const val KEY_HIGH = "high_threshold"
        private const val KEY_ALERTS = "alerts_enabled"
        private const val KEY_NAV_STYLE = "navigation_style"
        private const val KEY_FIRST_LAUNCH = "first_launch_done"
        private const val KEY_LANG = "app_language"
        private const val KEY_RECOMMENDATIONS = "recommendations_enabled"
        private const val KEY_FULL_CHARGE = "full_charge_reminder"

        @Volatile
        private var instance: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager =
            instance ?: synchronized(this) {
                instance ?: SettingsManager(context.applicationContext).also { instance = it }
            }
    }
}
