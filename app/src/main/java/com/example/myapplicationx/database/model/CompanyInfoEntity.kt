package com.example.myapplicationx.database.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "company_info")
data class CompanyInfoEntity(
    @PrimaryKey(autoGenerate = true)
    val companyId: Int = 0,  // Auto-generated ID

    @ColumnInfo(name = "company_name")
    val companyName: String = "",

    @ColumnInfo(name = "email", defaultValue = "")
    val email: String? = null,  // Nullable, with default value

    @ColumnInfo(name = "primary_phone")
    val primaryPhone: String = "",

    @ColumnInfo(name = "secondary_phone", defaultValue = "")
    val secondaryPhone: String? = null,  // Nullable, with default value

    @ColumnInfo(name = "address", defaultValue = "")
    val address: String? = null,  // Nullable, with default value

    @ColumnInfo(name = "website", defaultValue = "")
    val website: String? = null,  // Nullable, with default value
    
    @ColumnInfo(name = "signature_name", defaultValue = "")
    val signatureName: String? = null  // Nullable, with default value
) : Parcelable