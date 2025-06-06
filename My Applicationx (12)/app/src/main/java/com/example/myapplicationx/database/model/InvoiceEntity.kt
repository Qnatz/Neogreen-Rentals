package com.example.myapplicationx.database.model

import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = TenantEntity::class,
            parentColumns = ["tenant_id"],
            childColumns = ["tenant_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["tenant_id"]), // Index for tenant_id for faster queries
        Index(value = ["invoice_number"], unique = true) // Unique index for invoice_number
    ]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "invoice_id")
    val invoiceId: Int = 0,

    @ColumnInfo(name = "invoice_number")
    val invoiceNumber: String, // Unique identifier

    @ColumnInfo(name = "invoice_date")
    val invoiceDate: String,

    @ColumnInfo(name = "receivable_date")
    val receivableDate: String,

    @ColumnInfo(name = "tenant_id")
    val tenantId: Int,

    @ColumnInfo(name = "tenant_name")
    val tenantName: String,

    @ColumnInfo(name = "invoice_amount")
    var invoiceAmount: Double,

    @ColumnInfo(name = "invoice_notes")
    var invoiceNotes: String,

    @ColumnInfo(name = "invoice_amount_due")
    var invoiceAmountDue: Double? = 0.0,

    @ColumnInfo(name = "status")
    var status: String? = null
) : Parcelable