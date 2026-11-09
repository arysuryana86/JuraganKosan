package com.example.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.data.model.PaymentStatus
import com.example.data.repository.KosRepository
import java.util.concurrent.TimeUnit

class DueReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val repository = KosRepository()
            val unpaidInvoices = repository.invoices.value.filter {
                it.statusPembayaran != PaymentStatus.LUNAS
            }

            var notificationCount = 0

            unpaidInvoices.forEach { invoice ->
                // Determine urgency based on invoice data
                val urgencyMsg = when {
                    invoice.statusPembayaran == PaymentStatus.MENUNGGU_VERIFIKASI ->
                        "Bukti transfer terkirim, menunggu verifikasi Admin."
                    invoice.denda > 0 ->
                        "⚠️ Telah lewat jatuh tempo! Denda telah ditambahkan."
                    else ->
                        "Jatuh tempo pada ${invoice.tanggalJatuhTempo}. Harap segera dibayar."
                }

                NotificationHelper.sendInvoiceDueNotification(
                    context = appContext,
                    invoice = invoice,
                    urgencyMessage = urgencyMsg
                )
                notificationCount++
            }

            Result.success(workDataOf("NOTIFIED_COUNT" to notificationCount))
        } catch (e: Exception) {
            Result.failure(workDataOf("ERROR_MSG" to (e.localizedMessage ?: "Unknown error")))
        }
    }

    companion object {
        const val PERIODIC_WORK_TAG = "kos_due_reminder_periodic_work"
        const val IMMEDIATE_WORK_TAG = "kos_due_reminder_immediate_work"

        fun enqueuePeriodicReminder(context: Context) {
            val periodicRequest = PeriodicWorkRequestBuilder<DueReminderWorker>(
                12, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        }

        fun triggerImmediateReminder(context: Context) {
            val immediateRequest = OneTimeWorkRequestBuilder<DueReminderWorker>().build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                IMMEDIATE_WORK_TAG,
                ExistingWorkPolicy.REPLACE,
                immediateRequest
            )
        }
    }
}
