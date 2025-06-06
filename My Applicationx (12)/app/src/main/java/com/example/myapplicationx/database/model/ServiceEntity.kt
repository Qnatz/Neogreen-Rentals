package com.example.myapplicationx.database.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "service_id")
    val serviceId: Int = 0,

    @ColumnInfo(name = "service_name")
    val serviceName: String,

    @ColumnInfo(name = "is_metered")
    val isMetered: Boolean,

    @ColumnInfo(name = "is_fixed_price")
    val isFixedPrice: Boolean,

    @ColumnInfo(name = "unit_price")
    val unitPrice: Double? = null,

    @ColumnInfo(name = "fixed_price")
    val fixedPrice: Double? = null
) : Parcelable