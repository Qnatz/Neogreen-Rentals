package com.example.myapplicationx.database.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "service_tax_cross_ref",
    foreignKeys = [
        ForeignKey(
            entity = ServiceEntity::class,
            parentColumns = ["service_id"],
            childColumns = ["service_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TaxEntity::class,
            parentColumns = ["tax_id"],
            childColumns = ["tax_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["service_id"]),
        Index(value = ["tax_id"])
    ]
)
data class ServiceTaxCrossRef(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,  // Default value for auto-generation

    @ColumnInfo(name = "service_id")
    val serviceId: Int,

    @ColumnInfo(name = "tax_id")
    val taxId: Int,

    @ColumnInfo(name = "is_inclusive")
    val isInclusive: Boolean, // true = Inclusive, false = Exclusive

    @ColumnInfo(name = "tax_percentage_override")
    val taxPercentage: Double,

    // Added taxName for display purposes:
    @ColumnInfo(name = "tax_name")
    val taxName: String
) : Parcelable