package com.example.myapplicationx.ui.buildings

import androidx.lifecycle.*
import com.example.myapplicationx.database.model.HouseEntity
import com.example.myapplicationx.database.model.BuildingEntity
import com.example.myapplicationx.database.dao.HouseDao
import com.example.myapplicationx.database.dao.BuildingDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.example.myapplicationx.database.model.OccupancyStatus
import javax.inject.Inject

/**data class House(
    val houseId: Int,
    val houseName: String,
    val occupied: Int,
    val vacant: Int,
    val buildingId: Int? = null
)*/


@HiltViewModel
class HousesViewModel @Inject constructor(
    private val houseDao: HouseDao,
    private val buildingDao: BuildingDao,
) : ViewModel() {

    val houses: LiveData<List<HouseEntity>> = houseDao.getHouseEntities().asLiveData()

    fun addHouse(house: HouseEntity) {
        viewModelScope.launch {
            houseDao.insertHouse(house)

            val building = buildingDao.getBuildingById(house.buildingId)
            building?.let {
                val updatedBuilding = it.copy(vacantUnits = it.vacantUnits + 1)
                buildingDao.updateBuilding(updatedBuilding)
            }
        }
    }

    fun updateHouse(house: HouseEntity) {
        viewModelScope.launch {
            houseDao.updateHouse(house)
        }
    }

    fun deleteHouse(houseId: Int) {
        viewModelScope.launch {
            houseDao.deleteHouse(houseId)
        }
    }

    fun getHousesByBuildingId(buildingId: Int): LiveData<List<HouseEntity>> {
        return houseDao.getHousesByBuildingId(buildingId).asLiveData()
    }

    fun getHouseByHouseId(houseId: Int): LiveData<HouseEntity?> {
        return houseDao.getHouseEntity(houseId).asLiveData()
    }

    // Function to retrieve occupancy status for a specific building
    fun fetchOccupancyStatus(buildingId: Int): LiveData<OccupancyStatus> {
        val occupancyStatusLiveData = MutableLiveData<OccupancyStatus>()

        // Use viewModelScope.launch to call the suspending function
        viewModelScope.launch {
            try {
                // Get occupancy status in the background
                val occupancyStatus = houseDao.getOccupancyStatusForBuilding(buildingId)
                occupancyStatusLiveData.postValue(occupancyStatus) // Post value to LiveData
            } catch (e: Exception) {
                // Handle error if necessary
                occupancyStatusLiveData.postValue(null) // Or handle error state
            }
        }

        return occupancyStatusLiveData
    }

    fun updateHouseOccupancy(houseId: Int, isOccupied: Boolean) {
        viewModelScope.launch {
            val house = houseDao.getHouseById(houseId)
            house?.let {
                val updatedHouse = it.copy(
                    occupied = if (isOccupied) 1 else 0,
                    vacant = if (isOccupied) 0 else 1
                )
                houseDao.updateHouse(updatedHouse)
            }
        }
    }
    
   fun houseVacated(houseId: Int, occupied: Int, vacant: Int) {
    viewModelScope.launch {
        val house = houseDao.getHouseById(houseId) // Fetch house from database
        house?.let {
            val updatedHouse = it.copy(occupied = occupied, vacant = vacant)
            houseDao.updateHouse(updatedHouse) // Update the house in the database
            
        }
      }
    }
}