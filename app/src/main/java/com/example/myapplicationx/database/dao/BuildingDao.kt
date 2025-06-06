package com.example.myapplicationx.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myapplicationx.database.model.BuildingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingDao {

    // Check if there is any data in the buildings table
    @Query("SELECT COUNT(*) > 0 FROM buildings")
    suspend fun hasData(): Boolean

    // Get a single building by ID as a Flow
    @Query("SELECT * FROM buildings WHERE building_id = :buildingId")
    fun getBuildingEntity(buildingId: Int): Flow<BuildingEntity?>

    // Get all buildings as a Flow
    @Query("SELECT * FROM buildings")
    fun getBuildingEntities(): Flow<List<BuildingEntity>>

    // Get all buildings as LiveData
    @Query("SELECT * FROM buildings")
    fun getAllBuildings(): LiveData<List<BuildingEntity>>

    // One-time retrieval of all buildings
    @Query("SELECT * FROM buildings")
    suspend fun getBuildingsOnce(): List<BuildingEntity>

    // Insert a single building (ignores duplicates)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuilding(building: BuildingEntity): Long

    // Insert a list of buildings, ignoring duplicates
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreBuildings(buildings: List<BuildingEntity>): List<Long>

    // Upsert buildings (insert new or update existing on conflict)
    @Upsert
    suspend fun upsertBuildings(buildings: List<BuildingEntity>)

    // Update a single building
    @Update
    suspend fun updateBuilding(building: BuildingEntity)

    // Delete a single building by ID
    @Query("DELETE FROM buildings WHERE building_id = :buildingId")
    suspend fun deleteBuilding(buildingId: Int)

    @Query("SELECT * FROM buildings WHERE building_id = :buildingId")
    suspend fun getBuildingById(buildingId: Int): BuildingEntity

    // Delete multiple buildings by their IDs
    @Query("DELETE FROM buildings WHERE building_id IN (:buildingIds)")
    suspend fun deleteBuildings(buildingIds: List<Int>)

    @Query("UPDATE buildings SET vacant_units = vacant_units - 1, occupied_units = occupied_units + 1 WHERE building_id = :buildingId")
    suspend fun updateUnitsOnTenantAdded(buildingId: Int)
}