package com.example.myapplicationx.ui.settings.templates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TemplatesViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is templates Fragment"
    }
    val text: LiveData<String> = _text
}