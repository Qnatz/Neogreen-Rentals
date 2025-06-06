package com.example.myapplicationx

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.example.myapplicationx.workers.InvoicableStatusWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MyApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleDailyInvoicableUpdate()
    }

    private fun scheduleDailyInvoicableUpdate() {
        val currentTime = System.currentTimeMillis()
        val targetTime = getTargetTimeInMillis(hour = 2, minute = 0) // Set desired time (e.g., 2:00 AM)
        val initialDelay = if (targetTime > currentTime) {
            targetTime - currentTime
        } else {
            // Schedule for the next day
            targetTime + TimeUnit.DAYS.toMillis(1) - currentTime
        }

        val workRequest = OneTimeWorkRequestBuilder<InvoicableStatusWorker>()
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            "InvoicableStatusUpdate",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        Log.d("MyApp", "Scheduled daily invoicable update with initial delay: $initialDelay ms")
    }

    private fun getTargetTimeInMillis(hour: Int, minute: Int): Long {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}