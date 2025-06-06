package com.example.myapplicationx.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myapplicationx.database.model.CreditEntryEntity

@Dao
interface CreditEntryDao {

    @Query("SELECT * FROM credit_entries WHERE tenant_id = :tenantId ORDER BY credit_date DESC")
    fun getCreditEntriesByTenantId(tenantId: Int): LiveData<List<CreditEntryEntity>>

    @Query("SELECT * FROM credit_entries WHERE credit_entry_id = :creditEntryId")
    suspend fun getCreditEntryById(creditEntryId: Int): CreditEntryEntity?

    @Query("SELECT * FROM credit_entries")
    fun getAllCreditEntries(): LiveData<List<CreditEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditEntry(creditEntry: CreditEntryEntity)

    @Update
    suspend fun updateCreditEntry(creditEntry: CreditEntryEntity)

    @Delete
    suspend fun deleteCreditEntry(creditEntry: CreditEntryEntity)

    /**
     * Returns the sum of all credit-entry amounts for the given tenant.
     * If there are no entries, Room will emit null; you can map that to 0.0 in the ViewModel if needed.
     */
    @Query("SELECT SUM(amount) FROM credit_entries WHERE tenant_id = :tenantId")
    fun getTotalCreditForTenant(tenantId: Int): LiveData<Double>
}