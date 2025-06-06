package com.example.myapplicationx.database.model

import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.Parcelize
import com.example.myapplicationx.database.Converters

/**
 * Represents a tenant/customer.
 * Establishes a foreign key relationship with the HouseEntity.
 */
@Parcelize
@Entity(
    tableName = "tenants",
    foreignKeys = [ForeignKey(
        entity = HouseEntity::class,
        parentColumns = ["house_id"],
        childColumns = ["house_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["house_id"])] // Index for faster queries
)
@TypeConverters(Converters::class)
data class TenantEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "tenant_id")
    val tenantId: Int = 0,

    @ColumnInfo(name = "tenant_name")
    val tenantName: String,

    @ColumnInfo(name = "primary_phone")
    val primaryPhone: String,

    @ColumnInfo(name = "secondary_phone", defaultValue = "")
    val secondaryPhone: String? = null,

    @ColumnInfo(name = "email", defaultValue = "")
    val email: String? = null,

    @ColumnInfo(name = "house_id")
    val houseId: Int, // Foreign key reference to HouseEntity

    @ColumnInfo(name = "house_name")
    val houseName: String, // Redundant but useful for quick access

    @ColumnInfo(name = "date_occupied")
    val dateOccupied: String, // Consider using Date type with TypeConverter

    @ColumnInfo(name = "date_vacated")
    val dateVacated: String? = null, // Nullable for current tenants

    @ColumnInfo(name = "credit_balance")
    var creditBalance: Double = 0.0,

    @ColumnInfo(name = "debit_balance")
    var debitBalance: Double = 0.0
) : Parcelable