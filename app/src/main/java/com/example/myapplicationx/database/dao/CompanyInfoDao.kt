package com.example.myapplicationx.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplicationx.database.model.CompanyInfoEntity

@Dao
interface CompanyInfoDao {

    @Query("SELECT * FROM company_info LIMIT 1")  // Assuming you want a single CompanyInfo
    suspend fun getCompanyInfo(): CompanyInfoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCompanyInfo(companyInfo: CompanyInfoEntity)
}