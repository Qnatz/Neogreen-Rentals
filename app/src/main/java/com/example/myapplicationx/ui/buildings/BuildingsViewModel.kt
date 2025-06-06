package com.example.myapplicationx.ui.buildings

import androidx.lifecycle.*
import com.example.myapplicationx.database.dao.BuildingDao
import com.example.myapplicationx.database.model.BuildingEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
data class Building(
    val buildingId: Int,
    val buildingName: String,
    val buildingLocation: String,
    val occupiedUnits: Int = 0, 
    val vacantUnits: Int = 0,    
    val imageUrl: String? = null  
) */

@HiltViewModel
class BuildingsViewModel @Inject constructor(
    private val buildingDao: BuildingDao
) : ViewModel() {

    // Use `asLiveData()` to expose the building list as LiveData directly from the Flow
    val buildings: LiveData<List<BuildingEntity>> = buildingDao.getBuildingEntities().asLiveData()

    // Method to add a new building
    fun addBuilding(building: BuildingEntity) {
        viewModelScope.launch {
            buildingDao.insertBuilding(building)
        }
    }

    // Method to update an existing building
    fun updateBuilding(building: BuildingEntity) {
        viewModelScope.launch {
            buildingDao.updateBuilding(building)
        }
    }

    // Method to delete a building by ID
    fun deleteBuilding(buildingId: Int) {
        viewModelScope.launch {
            buildingDao.deleteBuilding(buildingId)
        }
    }

    // Get building details by ID
    fun getBuildingDetails(buildingId: Int): LiveData<BuildingEntity?> {
        return buildingDao.getBuildingEntity(buildingId).asLiveData() // Ensure you have this method in the DAO
    }
}