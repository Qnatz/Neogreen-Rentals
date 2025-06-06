package com.example.myapplicationx.database.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Represents a Tenant along with their associated Services and Invoices.
 * This is a data transfer object (DTO) that combines related entities.
 */
data class TenantWithServicesAndInvoices(
    @Embedded val tenant: TenantEntity,

    @Relation(
        parentColumn = "tenantId",
        entityColumn = "tenantId"
    )
    val services: List<ServiceEntity> = emptyList(),

    @Relation(
        parentColumn = "tenantId",
        entityColumn = "tenantId"
    )
    val invoices: List<InvoiceEntity> = emptyList()
)