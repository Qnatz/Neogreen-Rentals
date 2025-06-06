package com.example.myapplicationx.database.dao

import androidx.room.*
import com.example.myapplicationx.database.model.ReceiptEntity
import com.example.myapplicationx.database.model.ReceiptInvoiceCrossRef
import kotlinx.coroutines.flow.Flow
import androidx.room.RewriteQueriesToDropUnusedColumns

@Dao
interface ReceiptDao {
    // Create operations
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReceipt(receipt: ReceiptEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreReceipts(receipts: List<ReceiptEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateReceipt(receipt: ReceiptEntity)

    // Update operations
    @Update
    suspend fun updateReceipt(receipt: ReceiptEntity)

    // Delete operations
    @Query("DELETE FROM receipts WHERE receipt_id = :receiptId")
    suspend fun deleteReceiptById(receiptId: Int)

    @Query("DELETE FROM receipts WHERE receipt_id IN (:receiptIds)")
    suspend fun deleteReceiptsByIds(receiptIds: List<Int>)

    // Cross-ref operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: ReceiptInvoiceCrossRef): Long

    // Query operations - Flow return types
    @Query("SELECT * FROM receipts WHERE receipt_id = :receiptId")
    fun getReceiptById(receiptId: Int): Flow<ReceiptEntity?>

    @Query("SELECT * FROM receipts")
    fun getAllReceiptEntities(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE tenant_id = :tenantId")
    fun getReceiptsByTenantId(tenantId: Int): Flow<List<ReceiptEntity>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query("""
        SELECT receipt_invoice_xref.* 
        FROM receipts 
        INNER JOIN receipt_invoice_xref 
        ON receipts.receipt_number = receipt_invoice_xref.receipt_number
        WHERE receipts.receipt_number = :receiptNumber
    """)
    fun getReceiptWithInvoices(receiptNumber: String): Flow<List<ReceiptInvoiceCrossRef>>

    // Query operations - Suspend functions
    @Query("SELECT * FROM receipts WHERE receipt_id = :receiptId")
    suspend fun getReceiptByIdSync(receiptId: Int): ReceiptEntity?

    @Query("SELECT * FROM receipts WHERE receipt_number = :receiptNumber")
    suspend fun getReceiptByNumber(receiptNumber: String): ReceiptEntity?

    @Query("SELECT * FROM receipts WHERE tenant_id = :tenantId ORDER BY receipt_date DESC")
    suspend fun getReceiptsByTenant(tenantId: Int): List<ReceiptEntity>

    @Query("SELECT SUM(amount_received) FROM receipts WHERE tenant_id = :tenantId")
    suspend fun getTotalAmountReceivedByTenant(tenantId: Int): Double?
    
    @Transaction
    @Query("SELECT * FROM receipt_invoice_xref WHERE receipt_number = :receiptNumber")
    suspend fun getReceiptInvoiceCrossRefsByReceiptNumber(receiptNumber: String): List<ReceiptInvoiceCrossRef>
}