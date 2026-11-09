package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseCategory
import com.example.data.repository.KosRepository
import com.example.ui.AdminSummaryState
import com.example.ui.KosViewModel
import com.example.ui.components.PhotoUploadPicker
import com.example.ui.theme.*

@Composable
fun AdminDashboardScreen(
    viewModel: KosViewModel,
    onNavigateToRooms: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToComplaints: () -> Unit
) {
    val summary by viewModel.summaryState.collectAsState()
    val kosProfile by viewModel.kosProfile.collectAsState()
    val complaints by viewModel.complaints.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val context = LocalContext.current

    var showAddRoomDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showQuickReminderNotice by remember { mutableStateOf(false) }
    var showAiAssistantDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KosBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        // Juragan Greeting Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = KosBluePrimary)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Halo, Juragan Budi! 👋",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = KosWhite
                            )
                            Text(
                                text = kosProfile.namaKos,
                                style = MaterialTheme.typography.bodyMedium,
                                color = KosWhite.copy(alpha = 0.85f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(KosWhite.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Admin Kos",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = KosWhite
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = kosProfile.alamat,
                        style = MaterialTheme.typography.bodySmall,
                        color = KosWhite.copy(alpha = 0.75f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // AI Assistant Banner Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { showAiAssistantDialog = true }
                .testTag("banner_ai_assistant"),
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
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(KosBlueContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = KosBluePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Konsultasi Asisten AI",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = KosTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(KosBluePrimary)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text("GEMINI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = KosWhite)
                        }
                    }
                    Text(
                        text = "Tanya tips okupansi, draft pengumuman, aturan kos & efisiensi biaya.",
                        fontSize = 11.sp,
                        color = KosTextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Buka",
                    tint = KosBluePrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // WIREFRAME 1: Kartu Okupansi

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = KosWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(KosBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MeetingRoom,
                                contentDescription = null,
                                tint = KosBluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Status Okupansi Kamar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = KosTextPrimary
                        )
                    }

                    Text(
                        text = "${summary.occupancyRate}% Terisi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = KosBluePrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { summary.occupancyRate / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = KosBluePrimary,
                    trackColor = KosBlueContainer
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OccupancyBadge(
                        label = "Terisi",
                        value = "${summary.occupiedRooms} / ${summary.totalRooms}",
                        color = KosBluePrimary,
                        bgColor = KosBlueContainer
                    )
                    OccupancyBadge(
                        label = "Kosong",
                        value = "${summary.availableRooms} Kamar",
                        color = KosSuccess,
                        bgColor = KosSuccessContainer
                    )
                    OccupancyBadge(
                        label = "Perbaikan",
                        value = "${summary.repairRooms} Kamar",
                        color = KosWarning,
                        bgColor = KosWarningContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // WIREFRAME 1: Kartu Keuangan Bulan Ini
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = KosWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(KosSuccessContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = KosSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Keuangan Bulan Ini",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = KosTextPrimary
                        )
                    }

                    TextButton(
                        onClick = onNavigateToFinance,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Detail", color = KosBluePrimary, fontWeight = FontWeight.SemiBold)
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Detail",
                            tint = KosBluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                FinanceRow(label = "Pendapatan Masuk", amount = KosRepository.formatRupiah(summary.totalRevenue), color = KosSuccess)
                Divider(color = KosOutline, modifier = Modifier.padding(vertical = 8.dp))
                FinanceRow(label = "Pengeluaran Operasional", amount = KosRepository.formatRupiah(summary.totalExpense), color = KosError)
                Divider(color = KosOutline, modifier = Modifier.padding(vertical = 8.dp))
                FinanceRow(
                    label = "Estimasi Laba Bersih",
                    amount = KosRepository.formatRupiah(summary.netProfit),
                    color = KosBluePrimary,
                    isBold = true
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // WIREFRAME 1: Tindakan Cepat (Quick Actions)
        Text(
            text = "Tindakan Cepat",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = KosTextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = "Tambah\nKamar",
                icon = Icons.Default.AddHome,
                tag = "quick_add_room_btn",
                onClick = { showAddRoomDialog = true }
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = "Catat\nPengeluaran",
                icon = Icons.Default.ReceiptLong,
                tag = "quick_add_expense_btn",
                onClick = { showAddExpenseDialog = true }
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = "Tagih\nBayaran",
                icon = Icons.Default.NotificationsActive,
                tag = "quick_remind_bill_btn",
                onClick = { showQuickReminderNotice = true }
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = "Lihat\nKomplain",
                icon = Icons.Default.ReportProblem,
                tag = "quick_view_complaints_btn",
                badge = if (summary.pendingComplaintsCount > 0) "${summary.pendingComplaintsCount}" else null,
                onClick = onNavigateToComplaints
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // WIREFRAME 1: Perlu Perhatian
        Text(
            text = "Perlu Perhatian",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = KosTextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Attention Card 1: Unpaid invoices
        if (summary.unpaidCount > 0) {
            AttentionCard(
                title = "${summary.unpaidCount} Penghuni belum bayar tagihan",
                subtitle = "Total tertunggak: ${KosRepository.formatRupiah(summary.unpaidAmount)}",
                icon = Icons.Default.PendingActions,
                iconTint = KosWarning,
                bgTint = KosWarningContainer,
                actionText = "Lihat Tagihan",
                onClick = onNavigateToFinance
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Attention Card 2: Complaints
        if (summary.pendingComplaintsCount > 0) {
            val latestComplaint = complaints.firstOrNull { it.statusKomplain != com.example.data.model.ComplaintStatus.SELESAI }
            AttentionCard(
                title = "${summary.pendingComplaintsCount} Komplain perlu ditangani",
                subtitle = latestComplaint?.let { "${it.penghuniNama} (Kamar ${it.roomNomor}): ${it.judulMasalah}" } ?: "Ada keluhan baru",
                icon = Icons.Default.Build,
                iconTint = KosError,
                bgTint = KosErrorContainer,
                actionText = "Tindak Lanjuti",
                onClick = onNavigateToComplaints
            )
        }
    }

    // Modal Dialog: Tambah Kamar
    if (showAddRoomDialog) {
        AddRoomDialog(
            onDismiss = { showAddRoomDialog = false },
            onAddRoom = { no, tipe, harga, fas ->
                viewModel.addRoom(no, tipe, harga, fas)
                showAddRoomDialog = false
            }
        )
    }

    // Modal Dialog: Catat Pengeluaran
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onAddExpense = { kat, nominal, deskripsi, tgl, struk ->
                viewModel.addExpense(kat, nominal, deskripsi, tgl, struk)
                showAddExpenseDialog = false
            }
        )
    }


    // Snackbar / AlertDialog for Tagih Bayaran
    if (showQuickReminderNotice) {
        val reminderConfig by viewModel.reminderConfig.collectAsState()
        AlertDialog(
            onDismissRequest = { showQuickReminderNotice = false },
            icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = KosBluePrimary) },
            title = { Text("Pengingat Otomatis & WorkManager", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Sistem WorkManager berjalan di background setiap 12 jam untuk memeriksa jatuh tempo sewa.",
                        fontSize = 13.sp,
                        color = KosTextPrimary
                    )
                    Text(
                        text = "Status: ${if (reminderConfig.autoReminderEnabled) "Aktif Otomatis" else "Nonaktif"}\nTerakhir Sinkron: ${reminderConfig.lastSyncText}\nKamar Menunggak: ${summary.unpaidCount} kamar",
                        fontSize = 12.sp,
                        color = KosTextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.triggerBroadcastReminders(context)
                        showQuickReminderNotice = false
                        Toast.makeText(context, "WorkManager dijalankan: Pengingat dikirim ke ${summary.unpaidCount} kamar!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary)
                ) {
                    Text("Kirim Notifikasi Sekarang", color = KosWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickReminderNotice = false }) {
                    Text("Tutup", color = KosTextSecondary)
                }
            }
        )
    }

    // Modal: Asisten Pintar Kos AI (Gemini)
    if (showAiAssistantDialog) {
        AiAssistantDialog(
            summary = summary,
            namaKos = kosProfile.namaKos,
            onDismiss = { showAiAssistantDialog = false }
        )
    }
}


@Composable
private fun OccupancyBadge(
    label: String,
    value: String,
    color: Color,
    bgColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 12.sp, color = KosTextSecondary)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun FinanceRow(
    label: String,
    amount: String,
    color: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) KosTextPrimary else KosTextSecondary
        )
        Text(
            text = amount,
            style = if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    tag: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        colors = CardDefaults.cardColors(containerColor = KosWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(KosBlueContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = KosBluePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = KosTextPrimary,
                    lineHeight = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            if (badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 6.dp, end = 6.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(KosError),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge,
                        color = KosWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AttentionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    bgTint: Color,
    actionText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = KosWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = KosTextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = KosTextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = actionText,
                tint = KosTextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun AddRoomDialog(
    onDismiss: () -> Unit,
    onAddRoom: (String, String, Long, List<String>) -> Unit
) {
    var nomorKamar by remember { mutableStateOf("") }
    var tipeKamar by remember { mutableStateOf("Deluxe AC") }
    var hargaSewa by remember { mutableStateOf("1800000") }
    var fasilitasAc by remember { mutableStateOf(true) }
    var fasilitasKamarMandi by remember { mutableStateOf(true) }
    var fasilitasWifi by remember { mutableStateOf(true) }
    var fasilitasWaterHeater by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Kamar Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nomorKamar,
                    onValueChange = { nomorKamar = it },
                    label = { Text("Nomor Kamar (misal: 206)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_room_number")
                )

                OutlinedTextField(
                    value = tipeKamar,
                    onValueChange = { tipeKamar = it },
                    label = { Text("Tipe Kamar (misal: Deluxe AC)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_room_type")
                )

                OutlinedTextField(
                    value = hargaSewa,
                    onValueChange = { hargaSewa = it.filter { char -> char.isDigit() } },
                    label = { Text("Harga Sewa Dasar (Rp)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_room_price")
                )

                Text("Fasilitas Kamar:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = fasilitasAc, onCheckedChange = { fasilitasAc = it })
                    Text("AC", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Checkbox(checked = fasilitasKamarMandi, onCheckedChange = { fasilitasKamarMandi = it })
                    Text("KM Dalam", fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = fasilitasWifi, onCheckedChange = { fasilitasWifi = it })
                    Text("WiFi", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Checkbox(checked = fasilitasWaterHeater, onCheckedChange = { fasilitasWaterHeater = it })
                    Text("Water Heater", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nomorKamar.isNotBlank()) {
                        val listFas = mutableListOf<String>()
                        if (fasilitasAc) listFas.add("AC")
                        if (fasilitasKamarMandi) listFas.add("Kamar Mandi Dalam")
                        if (fasilitasWifi) listFas.add("WiFi")
                        if (fasilitasWaterHeater) listFas.add("Water Heater")
                        onAddRoom(nomorKamar, tipeKamar, hargaSewa.toLongOrNull() ?: 1800000L, listFas)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary),
                modifier = Modifier.testTag("submit_add_room_btn")
            ) {
                Text("Simpan Kamar", color = KosWhite)
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
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onAddExpense: (ExpenseCategory, Long, String, String, String?) -> Unit
) {
    var kategori by remember { mutableStateOf(ExpenseCategory.LISTRIK) }
    var nominal by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var buktiStruk by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Pengeluaran Operasional", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Pilih Kategori:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Column {
                    ExpenseCategory.values().forEach { cat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { kategori = cat }
                        ) {
                            RadioButton(selected = kategori == cat, onClick = { kategori = cat })
                            Text(cat.label, fontSize = 13.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = nominal,
                    onValueChange = { nominal = it.filter { c -> c.isDigit() } },
                    label = { Text("Nominal Pengeluaran (Rp)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_expense_amount")
                )

                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Keterangan (misal: Beli 5 token PLN)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_expense_desc")
                )

                // Struk / Nota Photo Picker
                PhotoUploadPicker(
                    label = "Foto Nota / Struk Biaya (Opsional)",
                    imagePath = buktiStruk,
                    onImageSelected = { buktiStruk = it },
                    sampleTitle = "Nota ${kategori.label}",
                    sampleType = "RECEIPT"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = nominal.toLongOrNull() ?: 0L
                    if (amount > 0 && deskripsi.isNotBlank()) {
                        onAddExpense(kategori, amount, deskripsi, "Hari ini", buktiStruk)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary),
                modifier = Modifier.testTag("submit_expense_btn")
            ) {
                Text("Simpan", color = KosWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = KosTextSecondary)
            }
        }
    )
}

