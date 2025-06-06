package com.example.myapplicationx.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myapplicationx.database.model.TenantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantDao {

    // Check if tenants table has any data
    @Query("SELECT COUNT(*) > 0 FROM tenants")
    suspend fun hasData(): Boolean

    // Flow-based queries
    @Query("SELECT * FROM tenants WHERE tenant_id = :tenantId")
    fun getTenantEntity(tenantId: Int): Flow<TenantEntity?>

    @Query("SELECT * FROM tenants")
    fun getTenantEntities(): Flow<List<TenantEntity>>

    @Query("SELECT * FROM tenants")
    fun getAllTenantsFlow(): Flow<List<TenantEntity>>

    // LiveData queries
    @Query("SELECT * FROM tenants")
    fun getAllTenants(): LiveData<List<TenantEntity>>

    @Query("SELECT * FROM tenants")
    fun dropdownTenants(): LiveData<List<TenantEntity>>

    // One-off suspend queries
    @Query("SELECT * FROM tenants")
    suspend fun getOneOffTenantEntities(): List<TenantEntity>

    @Query("SELECT * FROM tenants WHERE tenant_id = :tenantId")
    suspend fun getTenantById(tenantId: Int): TenantEntity?

    @Query("""
        SELECT * FROM tenants 
        WHERE tenant_name = :tenantName 
          AND house_id = :houseId 
          AND date_occupied = :dateOccupied 
        LIMIT 1
    """)
    suspend fun getTenantByDetails(
        tenantName: String,
        houseId: Int,
        dateOccupied: String
    ): TenantEntity?

    @Query("SELECT * FROM tenants ORDER BY tenant_id DESC LIMIT 1")
    suspend fun getMostRecentTenant(): TenantEntity?

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTenant(tenant: TenantEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreTenants(tenants: List<TenantEntity>): List<Long>

    @Upsert
    suspend fun upsertTenants(tenants: List<TenantEntity>)

    // Update operations
    @Update
    suspend fun updateTenant(tenant: TenantEntity)

    @Query("""
        UPDATE tenants 
        SET credit_balance = credit_balance + :creditToAdd 
        WHERE tenant_id = :tenantId
    """)
    suspend fun addCredit(tenantId: Int, creditToAdd: Double)

    @Query("""
        UPDATE tenants 
        SET debit_balance = debit_balance + :debitToAdd 
        WHERE tenant_id = :tenantId
    """)
    suspend fun addDebit(tenantId: Int, debitToAdd: Double)

    @Query("""
        UPDATE tenants 
        SET credit_balance = credit_balance + :creditToAdd, 
            debit_balance  = debit_balance  + :debitToAdd 
        WHERE tenant_id = :tenantId
    """)
    suspend fun addBalances(
        tenantId: Int,
        creditToAdd: Double,
        debitToAdd: Double
    )

    // Balance‐query methods
    @Query("SELECT credit_balance FROM tenants WHERE tenant_id = :tenantId")
    suspend fun getCreditBalance(tenantId: Int): Double

    @Query("SELECT debit_balance FROM tenants WHERE tenant_id = :tenantId")
    suspend fun getDebitBalance(tenantId: Int): Double

    @Query("""
        UPDATE tenants 
        SET credit_balance = credit_balance - :amount 
        WHERE tenant_id = :tenantId
    """)
    suspend fun deductCredit(tenantId: Int, amount: Double)

    // Delete operations
    @Query("DELETE FROM tenants WHERE tenant_id = :tenantId")
    suspend fun deleteTenant(tenantId: Int)

    @Query("DELETE FROM tenants WHERE tenant_id IN (:tenantIds)")
    suspend fun deleteTenants(tenantIds: List<Int>)

    // Join to fetch related house rent
    @Query("""
        SELECT h.rent_amount 
        FROM houses AS h 
        INNER JOIN tenants AS t 
          ON h.house_id = t.house_id 
        WHERE t.tenant_id = :tenantId
        LIMIT 1
    """)
    suspend fun getHouseRentByTenantId(tenantId: Int): Double
}