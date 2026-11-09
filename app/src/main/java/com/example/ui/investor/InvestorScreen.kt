package com.example.ui.investor

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.PayoutStatus
import com.example.data.model.ProfitSharingReport
import com.example.data.model.WithdrawalRecord
import com.example.data.repository.KosRepository
import com.example.ui.AdminSummaryState
import com.example.ui.KosViewModel
import com.example.ui.theme.*
import com.example.util.ReportExportService

// PORTAL INVESTOR MANDIRI (Role-Based View: Investor)
@Composable
fun InvestorScreen(
    viewModel: KosViewModel
) {
    val summary by viewModel.summaryState.collectAsState()
    val report = viewModel.getProfitSharingReport()
    val expenses by viewModel.expenses.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val context = LocalContext.current

    // Investor Andi Wijaya (30% shares)
    val mySharePersen = 30.0
    val myPayout = report.payouts.find { it.sahamPersen == mySharePersen }
    val myDividenNominal = myPayout?.nominalBagian ?: (report.labaBersih * (mySharePersen / 100.0)).toLong()
    val isPaidOut = myPayout?.status == PayoutStatus.DITRANSFER

    var selectedTab by remember { mutableIntStateOf(0) }
    var showDownloadDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KosBackground)
    ) {
        // TabRow for Investor Portal
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = KosWhite,
            contentColor = KosBluePrimary,
            edgePadding = 8.dp
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Performa & Okupansi", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_investor_dashboard")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Histori Dividen", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_investor_withdrawals")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Arus Kas", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_investor_cashflow")
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Struktur Saham", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_investor_shares")
            )
        }

        when (selectedTab) {
            0 -> InvestorDashboardTab(
                summary = summary,
                report = report,
                mySharePersen = mySharePersen,
                myDividenNominal = myDividenNominal,
                isPaidOut = isPaidOut,
                expenses = expenses,
                onDownloadClick = { showDownloadDialog = true }
            )
            1 -> InvestorWithdrawalHistoryTab(
                currentPayout = myPayout,
                pastWithdrawals = report.pastWithdrawals,
                onDownloadReport = { showDownloadDialog = true }
            )
            2 -> InvestorCashFlowTab(
                report = report,
                invoices = invoices,
                expenses = expenses
            )
            3 -> InvestorSharesTab(
                report = report
            )
        }
    }

    if (showDownloadDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadDialog = false },
            icon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = KosBluePrimary) },
            title = { Text("Laporan Bagi Hasil Pribadi", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Laporan resmi dividen properti KosConnect Residence periode ${report.periodeBulan} siap diunduh:")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(KosBlueContainer)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("• Investor: Andi Wijaya (30% Saham)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KosBlueDark)
                            Text("• Tingkat Okupansi: ${summary.occupancyRate}% (${summary.occupiedRooms}/${summary.totalRooms} Kamar)", fontSize = 12.sp, color = KosBlueDark)
                            Text("• Pendapatan Kotor: ${KosRepository.formatRupiah(report.totalPemasukan)}", fontSize = 12.sp, color = KosBlueDark)
                            Text("• Biaya Operasional: ${KosRepository.formatRupiah(report.totalPengeluaran)}", fontSize = 12.sp, color = KosBlueDark)
                            Text("• Laba Bersih Properti: ${KosRepository.formatRupiah(report.labaBersih)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KosBlueDark)
                            Text("• Hak Dividen Anda (30%): ${KosRepository.formatRupiah(myDividenNominal)}", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = KosSuccess)
                        }
                    }
                    Text("Dokumen PDF ini memiliki nomor serial resmi & validitas pembagian hasil.", fontSize = 11.sp, color = KosTextSecondary)
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val pdfFile = ReportExportService.generateInvestorReportPdf(
                                context = context,
                                investorName = "Andi Wijaya",
                                sharePercent = mySharePersen,
                                dividendAmount = myDividenNominal,
                                report = report,
                                pastWithdrawals = report.pastWithdrawals
                            )
                            ReportExportService.openFile(context, pdfFile, "application/pdf")
                            showDownloadDialog = false
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.FileOpen, contentDescription = null, tint = KosBluePrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Buka PDF", fontSize = 12.sp, color = KosBluePrimary, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val pdfFile = ReportExportService.generateInvestorReportPdf(
                                context = context,
                                investorName = "Andi Wijaya",
                                sharePercent = mySharePersen,
                                dividendAmount = myDividenNominal,
                                report = report,
                                pastWithdrawals = report.pastWithdrawals
                            )
                            ReportExportService.shareFile(
                                context = context,
                                file = pdfFile,
                                mimeType = "application/pdf",
                                title = "Laporan Dividen KosConnect - Andi Wijaya"
                            )
                            showDownloadDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = KosWhite, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bagikan PDF", color = KosWhite, fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadDialog = false }) {
                    Text("Tutup", color = KosTextSecondary)
                }
            }
        )
    }
}

