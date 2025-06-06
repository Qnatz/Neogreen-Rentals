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
    tableName = "receipt_invoice_xref",
    foreignKeys = [
        ForeignKey(
            entity = ReceiptEntity::class,
            parentColumns = ["receipt_number"],  // Reference receipt_number as FK
            childColumns = ["receipt_number"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["invoice_number"],  // Reference invoice_number as FK
            childColumns = ["invoice_number"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["receipt_number", "invoice_number"], unique = true),
    Index(value = ["invoice_number"])]
)
data class ReceiptInvoiceCrossRef(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rixref_id")
    val rixrefId: Long = 0,

    @ColumnInfo(name = "receipt_number")
    val receiptNumber: String,

    @ColumnInfo(name = "invoice_number")
    val invoiceNumber: String,

    @ColumnInfo(name = "amount_paid")
    val amountPaid: Double,

    @ColumnInfo(name = "receipt_date")
    val paymentDate: String
) :Parcelable