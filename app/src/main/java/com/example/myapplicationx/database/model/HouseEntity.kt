package com.example.myapplicationx.database.model

import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.Parcelize

/**
 * Defines a house/unit within a building.
 */
@Parcelize
@Entity(
    tableName = "houses",
    foreignKeys = [ForeignKey(
        entity = BuildingEntity::class,
        parentColumns = ["building_id"],  // Correct column name as in BuildingEntity
        childColumns = ["building_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["building_id"])]
)
data class HouseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "house_id")
    val houseId: Int = 0,

    @ColumnInfo(name = "house_name")
    val houseName: String,

    @ColumnInfo(name = "occupied", defaultValue = "0")
    val occupied: Int = 0,

    @ColumnInfo(name = "vacant", defaultValue = "0")
    val vacant: Int = 0,

    @ColumnInfo(name = "rent_amount", defaultValue = "0.0")
    val rentAmount: Double = 0.0,

    @ColumnInfo(name = "building_id")
    val buildingId: Int
) : Parcelable {

    // Property to return status based on occupancy
    val status: String
        get() = if (occupied > 0) "Occupied" else "Vacant"
}