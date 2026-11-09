package com.example.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.model.Invoice
import com.example.data.repository.KosRepository

object NotificationHelper {

    const val CHANNEL_REMINDERS_ID = "kos_due_reminders_channel"
    const val CHANNEL_COMMUNITY_ID = "kos_community_channel"

    private const val NOTIFICATION_ID_BASE = 2000

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Channel 1: Pengingat Tagihan & Jatuh Tempo
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS_ID,
                "Pengingat Tagihan & Jatuh Tempo",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pengingat otomatis jatuh tempo sewa kamar KosConnect"
                enableVibration(true)
                setShowBadge(true)
            }

            // Channel 2: Pengumuman & Komplain
            val communityChannel = NotificationChannel(
                CHANNEL_COMMUNITY_ID,
                "Pengumuman & Komplain Kos",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Pengumuman dan update status perbaikan komplain kos"
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(communityChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendInvoiceDueNotification(
        context: Context,
        invoice: Invoice,
        urgencyMessage: String = "Jatuh tempo sebentar lagi!"
    ) {
        createNotificationChannels(context)

        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TO", "BILLING")
            putExtra("INVOICE_ID", invoice.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            invoice.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nominalFormatted = KosRepository.formatRupiah(invoice.totalTagihan)
        val title = "⚠️ Pengingat Tagihan Kamar ${invoice.roomNomor}"
        val shortContent = "Sewa ${invoice.bulanTagihan} ($nominalFormatted) $urgencyMessage"
        val bigContent = "Halo ${invoice.penghuniNama}, tagihan sewa kamar ${invoice.roomNomor} untuk periode ${invoice.bulanTagihan} sebesar $nominalFormatted jatuh tempo pada ${invoice.tanggalJatuhTempo}. Silakan selesaikan pembayaran via VA atau QRIS."

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(shortContent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigContent))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_send,
                "Bayar Sekarang",
                pendingIntent
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_BASE + invoice.id.hashCode() % 1000,
                notification
            )
        } catch (_: SecurityException) {
            // Handled when permission is revoked
        }
    }

    fun sendInstantNotification(
        context: Context,
        title: String,
        body: String,
        notificationId: Int = NOTIFICATION_ID_BASE + 999
    ) {
        createNotificationChannels(context)

        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Handled
        }
    }
}
