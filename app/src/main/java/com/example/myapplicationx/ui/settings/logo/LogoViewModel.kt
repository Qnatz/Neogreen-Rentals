package com.example.myapplicationx.ui.settings.logo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.net.Uri

class LogoViewModel : ViewModel() {
    private val _logoUri = MutableLiveData<Uri?>(null)
    val logoUri: LiveData<Uri?> = _logoUri

    fun updateLogo(uri: Uri?) {
        _logoUri.value = uri
    }

    fun clearLogo() {
        _logoUri.value = null
    }
}