package com.example.myapplicationx.database.model

import androidx.room.*

data class InvoiceWithItems(
    @Embedded val invoice: InvoiceEntity,

    @Relation(
        parentColumn = "invoice_number",
        entityColumn = "invoice_number"
    )
    val items: List<InvoiceItemEntity>
)