// TAB 0: DASHBOARD PERFORMA & OKUPANSI
@Composable
private fun InvestorDashboardTab(
    summary: AdminSummaryState,
    report: ProfitSharingReport,
    mySharePersen: Double,
    myDividenNominal: Long,
    isPaidOut: Boolean,
    expenses: List<com.example.data.model.Expense>,
    onDownloadClick: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Investor Greeting & Dividend Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = KosBluePrimary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Halo, Investor Andi! 📈",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = KosWhite
                            )
                            Text(
                                text = "Portofolio: KosConnect Residence (20 Kamar)",
                                style = MaterialTheme.typography.bodySmall,
                                color = KosWhite.copy(alpha = 0.85f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(KosWhite.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Saham: ${mySharePersen.toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = KosWhite
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = KosWhite.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Estimasi Bagi Hasil Dividen Bulan Ini (${report.periodeBulan}):", fontSize = 12.sp, color = KosWhite.copy(alpha = 0.8f))
                    Text(
                        text = KosRepository.formatRupiah(myDividenNominal),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KosWhite
                    )
                    Text(
                        text = "*(30% dari Laba Bersih Properti ${KosRepository.formatRupiah(report.labaBersih)})*",
                        fontSize = 11.sp,
                        color = KosWhite.copy(alpha = 0.75f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // 2. PERFORMA OKUPANSI KOSAN (Detail Visual Breakdown)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Hotel, contentDescription = null, tint = KosBluePrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Performa Okupansi Kosan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = KosTextPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(KosSuccessContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("${summary.occupancyRate}% Okupansi", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KosSuccess)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Occupancy Progress Bar
                    LinearProgressIndicator(
                        progress = { summary.occupancyRate / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = KosBluePrimary,
                        trackColor = KosBlueContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3 Badge breakdown unit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(KosSuccessContainer)
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${summary.occupiedRooms} Kamar", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KosSuccess)
                                Text("Terisi (70%)", fontSize = 10.sp, color = KosSuccess)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(KosBlueContainer)
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${summary.availableRooms} Kamar", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KosBlueDark)
                                Text("Tersedia (20%)", fontSize = 10.sp, color = KosBlueDark)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(KosWarningContainer)
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("2 Kamar", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KosWarning)
                                Text("Maintenance (10%)", fontSize = 10.sp, color = KosWarning)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = KosOutline.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Potensi Omset vs Realisasi
                    val maxPotential = 37000000L
                    val realizationPercent = if (maxPotential > 0) ((report.totalPemasukan * 100) / maxPotential).toInt() else 0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Potensi Omset Maks (100%)", fontSize = 11.sp, color = KosTextSecondary)
                            Text(KosRepository.formatRupiah(maxPotential), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KosTextPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Realisasi Pemasukan", fontSize = 11.sp, color = KosTextSecondary)
                            Text("${KosRepository.formatRupiah(report.totalPemasukan)} ($realizationPercent%)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KosSuccess)
                        }
                    }
                }
            }
        }

        // 3. Ringkasan Arus Kas Transparan
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
                            text = "Laporan Arus Kas Transparan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = KosTextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(KosBlueContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Periode ${report.periodeBulan}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = KosBlueDark)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    CashFlowRow(
                        label = "(+) Pemasukan Sewa Terkumpul",
                        amount = KosRepository.formatRupiah(report.totalPemasukan),
                        color = KosSuccess
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("(-) Rincian Pengeluaran Operasional:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = KosTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))

                    expenses.take(4).forEach { exp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp, horizontal = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("• ${exp.deskripsi}", fontSize = 12.sp, color = KosTextSecondary)
                            Text("-${KosRepository.formatRupiah(exp.nominal)}", fontSize = 12.sp, color = KosError)
                        }
                    }

                    Divider(color = KosOutline, modifier = Modifier.padding(vertical = 10.dp))

                    CashFlowRow(
                        label = "(=) Laba Bersih Properti",
                        amount = KosRepository.formatRupiah(report.labaBersih),
                        color = KosBluePrimary,
                        isBold = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    CashFlowRow(
                        label = "(★) Porsi Dividen Anda (30%)",
                        amount = KosRepository.formatRupiah(myDividenNominal),
                        color = KosSuccess,
                        isBold = true
                    )
                }
            }
        }

        // 4. Status Pencairan & Rekening
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
                        Column {
                            Text("Status Pencairan Bulan Ini", fontSize = 12.sp, color = KosTextSecondary)
                            Text("Rekening BCA 8839-0129-39 (a.n Andi Wijaya)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = KosTextPrimary)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isPaidOut) KosSuccessContainer else KosWarningContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isPaidOut) "Sudah Ditransfer ✅" else "Menunggu Ditransfer ⏳",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPaidOut) KosSuccess else KosWarning
                            )
                        }
                    }
                }
            }
        }

        // 5. Tombol Unduh Laporan Bagi Hasil Pribadi (PDF)
        item {
            Button(
                onClick = onDownloadClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_unduh_laporan_pdf"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = KosWhite)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "UNDUH LAPORAN BAGI HASIL PRIBADI (PDF)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = KosWhite
                )
            }
        }
    }
}

