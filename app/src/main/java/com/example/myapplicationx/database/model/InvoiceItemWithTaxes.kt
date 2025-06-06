package com.example.myapplicationx.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class InvoiceItemWithTaxes(
    @Embedded val invoiceItem: InvoiceItemEntity,
    @Relation(
        parentColumn = "invoiceItemId", // adjust this to match the column name in InvoiceItemEntity
        entityColumn = "serviceId"      // adjust to match the column in ServiceTaxCrossRef that links to the invoice item
    )
    val taxCrossRefs: List<ServiceTaxCrossRef>
)