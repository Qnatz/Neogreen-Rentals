package com.example.myapplicationx.database.dao

import androidx.room.*
import com.example.myapplicationx.database.model.InvoiceEntity
import com.example.myapplicationx.database.model.InvoiceItemEntity
import com.example.myapplicationx.database.model.InvoiceWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {

    @Query("SELECT * FROM invoices WHERE invoice_number = :invoiceNumber")
    fun getInvoiceEntity(invoiceNumber: String): Flow<InvoiceEntity?>

    @Query("SELECT * FROM invoices")
    fun getInvoiceEntities(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE tenant_id = :tenantId")
    fun getInvoicesByTenantId(tenantId: Int): Flow<List<InvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Transaction
    @Query("SELECT * FROM invoices")
    fun getInvoicesWithItems(): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE invoice_number = :invoiceNumber")
    fun getInvoiceWithItems(invoiceNumber: String): Flow<InvoiceWithItems?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(invoiceItems: List<InvoiceItemEntity>): List<Long>

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)
    
    @Query("SELECT invoice_number FROM invoices")
    suspend fun getAllInvoiceNumbers(): List<String>
    
    @Transaction
    suspend fun insertInvoiceWithItems(invoice: InvoiceEntity, invoiceItems: List<InvoiceItemEntity>) {
        insertInvoice(invoice)
        insertInvoiceItems(invoiceItems)
    }
    
    @Query("""
    SELECT * 
    FROM invoices
    WHERE tenant_id = :tenantId 
      AND status != 'paid'
      AND invoice_amount_due > 0
""")
fun getUnpaidInvoicesForTenant(tenantId: Int): List<InvoiceEntity>

@Query("SELECT * FROM invoices WHERE tenant_id = :tenantId AND status != 'paid' AND invoice_amount_due > 0")
   fun getUnpaidInvoicesForTenantFlow(tenantId: Int): Flow<List<InvoiceEntity>>

@Query("SELECT * FROM invoices WHERE invoice_number = :invoiceNumber")
suspend fun getInvoiceByNumber(invoiceNumber: String): InvoiceEntity?
}