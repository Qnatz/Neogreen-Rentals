package com.example.myapplicationx.ui.accounts.invoices

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.myapplicationx.database.dao.*
import com.example.myapplicationx.database.model.*
import com.example.myapplicationx.database.NeogreenDB
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import android.util.Log
 
@HiltViewModel
class InvoicesViewModel @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val tenantDao: TenantDao,
    private val invoiceItemDao: InvoiceItemDao,
    private val database: NeogreenDB
) : ViewModel() {

    val invoicesWithItems: Flow<List<InvoiceWithItems>> = invoiceDao.getInvoicesWithItems()
    val draftInvoice = MutableLiveData<InvoiceEntity>()

fun addInvoice(invoice: InvoiceEntity, invoiceItems: List<InvoiceItemEntity>) {
    viewModelScope.launch(Dispatchers.IO) {
        database.withTransaction {
            val invoiceId = invoiceDao.insertInvoice(invoice)
            if (invoiceId != -1L && invoiceItems.isNotEmpty()) {
                invoiceItemDao.insertInvoiceItems(invoiceItems)
            }

            // Update tenant's balance if this is not a draft invoice
            if (invoice.status != "DRAFT" && invoice.tenantId > 0) {
                // Add to tenant's debit balance
                tenantDao.updateDebitBalance(invoice.tenantId, invoice.totalAmount) //This line was causing the error

                // If there's a payment, reduce tenant's debit or add to credit
                if (invoice.paidAmount > 0) {
                    processPayment(invoice.tenantId, invoice.paidAmount)
                }
            }
        }
    }
}

    private suspend fun processPayment(tenantId: Int, amount: Double) {
        val tenant = tenantDao.getTenantById(tenantId)
        tenant?.let {
            val debitAmount = tenant.debitBalance
            if (amount <= debitAmount) {
                // Payment fully applies to debit
                tenantDao.updateDebitBalance(tenantId, -amount)
            } else {
                // Part payment applies to debit, rest goes to credit
                tenantDao.updateDebitBalance(tenantId, -debitAmount)
                tenantDao.updateCreditBalance(tenantId, amount - debitAmount)
            }
        }
    }

    suspend fun updateInvoiceWithItems(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        tenantId: Int,
        originalTotal: Double,
        newTotal: Double,
        originalPaidAmount: Double,
        newPaidAmount: Double
    ) {
        database.withTransaction {
            val tenant = tenantDao.getTenantById(tenantId)
            
            tenant?.let {
                // Handle total amount change
                val totalDifference = newTotal - originalTotal
                if (totalDifference != 0.0) {
                    // Update tenant's debit balance
                    tenantDao.updateDebitBalance(tenantId, totalDifference)
                }
                
                // Handle payment amount change
                val paymentDifference = newPaidAmount - originalPaidAmount
                if (paymentDifference != 0.0) {
                    processPayment(tenantId, paymentDifference)
                }
            }

            // Update invoice and items
            updateInvoiceData(invoice, items)
        }
    }

    private suspend fun updateInvoiceData(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>
    ) {
        invoiceDao.updateInvoice(invoice)
        invoiceItemDao.deleteInvoiceItemsByInvoice(invoice.invoiceNumber)
        if (items.isNotEmpty()) {
            invoiceItemDao.insertInvoiceItems(items)
        }
    }

    fun deleteInvoice(invoiceWithItems: InvoiceWithItems) {
        viewModelScope.launch {
            try {
                database.withTransaction {
                    val invoice = invoiceWithItems.invoice
                    
                    // Update tenant balance if not a draft
                    if (invoice.status != "DRAFT" && invoice.tenantId > 0) {
                        // Reverse tenant debit balance
                        tenantDao.updateDebitBalance(invoice.tenantId, -invoice.totalAmount)
                        
                        // If payments were made, handle them
                        if (invoice.paidAmount > 0) {
                            // Reverse payment effects
                            val tenant = tenantDao.getTenantById(invoice.tenantId)
                            tenant?.let {
                                val creditBalance = tenant.creditBalance
                                if (creditBalance >= invoice.paidAmount) {
                                    // Fully reduce credit
                                    tenantDao.updateCreditBalance(invoice.tenantId, -invoice.paidAmount)
                                } else {
                                    // Reduce credit to zero and add remaining to debit
                                    tenantDao.updateCreditBalance(invoice.tenantId, -creditBalance)
                                    tenantDao.updateDebitBalance(invoice.tenantId, invoice.paidAmount - creditBalance)
                                }
                            }
                        }
                    }
                    
                    // Delete invoice and items
                    invoiceDao.deleteInvoice(invoice)
                    invoiceItemDao.deleteInvoiceItemsByInvoice(invoice.invoiceNumber)
                }
            } catch (e: Exception) {
                Log.e("DeleteInvoice", "Failed to delete invoice", e)
            }
        }
    }

    // Core invoice operations
    fun getInvoiceWithItems(invoiceNumber: String): Flow<InvoiceWithItems?> =
        invoiceDao.getInvoiceWithItems(invoiceNumber)

    fun getInvoicesByTenant(tenantId: Int): Flow<List<InvoiceEntity>> =
        invoiceDao.getInvoicesByTenantId(tenantId)

    suspend fun getInvoiceItems(invoiceNumber: String): List<InvoiceItemEntity> {
        return invoiceItemDao.getItemsByInvoice(invoiceNumber)
    }

    fun saveInvoiceWithItems(invoice: InvoiceEntity, items: List<InvoiceItemEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            database.withTransaction {
                if (invoice.invoiceId != 0) {
                    // This is an update - get the original for comparison
                    val originalInvoice = invoiceDao.getInvoiceByNumber(invoice.invoiceNumber)
                    originalInvoice?.let {
                        updateInvoiceWithItems(
                            invoice,
                            items,
                            invoice.tenantId,
                            originalInvoice.totalAmount,
                            invoice.totalAmount,
                            originalInvoice.paidAmount,
                            invoice.paidAmount
                        )
                    } ?: run {
                        // Fallback if original not found
                        updateInvoiceData(invoice, items)
                    }
                } else {
                    // This is a new invoice
                    addInvoice(invoice, items)
                }
            }
        }
    }

    fun saveDraftInvoice(invoice: InvoiceEntity, items: List<InvoiceItemEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            database.withTransaction {
                invoice.status = "DRAFT"
                if (invoice.invoiceId != 0) {
                    invoiceDao.updateInvoice(invoice)
                    invoiceItemDao.deleteInvoiceItemsByInvoice(invoice.invoiceNumber)
                } else {
                    invoiceDao.insertInvoice(invoice)
                }
                if (items.isNotEmpty()) {
                    invoiceItemDao.insertInvoiceItems(items)
                }
            }
        }
    }
    
    fun getUnpaidInvoicesForTenant(tenantId: Int): Flow<List<InvoiceEntity>> {
        return invoiceDao.getUnpaidInvoicesForTenantFlow(tenantId)
    }
}