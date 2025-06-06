package com.example.myapplicationx.database.dao

import androidx.room.*
import com.example.myapplicationx.database.model.ServiceTaxCrossRef

@Dao
interface ServiceTaxCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: ServiceTaxCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(crossRefs: List<ServiceTaxCrossRef>)

    @Delete
    suspend fun deleteCrossRef(crossRef: ServiceTaxCrossRef)

    // Single declaration only – duplicates removed
    @Query("SELECT * FROM service_tax_cross_ref WHERE service_id = :serviceId")
    suspend fun getCrossRefsForService(serviceId: Int): List<ServiceTaxCrossRef>

    @Query("SELECT * FROM service_tax_cross_ref WHERE tax_id = :taxId")
    suspend fun getCrossRefsForTax(taxId: Int): List<ServiceTaxCrossRef>

    @Query("DELETE FROM service_tax_cross_ref WHERE service_id = :serviceId")
    suspend fun deleteServiceTaxCrossRefsForService(serviceId: Int)
}