// TAB 1: HISTORI DIVIDEN BULANAN
@Composable
private fun InvestorWithdrawalHistoryTab(
    currentPayout: com.example.data.model.InvestorPayout?,
    pastWithdrawals: List<WithdrawalRecord>,
    onDownloadReport: () -> Unit
) {
    val totalPastWithdrawals = pastWithdrawals.sumOf { it.nominal }
    val currentNominal = currentPayout?.nominalBagian ?: 0L
    val totalAllTime = totalPastWithdrawals + (if (currentPayout?.status == PayoutStatus.DITRANSFER) currentNominal else 0L)

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Total Kumulatif Dividen Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = KosBluePrimary)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Total Dividen Diterima Sepanjang Masa",
                        fontSize = 12.sp,
                        color = KosWhite.copy(alpha = 0.85f)
                    )
                    Text(
                        text = KosRepository.formatRupiah(totalAllTime),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KosWhite
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = KosWhite.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Frekuensi Pencairan", fontSize = 11.sp, color = KosWhite.copy(alpha = 0.8f))
                            Text("${pastWithdrawals.size + 1} Bulan Berjalan", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KosWhite)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Rata-rata Dividen / Bulan", fontSize = 11.sp, color = KosWhite.copy(alpha = 0.8f))
                            Text(KosRepository.formatRupiah(totalAllTime / (pastWithdrawals.size + 1).coerceAtLeast(1)), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KosWhite)
                        }
                    }
                }
            }
        }

        // Dividen Bulan Berjalan (November 2026)
        if (currentPayout != null) {
            item {
                val isPaid = currentPayout.status == PayoutStatus.DITRANSFER
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = KosWhite),
                    border = BorderStroke(1.5.dp, if (isPaid) KosSuccess else KosWarning)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Bulan Berjalan: November 2026", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KosTextPrimary)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isPaid) KosSuccessContainer else KosWarningContainer)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isPaid) "SUDAH DITRANSFER ✅" else "MENUNGGU DITRANSFER ⏳",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPaid) KosSuccess else KosWarning
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Nominal Dividen (30% Saham):", fontSize = 12.sp, color = KosTextSecondary)
                        Text(KosRepository.formatRupiah(currentPayout.nominalBagian), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = KosBluePrimary)

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Rekening: ${currentPayout.nomorRekening}", fontSize = 12.sp, color = KosTextSecondary)
                        if (isPaid && currentPayout.tanggalTransfer != null) {
                            Text("Waktu Transfer: ${currentPayout.tanggalTransfer}", fontSize = 12.sp, color = KosSuccess, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onDownloadReport,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = KosBluePrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Unduh Slip Dividen PDF", color = KosBluePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Text("Histori Pencairan Dividen Bulan-Bulan Sebelumnya", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KosTextPrimary)
        }

        items(pastWithdrawals, key = { it.id }) { record ->
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(record.periodeBulan, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KosTextPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(KosSuccessContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("LUNAS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = KosSuccess)
                                }
                            }
                            Text("Ditransfer: ${record.tanggalTransfer} • Ref: ${record.bankRef}", fontSize = 11.sp, color = KosTextSecondary, modifier = Modifier.padding(top = 2.dp))
                            Text("Tujuan: ${record.nomorRekening}", fontSize = 11.sp, color = KosTextMuted)
                        }

                        Text(
                            text = KosRepository.formatRupiah(record.nominal),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = KosSuccess
                        )
                    }
                }
            }
        }
    }
}

