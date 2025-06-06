package com.example.myapplicationx.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myapplicationx.database.model.TaxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaxDao {

    // Check if there is any data in the taxes table
    @Query("SELECT COUNT(*) > 0 FROM taxes")
    suspend fun hasData(): Boolean

    // Get a single tax by ID as a Flow
    @Query("SELECT * FROM taxes WHERE tax_id = :taxId")
    fun getTaxEntity(taxId: Int): Flow<TaxEntity?>

    // Get all taxes as a Flow
    @Query("SELECT * FROM taxes")
    fun getTaxEntities(): Flow<List<TaxEntity>>

    // One-off retrieval of all taxes (not LiveData or Flow)
    @Query("SELECT * FROM taxes")
    suspend fun getOneOffTaxEntities(): List<TaxEntity>

    // Insert a single tax (ignore duplicates)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTax(tax: TaxEntity): Long

    // Insert multiple taxes, ignoring duplicates
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreTaxes(taxes: List<TaxEntity>): List<Long>

    // Upsert taxes (insert or update)
    @Upsert
    suspend fun upsertTaxes(taxes: List<TaxEntity>)

    // Update a single tax
    @Update
    suspend fun updateTax(tax: TaxEntity)

    // Delete a single tax by ID
    @Query("DELETE FROM taxes WHERE tax_id = :taxId")
    suspend fun deleteTax(taxId: Int)

    // Delete multiple taxes by ID
    @Query("DELETE FROM taxes WHERE tax_id IN (:taxIds)")
    suspend fun deleteTaxes(taxIds: List<Int>)

    // Get a single tax by ID (one-off retrieval)
    @Query("SELECT * FROM taxes WHERE tax_id = :taxId")
    suspend fun getTaxById(taxId: Int): TaxEntity?

    // Get all taxes as LiveData
    @Query("SELECT * FROM taxes")
    fun getAllTaxes(): LiveData<List<TaxEntity>>
}