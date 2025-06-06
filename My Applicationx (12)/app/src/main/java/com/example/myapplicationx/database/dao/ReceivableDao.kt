package com.example.myapplicationx.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myapplicationx.database.model.ReceivableEntity

@Dao
interface ReceivableDao {

    // Insert a new receivable or replace on conflict.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceivable(receivable: ReceivableEntity)

    // Update an existing receivable.
    @Update
    suspend fun updateReceivable(receivable: ReceivableEntity)

    // Delete a specific receivable.
    @Delete
    suspend fun deleteReceivable(receivable: ReceivableEntity)

    // Delete all receivables.
    @Query("DELETE FROM receivables")
    suspend fun deleteAllReceivables()

    // Get all overdue receivables: amountDue > 0 and dateReceivable is in the past.
    @Query("SELECT * FROM receivables WHERE amount_due > 0 AND date_receivable <= :currentDate ORDER BY date_receivable")
    fun getOverdueReceivables(currentDate: Long): LiveData<List<ReceivableEntity>>

    // Get all pending receivables: amountDue > 0 and dateReceivable is in the future.
    @Query("SELECT * FROM receivables WHERE amount_due > 0 AND date_receivable > :currentDate ORDER BY date_receivable")
    fun getPendingReceivables(currentDate: Long): LiveData<List<ReceivableEntity>>

    // Get a receivable by its invoice number.
    @Query("SELECT * FROM receivables WHERE invoice_number = :invoiceNumber LIMIT 1")
    suspend fun getReceivableByInvoiceNumber(invoiceNumber: String): ReceivableEntity?

    // Get all invoice numbers from the receivables table.
    @Query("SELECT invoice_number FROM receivables")
    suspend fun getAllReceivableInvoiceNumbers(): List<String>
}