// TAB 2: ARUS KAS PROPERTI
@Composable
private fun InvestorCashFlowTab(
    report: ProfitSharingReport,
    invoices: List<com.example.data.model.Invoice>,
    expenses: List<com.example.data.model.Expense>
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
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Transparansi Keuangan Properti", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = KosTextPrimary)
                    Text("Semua data pemasukan dan bukti biaya operasional disinkronkan secara realtime.", fontSize = 12.sp, color = KosTextSecondary, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Sewa Terkumpul", fontSize = 13.sp, color = KosTextSecondary)
                        Text(KosRepository.formatRupiah(report.totalPemasukan), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KosSuccess)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Biaya Operasional", fontSize = 13.sp, color = KosTextSecondary)
                        Text("-${KosRepository.formatRupiah(report.totalPengeluaran)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KosError)
                    }
                    Divider(color = KosOutline, modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Laba Bersih Siap Bagi Hasil", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KosTextPrimary)
                        Text(KosRepository.formatRupiah(report.labaBersih), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = KosBluePrimary)
                    }
                }
            }
        }

        item {
            Text("Rincian Seluruh Nota Pengeluaran Operasional", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KosTextPrimary)
        }

        items(expenses, key = { it.id }) { exp ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = KosWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(exp.deskripsi, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KosTextPrimary)
                        Text("${exp.kategori.label} • ${exp.tanggalPengeluaran}", fontSize = 11.sp, color = KosTextSecondary)
                    }
                    Text("-${KosRepository.formatRupiah(exp.nominal)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KosError)
                }
            }
        }
    }
}

// TAB 3: STRUKTUR SAHAM PROPERTI
@Composable
private fun InvestorSharesTab(
    report: ProfitSharingReport
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
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Struktur Kepemilikan Saham Properti", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = KosTextPrimary)
                    Text("Dividen dibagikan sesuai dengan persentase kepemilikan modal yang sah.", fontSize = 12.sp, color = KosTextSecondary, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))

                    report.payouts.forEach { payout ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(payout.investorNama, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = KosTextPrimary)
                                Text("${payout.sahamPersen}%", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KosBluePrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (payout.sahamPersen / 100f).toFloat() },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = if (payout.investorNama.contains("Andi")) KosBluePrimary else KosCyanAccent,
                                trackColor = KosBlueContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CashFlowRow(label: String, amount: String, color: Color, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = if (isBold) 14.sp else 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = KosTextPrimary
        )
        Text(
            text = amount,
            fontSize = if (isBold) 15.sp else 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = color
        )
    }
}
