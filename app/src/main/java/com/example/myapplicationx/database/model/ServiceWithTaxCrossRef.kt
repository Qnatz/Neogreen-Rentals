package com.example.myapplicationx.database.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ServiceWithTaxCrossRefs(
    @Embedded val service: ServiceEntity,
    @Relation(
        parentColumn = "service_id",
        entityColumn = "tax_id",
        associateBy = Junction(ServiceTaxCrossRef::class)
    )
    val taxCrossRefs: List<ServiceTaxCrossRef>
)