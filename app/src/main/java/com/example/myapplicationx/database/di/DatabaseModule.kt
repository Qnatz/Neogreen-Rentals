package com.example.myapplicationx.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplicationx.database.NeogreenDB
import com.example.myapplicationx.database.dao.*
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Singleton



@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope
    ): NeogreenDB {
        val database = Room.databaseBuilder(
            context.applicationContext,
            NeogreenDB::class.java,
            "neogreen_db"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    scope.launch(Dispatchers.IO) {
                        //database.tenantDao().upsertTenants(SampleTenants().getTenants() ?: emptyList())
                       // database.buildingDao().upsertBuildings(SampleBuildings().getBuildings() ?: emptyList())
                       // database.houseDao().upsertHouses(SampleHouses().getHouses() ?: emptyList())
                    }
                }
            })
            .fallbackToDestructiveMigration()
            .build()
        return database
    }

    @Provides
    fun provideTenantDao(database: NeogreenDB): TenantDao = database.tenantDao()

    @Provides
    fun provideBuildingDao(database: NeogreenDB): BuildingDao = database.buildingDao()

    @Provides
    fun provideHouseDao(database: NeogreenDB): HouseDao = database.houseDao()

    @Provides
    fun provideTaxDao(database: NeogreenDB): TaxDao = database.taxDao()

    @Provides
    fun provideCompanyInfoDao(database: NeogreenDB): CompanyInfoDao = database.companyInfoDao()
    
    @Provides
    fun provideReceivableDao(database: NeogreenDB): ReceivableDao = database.receivableDao()

    @Provides
    fun provideInvoiceDao(database: NeogreenDB): InvoiceDao = database.invoiceDao()
    
    @Provides
    fun provideReceiptDao(database: NeogreenDB): ReceiptDao = database.receiptDao()
    
    @Provides
    fun provideInvoicableDao(database: NeogreenDB): InvoicableDao = database.invoicableDao()
    
   @Provides
    fun provideCreditEntryDao(database: NeogreenDB): CreditEntryDao = database.creditEntryDao()
    
    @Provides
    fun provideInvoiceItemDao(database: NeogreenDB): InvoiceItemDao = database.invoiceItemDao()
    
    @Provides
    fun provideServiceDao(database: NeogreenDB): ServiceDao = database.serviceDao()

    @Provides
    fun provideServiceTaxCrossRefDao(database: NeogreenDB): ServiceTaxCrossRefDao = database.serviceTaxCrossRefDao()
    
    @Provides
    fun provideReceiveInvoiceCrossRefDao(database: NeogreenDB): ReceiptInvoiceCrossRefDao = database.receiptInvoiceCrossRefDao()

}