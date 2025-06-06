package com.example.myapplicationx.ui.accounts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel


//@HiltViewModel
class AccountsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is accounts Fragment"
    }
    val text: LiveData<String> = _text
}