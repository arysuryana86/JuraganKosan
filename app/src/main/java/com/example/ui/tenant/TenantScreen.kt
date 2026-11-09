package com.example.ui.tenant

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.data.repository.KosRepository
import com.example.ui.KosViewModel
import com.example.ui.admin.DigitalReceiptDialog
import com.example.ui.components.ImagePreviewDialog
import com.example.ui.components.NotificationPermissionBanner
import com.example.ui.components.PhotoUploadPicker
import com.example.ui.theme.*

// PORTAL PENGHUNI MANDIRI (Role-Based View: Tenant)
@Composable
fun TenantScreen(
    viewModel: KosViewModel
) {
    val invoices by viewModel.invoices.collectAsState()
    val complaints by viewModel.complaints.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val context = LocalContext.current

    // Tenant Ahmad Pratama (Kamar 101)
    val roomInvoices = invoices.filter { it.roomNomor == "101" }
    val myInvoice = roomInvoices.firstOrNull { it.bulanTagihan.contains("November", ignoreCase = true) }
        ?: roomInvoices.firstOrNull()
        ?: invoices.first()
    val pastInvoices = roomInvoices.filter { it.id != myInvoice.id }
    val myComplaints = complaints.filter { it.roomNomor == "101" }

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.BCA_VA) }
    var showPaymentModal by remember { mutableStateOf(false) }
    var selectedInvoiceForReceipt by remember { mutableStateOf<Invoice?>(null) }
    var showReportDialog by remember { mutableStateOf(false) }

    // Image preview state
    var previewImage by remember { mutableStateOf<String?>(null) }
    var previewTitle by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KosBackground)
    ) {
        // Tenant Header Profile
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(KosBluePrimary)
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(KosWhite),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = KosBluePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Halo, Ahmad Pratama! 👋",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = KosWhite
                        )
                        Text(
                            text = "Kamar 101 • Deluxe AC (Lt. 1)",
                            style = MaterialTheme.typography.bodySmall,
                            color = KosWhite.copy(alpha = 0.85f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(KosWhite.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Penghuni", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KosWhite)
                }
            }
        }

        // Notification Permission Prompt (Android 13+)
        NotificationPermissionBanner()

        // 4 Dedicated Navigation Tabs for Tenant Portal
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = KosWhite,
            contentColor = KosBluePrimary,
            edgePadding = 8.dp
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Status Sewa", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_tenant_lease_status")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Bayar Sewa", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_tenant_billing")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Riwayat Bayar", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_tenant_history")
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Komplain & Info", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_tenant_complaint_info")
            )
        }

        when (selectedTab) {
            0 -> TenantLeaseStatusTab(
                invoice = myInvoice,
                onNavigateToPay = { selectedTab = 1 },
                onViewReceipt = { selectedInvoiceForReceipt = myInvoice }
            )
            1 -> TenantBillingTab(
                invoice = myInvoice,
                pastInvoices = pastInvoices,
                selectedMethod = selectedPaymentMethod,
                onSelectMethod = { selectedPaymentMethod = it },
                onPayClick = { showPaymentModal = true },
                onViewReceiptClick = { selectedInvoiceForReceipt = myInvoice },
                onViewProofPhoto = { path, title ->
                    previewImage = path
                    previewTitle = title
                },
                onTestReminderNotification = {
                    viewModel.sendTestTenantReminder(context, myInvoice)
                    Toast.makeText(context, "Notifikasi pengingat dikirim ke status bar HP!", Toast.LENGTH_SHORT).show()
                }
            )
            2 -> TenantPaymentHistoryTab(
                invoices = roomInvoices.sortedByDescending { it.id },
                onViewReceipt = { selectedInvoiceForReceipt = it },
                onPayPending = {
                    selectedTab = 1
                    showPaymentModal = true
                }
            )
            3 -> TenantComplaintAndInfoTab(
                complaints = myComplaints,
                announcements = announcements,
                onAddComplaintClick = { showReportDialog = true },
                onViewPhoto = { path, title ->
                    previewImage = path
                    previewTitle = title
                }
            )
        }
    }

    // Modal Payment Simulation
    if (showPaymentModal) {
        PaymentSimulationDialog(
            invoice = myInvoice,
            method = selectedPaymentMethod,
            onDismiss = { showPaymentModal = false },
            onConfirmPay = { buktiPath ->
                viewModel.payInvoice(myInvoice.id, selectedPaymentMethod, buktiPath)
                showPaymentModal = false
                Toast.makeText(context, "Pembayaran berhasil diverifikasi secara instan!", Toast.LENGTH_LONG).show()
            },
            onUploadProofOnly = { buktiPath ->
                viewModel.uploadPaymentProof(myInvoice.id, buktiPath)
                showPaymentModal = false
                Toast.makeText(context, "Bukti pembayaran terkirim! Pengelola akan segera memverifikasi.", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Modal Kuitansi Resmi Digital
    if (selectedInvoiceForReceipt != null) {
        DigitalReceiptDialog(
            invoice = selectedInvoiceForReceipt!!,
            onDismiss = { selectedInvoiceForReceipt = null }
        )
    }

    // Modal Report Complaint
    if (showReportDialog) {
        TenantReportDialog(
            onDismiss = { showReportDialog = false },
            onSubmit = { judul, deskripsi, fotoKerusakan ->
                viewModel.addComplaint(
                    penghuniNama = "Ahmad Pratama",
                    roomNomor = "101",
                    judul = judul,
                    deskripsi = deskripsi,
                    kategori = "Fasilitas Kamar",
                    fotoKerusakanUri = fotoKerusakan
                )
                showReportDialog = false
                Toast.makeText(context, "Keluhan beserta foto kerusakan telah terkirim.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal Image Preview
    if (previewImage != null) {
        ImagePreviewDialog(
            title = previewTitle,
            imagePath = previewImage!!,
            onDismiss = { previewImage = null }
        )
    }
}

// TAB 0: STATUS SEWA & SISA MASA TINGGAL
@Composable
private fun TenantLeaseStatusTab(
    invoice: Invoice,
    onNavigateToPay: () -> Unit,
    onViewReceipt: () -> Unit
) {
    val isPaid = invoice.statusPembayaran == PaymentStatus.LUNAS

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Ringkasan Status Kontrak Aktif
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = KosWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Kamar 101 • Deluxe AC",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = KosTextPrimary
                            )
                            Text(
                                text = "Lantai 1 • KosConnect Residence",
                                fontSize = 12.sp,
                                color = KosTextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(KosSuccessContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "SEWA AKTIF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = KosSuccess
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sisa Masa Tinggal Visual Countdown
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(KosBlueContainer.copy(alpha = 0.5f))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        tint = KosBluePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Sisa Masa Tinggal",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = KosBluePrimary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(KosBluePrimary)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "52 Hari Tersisa",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = KosWhite
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Linear progress indicator: 313 / 365 hari
                            LinearProgressIndicator(
                                progress = { 313f / 365f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = KosBluePrimary,
                                trackColor = KosWhite
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Mulai: 1 Jan 2026", fontSize = 11.sp, color = KosTextSecondary)
                                Text("Berakhir: 31 Des 2026 (1 Tahun)", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = KosTextPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Detail Kontrak & Deposit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Deposit Jaminan", fontSize = 11.sp, color = KosTextSecondary)
                            Text("Rp 500.000 (Aman)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KosSuccess)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Jatuh Tempo Bulanan", fontSize = 11.sp, color = KosTextSecondary)
                            Text("Tiap Tgl 10", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KosTextPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Tarif Kamar", fontSize = 11.sp, color = KosTextSecondary)
                            Text("Rp 1.800.000 / bln", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KosBluePrimary)
                        }
                    }
                }
            }
        }

        // 2. Tagihan Bulan Berjalan Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = KosWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tagihan Bulan Ini (${invoice.bulanTagihan})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = KosTextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isPaid) KosSuccessContainer else KosErrorContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isPaid) "LUNAS" else "BELUM BAYAR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPaid) KosSuccess else KosError
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total yang harus dibayar:", fontSize = 12.sp, color = KosTextSecondary)
                            Text(
                                text = KosRepository.formatRupiah(invoice.totalTagihan),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isPaid) KosSuccess else KosBluePrimary
                            )
                        }

                        if (isPaid) {
                            OutlinedButton(
                                onClick = onViewReceipt,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null, tint = KosSuccess, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Kuitansi", color = KosSuccess, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        } else {
                            Button(
                                onClick = onNavigateToPay,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_quick_pay")
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, tint = KosWhite, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Bayar Sekarang", color = KosWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // 3. Fasilitas Kamar & Keuntungan Penghuni
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = KosWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Fasilitas Kamar Anda",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = KosTextPrimary
                    )
                    Text(
                        text = "Fasilitas lengkap standar kenyamanan KosConnect",
                        fontSize = 12.sp,
                        color = KosTextSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    val facilities = listOf(
                        Pair(Icons.Default.AcUnit, "AC 1 PK Inverter"),
                        Pair(Icons.Default.Bathtub, "Kamar Mandi Dalam & Water Heater"),
                        Pair(Icons.Default.Bed, "Kasur Springbed Queen (160x200)"),
                        Pair(Icons.Default.Wifi, "High-Speed WiFi 100 Mbps"),
                        Pair(Icons.Default.MeetingRoom, "Lemari Pakaian 2 Pintu & Cermin"),
                        Pair(Icons.Default.Desk, "Meja & Kursi Kerja Ergonomis"),
                        Pair(Icons.Default.WbSunny, "Jendela Hadap Taman Alami")
                    )

                    facilities.forEach { (icon, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(KosBlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = null, tint = KosBluePrimary, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(name, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = KosTextPrimary)
                        }
                    }
                }
            }
        }

        // 4. Aturan Pengembalian Deposit
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = KosBlueContainer.copy(alpha = 0.4f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, KosBluePrimary.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = KosBluePrimary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Deposit jaminan Rp 500.000 akan ditransfer kembali secara utuh H+1 setelah checkout jika kamar dan perabotan dalam kondisi baik.",
                        fontSize = 11.5.sp,
                        color = KosTextPrimary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

// TAB 1: BAYAR SEWA & INSTRUKSI VA/QRIS
@Composable
private fun TenantBillingTab(
    invoice: Invoice,
    pastInvoices: List<Invoice>,
    selectedMethod: PaymentMethod,
    onSelectMethod: (PaymentMethod) -> Unit,
    onPayClick: () -> Unit,
    onViewReceiptClick: () -> Unit,
    onViewProofPhoto: (String, String) -> Unit,
    onTestReminderNotification: () -> Unit
) {
    val isPaid = invoice.statusPembayaran == PaymentStatus.LUNAS
    val isPendingVerification = invoice.statusPembayaran == PaymentStatus.MENUNGGU_VERIFIKASI

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            // Status Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isPaid -> KosSuccessContainer
                        isPendingVerification -> KosWarningContainer
                        else -> KosErrorContainer
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isPaid -> KosSuccess
                                    isPendingVerification -> KosWarning
                                    else -> KosError
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isPaid -> Icons.Default.CheckCircle
                                isPendingVerification -> Icons.Default.HourglassTop
                                else -> Icons.Default.AccessTime
                            },
                            contentDescription = null,
                            tint = KosWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                isPaid -> "TAGIHAN SUDAH LUNAS"
                                isPendingVerification -> "MENUNGGU VERIFIKASI"
                                else -> "BELUM DIBAYAR"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = when {
                                isPaid -> KosSuccess
                                isPendingVerification -> KosWarning
                                else -> KosError
                            }
                        )
                        Text(
                            text = when {
                                isPaid -> "Terima kasih, pembayaran Anda telah diverifikasi."
                                isPendingVerification -> "Bukti transfer telah dikirim, menunggu konfirmasi pengelola."
                                else -> "Jatuh tempo: ${invoice.tanggalJatuhTempo}"
                            },
                            fontSize = 12.sp,
                            color = KosTextSecondary
                        )
                    }

                    if (isPaid) {
                        OutlinedButton(
                            onClick = onViewReceiptClick,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Kuitansi", fontSize = 11.sp, color = KosSuccess, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Card Pengingat Otomatis Jatuh Tempo
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = KosBlueContainer.copy(alpha = 0.6f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, KosBluePrimary.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(KosBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = KosWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pengingat Otomatis Jatuh Tempo (Aktif)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = KosBluePrimary
                            )
                            Text(
                                text = "Notifikasi otomatis dikirim H-3 & H-1 via WorkManager.",
                                fontSize = 11.sp,
                                color = KosTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onTestReminderNotification,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .testTag("btn_tenant_test_reminder")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = KosBluePrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tes Notifikasi Pengingat di HP Saya",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = KosBluePrimary
                        )
                    }
                }
            }
        }

        // Uploaded payment proof card (if any)
        if (!invoice.buktiPembayaranUri.isNullOrBlank()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            onViewProofPhoto(invoice.buktiPembayaranUri, "Bukti Pembayaran Kamar ${invoice.roomNomor}")
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = KosWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(KosSurfaceVariant)
                        ) {
                            AsyncImage(
                                model = invoice.buktiPembayaranUri,
                                contentDescription = "Bukti Transfer",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bukti Transfer Terlampir",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = KosTextPrimary
                            )
                            Text(
                                text = "Ketuk untuk melihat foto struk pembayaran",
                                fontSize = 11.sp,
                                color = KosTextSecondary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Lihat",
                            tint = KosBluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Rincian Tagihan Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = KosWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Rincian Tagihan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = KosTextPrimary
                        )
                        Text(
                            text = invoice.bulanTagihan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = KosBluePrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    BillItemRow(label = "Sewa Kamar ${invoice.roomNomor}", amount = KosRepository.formatRupiah(invoice.nominalKamar))
                    Spacer(modifier = Modifier.height(8.dp))
                    BillItemRow(label = "Tambahan Listrik & Air", amount = KosRepository.formatRupiah(invoice.tambahanListrik))
                    Spacer(modifier = Modifier.height(8.dp))
                    BillItemRow(label = "Denda Keterlambatan", amount = KosRepository.formatRupiah(invoice.denda))

                    Divider(color = KosOutline, modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Tagihan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = KosTextPrimary
                        )
                        Text(
                            text = KosRepository.formatRupiah(invoice.totalTagihan),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = KosBluePrimary
                        )
                    }
                }
            }
        }

        // Metode Pembayaran Card
        if (!isPaid) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = KosWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Pilih Metode Pembayaran",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = KosTextPrimary
                        )
                        Text(
                            text = "Pembayaran otomatis diverifikasi secara instan",
                            fontSize = 12.sp,
                            color = KosTextSecondary,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )

                        PaymentMethod.values().forEach { method ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSelectMethod(method) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedMethod == method,
                                    onClick = { onSelectMethod(method) },
                                    colors = RadioButtonDefaults.colors(selectedColor = KosBluePrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = method.label,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = KosTextPrimary
                                    )
                                    Text(
                                        text = if (method == PaymentMethod.QRIS) "Scan instan tanpa biaya admin" else "Nomor VA otomatis aktif 24 jam",
                                        fontSize = 11.sp,
                                        color = KosTextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onPayClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_bayar_sekarang"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, tint = KosWhite)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BAYAR SEKARANG",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = KosWhite
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = KosWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = KosSuccess,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Kuitansi Pembayaran Tersedia", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KosTextPrimary)
                        Text(
                            text = "Pembayaran untuk periode ${invoice.bulanTagihan} telah diverifikasi otomatis.",
                            fontSize = 12.sp,
                            color = KosTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )
                        Button(
                            onClick = onViewReceiptClick,
                            colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = KosWhite, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buka Kuitansi Resmi", color = KosWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// TAB 2: RIWAYAT PEMBAYARAN PRIBADI (PENGHUNI)
@Composable
private fun TenantPaymentHistoryTab(
    invoices: List<Invoice>,
    onViewReceipt: (Invoice) -> Unit,
    onPayPending: () -> Unit
) {
    val totalPaidCount = invoices.count { it.statusPembayaran == PaymentStatus.LUNAS }
    val totalAmountPaid = invoices.filter { it.statusPembayaran == PaymentStatus.LUNAS }.sumOf { it.totalTagihan }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = KosBluePrimary)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Riwayat Pembayaran Sewa Pribadi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = KosWhite
                    )
                    Text(
                        text = "Kamar 101 • Ahmad Pratama",
                        fontSize = 12.sp,
                        color = KosWhite.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = KosWhite.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Terbayar Lunas", fontSize = 11.sp, color = KosWhite.copy(alpha = 0.8f))
                            Text(
                                text = KosRepository.formatRupiah(totalAmountPaid),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = KosWhite
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Jumlah Transaksi", fontSize = 11.sp, color = KosWhite.copy(alpha = 0.8f))
                            Text(
                                text = "$totalPaidCount Bulan Lunas",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = KosWhite
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Daftar Tagihan & Kuitansi Per Bulan",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = KosTextPrimary
            )
        }

        if (invoices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = KosWhite)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada riwayat transaksi.", fontSize = 12.sp, color = KosTextSecondary)
                    }
                }
            }
        } else {
            items(invoices, key = { it.id }) { inv ->
                val isPaid = inv.statusPembayaran == PaymentStatus.LUNAS
                val isPending = inv.statusPembayaran == PaymentStatus.MENUNGGU_VERIFIKASI

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = KosWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Periode ${inv.bulanTagihan}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = KosTextPrimary
                                )
                                Text(
                                    text = if (isPaid) "Metode: ${inv.metodePembayaran?.label ?: "Transfer Bank"}"
                                    else "Jatuh Tempo: ${inv.tanggalJatuhTempo}",
                                    fontSize = 11.sp,
                                    color = KosTextSecondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when {
                                            isPaid -> KosSuccessContainer
                                            isPending -> KosWarningContainer
                                            else -> KosErrorContainer
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = when {
                                        isPaid -> "LUNAS"
                                        isPending -> "MENUNGGU"
                                        else -> "BELUM BAYAR"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isPaid -> KosSuccess
                                        isPending -> KosWarning
                                        else -> KosError
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = KosOutline.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = KosRepository.formatRupiah(inv.totalTagihan),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = if (isPaid) KosTextPrimary else KosBluePrimary
                                )
                                if (isPaid && inv.tanggalBayar != null) {
                                    Text(
                                        text = "Dibayar: ${inv.tanggalBayar}",
                                        fontSize = 10.5.sp,
                                        color = KosSuccess
                                    )
                                }
                            }

                            if (isPaid) {
                                OutlinedButton(
                                    onClick = { onViewReceipt(inv) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp).testTag("btn_kuitansi_${inv.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = KosBluePrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Kuitansi Resmi", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KosBluePrimary)
                                }
                            } else {
                                Button(
                                    onClick = onPayPending,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Bayar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KosWhite)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// TAB 3: KOMPLAIN & INFORMASI PENGHUNII
@Composable
private fun TenantComplaintAndInfoTab(
    complaints: List<Complaint>,
    announcements: List<Announcement>,
    onAddComplaintClick: () -> Unit,
    onViewPhoto: (String, String) -> Unit
) {
    var subTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = subTab,
            containerColor = KosWhite,
            contentColor = KosBluePrimary
        ) {
            Tab(
                selected = subTab == 0,
                onClick = { subTab = 0 },
                text = { Text("Tiket Komplain (${complaints.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = subTab == 1,
                onClick = { subTab = 1 },
                text = { Text("Papan Info & Tata Tertib", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            )
        }

        if (subTab == 0) {
            Scaffold(
                containerColor = KosBackground,
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = onAddComplaintClick,
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Lapor Kerusakan", fontWeight = FontWeight.Bold) },
                        containerColor = KosBluePrimary,
                        contentColor = KosWhite,
                        shape = CircleShape,
                        modifier = Modifier
                            .padding(bottom = 72.dp)
                            .testTag("fab_report_complaint")
                    )
                }
            ) { padding ->
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize().padding(padding)
                ) {
                    item {
                        Text(
                            text = "Layanan Perbaikan Fasilitas Kamar",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = KosTextPrimary
                        )
                        Text(
                            text = "Laporkan AC tidak dingin, kran bocor, atau lampu redup langsung ke teknisi.",
                            style = MaterialTheme.typography.bodySmall,
                            color = KosTextSecondary,
                            modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                        )
                    }

                    if (complaints.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = KosWhite)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircleOutline,
                                        contentDescription = null,
                                        tint = KosSuccess,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Tidak ada keluhan aktif", fontWeight = FontWeight.Bold, color = KosTextPrimary)
                                    Text("Semua fasilitas kamar Anda dalam kondisi baik.", fontSize = 12.sp, color = KosTextSecondary)
                                }
                            }
                        }
                    } else {
                        items(complaints, key = { it.id }) { c ->
                            val (statusBg, statusFg) = when (c.statusKomplain) {
                                ComplaintStatus.MENUNGGU -> Pair(KosErrorContainer, KosError)
                                ComplaintStatus.DIPROSES -> Pair(KosWarningContainer, KosWarning)
                                ComplaintStatus.SELESAI -> Pair(KosSuccessContainer, KosSuccess)
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = KosWhite),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(c.judulMasalah, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KosTextPrimary)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(statusBg)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(c.statusKomplain.label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusFg)
                                        }
                                    }
                                    Text(c.deskripsiMasalah, fontSize = 12.sp, color = KosTextSecondary, modifier = Modifier.padding(vertical = 6.dp))

                                    // Damage Photo Thumbnail (if provided)
                                    if (!c.fotoKerusakanUri.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { onViewPhoto(c.fotoKerusakanUri, "Foto Kerusakan: ${c.judulMasalah}") },
                                            color = KosSurfaceVariant
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color.White)
                                                ) {
                                                    AsyncImage(
                                                        model = c.fotoKerusakanUri,
                                                        contentDescription = "Foto Kerusakan",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("Foto Kerusakan Terlampir", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = KosTextPrimary)
                                                    Text("Ketuk untuk memperbesar foto", fontSize = 10.sp, color = KosTextSecondary)
                                                }
                                                Icon(Icons.Default.Visibility, contentDescription = null, tint = KosBluePrimary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    // Repair Photo by Technician (if completed)
                                    if (!c.fotoPerbaikanUri.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { onViewPhoto(c.fotoPerbaikanUri, "Foto Hasil Perbaikan: ${c.judulMasalah}") },
                                            color = KosSuccessContainer
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color.White)
                                                ) {
                                                    AsyncImage(
                                                        model = c.fotoPerbaikanUri,
                                                        contentDescription = "Foto Perbaikan",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("Foto Hasil Perbaikan Teknisi", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = KosSuccess)
                                                    Text("Pekerjaan telah selesai & diverifikasi", fontSize = 10.sp, color = KosTextSecondary)
                                                }
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KosSuccess, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    if (!c.catatanPerbaikan.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(KosBlueContainer.copy(alpha = 0.5f))
                                                .padding(8.dp)
                                        ) {
                                            Text("Respon Pengelola: ${c.catatanPerbaikan}", fontSize = 11.sp, color = KosBlueDark)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            TenantInfoContent(announcements = announcements)
        }
    }
}

@Composable
private fun TenantInfoContent(
    announcements: List<Announcement>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KosWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Papan Pengumuman Kos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = KosTextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(KosBlueContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${announcements.size} info aktif",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = KosBlueDark
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (announcements.isEmpty()) {
                        Text("Belum ada pengumuman terbaru.", fontSize = 12.sp, color = KosTextSecondary)
                    } else {
                        announcements.forEachIndexed { index, item ->
                            val (badgeBg, badgeFg) = when (item.prioritas) {
                                AnnouncementPriority.DARURAT -> Pair(KosErrorContainer, KosError)
                                AnnouncementPriority.PENTING -> Pair(KosWarningContainer, KosWarning)
                                AnnouncementPriority.NORMAL -> Pair(KosBlueContainer, KosBluePrimary)
                            }

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.judul,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = KosBluePrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(badgeBg)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.prioritas.label,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeFg
                                        )
                                    }
                                }
                                Text(
                                    text = item.tanggal,
                                    fontSize = 10.sp,
                                    color = KosTextMuted,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Text(
                                    text = item.isi,
                                    fontSize = 12.sp,
                                    color = KosTextSecondary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            if (index < announcements.size - 1) {
                                HorizontalDivider(
                                    color = KosOutline.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KosWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Kontak Pengelola & Darurat", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KosTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Penjaga Kos (Pak Slamet): 0812-9988-7766", fontSize = 13.sp, color = KosTextSecondary)
                    Text("Admin Kantor: 0813-1122-3344", fontSize = 13.sp, color = KosTextSecondary, modifier = Modifier.padding(top = 4.dp))
                    Text("Klinik Terdekat: RS Medika (500 meter)", fontSize = 13.sp, color = KosTextSecondary, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KosWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tata Tertib & Jam Malam", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KosTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("1. Pintu gerbang utama ditutup pukul 23:00 WIB untuk keamanan bersama.", fontSize = 12.sp, color = KosTextSecondary)
                    Text("2. Tamu menginap wajib melapor ke penjaga kos paling lambat pukul 20:00 WIB.", fontSize = 12.sp, color = KosTextSecondary, modifier = Modifier.padding(top = 4.dp))
                    Text("3. Dilarang merokok atau menggunakan kompor listrik berdaya > 800W di dalam kamar.", fontSize = 12.sp, color = KosTextSecondary, modifier = Modifier.padding(top = 4.dp))
                    Text("4. Menjaga ketenangan dan kebersihan area lorong serta dapur bersama.", fontSize = 12.sp, color = KosTextSecondary, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

// Payment Dialog Simulation with Virtual Account & QRIS
@Composable
fun PaymentSimulationDialog(
    invoice: Invoice,
    method: PaymentMethod,
    onDismiss: () -> Unit,
    onConfirmPay: (String?) -> Unit,
    onUploadProofOnly: (String) -> Unit
) {
    val context = LocalContext.current
    val vaCode = invoice.kodePembayaran ?: "${method.codePrefix}892837192"
    var buktiPath by remember { mutableStateOf<String?>(invoice.buktiPembayaranUri) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payment, contentDescription = null, tint = KosBluePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Instruksi Pembayaran", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = method.label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = KosBluePrimary
                )

                if (method == PaymentMethod.QRIS) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(KosWhite)
                            .border(1.dp, KosOutline, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "QRIS Barcode",
                                tint = KosTextPrimary,
                                modifier = Modifier.size(130.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Scan QRIS dengan e-wallet / mobile banking", fontSize = 11.sp, color = KosTextSecondary)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(KosBlueContainer)
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Nomor Virtual Account:", fontSize = 11.sp, color = KosTextSecondary)
                                TextButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("VA Code", vaCode))
                                        Toast.makeText(context, "Nomor VA berhasil disalin!", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = KosBluePrimary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Salin", fontSize = 11.sp, color = KosBluePrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                text = vaCode,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = KosBlueDark,
                                letterSpacing = 1.sp
                            )
                            Text("a.n KosConnect - Kamar ${invoice.roomNomor}", fontSize = 11.sp, color = KosTextSecondary, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Tagihan:", fontSize = 12.sp, color = KosTextSecondary)
                    Text(
                        KosRepository.formatRupiah(invoice.totalTagihan),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = KosTextPrimary
                    )
                }

                Divider(color = KosOutline)

                // Photo Picker for Proof of Transfer
                PhotoUploadPicker(
                    label = "Unggah Bukti Struk Transfer (Opsional)",
                    imagePath = buktiPath,
                    onImageSelected = { buktiPath = it },
                    sampleTitle = "Transfer ${method.label} Lunas",
                    sampleType = "RECEIPT"
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (buktiPath != null) {
                    Button(
                        onClick = { onUploadProofOnly(buktiPath!!) },
                        colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_upload_proof_only")
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, tint = KosWhite, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kirim Bukti Pembayaran", color = KosWhite, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = { onConfirmPay(buktiPath) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("confirm_pay_btn")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = KosSuccess, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simulasi Bayar Lunas", color = KosSuccess, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tutup", color = KosTextSecondary)
            }
        }
    )
}

@Composable
fun TenantReportDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String?) -> Unit
) {
    var judul by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var fotoKerusakanUri by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lapor Masalah Fasilitas", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = judul,
                    onValueChange = { judul = it },
                    label = { Text("Judul Kerusakan (misal: AC Bocor)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Deskripsi Detail Masalah") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Damage photo picker
                PhotoUploadPicker(
                    label = "Foto Bukti Kerusakan (Opsional)",
                    imagePath = fotoKerusakanUri,
                    onImageSelected = { fotoKerusakanUri = it },
                    sampleTitle = "Dokumentasi AC Rusak",
                    sampleType = "DAMAGE"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (judul.isNotBlank()) onSubmit(judul, deskripsi, fotoKerusakanUri)
                },
                colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary)
            ) {
                Text("Kirim Laporan", color = KosWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = KosTextSecondary)
            }
        }
    )
}

@Composable
private fun BillItemRow(label: String, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = KosTextSecondary)
        Text(text = amount, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = KosTextPrimary)
    }
}
