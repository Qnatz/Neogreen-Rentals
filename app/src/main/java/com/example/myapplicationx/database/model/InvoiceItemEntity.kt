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
      tableName = "invoice_items",
      foreignKeys = [
          ForeignKey(
              entity = InvoiceEntity::class,
              parentColumns = ["invoice_number"],
              childColumns = ["invoice_number"],
              onDelete = ForeignKey.CASCADE
          )
      ],
      indices = [Index(value = ["invoice_number"])]
  )
  data class InvoiceItemEntity(
      @PrimaryKey(autoGenerate = true)
      @ColumnInfo(name = "invoice_item_id")
      val invoiceItemId: Int = 0,

      @ColumnInfo(name = "invoice_number")
      var invoiceNumber: String,

      @ColumnInfo(name = "item_description")
      var itemDescription: String,

      @ColumnInfo(name = "item_units")
      var itemUnits: Double,
      
      @ColumnInfo(name = "item_unit_price")
      var itemUnitPrice: Double,
      
      @ColumnInfo(name = "item_rate")
      var itemRate: Double,

      // Persisted calculated values:
      @ColumnInfo(name = "computed_net_base")
      var computedNetBase: Double,   // base rate with inclusive tax deducted

      @ColumnInfo(name = "inclusive_tax")
      var inclusiveTax: Double,      // tax amount already included in baseRate

      @ColumnInfo(name = "exclusive_tax")
      var exclusiveTax: Double,      // additional tax computed on net base

      @ColumnInfo(name = "computed_tax")
      var computedTax: Double,       // total tax (inclusive + exclusive)

      @ColumnInfo(name = "computed_total")
      var computedTotal: Double,     // final amount payable (baseRate + exclusive tax)

      // New field to persist tax details. Make sure you add an appropriate TypeConverter.
      @ColumnInfo(name = "tax_cross_refs")
      var taxCrossRefs: List<ServiceTaxCrossRef>? = null,

      // Add the serviceId property
      @ColumnInfo(name = "service_id")
      var serviceId: Int? = null
  ) : Parcelable