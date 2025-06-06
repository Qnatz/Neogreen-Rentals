package com.example.myapplicationx.ui.settings.services

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.viewModelScope
import com.example.myapplicationx.database.dao.ServiceDao
import com.example.myapplicationx.database.model.ServiceEntity
import com.example.myapplicationx.database.model.ServiceTaxCrossRef
import com.example.myapplicationx.database.model.TaxEntity
import com.example.myapplicationx.database.model.ServiceWithTaxCrossRefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServicesViewModel @Inject constructor(
    private val serviceDao: ServiceDao
) : ViewModel() {

    val allServices: LiveData<List<ServiceEntity>> = serviceDao.getAllServices()
    val allTaxes: LiveData<List<TaxEntity>> = serviceDao.getAllTaxes()
    val allSTInvoiceDetails: Flow<List<ServiceWithTaxCrossRefs>> = serviceDao.getAllSTInvoiceDetails()
      // Exposes a list of services along with their associated taxes
    val allServicesWithTaxes: LiveData<List<ServiceWithTaxCrossRefs>> = serviceDao.getAllServicesWithTaxCrossRefs()


    fun insertService(service: ServiceEntity) = viewModelScope.launch {
        serviceDao.insertService(service)
    }

    fun updateService(service: ServiceEntity) = viewModelScope.launch {
        serviceDao.updateService(service)
    }

    fun deleteService(service: ServiceEntity) = viewModelScope.launch {
        serviceDao.deleteService(service)
    }

    fun getServiceById(serviceId: Int): LiveData<ServiceEntity> = serviceDao.getServiceById(serviceId)

    // Updated method: returns ServiceWithTaxCrossRefs including join data (isInclusive, etc.)
    fun getServiceWithTaxCrossRefs(serviceId: Int): LiveData<ServiceWithTaxCrossRefs> =
        serviceDao.getServiceWithTaxCrossRefs(serviceId)

    fun insertServiceWithTaxes(service: ServiceEntity, taxCrossRefs: List<ServiceTaxCrossRef>) = viewModelScope.launch {
        val id = serviceDao.insertService(service).toInt()
        if (id > 0) { // Ensure service was inserted successfully
            taxCrossRefs.forEach { crossRef ->
                // Update the cross-reference with the generated service id.
                serviceDao.insertServiceTaxCrossRef(crossRef.copy(serviceId = id))
            }
        } else {
            // Handle insertion failure if needed.
        }
    }

    fun updateServiceWithTaxes(service: ServiceEntity, taxCrossRefs: List<ServiceTaxCrossRef>) = viewModelScope.launch {
        serviceDao.updateServiceAndTaxes(service, taxCrossRefs)
    }
}