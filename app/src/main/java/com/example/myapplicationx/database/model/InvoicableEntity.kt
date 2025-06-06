package com.example.myapplicationx.database.model

import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.Parcelize
import com.example.myapplicationx.database.Converters

@Parcelize
@Entity(
    tableName = "invoicables",
    foreignKeys = [ForeignKey(
        entity = TenantEntity::class,
        parentColumns = ["tenant_id"],
        childColumns = ["tenant_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
@TypeConverters(Converters::class) // Apply your custom converters
data class InvoicableEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "invoicable_id")
    val invoicableId: Int = 0,

    @ColumnInfo(name = "tenant_id", index = true)
    val tenantId: Int,

    @ColumnInfo(name = "tenant_name")
    val tenantName: String,

    @ColumnInfo(name = "next_billing_date")
    val nextBillingDate: Long, // Use LocalDate

    @ColumnInfo(name = "status")
    val status: String // "Pending", "Due", or "Overdue"
) : Parcelable