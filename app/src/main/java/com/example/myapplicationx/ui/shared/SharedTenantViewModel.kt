package com.example.myapplicationx.ui.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.ZoneId
import com.example.myapplicationx.database.model.InvoicableEntity
import com.example.myapplicationx.database.dao.InvoicableDao
import com.example.myapplicationx.database.dao.HouseDao
import com.example.myapplicationx.database.model.TenantData
import javax.inject.Inject

@HiltViewModel
class SharedTenantViewModel @Inject constructor(
    private val invoicableDao: InvoicableDao,
    private val houseDao: HouseDao
) : ViewModel() {

    private val _tenantData = MutableLiveData<TenantData?>()
    val tenantData: LiveData<TenantData?> get() = _tenantData

    fun setTenantData(
        tenantId: Int,
        tenantName: String,
        dateOccupied: String,
        nextBillingDate: String,
        status: String,
        houseId: Int,
        occupied: Int,
        vacant: Int
    ) {
        val tenant = TenantData(
            tenantId = tenantId,
            tenantName = tenantName,
            dateOccupied = dateOccupied,
            nextBillingDate = nextBillingDate,
            status = status,
            houseId = houseId,
            occupied = occupied,
            vacant = vacant
        )

        _tenantData.value = tenant
        createInvoicableForTenant(tenant)
    }

    private fun createInvoicableForTenant(tenant: TenantData) {
        viewModelScope.launch {
            val nextBillingDate = convertToLong(tenant.dateOccupied)
            val invoicable = InvoicableEntity(
                tenantId = tenant.tenantId,
                tenantName = tenant.tenantName,
                nextBillingDate = nextBillingDate,
                status = "Pending"
            )
            invoicableDao.insertInvoicable(invoicable)
            
            // Update house occupancy
            updateHouseOccupancy(tenant.houseId, tenant.occupied, tenant.vacant)
        }
    }
    
   fun updateHouseOccupancy(houseId: Int, occupied: Int, vacant: Int) {
    viewModelScope.launch {
        val house = houseDao.getHouseById(houseId) // Fetch house from database
        house?.let {
            val updatedHouse = it.copy(occupied = occupied, vacant = vacant)
            houseDao.updateHouse(updatedHouse) // Update the house in the database
            
        }
      }
    }
    
    private fun convertToLong(startDate: String): Long =
    LocalDate.parse(startDate)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant().toEpochMilli()

    private fun calculateNextDueDate(startDate: String): Long =
    LocalDate.parse(startDate).plusMonths(1)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant().toEpochMilli()
    

    private fun parseDate(dateString: String): LocalDate {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
        return LocalDate.parse(dateString, formatter)
    }
}