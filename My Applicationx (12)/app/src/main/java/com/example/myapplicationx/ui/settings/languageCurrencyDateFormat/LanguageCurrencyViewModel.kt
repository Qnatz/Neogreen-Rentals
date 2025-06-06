package com.example.myapplicationx.ui.settings.languageCurrencyDateFormat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LanguageCurrencyViewModel(application: Application) : AndroidViewModel(application) {

    val countryDetails = MutableLiveData<Country?>()

    private val settingsManager = SettingsManager(application.applicationContext)

    /**
     * Fetch country details based on latitude/longitude and then save settings.
     */
    fun loadCountryData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val details = CountryHelper.getCountryDetails(getApplication(), latitude, longitude)
            details?.let {
                // Choose the first available value from each list (or empty string if none).
                val language = it.languages.firstOrNull() ?: ""
                val currency = it.currencies.firstOrNull() ?: ""
                val timezone = it.timezones.firstOrNull() ?: ""
                // Use a saved date format if one exists, or default to the first entry in our static list.
                val dateFormat = settingsManager.getDateFormat() ?: Constants.DATE_FORMATS[0]
                settingsManager.saveAll(it.name, language, currency, timezone, dateFormat)
            }
            countryDetails.postValue(details)
        }
    }

    fun getSavedCountry(): String? = settingsManager.getCountry()
    fun getSavedLanguage(): String? = settingsManager.getLanguage()
    fun getSavedCurrency(): String? = settingsManager.getCurrency()
    fun getSavedTimezone(): String? = settingsManager.getTimezone()
    fun getSavedDateFormat(): String? = settingsManager.getDateFormat()

    fun saveSettings(
        country: String,
        language: String,
        currency: String,
        timezone: String,
        dateFormat: String
    ) {
        settingsManager.saveAll(country, language, currency, timezone, dateFormat)
    }
}