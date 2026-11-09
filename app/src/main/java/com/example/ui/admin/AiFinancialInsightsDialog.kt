package com.example.ui.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Expense
import com.example.data.model.ProfitSharingReport
import com.example.ui.AdminSummaryState
import com.example.ui.theme.*
import com.example.util.AiSmartService
import kotlinx.coroutines.launch

@Composable
fun AiFinancialInsightsDialog(
    report: ProfitSharingReport,
    summary: AdminSummaryState,
    expenses: List<Expense>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var streamedText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun startAnalysis() {
        isLoading = true
        errorMessage = null
        streamedText = ""

        coroutineScope.launch {
            try {
                AiSmartService.generateFinancialAnalysisStream(report, summary, expenses).collect { chunk ->
                    streamedText += chunk
                    isLoading = false
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Gagal memproses analisis AI"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        startAnalysis()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp)),
            color = KosWhite
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KosBluePrimary)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(KosWhite.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = KosWhite,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "AI Financial Advisor",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KosWhite
                                )
                                Text(
                                    text = "Analisis Cerdas & Rekomendasi Profit Kos",
                                    fontSize = 11.sp,
                                    color = KosWhite.copy(alpha = 0.85f)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(KosWhite.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = KosWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    // Summary Pills
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = KosBlueContainer.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Periode", fontSize = 11.sp, color = KosTextSecondary)
                                Text(report.periodeBulan, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KosBlueDark)
                            }
                            Column {
                                Text("Okupansi", fontSize = 11.sp, color = KosTextSecondary)
                                Text("${summary.occupancyRate}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KosBluePrimary)
                            }
                            Column {
                                Text("Laba Bersih", fontSize = 11.sp, color = KosTextSecondary)
                                Text(
                                    com.example.data.repository.KosRepository.formatRupiah(report.labaBersih),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KosSuccess
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoading && streamedText.isBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = KosBluePrimary, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    "Gemini sedang menganalisis laporan keuangan & biaya operasional...",
                                    fontSize = 12.sp,
                                    color = KosTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else if (errorMessage != null && streamedText.isBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = KosErrorContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Gagal Memuat Analisis", fontWeight = FontWeight.Bold, color = KosError)
                                Text(errorMessage ?: "", fontSize = 12.sp, color = KosTextPrimary, modifier = Modifier.padding(top = 4.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { startAnalysis() },
                                    colors = ButtonDefaults.buttonColors(containerColor = KosError)
                                ) {
                                    Text("Coba Lagi", color = KosWhite)
                                }
                            }
                        }
                    } else {
                        // Narrative Output
                        Text(
                            text = streamedText,
                            fontSize = 13.5.sp,
                            color = KosTextPrimary,
                            lineHeight = 22.sp,
                            modifier = Modifier.testTag("ai_financial_insights_text")
                        )

                        if (isLoading) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = KosBluePrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sedang menyusun rekomendasi lanjutan...", fontSize = 11.sp, color = KosTextMuted)
                            }
                        }
                    }
                }

                // Bottom Action Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = KosSurfaceVariant,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { startAnalysis() },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Analisis Ulang", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (streamedText.isNotBlank()) {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Analisis Keuangan KosConnect", streamedText))
                                    Toast.makeText(context, "Hasil analisis disalin ke clipboard", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = streamedText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = KosWhite, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salin Hasil", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KosWhite)
                        }
                    }
                }
            }
        }
    }
}
