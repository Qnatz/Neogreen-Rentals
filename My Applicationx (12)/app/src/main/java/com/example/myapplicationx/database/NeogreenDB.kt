package com.example.myapplicationx.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplicationx.database.dao.*
import com.example.myapplicationx.database.model.*

@Database(
    entities = [
        TenantEntity::class,
        BuildingEntity::class,
        HouseEntity::class,
        CompanyInfoEntity::class,
        TaxEntity::class,
        ReceivableEntity::class,
        InvoiceEntity::class,
        ReceiptEntity::class,
        InvoicableEntity::class,
        InvoiceItemEntity::class,
        CreditEntryEntity::class,
        ServiceEntity::class,
        ServiceTaxCrossRef::class,
        ReceiptInvoiceCrossRef::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class NeogreenDB : RoomDatabase() {
    abstract fun tenantDao(): TenantDao

    abstract fun buildingDao(): BuildingDao

    abstract fun houseDao(): HouseDao

    abstract fun companyInfoDao(): CompanyInfoDao

    abstract fun taxDao(): TaxDao

    abstract fun serviceDao(): ServiceDao

    abstract fun receivableDao(): ReceivableDao

    abstract fun invoiceDao(): InvoiceDao

    abstract fun receiptDao(): ReceiptDao

    abstract fun invoicableDao(): InvoicableDao

    abstract fun creditEntryDao(): CreditEntryDao

    abstract fun invoiceItemDao(): InvoiceItemDao

    abstract fun serviceTaxCrossRefDao(): ServiceTaxCrossRefDao

    abstract fun receiptInvoiceCrossRefDao(): ReceiptInvoiceCrossRefDao

    companion object {
        @Volatile
        private var INSTANCE: NeogreenDB? = null

        fun getInstance(context: Context): NeogreenDB =
            INSTANCE ?: synchronized(this) {
                val instance =
                    Room
                        .databaseBuilder(
                            context.applicationContext,
                            NeogreenDB::class.java,
                            "neogreen_database",
                        ).build()
                INSTANCE = instance
                instance
            }
    }
}
