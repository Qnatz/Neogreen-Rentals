package com.example.myapplicationx.ui.accounts.receivables

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplicationx.database.dao.InvoiceDao
import com.example.myapplicationx.database.dao.ReceivableDao
import com.example.myapplicationx.database.model.InvoiceEntity
import com.example.myapplicationx.database.model.ReceivableEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ReceivableViewModel @Inject constructor(
    private val receivableDao: ReceivableDao,
    private val invoiceDao: InvoiceDao
) : ViewModel() {

    // LiveData holding a single invoice selected from the UI.
    val invoice = MutableLiveData<InvoiceEntity?>()
    private val _availableInvoiceNumbers = MutableLiveData<List<String>>()
    val availableInvoiceNumbers: LiveData<List<String>> get() = _availableInvoiceNumbers

    // LiveData for displaying pending and overdue receivables in the UI.
    val pendingReceivables: LiveData<List<ReceivableEntity>> =
        receivableDao.getPendingReceivables(System.currentTimeMillis())
    val overdueReceivables: LiveData<List<ReceivableEntity>> =
        receivableDao.getOverdueReceivables(System.currentTimeMillis())

    init {
        observeInvoices() // Observe all invoices (if needed)
    }

    private fun observeInvoices() {
    viewModelScope.launch {
        invoiceDao.getInvoiceEntities().collect { invoices ->
            invoices.forEach { invoice ->
                createOrUpdateReceivable(invoice)
                }
            }
        }
    }
    
    /**
     * New method to fetch and observe a single invoice based on invoice number.
     */
    fun observeInvoice(invoiceNumber: String) {
        viewModelScope.launch {
            invoiceDao.getInvoiceEntity(invoiceNumber).collect { invoiceEntity ->
                invoice.postValue(invoiceEntity)
            }
        }
    }

    private suspend fun createOrUpdateReceivable(invoice: InvoiceEntity) {
        val existingReceivable = receivableDao.getReceivableByInvoiceNumber(invoice.invoiceNumber)
        val amountDueValue = invoice.invoiceAmountDue ?: 0.0

        if (amountDueValue > 0.0) {
            val receivable = existingReceivable?.copy(
                amountDue = amountDueValue
            ) ?: ReceivableEntity(
                invoiceNumber = invoice.invoiceNumber,
                tenantName = invoice.tenantName,
                dateReceivable = convertDateStringToMillis(invoice.invoiceDate),
                amountDue = amountDueValue
            )
            receivableDao.insertReceivable(receivable)
        } else {
            existingReceivable?.let { receivableDao.deleteReceivable(it) }
        }
    }

    private fun convertDateStringToMillis(dateString: String): Long {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = formatter.parse(dateString)
        return if (date != null) {
            val calendar = Calendar.getInstance().apply {
                time = date
                add(Calendar.DAY_OF_YEAR, 7)
            }
            calendar.timeInMillis
        } else {
            0L
        }
    }
    
    // --- Manual CRUD Methods for Receivables ---

    /**
     * Inserts a receivable into the database.
     */
    fun insertReceivable(receivable: ReceivableEntity) {
        viewModelScope.launch {
            try {
                receivableDao.insertReceivable(receivable)
            } catch (e: Exception) {
                Log.e("ReceivableViewModel", "Error inserting receivable", e)
            }
        }
    }

    /**
     * Updates an existing receivable in the database.
     */
    fun updateReceivable(receivable: ReceivableEntity) {
        viewModelScope.launch {
            try {
                receivableDao.updateReceivable(receivable)
            } catch (e: Exception) {
                Log.e("ReceivableViewModel", "Error updating receivable", e)
            }
        }
    }

    /**
     * Deletes a receivable from the database.
     */
    fun deleteReceivable(receivable: ReceivableEntity) {
        viewModelScope.launch {
            try {
                receivableDao.deleteReceivable(receivable)
            } catch (e: Exception) {
                Log.e("ReceivableViewModel", "Error deleting receivable", e)
            }
        }
    }

    /**
     * Fetches invoice numbers that do not yet have a corresponding receivable.
     */
    fun fetchAvailableInvoiceNumbers() {
        viewModelScope.launch {
            try {
                val receivableInvoiceNumbers = receivableDao.getAllReceivableInvoiceNumbers()
                val allInvoiceNumbers = invoiceDao.getAllInvoiceNumbers()
                val availableNumbers = allInvoiceNumbers.filterNot { receivableInvoiceNumbers.contains(it) }
                _availableInvoiceNumbers.postValue(availableNumbers)
            } catch (e: Exception) {
                Log.e("ReceivableViewModel", "Error fetching available invoice numbers", e)
            }
        }
    }
}