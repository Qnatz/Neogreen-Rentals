package com.example.myapplicationx.database.model

import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@Entity(
    tableName = "credit_entries",
    foreignKeys = [
        ForeignKey(
            entity = TenantEntity::class,
            parentColumns = ["tenant_id"], // Matches the column name in TenantEntity
            childColumns = ["tenant_id"], // Matches the column name in CreditEntryEntity
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["tenant_id"]), // Matches the column name in CreditEntryEntity
        Index(value = ["credit_entry_number"], unique = true)
    ]
)
data class CreditEntryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "credit_entry_id")
    val creditEntryId: Int = 0,

    @ColumnInfo(name = "credit_entry_number")
    val creditEntryNumber: String,

    @ColumnInfo(name = "tenant_id") // Matches the column name used in the ForeignKey
    val tenantId: Int,

    @ColumnInfo(name = "tenant_name")
    val tenantName: String,

    @ColumnInfo(name = "credit_date")
    val creditDate: String,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "credit_note")
    val creditNote: String
) : Parcelable