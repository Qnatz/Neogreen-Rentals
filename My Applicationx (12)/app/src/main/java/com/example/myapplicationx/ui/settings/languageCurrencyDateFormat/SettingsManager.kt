package com.example.myapplicationx.ui.settings.languageCurrencyDateFormat

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_COUNTRY = "country"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_TIMEZONE = "timezone"
        private const val KEY_DATE_FORMAT = "date_format"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveAll(
        country: String,
        language: String,
        currency: String,
        timezone: String,
        dateFormat: String
    ) {
        sharedPreferences.edit().apply {
            putString(KEY_COUNTRY, country)
            putString(KEY_LANGUAGE, language)
            putString(KEY_CURRENCY, currency)
            putString(KEY_TIMEZONE, timezone)
            putString(KEY_DATE_FORMAT, dateFormat)
            apply()
        }
    }

    fun getCountry(): String? = sharedPreferences.getString(KEY_COUNTRY, null)
    fun getLanguage(): String? = sharedPreferences.getString(KEY_LANGUAGE, null)
    fun getCurrency(): String? = sharedPreferences.getString(KEY_CURRENCY, null)
    fun getTimezone(): String? = sharedPreferences.getString(KEY_TIMEZONE, null)
    fun getDateFormat(): String? = sharedPreferences.getString(KEY_DATE_FORMAT, null)
}