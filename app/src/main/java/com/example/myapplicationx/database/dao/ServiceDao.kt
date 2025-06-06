package com.example.myapplicationx.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.myapplicationx.database.model.ServiceEntity
import com.example.myapplicationx.database.model.ServiceTaxCrossRef
import com.example.myapplicationx.database.model.ServiceWithTaxCrossRefs
import com.example.myapplicationx.database.model.TaxEntity

@Dao
interface ServiceDao {

    // ServiceEntity Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceEntity): Long

    @Update
    suspend fun updateService(service: ServiceEntity)

    @Delete
    suspend fun deleteService(service: ServiceEntity)

    @Query("SELECT * FROM services WHERE service_id = :serviceId")
    fun getServiceById(serviceId: Int): LiveData<ServiceEntity>

    @Query("SELECT * FROM services")
    fun getAllServices(): LiveData<List<ServiceEntity>>

    // TaxEntity Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTax(tax: TaxEntity): Long

    @Update
    suspend fun updateTax(tax: TaxEntity)

    @Delete
    suspend fun deleteTax(tax: TaxEntity)

    @Query("SELECT * FROM taxes WHERE tax_id = :taxId")
    fun getTaxById(taxId: Int): LiveData<TaxEntity>

    @Query("SELECT * FROM taxes")
    fun getAllTaxes(): LiveData<List<TaxEntity>>

    // ServiceTaxCrossRef Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceTaxCrossRef(crossRef: ServiceTaxCrossRef)

    @Delete
    suspend fun deleteServiceTaxCrossRef(crossRef: ServiceTaxCrossRef)

    @Query("SELECT * FROM service_tax_cross_ref WHERE service_id = :serviceId")
    suspend fun getTaxesForService(serviceId: Int): List<ServiceTaxCrossRef>

    @Query("DELETE FROM service_tax_cross_ref WHERE service_id = :serviceId")
    suspend fun deleteServiceTaxesForService(serviceId: Int)

    // Data Retrieval with Associations using the join table data.
    @Transaction
    @Query("SELECT * FROM services WHERE service_id = :serviceId")
    fun getServiceWithTaxCrossRefs(serviceId: Int): LiveData<ServiceWithTaxCrossRefs>

    @Transaction
    @Query("SELECT * FROM services")
    fun getAllServicesWithTaxCrossRefs(): LiveData<List<ServiceWithTaxCrossRefs>>

    // New transactional method:
    @Transaction
    suspend fun updateServiceAndTaxes(service: ServiceEntity, taxCrossRefs: List<ServiceTaxCrossRef>) {
        updateService(service)
        deleteServiceTaxesForService(service.serviceId)
        taxCrossRefs.forEach { crossRef ->
            insertServiceTaxCrossRef(crossRef.copy(serviceId = service.serviceId))
        }
    }
    
    @Transaction
    @Query("SELECT * FROM services")
    fun getAllSTInvoiceDetails(): Flow<List<ServiceWithTaxCrossRefs>>

}