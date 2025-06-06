package com.example.myapplicationx.database.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "taxes")
data class TaxEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "tax_id")
    val taxId: Int = 0,

    @ColumnInfo(name = "tax_name")
    val taxName: String,
    
    @ColumnInfo(name = "tax_percentage", defaultValue = "NULL")
    val taxPercentage: Double? = null // Nullable for optional tax percentage
) : Parcelable