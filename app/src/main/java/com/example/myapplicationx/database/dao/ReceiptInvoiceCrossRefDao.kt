package com.example.myapplicationx.database.dao

import androidx.room.*
import com.example.myapplicationx.database.model.ReceiptInvoiceCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptInvoiceCrossRefDao {
    // Basic CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: ReceiptInvoiceCrossRef): Long

    @Update
    suspend fun updateCrossRef(crossRef: ReceiptInvoiceCrossRef)

    @Delete
    suspend fun deleteCrossRef(crossRef: ReceiptInvoiceCrossRef)

    // Query operations
    @Query("SELECT * FROM receipt_invoice_xref WHERE rixref_id = :crossRefId")
    suspend fun getById(crossRefId: Long): ReceiptInvoiceCrossRef?

    @Query("SELECT * FROM receipt_invoice_xref WHERE receipt_number = :receiptNumber")
    fun getByReceiptNumber(receiptNumber: String): Flow<List<ReceiptInvoiceCrossRef>>

    @Query("SELECT * FROM receipt_invoice_xref WHERE invoice_number = :invoiceNumber")
    fun getByInvoiceNumber(invoiceNumber: String): Flow<List<ReceiptInvoiceCrossRef>>

    @Query("""
        SELECT * FROM receipt_invoice_xref 
        WHERE receipt_number = :receiptNumber 
        AND invoice_number = :invoiceNumber
    """)
    suspend fun getByCompositeKey(
        receiptNumber: String, 
        invoiceNumber: String
    ): ReceiptInvoiceCrossRef?

    // Bulk operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(crossRefs: List<ReceiptInvoiceCrossRef>): List<Long>

    @Query("DELETE FROM receipt_invoice_xref WHERE rixref_id = :crossRefId")
    suspend fun deleteById(crossRefId: Long)

    @Transaction
    @Query("DELETE FROM receipt_invoice_xref WHERE receipt_number = :receiptNumber")
    suspend fun deleteByReceiptNumber(receiptNumber: String)

    // Transaction
    @Transaction
    suspend fun replaceAllocations(
        receiptNumber: String,
        newAllocations: List<ReceiptInvoiceCrossRef>
    ) {
        deleteByReceiptNumber(receiptNumber)
        insertAll(newAllocations)
    }

    // Utility
    @Query("SELECT EXISTS(SELECT 1 FROM receipt_invoice_xref WHERE receipt_number = :receiptNumber LIMIT 1)")
    suspend fun hasAllocationsForReceipt(receiptNumber: String): Boolean

    @Query("SELECT SUM(amount_paid) FROM receipt_invoice_xref WHERE invoice_number = :invoiceNumber")
    fun getTotalPaidForInvoice(invoiceNumber: String): Flow<Double>
    
    @Query("SELECT * FROM receipt_invoice_xref WHERE receipt_number = :receiptNumber")
    fun getCrossRefsByReceiptFlow(receiptNumber: String): Flow<List<ReceiptInvoiceCrossRef>>
    
    @Query("SELECT * FROM receipt_invoice_xref WHERE invoice_number = :invoiceNumber")
    suspend fun getByInvoiceNumberList(invoiceNumber: String): List<ReceiptInvoiceCrossRef>

    @Query("SELECT * FROM receipt_invoice_xref WHERE receipt_number = :receiptNumber")
    suspend fun getByReceiptNumberList(receiptNumber: String): List<ReceiptInvoiceCrossRef>
}
