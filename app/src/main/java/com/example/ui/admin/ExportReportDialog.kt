package com.example.ui.admin

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Expense
import com.example.data.model.Invoice
import com.example.data.model.ProfitSharingReport
import com.example.ui.theme.*
import com.example.util.ReportExportService

@Composable
fun ExportReportDialog(
    report: ProfitSharingReport,
    invoices: List<Invoice>,
    expenses: List<Expense>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedFormat by remember { mutableStateOf("PDF") } // "PDF" or "CSV"
    var isExporting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(KosBlueContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        tint = KosBluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("Ekspor Laporan Keuangan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Pilih format dokumen laporan keuangan dan bagi hasil untuk periode ${report.periodeBulan}:",
                    fontSize = 12.sp,
                    color = KosTextSecondary
                )

                // Option 1: PDF Document
                FormatSelectCard(
                    icon = Icons.Default.PictureAsPdf,
                    title = "Dokumen PDF Resmi (A4)",
                    subtitle = "Lengkap dengan kop surat, tabel sewa, biaya operasional, dan stempel digital.",
                    selected = selectedFormat == "PDF",
                    accentColor = KosBluePrimary,
                    onClick = { selectedFormat = "PDF" }
                )

                // Option 2: Excel / CSV
                FormatSelectCard(
                    icon = Icons.Default.TableChart,
                    title = "Spreadsheet Excel / CSV",
                    subtitle = "Format tabel data mentah yang dapat diolah langsung di Excel, Google Sheets, atau Calc.",
                    selected = selectedFormat == "CSV",
                    accentColor = KosSuccess,
                    onClick = { selectedFormat = "CSV" }
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = KosSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = KosTextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "File yang diekspor dapat langsung dibuka atau dibagikan ke WhatsApp dan Email.",
                            fontSize = 11.sp,
                            color = KosTextSecondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isExporting = true
                    try {
                        if (selectedFormat == "PDF") {
                            val file = ReportExportService.generateFinancialReportPdf(
                                context, report, invoices, expenses
                            )
                            ReportExportService.shareFile(
                                context,
                                file,
                                "application/pdf",
                                "Laporan Keuangan KosConnect ${report.periodeBulan}"
                            )
                        } else {
                            val file = ReportExportService.generateFinancialCsv(
                                context, report, invoices, expenses
                            )
                            ReportExportService.shareFile(
                                context,
                                file,
                                "text/csv",
                                "Laporan Keuangan KosConnect ${report.periodeBulan}"
                            )
                        }
                        Toast.makeText(context, "Laporan berhasil dibuat!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Gagal mengekspor laporan: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isExporting = false
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary),
                modifier = Modifier.testTag("btn_confirm_export")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = KosWhite, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selectedFormat == "PDF") "Ekspor PDF" else "Ekspor Excel/CSV",
                    color = KosWhite,
                    fontWeight = FontWeight.Bold
                )
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
private fun FormatSelectCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) accentColor.copy(alpha = 0.08f) else KosWhite,
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) accentColor else KosOutline
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = accentColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KosTextPrimary)
                Text(subtitle, fontSize = 10.5.sp, color = KosTextSecondary, lineHeight = 14.sp)
            }
        }
    }
}
