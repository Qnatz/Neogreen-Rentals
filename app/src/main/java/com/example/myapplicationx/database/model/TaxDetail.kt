package com.example.myapplicationx.database.model

data class TaxDetail(
    val taxName: String,
    val isInclusive: Boolean,
    val taxPercentage: Double,
    val taxAmount: Double
)