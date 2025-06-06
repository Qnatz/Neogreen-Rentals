package com.example.myapplicationx.ui.accounts.creditEntries

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.myapplicationx.utils.Event
import com.example.myapplicationx.database.NeogreenDB
import com.example.myapplicationx.database.dao.CreditEntryDao
import com.example.myapplicationx.database.dao.TenantDao
import com.example.myapplicationx.database.model.CreditEntryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import javax.inject.Inject

@HiltViewModel
class CreditViewModel @Inject constructor(
    private val creditEntryDao: CreditEntryDao,
    private val tenantDao: TenantDao,
    private val database: NeogreenDB
) : ViewModel() {

    private val _errorEvent = MutableLiveData<Event<String>>()
    val errorEvent: LiveData<Event<String>> get() = _errorEvent

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
            try {
                database.withTransaction {
                    creditEntryDao.insertCreditEntry(creditEntry)
                    tenantDao.addCredit(creditEntry.tenantId, creditEntry.amount)
                }
            } catch (e: IllegalStateException) { // Catch specific exception
                _errorEvent.postValue(Event(e.message ?: "Error adding credit entry."))
            } catch (e: Exception) { // Catch other general exceptions
                _errorEvent.postValue(Event("An unexpected error occurred while adding credit entry."))
            }
        }
    }

    // Update an existing credit entry
    fun updateCreditEntry(creditEntry: CreditEntryEntity) {
        viewModelScope.launch {
            try {
                database.withTransaction {
                    val oldEntry = creditEntryDao.getCreditEntryById(creditEntry.creditEntryId)
                    if (oldEntry != null) {
                    val amountDifference = creditEntry.amount - oldEntry.amount
                    if (amountDifference < 0) {
                        val currentBalance = tenantDao.getCreditBalance(creditEntry.tenantId)
                        if (currentBalance + amountDifference < 0) {
                            throw IllegalStateException("Updating credit entry would result in a negative tenant credit balance.")
                        }
                    }
                    creditEntryDao.updateCreditEntry(creditEntry)
                    if (amountDifference != 0.0) {
                        tenantDao.addCredit(creditEntry.tenantId, amountDifference)
                    }
                } else {
                    // Optionally, handle the case where the entry doesn't exist.
                    // If it's an update, oldEntry should ideally exist.
                    // If we want to prevent creating negative balance even when oldEntry is null (e.g. treating as new entry update)
                    // similar checks could be added here. For now, aligns with spec.
                    creditEntryDao.updateCreditEntry(creditEntry)
                    _errorEvent.postValue(Event("Attempted to update a non-existent credit entry. Only updated data, no balance change."))
                }
            }
        } catch (e: IllegalStateException) {
            _errorEvent.postValue(Event(e.message ?: "Error updating credit entry."))
        } catch (e: Exception) {
            _errorEvent.postValue(Event("An unexpected error occurred while updating credit entry."))
        }
        }
    }

    // Delete a credit entry
    fun deleteCreditEntry(creditEntry: CreditEntryEntity) {
        viewModelScope.launch {
            try {
                database.withTransaction {
                    val currentBalance = tenantDao.getCreditBalance(creditEntry.tenantId)
                    if (currentBalance - creditEntry.amount < 0) {
                        throw IllegalStateException("Deleting credit entry would result in a negative tenant credit balance.")
                    }
                    creditEntryDao.deleteCreditEntry(creditEntry)
                    tenantDao.addCredit(creditEntry.tenantId, -creditEntry.amount)
                }
            } catch (e: IllegalStateException) {
                _errorEvent.postValue(Event(e.message ?: "Error deleting credit entry."))
            } catch (e: Exception) {
                _errorEvent.postValue(Event("An unexpected error occurred while deleting credit entry."))
            }
        }
    }
    
    // Get the total credit amount for a tenant
    fun getTotalCreditForTenant(tenantId: Int): LiveData<Double> =
        creditEntryDao.getTotalCreditForTenant(tenantId)
}