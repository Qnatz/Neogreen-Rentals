package com.example.myapplicationx.ui.accounts.creditEntries

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.myapplicationx.database.dao.CreditEntryDao
import com.example.myapplicationx.database.model.CreditEntryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreditViewModel @Inject constructor(
    private val creditEntryDao: CreditEntryDao
) : ViewModel() {

    // LiveData for all credit entries
    val allCreditEntries: LiveData<List<CreditEntryEntity>> =
        creditEntryDao.getAllCreditEntries()

    // Fetch credit entries for a specific tenant
    fun getCreditEntriesByTenantId(tenantId: Int): LiveData<List<CreditEntryEntity>> =
        creditEntryDao.getCreditEntriesByTenantId(tenantId)

    // Fetch a single credit entry by its ID, wrapped in liveData for suspend DAO
    fun getCreditEntryById(creditEntryId: Int): LiveData<CreditEntryEntity?> = liveData {
        emit(creditEntryDao.getCreditEntryById(creditEntryId))
    }

    // Insert a new credit entry
    fun addCreditEntry(creditEntry: CreditEntryEntity) {
        viewModelScope.launch {
            creditEntryDao.insertCreditEntry(creditEntry)
        }
    }

    // Update an existing credit entry
    fun updateCreditEntry(creditEntry: CreditEntryEntity) {
        viewModelScope.launch {
            creditEntryDao.updateCreditEntry(creditEntry)
        }
    }

    // Delete a credit entry
    fun deleteCreditEntry(creditEntry: CreditEntryEntity) {
        viewModelScope.launch {
            creditEntryDao.deleteCreditEntry(creditEntry)
        }
    }
    
    // Get the total credit amount for a tenant
    fun getTotalCreditForTenant(tenantId: Int): LiveData<Double> =
        creditEntryDao.getTotalCreditForTenant(tenantId)
}