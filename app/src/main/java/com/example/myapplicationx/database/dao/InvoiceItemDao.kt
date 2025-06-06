package com.example.myapplicationx.database.dao

import androidx.room.*
import com.example.myapplicationx.database.model.InvoiceItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItem(item: InvoiceItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItemEntity>)

    @Update
    suspend fun updateInvoiceItem(item: InvoiceItemEntity)

    @Delete
    suspend fun deleteInvoiceItem(item: InvoiceItemEntity)

    // Query invoice items by invoice number (using the column "invoice_number")
    @Query("SELECT * FROM invoice_items WHERE invoice_number = :invoiceNumber")
    suspend fun getItemsByInvoice(invoiceNumber: String): List<InvoiceItemEntity>

    // Alternative method using Flow
    @Query("SELECT * FROM invoice_items WHERE invoice_number = :invoiceNumber")
    fun getInvoiceItemsByInvoiceFlow(invoiceNumber: String): Flow<List<InvoiceItemEntity>>

    // Retrieve a specific invoice item by its primary key (invoice_item_id)
    @Query("SELECT * FROM invoice_items WHERE invoice_item_id = :invoiceItemId LIMIT 1")
    suspend fun getInvoiceItemById(invoiceItemId: Int): InvoiceItemEntity?

    // Delete all invoice items associated with a specific invoice number
    @Query("DELETE FROM invoice_items WHERE invoice_number = :invoiceNumber")
    suspend fun deleteInvoiceItemsByInvoice(invoiceNumber: String)

    // Get the total for an invoice by summing the item_rate column.
    // Adjust this query if you wish to sum a different column (e.g., computed_total).
    @Query("SELECT SUM(item_rate) FROM invoice_items WHERE invoice_number = :invoiceNumber")
    suspend fun getTotalForInvoice(invoiceNumber: String): Double?
}