package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.TenancyAgreement
import com.example.data.repository.KosRepository
import com.example.ui.theme.*
import com.example.util.ReportExportService

@Composable
fun TenancyAgreementDialog(
    agreement: TenancyAgreement,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("dialog_tenancy_agreement"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = KosWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
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
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(KosWhite),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = KosBluePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Surat Perjanjian Sewa Digital",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = KosWhite
                                )
                                Text(
                                    text = "No: ${agreement.nomorSurat}",
                                    fontSize = 11.5.sp,
                                    color = KosWhite.copy(alpha = 0.85f)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = KosWhite)
                        }
                    }
                }

                // Scrollable Contract Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Legal Status Banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = KosSuccessContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = KosSuccess,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "DOKUMEN HUKUM RESMI & SAH",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = KosSuccess
                                )
                                Text(
                                    text = "Telah ditandatangani secara elektronik sesuai UU ITE No. 11/2008.",
                                    fontSize = 11.sp,
                                    color = KosTextSecondary
                                )
                            }
                        }
                    }

                    // Parties Info Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = KosBlueContainer.copy(alpha = 0.4f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, KosBluePrimary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Pihak Pertama
                            Column {
                                Text("PIHAK PERTAMA (PENGELOLA KOS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KosBluePrimary)
                                Text(
                                    text = "${agreement.pihakPertamaNama} (${agreement.pihakPertamaJabatan})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KosTextPrimary
                                )
                                Text("Alamat: ${agreement.pihakPertamaAlamat}", fontSize = 11.5.sp, color = KosTextSecondary)
                                Text("Kontak: ${agreement.pihakPertamaHp}", fontSize = 11.5.sp, color = KosTextSecondary)
                            }

                            HorizontalDivider(color = KosBluePrimary.copy(alpha = 0.2f))

                            // Pihak Kedua
                            Column {
                                Text("PIHAK KEDUA (PENYEWA / PENGHUNI)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KosBluePrimary)
                                Text(
                                    text = agreement.pihakKeduaNama,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KosTextPrimary
                                )
                                Text("NIK / KTP: ${agreement.pihakKeduaNik} • Pekerjaan: ${agreement.pihakKeduaPekerjaan}", fontSize = 11.5.sp, color = KosTextSecondary)
                                Text("Kamar Sewa: Kamar ${agreement.roomNomor} (${agreement.tipeKamar})", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = KosBlueDark)
                                Text("No. WhatsApp: ${agreement.pihakKeduaHp}", fontSize = 11.5.sp, color = KosTextSecondary)
                            }
                        }
                    }

                    // Summary Key Terms Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = KosWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Ketentuan Pokok Sewa", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KosTextPrimary)
                            Spacer(modifier = Modifier.height(10.dp))

                            ContractItemRow("Masa Sewa", "${agreement.tanggalMulai} s/d ${agreement.tanggalSelesai}")
                            ContractItemRow("Tarif Sewa Bulanan", "${KosRepository.formatRupiah(agreement.hargaSewaBulanan)} / bulan")
                            ContractItemRow("Jatuh Tempo Pembayaran", "Setiap Tanggal ${agreement.jatuhTempoTgl} Tiap Bulan")
                            ContractItemRow("Uang Jaminan (Deposit)", "${KosRepository.formatRupiah(agreement.depositJaminan)} (Dapat Dicairkan)")
                            ContractItemRow("Denda Keterlambatan", "${KosRepository.formatRupiah(agreement.dendaKeterlambatanPerHari)} / hari kalender")
                        }
                    }

                    // Facilities Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = KosWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Fasilitas Kamar Termasuk", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KosTextPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            agreement.fasilitas.forEach { fas ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = KosSuccess, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(fas, fontSize = 12.sp, color = KosTextPrimary)
                                }
                            }
                        }
                    }

                    // Rules & Restrictions Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = KosWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Pasal Tata Tertib & Larangan Utama", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KosTextPrimary)
                            Text("• Jam Malam: Pintu gerbang utama ditutup dan dikunci pukul 23:00 WIB demi keamanan.", fontSize = 11.5.sp, color = KosTextSecondary)
                            Text("• Tamu: Tamu dilarang menginap tanpa izin tertulis pengelola kos.", fontSize = 11.5.sp, color = KosTextSecondary)
                            Text("• Larangan Mutlak: Bebas Narkoba, Miras, Senjata, Asusila, dan Perjudian. Pelanggaran berakibat pemutusan sewa seketika & dilaporkan ke Kepolisian.", fontSize = 11.5.sp, color = KosError, fontWeight = FontWeight.Medium)
                            Text("• Merokok: Dilarang merokok atau vape di dalam kamar tidur.", fontSize = 11.5.sp, color = KosTextSecondary)
                            Text("• Subletting: Dilarang mengalihkan atau menyewakan ulang kamar kepada pihak ketiga.", fontSize = 11.5.sp, color = KosTextSecondary)
                        }
                    }

                    // Digital Signature & Meterai Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = KosBlueContainer.copy(alpha = 0.3f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, KosBluePrimary.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Pengesahan Tanda Tangan Elektronik", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = KosBluePrimary)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Pihak I Sign Box
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(1.dp, KosOutline, RoundedCornerShape(8.dp))
                                        .background(KosWhite)
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Pihak Pertama", fontSize = 10.sp, color = KosTextSecondary)
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = KosSuccess, modifier = Modifier.size(24.dp).padding(vertical = 2.dp))
                                    Text("VERIFIED", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = KosSuccess)
                                    Text(agreement.pihakPertamaNama, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = KosTextPrimary, maxLines = 1)
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Meterai Box
                                Column(
                                    modifier = Modifier
                                        .width(76.dp)
                                        .border(1.dp, Color(0xFFE65100), RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFFF3E0))
                                        .padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("METERAI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                    Text("10000", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFE65100))
                                    Text("ELEKTRONIK", fontSize = 7.5.sp, color = Color(0xFF757575))
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Pihak II Sign Box
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(1.dp, KosOutline, RoundedCornerShape(8.dp))
                                        .background(KosWhite)
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Pihak Kedua", fontSize = 10.sp, color = KosTextSecondary)
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = KosSuccess, modifier = Modifier.size(24.dp).padding(vertical = 2.dp))
                                    Text("VERIFIED", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = KosSuccess)
                                    Text(agreement.pihakKeduaNama, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = KosTextPrimary, maxLines = 1)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Kode Verifikasi: ${agreement.digitalSignatureHash}",
                                fontSize = 9.5.sp,
                                color = KosTextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Action Buttons at Bottom
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = KosWhite
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val pdfFile = ReportExportService.generateTenancyAgreementPdf(context, agreement)
                                ReportExportService.shareFile(
                                    context = context,
                                    file = pdfFile,
                                    mimeType = "application/pdf",
                                    title = "Surat Perjanjian Sewa Kamar ${agreement.roomNomor} - ${agreement.pihakKeduaNama}"
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_share_agreement_pdf")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = KosBluePrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bagikan PDF", color = KosBluePrimary, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }

                        Button(
                            onClick = {
                                val pdfFile = ReportExportService.generateTenancyAgreementPdf(context, agreement)
                                ReportExportService.openFile(context, pdfFile, "application/pdf")
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp)
                                .testTag("btn_open_agreement_pdf")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = KosWhite, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buka Dokumen PDF", color = KosWhite, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContractItemRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = KosTextSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KosTextPrimary)
    }
}
