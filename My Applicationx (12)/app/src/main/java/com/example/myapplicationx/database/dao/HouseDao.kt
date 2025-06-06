package com.example.myapplicationx.database.dao

import androidx.room.*
import com.example.myapplicationx.database.model.HouseEntity
import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.LiveData
import com.example.myapplicationx.database.model.OccupancyStatus


@Dao
interface HouseDao {

    @Query("SELECT COUNT(*) > 0 FROM houses")
    suspend fun hasData(): Boolean

    // Get a single house by ID as a Flow
    @Query("SELECT * FROM houses WHERE house_id = :houseId")
    fun getHouseEntity(houseId: Int): Flow<HouseEntity?>

    // Get all houses as a Flow
    @Query("SELECT * FROM houses")
    fun getHouseEntities(): Flow<List<HouseEntity>>

    // Get houses by building ID as a Flow
    @Query("SELECT * FROM houses WHERE building_id = :buildingId")
    fun getHousesByBuildingId(buildingId: Int): Flow<List<HouseEntity>>

    // One-off retrieval of all houses (not LiveData or Flow)
    @Query("SELECT * FROM houses")
    suspend fun getOneOffHouseEntities(): List<HouseEntity>

    // Insert a single house (ignore duplicates)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHouse(house: HouseEntity): Long

    // Insert or ignore duplicate houses
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreHouses(houses: List<HouseEntity>): List<Long>

    // Upsert houses (insert or update)
    @Upsert
    suspend fun upsertHouses(houses: List<HouseEntity>)

    // Update a single house
    @Update
    suspend fun updateHouse(house: HouseEntity)

    // Delete a single house by ID
    @Query("DELETE FROM houses WHERE house_id = :houseId")
    suspend fun deleteHouse(houseId: Int)

    // Delete multiple houses by ID
    @Query("DELETE FROM houses WHERE house_id IN (:houseIds)")
    suspend fun deleteHouses(houseIds: List<Int>)
    
    // For populating dropdown
    @Query("SELECT * FROM houses WHERE vacant = 1")
    fun getVacantHouses(): LiveData<List<HouseEntity>>
    
    @Query("UPDATE houses SET occupied = :occupied, vacant = :vacant WHERE house_id = :houseId")
    suspend fun updateHouseStatus(houseId: Int, occupied: Int, vacant: Int)

    // Get the house by its ID
    @Query("SELECT * FROM houses WHERE house_id = :houseId")
    suspend fun getHouseById(houseId: Int): HouseEntity

    @Query("SELECT building_id FROM houses WHERE house_id = :houseId")
    suspend fun getBuildingIdByHouseId(houseId: Int): Int
    
    @Query("""
        SELECT 
            (SELECT COUNT(*) FROM houses WHERE building_id = :buildingId AND occupied = 1) AS occupiedUnits,
            (SELECT COUNT(*) FROM houses WHERE building_id = :buildingId AND vacant = 1) AS vacantUnits
    """)
    suspend fun getOccupancyStatusForBuilding(buildingId: Int): OccupancyStatus
}