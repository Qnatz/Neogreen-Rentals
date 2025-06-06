package com.example.myapplicationx.ui.settings.companyInfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplicationx.database.dao.CompanyInfoDao
import com.example.myapplicationx.database.model.CompanyInfoEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompanyInfoViewModel @Inject constructor(
    private val companyInfoDao: CompanyInfoDao
) : ViewModel() {
    
    private val _companyInfo = MutableLiveData<CompanyInfoEntity?>()
    val companyInfo: LiveData<CompanyInfoEntity?> = _companyInfo

    init {
        loadCompanyInfo()
    }

    // Load company info from the database
    private fun loadCompanyInfo() {
        viewModelScope.launch {
            _companyInfo.value = companyInfoDao.getCompanyInfo()  // Assuming getCompanyInfo() returns a single CompanyInfoEntity
        }
    }
    
    fun getCompanyInfo(callback: (CompanyInfoEntity?) -> Unit) {
    viewModelScope.launch {
        try {
            // Assuming companyInfoDao.getCompanyInfo() returns a single CompanyInfoEntity
            val company = companyInfoDao.getCompanyInfo() 
            callback(company)
        } catch (e: Exception) {
            e.printStackTrace()
            callback(null)
            }
        }
    }
    
    // Update or insert company information
    fun updateCompanyInfo(updatedInfo: CompanyInfoEntity) {
        viewModelScope.launch {
            companyInfoDao.upsertCompanyInfo(updatedInfo)  // Assuming upsertCompanyInfo() is implemented in CompanyInfoDao
            _companyInfo.value = updatedInfo
        }
    }
}