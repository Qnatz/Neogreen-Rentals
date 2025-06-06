package com.example.myapplicationx.database.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReceiptInvoice(
    val invoiceNumber: String,
    val amountPaid: Double,
    val remainingBalance: Double,
    val invoiceDate: String,
    val invoiceStatus: String? = null,
    var isSelected: Boolean = false // Added selection state
) : Parcelable