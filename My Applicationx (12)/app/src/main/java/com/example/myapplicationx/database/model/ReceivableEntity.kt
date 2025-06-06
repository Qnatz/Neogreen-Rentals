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
    tableName = "receivables",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["invoice_number"],
            childColumns = ["invoice_number"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["invoice_number"], unique = true)]
)
data class ReceivableEntity(
    @PrimaryKey
    @ColumnInfo(name = "invoice_number")
    val invoiceNumber: String,
    @ColumnInfo(name = "tenant_name")
    val tenantName: String,
    @ColumnInfo(name = "date_receivable")
    val dateReceivable: Long,
    @ColumnInfo(name = "amount_due")
    val amountDue: Double
) : Parcelable {
    val isOverdue: Boolean
        get() = System.currentTimeMillis() > dateReceivable

    val isPending: Boolean
        get() = System.currentTimeMillis() <= dateReceivable
}