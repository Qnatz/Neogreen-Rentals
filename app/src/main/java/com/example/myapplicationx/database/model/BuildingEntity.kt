package com.example.myapplicationx.database.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Defines a building.
 * It may contain multiple houses/units.
 */
@Parcelize
@Entity(tableName = "buildings")
data class BuildingEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "building_id")
    val buildingId: Int = 0,

    @ColumnInfo(name = "building_name")
    val buildingName: String,

    @ColumnInfo(name = "building_location")
    val buildingLocation: String,

    @ColumnInfo(name = "occupied_units")
    val occupiedUnits: Int = 0,  // Default to 0 if not specified

    @ColumnInfo(name = "vacant_units")
    val vacantUnits: Int = 0,  // Default to 0 if not specified

    @ColumnInfo(name = "image_url")
    val imageUrl: String? = null
) : Parcelable {

    // Derived property to calculate the total number of units (occupied + vacant)
    val totalUnits: Int
        get() = occupiedUnits + vacantUnits
}