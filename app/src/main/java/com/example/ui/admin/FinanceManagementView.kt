package com.example.ui.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.KosRepository
import com.example.ui.KosViewModel
import com.example.ui.theme.*
import com.example.util.ReportExportService
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.ui.components.ImagePreviewDialog
import java.io.File

@Composable
fun FinanceManagementView(
    viewModel: KosViewModel
) {
    val invoices by viewModel.invoices.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val summary by viewModel.summaryState.collectAsState()
    val report = viewModel.getProfitSharingReport()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog states
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showCreateInvoiceDialog by remember { mutableStateOf(false) }
    var showMassGenerateDialog by remember { mutableStateOf(false) }
    var selectedInvoiceForReceipt by remember { mutableStateOf<Invoice?>(null) }
    var invoiceToAdjust by remember { mutableStateOf<Invoice?>(null) }
    var invoiceToDeleteConfirm by remember { mutableStateOf<Invoice?>(null) }
    var expenseToDeleteConfirm by remember { mutableStateOf<Expense?>(null) }
    var showAddInvestorDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showAiInsightsDialog by remember { mutableStateOf(false) }
    var previewPhotoUri by remember { mutableStateOf<String?>(null) }
    var previewPhotoTitle by remember { mutableStateOf("") }
    var invoiceToVerify by remember { mutableStateOf<Invoice?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KosBackground)
    ) {
        // Tab Navigation
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = KosWhite,
            contentColor = KosBluePrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Tagihan (${invoices.size})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                icon = { Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Pengeluaran (${expenses.size})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                icon = { Icon(Icons.Default.MoneyOff, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Bagi Hasil", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                icon = { Icon(Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        // Export Action Banner
        Surface(
            color = KosWhite,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(KosBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = KosBluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Laporan Periode ${report.periodeBulan}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = KosTextPrimary
                        )
                        Text(
                            text = "Ekspor PDF Resmi & Excel / CSV",
                            fontSize = 10.sp,
                            color = KosTextSecondary
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { showAiInsightsDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("btn_ai_finance_insights")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = KosBluePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Analisis AI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KosBluePrimary)
                    }

                    Button(
                        onClick = { showExportDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("btn_export_finance_report")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = KosWhite,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ekspor", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }


        when (selectedTab) {
            0 -> InvoicesTabContent(
                invoices = invoices,
                onViewReceipt = { selectedInvoiceForReceipt = it },
                onPreviewProof = { inv ->
                    previewPhotoUri = inv.buktiPembayaranUri
                    previewPhotoTitle = "Bukti Transfer Kamar ${inv.roomNomor} (${inv.penghuniNama})"
                    invoiceToVerify = inv
                },
                onMarkPaid = { inv ->
                    viewModel.payInvoice(inv.id, inv.metodePembayaran ?: PaymentMethod.BCA_VA, inv.buktiPembayaranUri)
                    Toast.makeText(context, "Tagihan Kamar ${inv.roomNomor} ditandai lunas.", Toast.LENGTH_SHORT).show()
                },
                onMarkUnpaid = { inv ->
                    viewModel.markInvoiceUnpaid(inv.id)
                    Toast.makeText(context, "Status tagihan Kamar ${inv.roomNomor} diubah ke Belum Bayar.", Toast.LENGTH_SHORT).show()
                },
                onAdjustInvoice = { invoiceToAdjust = it },
                onShareWhatsApp = { inv ->
                    shareInvoiceWhatsApp(context, inv)
                },
                onDeleteInvoice = { invoiceToDeleteConfirm = it },
                onCreateInvoiceClick = { showCreateInvoiceDialog = true },
                onMassGenerateClick = { showMassGenerateDialog = true },
                onTriggerReminders = {
                    viewModel.triggerBroadcastReminders(context)
                    Toast.makeText(context, "WorkManager dijalankan: Notifikasi pengingat dikirim ke penghuni menunggak!", Toast.LENGTH_SHORT).show()
                }
            )
            1 -> ExpensesTabContent(
                expenses = expenses,
                totalExpense = summary.totalExpense,
                onAddExpenseClick = { showAddExpenseDialog = true },
                onDeleteExpense = { expenseToDeleteConfirm = it },
                onPreviewReceipt = { uri, title ->
                    previewPhotoUri = uri
                    previewPhotoTitle = title
                    invoiceToVerify = null
                }
            )
            2 -> ProfitSharingTabContent(
                report = report,
                onTransferPayout = { payout ->
                    viewModel.transferInvestorPayout(payout.investorNama)
                    Toast.makeText(context, "Dividen ${payout.investorNama} berhasil ditransfer!", Toast.LENGTH_SHORT).show()
                },
                onRevertPayout = { payout ->
                    viewModel.revertInvestorPayout(payout.investorNama)
                    Toast.makeText(context, "Status dividen ${payout.investorNama} dikembalikan ke Menunggu.", Toast.LENGTH_SHORT).show()
                },
                onAddInvestorClick = { showAddInvestorDialog = true }
            )
        }
    }

    // Modal: Tambah Pengeluaran
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onAddExpense = { kat, nominal, deskripsi, tgl, struk ->
                viewModel.addExpense(kat, nominal, deskripsi, tgl, struk)
                showAddExpenseDialog = false
                Toast.makeText(context, "Pengeluaran berhasil dicatat", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal: Buat Tagihan Baru
    if (showCreateInvoiceDialog) {
        CreateInvoiceDialog(
            rooms = rooms.filter { it.status == RoomStatus.TERISI },
            onDismiss = { showCreateInvoiceDialog = false },
            onCreate = { no, nama, kamar, listrik, denda, bulan, tempo, method ->
                viewModel.createInvoice(no, nama, kamar, listrik, denda, bulan, tempo, method)
                showCreateInvoiceDialog = false
                Toast.makeText(context, "Tagihan untuk Kamar $no berhasil diterbitkan", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal: Terbitkan Tagihan Massal
    if (showMassGenerateDialog) {
        var bulan by remember { mutableStateOf("Desember 2026") }
        var tempo by remember { mutableStateOf("10 Des 2026") }

        AlertDialog(
            onDismissRequest = { showMassGenerateDialog = false },
            icon = { Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = KosBluePrimary) },
            title = { Text("Terbitkan Tagihan Massal", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Sistem akan membuat tagihan sewa otomatis untuk semua kamar yang sedang terisi.", fontSize = 12.sp, color = KosTextSecondary)
                    OutlinedTextField(
                        value = bulan,
                        onValueChange = { bulan = it },
                        label = { Text("Periode Bulan") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempo,
                        onValueChange = { tempo = it },
                        label = { Text("Tanggal Jatuh Tempo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val count = viewModel.generateMonthlyInvoices(bulan, tempo)
                        showMassGenerateDialog = false
                        Toast.makeText(context, "Berhasil menerbitkan $count tagihan baru", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary)
                ) {
                    Text("Terbitkan Sekarang", color = KosWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMassGenerateDialog = false }) {
                    Text("Batal", color = KosTextSecondary)
                }
            }
        )
    }

    // Modal: Digital Receipt (Kuitansi Resmi)
    if (selectedInvoiceForReceipt != null) {
        DigitalReceiptDialog(
            invoice = selectedInvoiceForReceipt!!,
            onDismiss = { selectedInvoiceForReceipt = null }
        )
    }

    // Modal: Koreksi Tagihan
    if (invoiceToAdjust != null) {
        val inv = invoiceToAdjust!!
        var listrikStr by remember { mutableStateOf(inv.tambahanListrik.toString()) }
        var dendaStr by remember { mutableStateOf(inv.denda.toString()) }

        AlertDialog(
            onDismissRequest = { invoiceToAdjust = null },
            title = { Text("Koreksi Tagihan Kamar ${inv.roomNomor}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Sewa Kamar Dasar: ${KosRepository.formatRupiah(inv.nominalKamar)}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = listrikStr,
                        onValueChange = { listrikStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Tambahan Listrik & Air (Rp)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dendaStr,
                        onValueChange = { dendaStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Denda Keterlambatan (Rp)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val listrik = listrikStr.toLongOrNull() ?: 0L
                        val denda = dendaStr.toLongOrNull() ?: 0L
                        viewModel.updateInvoiceAdjustment(inv.id, listrik, denda)
                        invoiceToAdjust = null
                        Toast.makeText(context, "Tagihan berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary)
                ) {
                    Text("Simpan", color = KosWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { invoiceToAdjust = null }) {
                    Text("Batal", color = KosTextSecondary)
                }
            }
        )
    }

    // Confirm Delete Invoice
    if (invoiceToDeleteConfirm != null) {
        val inv = invoiceToDeleteConfirm!!
        AlertDialog(
            onDismissRequest = { invoiceToDeleteConfirm = null },
            title = { Text("Hapus Tagihan Kamar ${inv.roomNomor}", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin membatalkan/menghapus tagihan ${inv.bulanTagihan} ini?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteInvoice(inv.id)
                        invoiceToDeleteConfirm = null
                        Toast.makeText(context, "Tagihan telah dihapus", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KosError)
                ) {
                    Text("Hapus", color = KosWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { invoiceToDeleteConfirm = null }) {
                    Text("Batal", color = KosTextSecondary)
                }
            }
        )
    }

    // Confirm Delete Expense
    if (expenseToDeleteConfirm != null) {
        val exp = expenseToDeleteConfirm!!
        AlertDialog(
            onDismissRequest = { expenseToDeleteConfirm = null },
            title = { Text("Hapus Pengeluaran", fontWeight = FontWeight.Bold) },
            text = { Text("Hapus catatan pengeluaran '${exp.deskripsi}' (${KosRepository.formatRupiah(exp.nominal)})?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExpense(exp.id)
                        expenseToDeleteConfirm = null
                        Toast.makeText(context, "Catatan pengeluaran dihapus", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KosError)
                ) {
                    Text("Hapus", color = KosWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDeleteConfirm = null }) {
                    Text("Batal", color = KosTextSecondary)
                }
            }
        )
    }

    // Modal: Tambah Investor
    if (showAddInvestorDialog) {
        AddInvestorDialog(
            onDismiss = { showAddInvestorDialog = false },
            onAdd = { nama, email, rek, persen ->
                viewModel.addInvestor(nama, email, rek, persen)
                showAddInvestorDialog = false
                Toast.makeText(context, "Investor $nama berhasil didaftarkan ($persen% saham)", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal: Ekspor Laporan Keuangan (PDF & CSV)
    if (showExportDialog) {
        ExportReportDialog(
            report = report,
            invoices = invoices,
            expenses = expenses,
            onDismiss = { showExportDialog = false }
        )
    }

    // Modal: AI Financial Insights & Rekomendasi
    if (showAiInsightsDialog) {
        AiFinancialInsightsDialog(
            report = report,
            summary = summary,
            expenses = expenses,
            onDismiss = { showAiInsightsDialog = false }
        )
    }

    // Modal: Preview Foto Bukti Transfer / Nota Pengeluaran
    if (previewPhotoUri != null) {
        val inv = invoiceToVerify
        val canVerify = inv != null && inv.statusPembayaran != PaymentStatus.LUNAS
        ImagePreviewDialog(
            imagePath = previewPhotoUri!!,
            title = previewPhotoTitle,
            onDismiss = {
                previewPhotoUri = null
                invoiceToVerify = null
            },
            actionButtonLabel = if (canVerify) "Verifikasi & Tandai Lunas" else null,
            onActionClick = if (canVerify) {
                {
                    viewModel.payInvoice(inv!!.id, inv.metodePembayaran ?: PaymentMethod.BCA_VA, inv.buktiPembayaranUri)
                    Toast.makeText(context, "Pembayaran Kamar ${inv.roomNomor} berhasil diverifikasi & ditandai Lunas!", Toast.LENGTH_SHORT).show()
                    previewPhotoUri = null
                    invoiceToVerify = null
                }
            } else null
        )
    }
}


@Composable
fun AddInvestorDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, Double) -> Unit
) {
    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var rekening by remember { mutableStateOf("") }
    var sahamStr by remember { mutableStateOf("10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daftarkan Investor Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Lengkap Investor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Investor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rekening,
                    onValueChange = { rekening = it },
                    label = { Text("Bank & Nomor Rekening (misal: BCA 8839-0129)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sahamStr,
                    onValueChange = { sahamStr = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Persentase Saham / Bagi Hasil (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "*Kalkulasi bagi hasil akan otomatis dihitung setiap akhir bulan berdasarkan persentase ini.",
                    fontSize = 11.sp,
                    color = KosTextMuted
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val persen = sahamStr.toDoubleOrNull() ?: 0.0
                    if (nama.isNotBlank() && persen > 0.0) {
                        onAdd(nama, email, rekening, persen)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary)
            ) {
                Text("Daftarkan", color = KosWhite)
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
private fun InvoicesTabContent(
    invoices: List<Invoice>,
    onViewReceipt: (Invoice) -> Unit,
    onPreviewProof: (Invoice) -> Unit,
    onMarkPaid: (Invoice) -> Unit,
    onMarkUnpaid: (Invoice) -> Unit,
    onAdjustInvoice: (Invoice) -> Unit,
    onShareWhatsApp: (Invoice) -> Unit,
    onDeleteInvoice: (Invoice) -> Unit,
    onCreateInvoiceClick: () -> Unit,
    onMassGenerateClick: () -> Unit,
    onTriggerReminders: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<PaymentStatus?>(null) }

    val filteredInvoices = remember(invoices, searchQuery, selectedStatusFilter) {
        var list = invoices
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.roomNomor.lowercase().contains(q) ||
                it.penghuniNama.lowercase().contains(q) ||
                it.bulanTagihan.lowercase().contains(q)
            }
        }
        if (selectedStatusFilter != null) {
            list = list.filter { it.statusPembayaran == selectedStatusFilter }
        }
        list
    }

    val totalPaid = invoices.filter { it.statusPembayaran == PaymentStatus.LUNAS }.sumOf { it.totalTagihan }
    val totalUnpaid = invoices.filter { it.statusPembayaran != PaymentStatus.LUNAS }.sumOf { it.totalTagihan }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Summary & Action Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KosWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Terkumpul (Lunas)", fontSize = 11.sp, color = KosTextSecondary)
                            Text(KosRepository.formatRupiah(totalPaid), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = KosSuccess)
                        }
                        Divider(modifier = Modifier.height(38.dp).width(1.dp), color = KosOutline)
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Belum Dibayar (Tertunggak)", fontSize = 11.sp, color = KosTextSecondary)
                            Text(KosRepository.formatRupiah(totalUnpaid), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = KosWarning)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onCreateInvoiceClick,
                            colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = KosWhite, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tagihan Baru", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KosWhite)
                        }

                        OutlinedButton(
                            onClick = onMassGenerateClick,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = KosBluePrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tagihan Massal", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = KosBluePrimary)
                        }
                    }
                }
            }
        }

        // Card WorkManager Pengingat Otomatis (Opsi B)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = KosBlueContainer.copy(alpha = 0.5f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, KosBluePrimary.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(KosBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = KosWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Pengingat Otomatis (WorkManager)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = KosBluePrimary
                            )
                            Text(
                                text = "Background sync aktif setiap 12 jam (H-3 & H-1)",
                                fontSize = 11.sp,
                                color = KosTextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = onTriggerReminders,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("btn_broadcast_reminders")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = KosWhite, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kirim Notif", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KosWhite)
                    }
                }
            }
        }

        // Search & Filter
        item {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari kamar atau nama penghuni...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = KosTextMuted) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = KosTextMuted)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = KosWhite,
                        unfocusedContainerColor = KosWhite,
                        focusedBorderColor = KosBluePrimary,
                        unfocusedBorderColor = KosOutline
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_search_invoices")
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedStatusFilter == null,
                            onClick = { selectedStatusFilter = null },
                            label = { Text("Semua (${invoices.size})", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KosBluePrimary,
                                selectedLabelColor = KosWhite
                            )
                        )
                    }
                    val pendingCount = invoices.count { it.statusPembayaran == PaymentStatus.MENUNGGU_VERIFIKASI }
                    if (pendingCount > 0) {
                        item {
                            FilterChip(
                                selected = selectedStatusFilter == PaymentStatus.MENUNGGU_VERIFIKASI,
                                onClick = { selectedStatusFilter = if (selectedStatusFilter == PaymentStatus.MENUNGGU_VERIFIKASI) null else PaymentStatus.MENUNGGU_VERIFIKASI },
                                label = { Text("Verifikasi Bukti ($pendingCount)", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = KosWarning,
                                    selectedLabelColor = KosWhite
                                )
                            )
                        }
                    }
                    item {
                        val unpaidCount = invoices.count { it.statusPembayaran == PaymentStatus.BELUM_BAYAR }
                        FilterChip(
                            selected = selectedStatusFilter == PaymentStatus.BELUM_BAYAR,
                            onClick = { selectedStatusFilter = if (selectedStatusFilter == PaymentStatus.BELUM_BAYAR) null else PaymentStatus.BELUM_BAYAR },
                            label = { Text("Belum Bayar ($unpaidCount)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KosError,
                                selectedLabelColor = KosWhite
                            )
                        )
                    }
                    item {
                        val paidCount = invoices.count { it.statusPembayaran == PaymentStatus.LUNAS }
                        FilterChip(
                            selected = selectedStatusFilter == PaymentStatus.LUNAS,
                            onClick = { selectedStatusFilter = if (selectedStatusFilter == PaymentStatus.LUNAS) null else PaymentStatus.LUNAS },
                            label = { Text("Lunas ($paidCount)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KosSuccess,
                                selectedLabelColor = KosWhite
                            )
                        )
                    }
                }
            }
        }

        // Invoice Items
        items(filteredInvoices, key = { it.id }) { inv ->
            InvoiceEnhancedCard(
                invoice = inv,
                onViewReceipt = { onViewReceipt(inv) },
                onPreviewProof = { onPreviewProof(inv) },
                onMarkPaid = { onMarkPaid(inv) },
                onMarkUnpaid = { onMarkUnpaid(inv) },
                onAdjust = { onAdjustInvoice(inv) },
                onShare = { onShareWhatsApp(inv) },
                onDelete = { onDeleteInvoice(inv) }
            )
        }
    }
}

@Composable
private fun InvoiceEnhancedCard(
    invoice: Invoice,
    onViewReceipt: () -> Unit,
    onPreviewProof: () -> Unit,
    onMarkPaid: () -> Unit,
    onMarkUnpaid: () -> Unit,
    onAdjust: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val isPaid = invoice.statusPembayaran == PaymentStatus.LUNAS

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onViewReceipt),
        colors = CardDefaults.cardColors(containerColor = KosWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(KosBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = invoice.roomNomor,
                            fontWeight = FontWeight.ExtraBold,
                            color = KosBluePrimary,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = invoice.penghuniNama,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = KosTextPrimary
                        )
                        Text(
                            text = "${invoice.bulanTagihan} • Tenggat: ${invoice.tanggalJatuhTempo}",
                            fontSize = 11.sp,
                            color = KosTextSecondary
                        )
                    }
                }

                val statusText = when (invoice.statusPembayaran) {
                    PaymentStatus.LUNAS -> "LUNAS"
                    PaymentStatus.MENUNGGU_VERIFIKASI -> "VERIFIKASI"
                    PaymentStatus.BELUM_BAYAR -> "BELUM BAYAR"
                }
                val statusBg = when (invoice.statusPembayaran) {
                    PaymentStatus.LUNAS -> KosSuccessContainer
                    PaymentStatus.MENUNGGU_VERIFIKASI -> KosWarningContainer
                    PaymentStatus.BELUM_BAYAR -> KosErrorContainer
                }
                val statusFg = when (invoice.statusPembayaran) {
                    PaymentStatus.LUNAS -> KosSuccess
                    PaymentStatus.MENUNGGU_VERIFIKASI -> KosWarning
                    PaymentStatus.BELUM_BAYAR -> KosError
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusFg
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Breakdown Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(KosSurfaceVariant)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Sewa Kamar", fontSize = 12.sp, color = KosTextSecondary)
                    Text(KosRepository.formatRupiah(invoice.nominalKamar), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = KosTextPrimary)
                }
                if (invoice.tambahanListrik > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Listrik & Air", fontSize = 12.sp, color = KosTextSecondary)
                        Text(KosRepository.formatRupiah(invoice.tambahanListrik), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = KosTextPrimary)
                    }
                }
                if (invoice.denda > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Denda Keterlambatan", fontSize = 12.sp, color = KosError)
                        Text(KosRepository.formatRupiah(invoice.denda), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KosError)
                    }
                }
                Divider(color = KosOutline, modifier = Modifier.padding(vertical = 2.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Tagihan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KosTextPrimary)
                    Text(KosRepository.formatRupiah(invoice.totalTagihan), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = KosBluePrimary)
                }
            }

            // Photo proof thumbnail card if attached
            if (invoice.buktiPembayaranUri != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onPreviewProof),
                    colors = CardDefaults.cardColors(containerColor = KosBlueContainer.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, KosBluePrimary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val file = remember(invoice.buktiPembayaranUri) { File(invoice.buktiPembayaranUri) }
                        val model = remember(invoice.buktiPembayaranUri) {
                            if (file.exists()) file else Uri.parse(invoice.buktiPembayaranUri)
                        }
                        AsyncImage(
                            model = model,
                            contentDescription = "Bukti Transfer",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(KosWhite),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bukti Transfer Terlampir",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = KosBluePrimary
                            )
                            Text(
                                text = if (invoice.statusPembayaran == PaymentStatus.MENUNGGU_VERIFIKASI) "Klik untuk periksa & verifikasi lunas" else "Klik untuk melihat foto kuitansi transfer",
                                fontSize = 11.sp,
                                color = KosTextSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Lihat Foto",
                            tint = KosBluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Payment method & Virtual Account info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Metode: ${invoice.metodePembayaran?.label ?: "VA"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = KosTextSecondary
                    )
                    if (invoice.kodePembayaran != null) {
                        Text(
                            text = "Kode: ${invoice.kodePembayaran}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = KosBluePrimary
                        )
                    }
                }

                if (isPaid && invoice.tanggalBayar != null) {
                    Text(
                        text = "Dibayar: ${invoice.tanggalBayar}",
                        fontSize = 11.sp,
                        color = KosSuccess,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = KosOutline)
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(34.dp).clip(CircleShape).background(KosBlueContainer)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Bagikan WA", tint = KosBluePrimary, modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = onAdjust,
                        modifier = Modifier.size(34.dp).clip(CircleShape).background(KosSurfaceVariant)
                    ) {
                        Icon(Icons.Default.EditNote, contentDescription = "Koreksi", tint = KosTextPrimary, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp).clip(CircleShape).background(KosErrorContainer)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = KosError, modifier = Modifier.size(16.dp))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onViewReceipt,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Kuitansi", fontSize = 11.sp, color = KosBluePrimary, fontWeight = FontWeight.Bold)
                    }

                    if (!isPaid) {
                        Button(
                            onClick = onMarkPaid,
                            colors = ButtonDefaults.buttonColors(containerColor = KosSuccess),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = KosWhite, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (invoice.statusPembayaran == PaymentStatus.MENUNGGU_VERIFIKASI) "Verifikasi Lunas" else "Tandai Lunas",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = KosWhite
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = onMarkUnpaid,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Batalkan Lunas", fontSize = 11.sp, color = KosWarning)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpensesTabContent(
    expenses: List<Expense>,
    totalExpense: Long,
    onAddExpenseClick: () -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    onPreviewReceipt: (String, String) -> Unit
) {
    var selectedCatFilter by remember { mutableStateOf<ExpenseCategory?>(null) }

    val filteredExpenses = remember(expenses, selectedCatFilter) {
        if (selectedCatFilter == null) expenses else expenses.filter { it.kategori == selectedCatFilter }
    }

    Scaffold(
        containerColor = KosBackground,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddExpenseClick,
                containerColor = KosBluePrimary,
                contentColor = KosWhite,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("fab_add_expense")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Catat Biaya", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = KosErrorContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Pengeluaran Operasional", fontSize = 12.sp, color = KosTextSecondary)
                            Text(
                                KosRepository.formatRupiah(totalExpense),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = KosError
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = KosError,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Category filter chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedCatFilter == null,
                            onClick = { selectedCatFilter = null },
                            label = { Text("Semua (${expenses.size})", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KosBluePrimary,
                                selectedLabelColor = KosWhite
                            )
                        )
                    }
                    items(ExpenseCategory.values()) { cat ->
                        val count = expenses.count { it.kategori == cat }
                        FilterChip(
                            selected = selectedCatFilter == cat,
                            onClick = { selectedCatFilter = if (selectedCatFilter == cat) null else cat },
                            label = { Text("${cat.label} ($count)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KosBluePrimary,
                                selectedLabelColor = KosWhite
                            )
                        )
                    }
                }
            }

            items(filteredExpenses, key = { it.id }) { exp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(KosBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            val icon = when (exp.kategori) {
                                ExpenseCategory.LISTRIK -> Icons.Default.Bolt
                                ExpenseCategory.AIR -> Icons.Default.WaterDrop
                                ExpenseCategory.GAJI -> Icons.Default.Badge
                                ExpenseCategory.PERBAIKAN -> Icons.Default.Handyman
                                ExpenseCategory.WIFI -> Icons.Default.Wifi
                                ExpenseCategory.LAINNYA -> Icons.Default.Category
                            }
                            Icon(icon, contentDescription = null, tint = KosBluePrimary, modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(exp.deskripsi, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KosTextPrimary)
                            Text("${exp.kategori.label} • ${exp.tanggalPengeluaran}", fontSize = 11.sp, color = KosTextSecondary)
                        }

                        Text(
                            text = "-${KosRepository.formatRupiah(exp.nominal)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = KosError
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        if (exp.buktiStruk != null) {
                            val file = remember(exp.buktiStruk) { File(exp.buktiStruk) }
                            val model = remember(exp.buktiStruk) {
                                if (file.exists()) file else Uri.parse(exp.buktiStruk)
                            }
                            IconButton(
                                onClick = { onPreviewReceipt(exp.buktiStruk, "Nota: ${exp.deskripsi}") },
                                modifier = Modifier.size(32.dp)
                            ) {
                                AsyncImage(
                                    model = model,
                                    contentDescription = "Nota Pengeluaran",
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(1.dp, KosBluePrimary.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        IconButton(
                            onClick = { onDeleteExpense(exp) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = KosTextMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfitSharingTabContent(
    report: ProfitSharingReport,
    onTransferPayout: (InvestorPayout) -> Unit,
    onRevertPayout: (InvestorPayout) -> Unit,
    onAddInvestorClick: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = KosBluePrimary)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Kalkulasi Bagi Hasil Bersih", fontSize = 13.sp, color = KosWhite.copy(alpha = 0.8f))
                    Text(
                        KosRepository.formatRupiah(report.labaBersih),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KosWhite
                    )
                    Text("Periode: ${report.periodeBulan}", fontSize = 12.sp, color = KosWhite.copy(alpha = 0.7f), modifier = Modifier.padding(top = 2.dp))

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = KosWhite.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Pemasukan Sewa (+)", fontSize = 11.sp, color = KosWhite.copy(alpha = 0.8f))
                            Text(KosRepository.formatRupiah(report.totalPemasukan), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KosWhite)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Pengeluaran Operasional (-)", fontSize = 11.sp, color = KosWhite.copy(alpha = 0.8f))
                            Text(KosRepository.formatRupiah(report.totalPengeluaran), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KosWhite)
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Pemegang Saham & Payout",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KosTextPrimary
                )

                TextButton(
                    onClick = onAddInvestorClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = KosBluePrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Investor", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KosBluePrimary)
                }
            }
        }

        items(report.payouts, key = { it.id }) { payout ->
            val isTransferred = payout.status == PayoutStatus.DITRANSFER

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = KosWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(payout.investorNama, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KosTextPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(KosBlueContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("${payout.sahamPersen}% Saham", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = KosBlueDark)
                                }
                            }
                            Text(payout.nomorRekening, fontSize = 11.sp, color = KosTextSecondary, modifier = Modifier.padding(top = 2.dp))
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = KosRepository.formatRupiah(payout.nominalBagian),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = KosBluePrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isTransferred) KosSuccessContainer else KosWarningContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = payout.status.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTransferred) KosSuccess else KosWarning
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = KosOutline)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isTransferred && payout.tanggalTransfer != null) {
                            Text(
                                text = "Transfer: ${payout.tanggalTransfer}",
                                fontSize = 11.sp,
                                color = KosSuccess,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "Menunggu transfer pengelola",
                                fontSize = 11.sp,
                                color = KosTextMuted
                            )
                        }

                        if (!isTransferred) {
                            Button(
                                onClick = { onTransferPayout(payout) },
                                colors = ButtonDefaults.buttonColors(containerColor = KosSuccess),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = KosWhite, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Proses Transfer", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KosWhite)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onRevertPayout(payout) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Batalkan", fontSize = 11.sp, color = KosTextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Modal Kuitansi Resmi Digital
@Composable
fun DigitalReceiptDialog(
    invoice: Invoice,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isPaid = invoice.statusPembayaran == PaymentStatus.LUNAS

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Kuitansi Pembayaran", fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isPaid) KosSuccessContainer else KosErrorContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isPaid) "LUNAS" else "BELUM LUNAS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaid) KosSuccess else KosError
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(KosSurfaceVariant)
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("KosConnect Residence", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KosBluePrimary)
                        Text("No. Transaksi: ${invoice.paymentGatewayRef ?: "TX-${invoice.id.take(8)}"}", fontSize = 11.sp, color = KosTextSecondary)
                        Text("Penyewa: ${invoice.penghuniNama} (Kamar ${invoice.roomNomor})", fontSize = 12.sp, color = KosTextPrimary)
                        Text("Periode: ${invoice.bulanTagihan}", fontSize = 12.sp, color = KosTextSecondary)
                        if (isPaid && invoice.tanggalBayar != null) {
                            Text("Waktu Pembayaran: ${invoice.tanggalBayar}", fontSize = 11.sp, color = KosSuccess, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Text("Rincian Biaya:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Sewa Kamar ${invoice.roomNomor}", fontSize = 12.sp, color = KosTextSecondary)
                    Text(KosRepository.formatRupiah(invoice.nominalKamar), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tambahan Listrik & Air", fontSize = 12.sp, color = KosTextSecondary)
                    Text(KosRepository.formatRupiah(invoice.tambahanListrik), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                if (invoice.denda > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Denda Keterlambatan", fontSize = 12.sp, color = KosError)
                        Text(KosRepository.formatRupiah(invoice.denda), fontSize = 12.sp, color = KosError, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = KosOutline)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Pembayaran", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(KosRepository.formatRupiah(invoice.totalTagihan), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = KosBluePrimary)
                }

                if (invoice.kodePembayaran != null) {
                    Text(
                        text = "Metode: ${invoice.metodePembayaran?.label} (${invoice.kodePembayaran})",
                        fontSize = 11.sp,
                        color = KosTextMuted
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val pdfFile = ReportExportService.generateReceiptPdf(context, invoice)
                        ReportExportService.shareFile(
                            context,
                            pdfFile,
                            "application/pdf",
                            "Kuitansi Sewa Kamar ${invoice.roomNomor} - ${invoice.penghuniNama}"
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = KosBluePrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cetak PDF", fontSize = 12.sp, color = KosBluePrimary, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        shareInvoiceWhatsApp(context, invoice)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = KosWhite, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bagikan Teks", color = KosWhite, fontSize = 12.sp)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = KosTextSecondary)
            }
        }
    )
}

// Dialog: Buat Tagihan Baru Manual
@Composable
fun CreateInvoiceDialog(
    rooms: List<Room>,
    onDismiss: () -> Unit,
    onCreate: (String, String, Long, Long, Long, String, String, PaymentMethod) -> Unit
) {
    var selectedRoom by remember { mutableStateOf(rooms.firstOrNull()) }
    var nominalKamar by remember { mutableStateOf(selectedRoom?.hargaSewaDasar?.toString() ?: "1800000") }
    var tambahanListrik by remember { mutableStateOf("150000") }
    var denda by remember { mutableStateOf("0") }
    var bulan by remember { mutableStateOf("Desember 2026") }
    var tempo by remember { mutableStateOf("10 Des 2026") }
    var method by remember { mutableStateOf(PaymentMethod.BCA_VA) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Terbitkan Tagihan Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Pilih Kamar & Penghuni:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

                Column {
                    rooms.forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedRoom = r
                                    nominalKamar = r.hargaSewaDasar.toString()
                                }
                        ) {
                            RadioButton(selected = selectedRoom?.id == r.id, onClick = {
                                selectedRoom = r
                                nominalKamar = r.hargaSewaDasar.toString()
                            })
                            Text("Kamar ${r.nomorKamar} - ${r.penghuniNama ?: "Penghuni"}", fontSize = 13.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = bulan,
                    onValueChange = { bulan = it },
                    label = { Text("Bulan Tagihan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nominalKamar,
                    onValueChange = { nominalKamar = it.filter { c -> c.isDigit() } },
                    label = { Text("Nominal Sewa Kamar (Rp)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tambahanListrik,
                    onValueChange = { tambahanListrik = it.filter { c -> c.isDigit() } },
                    label = { Text("Tambahan Listrik/Air (Rp)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tempo,
                    onValueChange = { tempo = it },
                    label = { Text("Tanggal Jatuh Tempo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = selectedRoom
                    if (r != null) {
                        onCreate(
                            r.nomorKamar,
                            r.penghuniNama ?: "Penghuni",
                            nominalKamar.toLongOrNull() ?: r.hargaSewaDasar,
                            tambahanListrik.toLongOrNull() ?: 0L,
                            denda.toLongOrNull() ?: 0L,
                            bulan,
                            tempo,
                            method
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary)
            ) {
                Text("Terbitkan", color = KosWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = KosTextSecondary)
            }
        }
    )
}

// Helper: Share Invoice via WhatsApp
private fun shareInvoiceWhatsApp(context: Context, invoice: Invoice) {
    val statusText = if (invoice.statusPembayaran == PaymentStatus.LUNAS) "LUNAS ✅" else "BELUM BAYAR ⚠️"
    val msg = """
*TAGIHAN KOSCONNECT RESIDENCE*
Kepada: ${invoice.penghuniNama} (Kamar ${invoice.roomNomor})
Bulan: ${invoice.bulanTagihan}
Status: $statusText

*Rincian Biaya:*
- Sewa Kamar: ${KosRepository.formatRupiah(invoice.nominalKamar)}
- Listrik/Air: ${KosRepository.formatRupiah(invoice.tambahanListrik)}
- Denda: ${KosRepository.formatRupiah(invoice.denda)}
*TOTAL:* ${KosRepository.formatRupiah(invoice.totalTagihan)}

Jatuh Tempo: ${invoice.tanggalJatuhTempo}
Metode: ${invoice.metodePembayaran?.label ?: "BCA Virtual Account"}
No. Rekening/VA: ${invoice.kodePembayaran ?: "800129847291823"}

Terima kasih atas kerja samanya.
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, msg)
    }

    try {
        context.startActivity(Intent.createChooser(intent, "Kirim Tagihan via"))
    } catch (e: Exception) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Tagihan Kos", msg))
        Toast.makeText(context, "Pesan tagihan disalin ke clipboard", Toast.LENGTH_SHORT).show()
    }
}
