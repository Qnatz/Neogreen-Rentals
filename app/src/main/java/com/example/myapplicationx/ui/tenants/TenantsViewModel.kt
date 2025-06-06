package com.example.myapplicationx.ui.tenants

import android.util.Log 
import androidx.lifecycle.* 
import androidx.room.withTransaction 
import com.example.myapplicationx.database.NeogreenDB 
import com.example.myapplicationx.database.dao.TenantDao 
import com.example.myapplicationx.database.dao.HouseDao 
import com.example.myapplicationx.database.model.TenantEntity 
import com.example.myapplicationx.database.model.HouseEntity 
import dagger.hilt.android.lifecycle.HiltViewModel 
import kotlinx.coroutines.Dispatchers 
import kotlinx.coroutines.launch 
import kotlinx.coroutines.withContext 
import java.text.SimpleDateFormat 
import java.util.Date 
import java.util.Locale 
import java.lang.IllegalStateException
import javax.inject.Inject
import com.example.myapplicationx.utils.Event


@HiltViewModel 
class TenantsViewModel @Inject constructor( 
private val tenantDao: TenantDao, 
private val houseDao: HouseDao, 
private val database: NeogreenDB ) : ViewModel() {

    private val _errorEvent = MutableLiveData<Event<String>>()
    val errorEvent: LiveData<Event<String>> get() = _errorEvent

// Full tenant list
val tenants: LiveData<List<TenantEntity>> = tenantDao.getTenantEntities().asLiveData()
// Dropdown tenant list (alias for fragments expecting tenantsL)
val tenantsL: LiveData<List<TenantEntity>> = tenantDao.dropdownTenants()
// Vacant houses for tenant assignment
val vacantHouses: LiveData<List<HouseEntity>> = houseDao.getVacantHouses()

// LiveData to emit newly added tenant ID
private val _addedTenantId = MutableLiveData<Long>()
val addedTenantId: LiveData<Long> get() = _addedTenantId

/**
 * Insert a new tenant and emit its generated ID.
 */
fun addTenant(tenant: TenantEntity) {
    viewModelScope.launch(Dispatchers.IO) {
        val id = tenantDao.insertTenant(tenant)
        _addedTenantId.postValue(id)
    }
}

/** Update an existing tenant. */
fun updateTenant(tenant: TenantEntity) {
    viewModelScope.launch(Dispatchers.IO) {
        tenantDao.updateTenant(tenant)
    }
}

/** Delete a tenant by ID. */
fun deleteTenant(tenantId: Int) {
    viewModelScope.launch(Dispatchers.IO) {
        tenantDao.deleteTenant(tenantId)
    }
}

/** Add credit to a tenant's balance. */
fun updateCreditBalance(tenantId: Int, amount: Double) {
    viewModelScope.launch(Dispatchers.IO) {
        try {
            if (amount < 0) {
                val currentBalance = tenantDao.getCreditBalance(tenantId)
                if (currentBalance + amount < 0) {
                    throw IllegalStateException("Updating credit balance would result in a negative balance for tenant $tenantId.")
                }
            }
            tenantDao.addCredit(tenantId, amount)
        } catch (e: IllegalStateException) {
            _errorEvent.postValue(Event(e.message ?: "Error updating credit balance due to validation issue."))
            Log.e("TenantsViewModel", "Validation error updating credit", e)
        } catch (e: Exception) {
            _errorEvent.postValue(Event("An unexpected error occurred while updating credit balance."))
            Log.e("TenantsViewModel", "Error updating credit", e)
        }
    }
}

/** Add debit to a tenant's balance. */
fun updateDebitBalance(tenantId: Int, amount: Double) {
    viewModelScope.launch(Dispatchers.IO) {
        try {
            tenantDao.addDebit(tenantId, amount)
        } catch (e: Exception) {
            _errorEvent.postValue(Event("An unexpected error occurred while updating debit balance."))
            Log.e("TenantsViewModel", "Error updating debit", e)
        }
    }
}

/** Alias for updateDebitBalance (for clearer naming). */
fun addDebitToTenant(tenantId: Int, amount: Double) = updateDebitBalance(tenantId, amount)

/** Add both credit and debit in one transaction. */
fun addBalancesToTenant(tenantId: Int, creditToAdd: Double, debitToAdd: Double) {
    viewModelScope.launch(Dispatchers.IO) {
        try {
            if (creditToAdd < 0) {
                val currentBalance = tenantDao.getCreditBalance(tenantId)
                if (currentBalance + creditToAdd < 0) {
                    throw IllegalStateException("Adding balances would result in a negative credit balance for tenant $tenantId.")
                }
            }
            tenantDao.addBalances(tenantId, creditToAdd, debitToAdd)
        } catch (e: IllegalStateException) {
            _errorEvent.postValue(Event(e.message ?: "Error adding balances due to validation issue."))
            Log.e("TenantsViewModel", "Validation error adding balances", e)
        } catch (e: Exception) {
            _errorEvent.postValue(Event("An unexpected error occurred while adding balances."))
            Log.e("TenantsViewModel", "Error adding balances", e)
        }
    }
}

/** Fetch a tenant by ID asynchronously as LiveData for UI observers. */
fun getTenantByIdLive(tenantId: Int): LiveData<TenantEntity?> = liveData(Dispatchers.IO) {
    emit(tenantDao.getTenantById(tenantId))
}

/** Fetch current credit balance as LiveData. */
fun getCreditBalanceLive(tenantId: Int): LiveData<Double> = liveData(Dispatchers.IO) {
    emit(tenantDao.getCreditBalance(tenantId))
}

/** Fetch current debit balance as LiveData. */
fun getDebitBalanceLive(tenantId: Int): LiveData<Double> = liveData(Dispatchers.IO) {
    emit(tenantDao.getDebitBalance(tenantId))
}

/**
 * Fetch house rent rate by tenant ID.
 * Provides callback for fragments expecting this signature.
 */
fun getHouseRentByTenantId(tenantId: Int, callback: (Double) -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
        val rent = try {
            tenantDao.getHouseRentByTenantId(tenantId)
        } catch (e: Exception) {
            Log.e("TenantsViewModel", "Error fetching rent", e)
            0.0
        }
        withContext(Dispatchers.Main) {
            callback(rent)
        }
    }
}

