package com.example.myapplicationx.database.model

data class TenantData(
    val tenantId: Int,
    val tenantName: String,
    val dateOccupied: String,
    val nextBillingDate: String,
    val status: String,
    val houseId: Int,
    val occupied: Int,
    val vacant: Int
)