package com.example.myapplicationx.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplicationx.database.dao.InvoicableDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@HiltWorker
class InvoicableStatusWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted workerParams: WorkerParameters,
        private val invoicableDao: InvoicableDao,
    ) : CoroutineWorker(context, workerParams) {
        override suspend fun doWork(): Result {
            try {
                val invoicables = invoicableDao.getAllInvoicables()
                val updatedInvoicables =
                    invoicables.map { invoicable ->
                        // Calculate days until the next billing date
                        val daysLeft = calculateDaysUntilNextBilling(invoicable.nextBillingDate)
                        // If the billing date is in the future (daysLeft > 0), status should be "Dueup"
                        // Otherwise (today or in the past), status remains "Pending"
                        val computedStatus = if (daysLeft > 0) "Dueup" else "Pending"
                        if (invoicable.status != computedStatus) {
                            invoicable.copy(status = computedStatus)
                        } else {
                            invoicable
                        }
                    }
                invoicableDao.updateInvoicables(updatedInvoicables)
                return Result.success()
            } catch (e: Exception) {
                return Result.failure()
            }
        }

        private fun calculateDaysUntilNextBilling(nextBillingDate: Long): Long {
            val billingDate =
                Instant
                    .ofEpochMilli(nextBillingDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            return ChronoUnit.DAYS.between(LocalDate.now(), billingDate)
        }
    }
