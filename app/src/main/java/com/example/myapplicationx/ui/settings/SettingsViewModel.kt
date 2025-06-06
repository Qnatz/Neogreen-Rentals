package com.example.myapplicationx.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    private val _settings = MutableLiveData<List<Settings>>().apply {
        value = listOf(
            Settings(settingId = 1, settingName = "Company Logo"),
            Settings(settingId = 2, settingName = "Company Information"),
            Settings(settingId = 3, settingName = "Company Signature"),
            Settings(settingId = 4, settingName = "Language, Currency, Date Format"),
            Settings(settingId = 5, settingName = "Invoice and Reciept Templates"),
            Settings(settingId = 6, settingName = "Taxes"),
            Settings(settingId = 7, settingName = "Services"),
            Settings(settingId = 8, settingName = "Backup"),
            Settings(settingId = 9, settingName = "Services and Taxes")
        )
    }

    // Exposed LiveData
    val settings: LiveData<List<Settings>> = _settings
}