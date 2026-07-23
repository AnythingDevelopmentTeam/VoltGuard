package com.example.voltguard

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    private const val PREF_LANG = "app_language"
    private const val DEFAULT_LANG = "en"

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("voltguard_prefs", Context.MODE_PRIVATE)
        return prefs.getString(PREF_LANG, DEFAULT_LANG) ?: DEFAULT_LANG
    }

    fun setLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences("voltguard_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_LANG, language).apply()
    }

    fun applyLocale(context: Context): Context {
        val lang = getLanguage(context)
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
