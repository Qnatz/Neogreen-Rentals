package com.example.myapplicationx.ui.settings.taxes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplicationx.database.model.TaxEntity
import com.example.myapplicationx.database.dao.TaxDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaxesViewModel @Inject constructor(
    private val taxDao: TaxDao
) : ViewModel() {

    // Get all taxes as a Flow
    val taxes: Flow<List<TaxEntity>> = taxDao.getTaxEntities()

    // Add a new tax
    fun addTax(tax: TaxEntity) {
        viewModelScope.launch {
            taxDao.insertTax(tax)
        }
    }

    // Update an existing tax
    fun updateTax(tax: TaxEntity) {
        viewModelScope.launch {
            taxDao.updateTax(tax)
        }
    }

    // Delete a tax by ID
    fun deleteTax(taxId: Int) {
        viewModelScope.launch {
            taxDao.deleteTax(taxId)
        }
    }

    // Delete multiple taxes by ID
    fun deleteTaxes(taxIds: List<Int>) {
        viewModelScope.launch {
            taxDao.deleteTaxes(taxIds)
        }
    }
}