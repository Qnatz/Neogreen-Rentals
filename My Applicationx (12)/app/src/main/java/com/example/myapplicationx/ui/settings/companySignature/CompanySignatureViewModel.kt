package com.example.myapplicationx.ui.settings.companySignature

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CompanySignatureViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _companySignature = MutableLiveData<CompanySignature>()
    val companySignature: LiveData<CompanySignature> get() = _companySignature

    init {
        // Load the saved signature path from SharedPreferences on initialization
        val savedPath = sharedPreferences.getString("signature_path", "") ?: ""
        Log.d("CompanySignatureVM", "Loaded saved signature path: $savedPath")
        _companySignature.value = CompanySignature(signatureUrl = savedPath)
    }

    fun updateCompanySignature(signature: CompanySignature) {
        _companySignature.value = signature
        // Persist the signature URL using SharedPreferences
        sharedPreferences.edit().putString("signature_path", signature.signatureUrl).apply()
        Log.d("CompanySignatureVM", "Updated signature path in SharedPreferences: ${signature.signatureUrl}")
    }
}