/**
 * Adjust invoice balances (credit/debit) atomically based on payment changes.
 */
fun adjustInvoiceBalances(
    tenantId: Int,
    originalPaid: Double,
    newTotal: Double
) {
    viewModelScope.launch(Dispatchers.IO) {
        try {
            tenantDao.getTenantById(tenantId)?.let { tenant ->
                database.withTransaction {
                    val currentCredit = tenant.creditBalance
                    val currentDebit = tenant.debitBalance
                val originalTotal = originalPaid + currentDebit - currentCredit

                when {
                    newTotal > originalTotal -> {
                        val diff = newTotal - originalTotal
                        val creditUsed = minOf(currentCredit, diff)
                        if (creditUsed > 0) tenantDao.deductCredit(tenantId, creditUsed)
                        val debitInc = diff - creditUsed
                        if (debitInc > 0) tenantDao.addDebit(tenantId, debitInc)
                    }
                    newTotal < originalTotal -> {
                        val diff = originalTotal - newTotal
                        val overpay = originalPaid - newTotal
                        if (overpay > 0) tenantDao.addCredit(tenantId, overpay)
                        val debitDec = diff - overpay
                        if (debitDec > 0) tenantDao.addDebit(tenantId, -debitDec)
                    }
                }
            } ?: _errorEvent.postValue(Event("Tenant not found for adjusting invoice balances."))
        } catch (e: IllegalStateException) {
            _errorEvent.postValue(Event(e.message ?: "Error adjusting invoice balances due to validation."))
        } catch (e: Exception) {
            _errorEvent.postValue(Event("An unexpected error occurred while adjusting invoice balances: ${e.message}"))
        }
    }
}

}

