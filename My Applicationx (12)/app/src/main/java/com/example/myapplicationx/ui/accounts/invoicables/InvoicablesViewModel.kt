package com.example.myapplicationx.ui.accounts.invoicables

import androidx.lifecycle.*
import com.example.myapplicationx.database.dao.InvoicableDao
import com.example.myapplicationx.database.model.InvoicableEntity
import com.example.myapplicationx.database.dao.TenantDao
import com.example.myapplicationx.database.model.TenantEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class InvoicablesViewModel @Inject constructor(
    private val tenantDao: TenantDao,
    private val invoicableDao: InvoicableDao,
) : ViewModel() {

    // Original tenants list from database
    val tenants: LiveData<List<TenantEntity>> = tenantDao.getTenantEntities().asLiveData()

    // Query invoicables by status (assumed to be updated by the background worker)
    val dueUpInvoicables: LiveData<List<InvoicableEntity>> =
        invoicableDao.getInvoicablesByStatus("Dueup").asLiveData()

    val pendingInvoicables: LiveData<List<InvoicableEntity>> =
        invoicableDao.getInvoicablesByStatus("Pending").asLiveData()
    
    fun addInvoicable(invoicable: InvoicableEntity) {
        viewModelScope.launch {
            invoicableDao.insertInvoicable(invoicable)
        }
    }

    // Called from the Add Invoice screen upon invoice creation.
    // It updates the invoicable's next billing date and resets status to "dueup".
    fun updateInvoicableData(invoicableId: Int) {
    viewModelScope.launch {
        // Retrieve the invoicable entity by its ID
        val invoicable = invoicableDao.getInvoicableById(invoicableId)
        
        // Ensure the entity exists
        if (invoicable != null) {
            // Calculate the new next billing date (e.g., by adding one month)
            val nextBillingDate = calculateNextDueDate(invoicable.nextBillingDate.toString())
            
            // Create an updated copy of the entity with the new status and next billing date
            val updatedInvoicable = invoicable.copy(status = "Dueup", nextBillingDate = nextBillingDate)
            
            // Update the entity in the database
            invoicableDao.updateInvoicable(updatedInvoicable)
        } else {
            // Handle the case where the invoicable entity is not found
            // For example, log an error or throw an exception
            }
        }
    }
    
    fun deleteInvoicable(tenantId: Int) {
        viewModelScope.launch {
            invoicableDao.deleteInvoicableByTenantId(tenantId)
        }
    }
    
    /**
     * Calculates the next due date by adding one month to the given start date.
     * The startDate parameter can either be:
     *   - A formatted date string in "yyyy-MM-dd", or
     *   - A numeric string representing epoch milliseconds.
     * The function returns the next due date as epoch milliseconds.
     */
    private fun calculateNextDueDate(startDate: String): Long {
        val localDate: LocalDate = if (startDate.all { it.isDigit() }) {
            Instant.ofEpochMilli(startDate.toLong()).atZone(ZoneId.systemDefault()).toLocalDate()
        } else {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
            LocalDate.parse(startDate, formatter)
        }
        return localDate.plusMonths(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()
    }
}