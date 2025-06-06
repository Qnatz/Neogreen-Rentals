package com.example.myapplicationx.database.dao

import androidx.room.*
import com.example.myapplicationx.database.model.InvoicableEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface InvoicableDao {

    @Query("SELECT * FROM invoicables WHERE status = :status ORDER BY next_billing_date ASC")
    fun getInvoicablesByStatus(status: String): Flow<List<InvoicableEntity>>

    // Returns invoicables whose next billing date is in the future (Dueup items)
    @Query("SELECT * FROM invoicables WHERE next_billing_date > :currentDate ORDER BY next_billing_date ASC")
    fun getInvoicablesDueUp(currentDate: LocalDate): Flow<List<InvoicableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoicable(invoicable: InvoicableEntity)

    @Update
    suspend fun updateInvoicable(invoicable: InvoicableEntity)

    @Delete
    suspend fun deleteInvoicable(invoicable: InvoicableEntity)
    
    @Query("DELETE FROM invoicables WHERE tenant_id = :tenantId")
    suspend fun deleteInvoicableByTenantId(tenantId: Int)
    
    @Query("SELECT * FROM invoicables WHERE tenant_id = :tenantId LIMIT 1")
    suspend fun getInvoicableByTenantId(tenantId: Int): InvoicableEntity?

    @Query("SELECT * FROM invoicables ORDER BY next_billing_date ASC")
    suspend fun getAllInvoicables(): List<InvoicableEntity>

    @Update
    suspend fun updateInvoicables(invoicables: List<InvoicableEntity>)

    @Query("SELECT * FROM invoicables WHERE invoicable_id = :invoicableId")
    suspend fun getInvoicableById(invoicableId: Int): InvoicableEntity?

}