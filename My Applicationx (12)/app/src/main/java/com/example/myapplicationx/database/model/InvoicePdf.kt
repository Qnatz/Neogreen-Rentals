package com.example.myapplicationx.database.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class InvoicePdf(
    val invoiceTitle: String,
    val invoiceNumber: String,
    val invoiceDate: String,
    val receivableDate: String,
    val companyLogo: String, // Resource ID
    val companyName: String,
    val companyAddress: String,
    val companyContact: String,
    val companyTel1: String,
    val companyTel2: String,
    val customerName: String,
    val customerAddress: String,
    val customerContact: String,
    val customerTel1: String,
    val customerTel2: String,
    val invoiceItems: List<InvoiceItem>,
    val subtotal: String,
    val tax: String,
    val total: String,
    val signatureName: String,
    val paymentInstructions: String,
    val paymentBankDetails: String,
    val signature: String? = null
) : Parcelable

@Parcelize
data class InvoiceItem(
    val itemDescription: String,
    val itemRate: String,
    val itemUnits: String,
    val itemUnitPrice: String
) : Parcelable