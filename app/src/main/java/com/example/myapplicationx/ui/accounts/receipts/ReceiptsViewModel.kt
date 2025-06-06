package com.example.myapplicationx.ui.accounts.receipts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplicationx.database.dao.InvoiceDao
import com.example.myapplicationx.database.dao.ReceiptDao
import com.example.myapplicationx.database.dao.ReceiptInvoiceCrossRefDao
import com.example.myapplicationx.database.dao.TenantDao
import com.example.myapplicationx.database.model.InvoiceEntity
import com.example.myapplicationx.database.model.ReceiptEntity
import com.example.myapplicationx.database.model.ReceiptInvoice
import com.example.myapplicationx.database.model.ReceiptInvoiceCrossRef
import com.example.myapplicationx.database.model.TenantEntity
import com.example.myapplicationx.database.NeogreenDB
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.room.withTransaction
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class ReceiptsViewModel @Inject constructor(
    private val receiptDao: ReceiptDao,
    private val crossRefDao: ReceiptInvoiceCrossRefDao,
    private val tenantDao: TenantDao,
    private val invoiceDao: InvoiceDao,
    private val database: NeogreenDB
) : ViewModel() {

    private val _unpaidInvoices = MutableLiveData<List<InvoiceEntity>>()
    private val _receiptError = MutableLiveData<String>()
    private val _debugReceiptData = MutableLiveData<String>()

    val unpaidInvoices: LiveData<List<InvoiceEntity>> get() = _unpaidInvoices
    val receiptError: LiveData<String> get() = _receiptError
    val debugReceiptData: LiveData<String> get() = _debugReceiptData

    // Method for getting all tenants - Changed to match TenantDao return type (LiveData)
    fun getAllTenants(): LiveData<List<TenantEntity>> {
        return tenantDao.getAllTenants()
    }

    // Method for getting tenant by ID
    suspend fun getTenantById(tenantId: Int): TenantEntity? {
        return tenantDao.getTenantById(tenantId)
    }

    // Method for getting receipt invoice cross refs - Modified to use suspend function
    suspend fun getReceiptInvoiceCrossRefs(receiptNumber: String): List<ReceiptInvoiceCrossRef> {
        return crossRefDao.getByReceiptNumberList(receiptNumber)
    }

    // Method for inserting receipt with invoices
    suspend fun insertReceiptWithInvoices(receipt: ReceiptEntity, crossRefs: List<ReceiptInvoiceCrossRef>) {
        database.withTransaction {
            receiptDao.insertReceipt(receipt)
            crossRefDao.insertAll(crossRefs)
            
            // Update invoices based on payments
            for (crossRef in crossRefs) {
                val invoice = invoiceDao.getInvoiceByNumber(crossRef.invoiceNumber)
                if (invoice != null) {
                    val newDue = (invoice.invoiceAmountDue ?: 0.0) - crossRef.amountPaid
                    val status = if (newDue <= 0) "Paid" else "Partial"
                    val updatedInvoice = invoice.copy(
                        invoiceAmountDue = newDue,
                        status = status
                    )
                    invoiceDao.updateInvoice(updatedInvoice)
                }
            }
            
            // Update tenant debit balance (reduced credit instead of adding)
            tenantDao.deductCredit(receipt.tenantId, receipt.amountReceived)
        }
    }

    // MODIFIED: Update tenant balance logic for receipt edits
    suspend fun updateReceiptWithInvoices(receipt: ReceiptEntity, crossRefs: List<ReceiptInvoiceCrossRef>) {
        database.withTransaction {
            val oldReceipt = receiptDao.getReceiptByIdSync(receipt.receiptId)
            val amountDifference = receipt.amountReceived - (oldReceipt?.amountReceived ?: 0.0)
            
            // Restore old invoice amounts and delete old cross-refs
            val oldCrossRefs = crossRefDao.getByReceiptNumberList(receipt.receiptNumber)
            oldCrossRefs.forEach { oldRef ->
                invoiceDao.getInvoiceByNumber(oldRef.invoiceNumber)?.let { invoice ->
                    invoiceDao.updateInvoice(invoice.copy(
                        invoiceAmountDue = (invoice.invoiceAmountDue ?: 0.0) + oldRef.amountPaid
                    ))
                }
            }
            crossRefDao.deleteByReceiptNumber(receipt.receiptNumber)
            
            // Update receipt and insert new cross-refs
            receiptDao.updateReceipt(receipt)
            crossRefDao.insertAll(crossRefs)
            
            // Apply new payments
            crossRefs.forEach { crossRef ->
                invoiceDao.getInvoiceByNumber(crossRef.invoiceNumber)?.let { invoice ->
                    invoiceDao.updateInvoice(invoice.copy(
                        invoiceAmountDue = (invoice.invoiceAmountDue ?: 0.0) - crossRef.amountPaid,
                        status = if ((invoice.invoiceAmountDue ?: 0.0) - crossRef.amountPaid <= 0) "Paid" else "Partial"
                    ))
                }
            }
            
            // Adjust tenant balance
            if (amountDifference != 0.0) {
                if (amountDifference > 0) {
                    tenantDao.deductCredit(receipt.tenantId, amountDifference)
                } else {
                    tenantDao.addCredit(receipt.tenantId, -amountDifference)
                }
            }
        }
    }

    fun createReceipt(
        tenantId: Int,
        tenantName: String,
        amount: Double,
        isAuto: Boolean = false,
        selectedInvoices: List<ReceiptInvoice> = emptyList(),
        creditNote: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                database.withTransaction {
                    if (isAuto) {
                        createAutoReceipt(tenantId, tenantName, amount, creditNote)
                    } else {
                        createManualReceipt(tenantId, tenantName, amount, selectedInvoices, creditNote)
                    }
                }
            } catch (e: Exception) {
                _receiptError.postValue("Receipt creation failed: ${e.message}")
            }
        }
    }

    // MODIFIED: Auto-receipt logic to deduct credit
    private suspend fun createAutoReceipt(
        tenantId: Int,
        tenantName: String,
        paymentAmount: Double,
        creditNote: String
    ) {
        val tenant = tenantDao.getTenantById(tenantId) ?: throw Exception("Tenant not found")
        val unpaidInvoices = invoiceDao.getUnpaidInvoicesForTenant(tenantId)
            .sortedBy { it.invoiceDate }

        var remainingPayment = paymentAmount
        val receiptInvoices = mutableListOf<ReceiptInvoice>()

        for (invoice in unpaidInvoices) {
            if (remainingPayment <= 0) break
            
            val payable = invoice.invoiceAmountDue ?: 0.0
            val payment = minOf(payable, remainingPayment)
            
            updateInvoiceStatus(invoice, payment)
            
            receiptInvoices.add(ReceiptInvoice(
                invoiceNumber = invoice.invoiceNumber,
                amountPaid = payment,
                remainingBalance = invoice.invoiceAmountDue ?: 0.0,
                invoiceDate = invoice.invoiceDate,
                invoiceStatus = invoice.status
            ))
            
            remainingPayment -= payment
        }

        val actualPayment = paymentAmount - remainingPayment
        val receipt = ReceiptEntity(
            receiptNumber = generateReceiptNumber(tenantId),
            tenantId = tenantId,
            tenantName = tenantName,
            amountReceived = actualPayment,
            receiptDate = getCurrentDate(),
            receiptNote = creditNote,
            isAutoGenerated = true
        )
        
        saveReceiptWithInvoices(receipt, receiptInvoices)
        tenantDao.deductCredit(tenantId, actualPayment) // Deduct credit instead of adding
    }

    private suspend fun createManualReceipt(
        tenantId: Int,
        tenantName: String,
        amount: Double,
        selectedInvoices: List<ReceiptInvoice>,
        creditNote: String
    ) {
        val totalPaid = selectedInvoices.sumOf { it.amountPaid }
        if (totalPaid != amount) throw Exception("Selected invoices total ($totalPaid) doesn't match payment amount ($amount)")

        for (ri in selectedInvoices) {
            invoiceDao.getInvoiceByNumber(ri.invoiceNumber)?.let { invoice ->
                updateInvoiceStatus(invoice, ri.amountPaid)
            }
        }

        val receipt = ReceiptEntity(
            receiptNumber = generateReceiptNumber(tenantId),
            tenantId = tenantId,
            tenantName = tenantName,
            amountReceived = amount,
            receiptDate = getCurrentDate(),
            receiptNote = creditNote,
            isAutoGenerated = false
        )
        
        saveReceiptWithInvoices(receipt, selectedInvoices)
        tenantDao.deductCredit(tenantId, amount) // Deduct credit instead of adding
    }

    private suspend fun updateInvoiceStatus(invoice: InvoiceEntity, payment: Double) {
        val newDue = (invoice.invoiceAmountDue ?: 0.0) - payment
        val status = when {
            newDue <= 0 -> "Paid"
            else -> "Partial"
        }
        
        invoiceDao.updateInvoice(invoice.copy(
            invoiceAmountDue = newDue,
            status = status
        ))
    }

    private suspend fun saveReceiptWithInvoices(
        receipt: ReceiptEntity,
        invoicePayments: List<ReceiptInvoice>
    ) {
        receiptDao.insertReceipt(receipt)
        
        val crossRefs = invoicePayments.map {
            ReceiptInvoiceCrossRef(
                receiptNumber = receipt.receiptNumber,
                invoiceNumber = it.invoiceNumber,
                amountPaid = it.amountPaid,
                paymentDate = receipt.receiptDate
            )
        }
        
        if (crossRefs.isNotEmpty()) {
            crossRefDao.insertAll(crossRefs)
        }
        
        _debugReceiptData.postValue("Created receipt ${receipt.receiptNumber} with ${crossRefs.size} allocations")
    }

    // This method is no longer used because we now deduct credit directly in the relevant methods
    private suspend fun updateTenantBalance(tenant: TenantEntity, paymentAmount: Double) {
        tenantDao.deductCredit(tenant.tenantId, paymentAmount) // Changed to deduct credit
    }

    private fun generateReceiptNumber(tenantId: Int): String {
        val date = getCurrentDate().replace("-", "")
        return "REC-$tenantId-${date}-${System.currentTimeMillis() % 1000}"
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Existing CRUD operations
    fun getReceiptById(receiptId: Int): LiveData<ReceiptEntity?> =
        receiptDao.getReceiptById(receiptId).asLiveData()

    val allReceipts: Flow<List<ReceiptEntity>> = receiptDao.getAllReceipts()

    fun loadUnpaidInvoicesForTenant(tenantId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                invoiceDao.getUnpaidInvoicesForTenantFlow(tenantId).collect { invoices ->
                    _unpaidInvoices.postValue(invoices)
                }
            } catch (e: Exception) {
                _receiptError.postValue("Error loading unpaid invoices: ${e.message}")
            }
        }
    }
    
    // Added for EditReceiptFragment
    suspend fun getInvoiceDetails(invoiceNumber: String): InvoiceEntity? {
        return invoiceDao.getInvoiceByNumber(invoiceNumber)
    }

    fun deleteReceiptById(receiptId: Int) = viewModelScope.launch(Dispatchers.IO) {
        receiptDao.deleteReceiptById(receiptId)
    }
    
    fun createCreditReceipt(
        tenantId: Int,
        tenantName: String,
        amount: Double,
        creditNote: String
    ) = createReceipt(
        tenantId = tenantId,
        tenantName = tenantName,
        amount = amount,
        isAuto = true,
        creditNote = creditNote
    )
}