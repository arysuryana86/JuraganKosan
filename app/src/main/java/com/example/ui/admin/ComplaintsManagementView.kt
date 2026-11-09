package com.example.ui.admin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import com.example.ui.KosViewModel
import com.example.ui.components.ImagePreviewDialog
import com.example.ui.components.PhotoUploadPicker
import com.example.ui.theme.*
import com.example.util.AiSmartService
import com.example.util.ComplaintAiTriage
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun ComplaintsManagementView(
    viewModel: KosViewModel
) {
    val complaints by viewModel.complaints.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedComplaint by remember { mutableStateOf<Complaint?>(null) }
    var showAddAnnouncementDialog by remember { mutableStateOf(false) }

    var previewImage by remember { mutableStateOf<String?>(null) }
    var previewTitle by remember { mutableStateOf("") }

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
            val pendingCount = complaints.count { it.statusKomplain != ComplaintStatus.SELESAI }
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Tiket Keluhan", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        if (pendingCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(KosError)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("$pendingCount", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = KosWhite)
                            }
                        }
                    }
                },
                icon = { Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Pengumuman (${announcements.size})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                icon = { Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        when (selectedTab) {
            0 -> ComplaintsListTab(
                complaints = complaints,
                rooms = rooms,
                onComplaintClick = { selectedComplaint = it },
                onContactTenant = { c ->
                    val r = rooms.find { it.nomorKamar == c.roomNomor }
                    val phone = r?.penghuniHp
                    if (!phone.isNullOrBlank()) {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Kontak: $phone", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Kontak tidak tersedia", Toast.LENGTH_SHORT).show()
                    }
                },
                onViewPhoto = { path, title ->
                    previewImage = path
                    previewTitle = title
                }
            )
            1 -> AnnouncementsListTab(
                announcements = announcements,
                onAddClick = { showAddAnnouncementDialog = true },
                onDeleteClick = { ann ->
                    viewModel.deleteAnnouncement(ann.id)
                    Toast.makeText(context, "Pengumuman dihapus", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    if (selectedComplaint != null) {
        UpdateComplaintDialog(
            complaint = selectedComplaint!!,
            onDismiss = { selectedComplaint = null },
            onUpdate = { newStatus, catatan, fotoPerbaikan ->
                viewModel.updateComplaintStatus(selectedComplaint!!.id, newStatus, catatan, fotoPerbaikan)
                selectedComplaint = null
                Toast.makeText(context, "Status penanganan tiket berhasil diperbarui", Toast.LENGTH_SHORT).show()
            },
            onViewPhoto = { path, title ->
                previewImage = path
                previewTitle = title
            }
        )
    }

    if (showAddAnnouncementDialog) {
        AddAnnouncementDialog(
            onDismiss = { showAddAnnouncementDialog = false },
            onAdd = { judul, isi, prioritas, kat ->
                viewModel.addAnnouncement(judul, isi, prioritas, kat)
                showAddAnnouncementDialog = false
                Toast.makeText(context, "Pengumuman berhasil diterbitkan", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (previewImage != null) {
        ImagePreviewDialog(
            title = previewTitle,
            imagePath = previewImage!!,
            onDismiss = { previewImage = null }
        )
    }
}


@Composable
private fun ComplaintsListTab(
    complaints: List<Complaint>,
    rooms: List<Room>,
    onComplaintClick: (Complaint) -> Unit,
    onContactTenant: (Complaint) -> Unit,
    onViewPhoto: (String, String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf<ComplaintStatus?>(null) }

    val filteredComplaints = remember(complaints, selectedFilter) {
        if (selectedFilter == null) complaints else complaints.filter { it.statusKomplain == selectedFilter }
    }

    val pendingCount = complaints.count { it.statusKomplain == ComplaintStatus.MENUNGGU }
    val progressCount = complaints.count { it.statusKomplain == ComplaintStatus.DIPROSES }
    val resolvedCount = complaints.count { it.statusKomplain == ComplaintStatus.SELESAI }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Metric Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KosWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Menunggu", fontSize = 11.sp, color = KosTextSecondary)
                        Text("$pendingCount", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = KosError)
                    }
                    Divider(modifier = Modifier.height(36.dp).width(1.dp), color = KosOutline)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Diproses", fontSize = 11.sp, color = KosTextSecondary)
                        Text("$progressCount", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = KosWarning)
                    }
                    Divider(modifier = Modifier.height(36.dp).width(1.dp), color = KosOutline)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Selesai", fontSize = 11.sp, color = KosTextSecondary)
                        Text("$resolvedCount", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = KosSuccess)
                    }
                }
            }
        }

        // Filter chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("Semua (${complaints.size})", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KosBluePrimary,
                            selectedLabelColor = KosWhite
                        )
                    )
                }
                items(ComplaintStatus.values()) { status ->
                    val count = complaints.count { it.statusKomplain == status }
                    FilterChip(
                        selected = selectedFilter == status,
                        onClick = { selectedFilter = if (selectedFilter == status) null else status },
                        label = { Text("${status.label} ($count)", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (status) {
                                ComplaintStatus.MENUNGGU -> KosError
                                ComplaintStatus.DIPROSES -> KosWarning
                                ComplaintStatus.SELESAI -> KosSuccess
                            },
                            selectedLabelColor = KosWhite
                        )
                    )
                }
            }
        }

        items(filteredComplaints, key = { it.id }) { complaint ->
            ComplaintItemCard(
                complaint = complaint,
                onClick = { onComplaintClick(complaint) },
                onContactClick = { onContactTenant(complaint) },
                onViewPhoto = onViewPhoto
            )
        }
    }
}

@Composable
private fun ComplaintItemCard(
    complaint: Complaint,
    onClick: () -> Unit,
    onContactClick: () -> Unit,
    onViewPhoto: (String, String) -> Unit
) {

    val (statusBg, statusFg, statusIcon) = when (complaint.statusKomplain) {
        ComplaintStatus.MENUNGGU -> Triple(KosErrorContainer, KosError, Icons.Default.Warning)
        ComplaintStatus.DIPROSES -> Triple(KosWarningContainer, KosWarning, Icons.Default.Build)
        ComplaintStatus.SELESAI -> Triple(KosSuccessContainer, KosSuccess, Icons.Default.CheckCircle)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("complaint_card_${complaint.id}"),
        colors = CardDefaults.cardColors(containerColor = KosWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            text = complaint.roomNomor,
                            fontWeight = FontWeight.ExtraBold,
                            color = KosBluePrimary,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = complaint.penghuniNama,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = KosTextPrimary
                        )
                        Text(complaint.createdAt, fontSize = 11.sp, color = KosTextSecondary)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(statusIcon, contentDescription = null, tint = statusFg, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = complaint.statusKomplain.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusFg
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = complaint.judulMasalah,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KosTextPrimary
            )
            Text(
                text = complaint.deskripsiMasalah,
                style = MaterialTheme.typography.bodySmall,
                color = KosTextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (!complaint.catatanPerbaikan.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(KosBlueContainer.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Handyman, contentDescription = null, tint = KosBluePrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tindakan: ${complaint.catatanPerbaikan}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = KosBlueDark
                        )
                    }
                }
            }

            // Photos Row
            if (!complaint.fotoKerusakanUri.isNullOrBlank() || !complaint.fotoPerbaikanUri.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!complaint.fotoKerusakanUri.isNullOrBlank()) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onViewPhoto(complaint.fotoKerusakanUri, "Foto Kerusakan Kamar ${complaint.roomNomor}")
                                },
                            color = KosSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White)
                                ) {
                                    AsyncImage(
                                        model = complaint.fotoKerusakanUri,
                                        contentDescription = "Bukti Kerusakan",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Bukti Foto", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KosTextPrimary)
                                    Text("Kerusakan", fontSize = 10.sp, color = KosTextSecondary)
                                }
                            }
                        }
                    }

                    if (!complaint.fotoPerbaikanUri.isNullOrBlank()) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onViewPhoto(complaint.fotoPerbaikanUri, "Hasil Perbaikan Kamar ${complaint.roomNomor}")
                                },
                            color = KosSuccessContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White)
                                ) {
                                    AsyncImage(
                                        model = complaint.fotoPerbaikanUri,
                                        contentDescription = "Hasil Perbaikan",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Foto Selesai", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KosSuccess)
                                    Text("Hasil Teknisi", fontSize = 10.sp, color = KosTextSecondary)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = KosOutline)
            Spacer(modifier = Modifier.height(8.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onContactClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = KosBluePrimary, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hubungi Penghuni", fontSize = 11.sp, color = KosBluePrimary, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Update Penanganan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KosWhite)
                }
            }
        }
    }
}

@Composable
private fun AnnouncementsListTab(
    announcements: List<Announcement>,
    onAddClick: () -> Unit,
    onDeleteClick: (Announcement) -> Unit
) {
    Scaffold(
        containerColor = KosBackground,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                containerColor = KosBluePrimary,
                contentColor = KosWhite,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("fab_add_announcement")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pengumuman Baru", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            item {
                Text(
                    text = "Papan Informasi & Pengumuman Penghuni",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KosTextPrimary
                )
                Text(
                    text = "Semua pengumuman yang diterbitkan di sini akan langsung tampil pada layar seluruh penghuni kos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = KosTextSecondary,
                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                )
            }

            items(announcements, key = { it.id }) { ann ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = KosWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (ann.prioritas) {
                                            AnnouncementPriority.NORMAL -> KosBlueContainer
                                            AnnouncementPriority.PENTING -> KosWarningContainer
                                            AnnouncementPriority.DARURAT -> KosErrorContainer
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = ann.prioritas.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (ann.prioritas) {
                                        AnnouncementPriority.NORMAL -> KosBluePrimary
                                        AnnouncementPriority.PENTING -> KosWarning
                                        AnnouncementPriority.DARURAT -> KosError
                                    }
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(ann.tanggal, fontSize = 11.sp, color = KosTextMuted)
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { onDeleteClick(ann) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = KosTextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(ann.judul, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KosTextPrimary)
                        Text(ann.isi, fontSize = 13.sp, color = KosTextSecondary, modifier = Modifier.padding(top = 4.dp))

                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(KosSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(ann.kategori, fontSize = 10.sp, color = KosTextSecondary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateComplaintDialog(
    complaint: Complaint,
    onDismiss: () -> Unit,
    onUpdate: (ComplaintStatus, String?, String?) -> Unit,
    onViewPhoto: (String, String) -> Unit = { _, _ -> }
) {
    var status by remember { mutableStateOf<ComplaintStatus>(complaint.statusKomplain) }
    var catatan by remember { mutableStateOf(complaint.catatanPerbaikan ?: "") }
    var fotoPerbaikan by remember { mutableStateOf(complaint.fotoPerbaikanUri) }

    val coroutineScope = rememberCoroutineScope()
    var isAnalyzing by remember { mutableStateOf(false) }
    var aiTriage by remember { mutableStateOf<ComplaintAiTriage?>(null) }
    var aiError by remember { mutableStateOf<String?>(null) }

    fun runAiTriage() {
        isAnalyzing = true
        aiError = null
        coroutineScope.launch {
            try {
                val result = AiSmartService.analyzeComplaint(
                    judul = complaint.judulMasalah,
                    deskripsi = complaint.deskripsiMasalah,
                    roomNomor = complaint.roomNomor
                )
                aiTriage = result
            } catch (e: Exception) {
                aiError = e.localizedMessage ?: "Gagal menganalisis komplain"
            } finally {
                isAnalyzing = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Update Laporan Komplain", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(KosBlueContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Kamar ${complaint.roomNomor}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KosBluePrimary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${complaint.judulMasalah}: \"${complaint.deskripsiMasalah}\"",
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = KosTextPrimary
                )

                // AI Triage Action Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = KosBlueContainer.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = KosBluePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Triage & Estimasi Biaya", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = KosBlueDark)
                            }

                            if (!isAnalyzing && aiTriage == null) {
                                Button(
                                    onClick = { runAiTriage() },
                                    colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp).testTag("btn_run_ai_triage")
                                ) {
                                    Text("Analisis AI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KosWhite)
                                }
                            }
                        }

                        if (isAnalyzing) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = KosBluePrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Gemini sedang menganalisis kerusakan...", fontSize = 11.sp, color = KosTextSecondary)
                            }
                        }

                        if (aiError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Gagal: $aiError", fontSize = 11.sp, color = KosError)
                        }

                        if (aiTriage != null) {
                            val triage = aiTriage!!
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = KosOutline)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tingkat Urgensi:", fontSize = 11.sp, color = KosTextSecondary)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (triage.tingkatUrgensi.uppercase()) {
                                                "MENDESAK" -> KosErrorContainer
                                                "SEDANG" -> KosWarningContainer
                                                else -> KosBlueContainer
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = triage.tingkatUrgensi.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (triage.tingkatUrgensi.uppercase()) {
                                            "MENDESAK" -> KosError
                                            "SEDANG" -> KosWarning
                                            else -> KosBluePrimary
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Estimasi Biaya Wajar:", fontSize = 11.sp, color = KosTextSecondary)
                                Text(triage.estimasiBiayaFormatted, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = KosTextPrimary)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Diagnosa: ${triage.solusiTeknis}",
                                fontSize = 11.5.sp,
                                color = KosTextPrimary,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Saran: ${triage.rekomendasiTindakan}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = KosBlueDark,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    catatan = "${triage.rekomendasiTindakan} (Est. Biaya: ${triage.estimasiBiayaFormatted})"
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.fillMaxWidth().height(32.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Terapkan ke Catatan Penanganan", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Text("Status Penanganan:", fontSize = 12.sp, color = KosTextSecondary, fontWeight = FontWeight.SemiBold)
                Column {
                    ComplaintStatus.values().forEach { s ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { status = s }
                        ) {
                            RadioButton(selected = status == s, onClick = { status = s })
                            Text(s.label, fontSize = 13.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Catatan Penanganan / Teknisi") },
                    placeholder = { Text("misal: Teknisi AC datang jam 14:00") },
                    modifier = Modifier.fillMaxWidth().testTag("input_complaint_notes")
                )

                // Damage photo from tenant preview
                if (!complaint.fotoKerusakanUri.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onViewPhoto(complaint.fotoKerusakanUri, "Bukti Kerusakan Kamar ${complaint.roomNomor}")
                            },
                        color = KosSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White)
                            ) {
                                AsyncImage(
                                    model = complaint.fotoKerusakanUri,
                                    contentDescription = "Bukti Kerusakan",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Foto Bukti dari Penghuni", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KosTextPrimary)
                                Text("Ketuk untuk melihat foto dalam ukuran penuh", fontSize = 10.sp, color = KosTextSecondary)
                            }
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = KosBluePrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Technician Repair Photo Upload
                PhotoUploadPicker(
                    label = "Dokumentasi Foto Hasil Perbaikan Teknisi (Opsional)",
                    imagePath = fotoPerbaikan,
                    onImageSelected = { fotoPerbaikan = it },
                    sampleTitle = "Hasil Perbaikan Selesai",
                    sampleType = "REPAIR"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onUpdate(status, catatan.ifBlank { null }, fotoPerbaikan) },
                colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary)
            ) {
                Text("Simpan Status", color = KosWhite)
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
fun AddAnnouncementDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, AnnouncementPriority, String) -> Unit
) {
    var judul by remember { mutableStateOf("") }
    var isi by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf("Pemeliharaan") }
    var prioritas by remember { mutableStateOf(AnnouncementPriority.NORMAL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat Pengumuman Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = judul,
                    onValueChange = { judul = it },
                    label = { Text("Judul Pengumuman") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = isi,
                    onValueChange = { isi = it },
                    label = { Text("Isi Pengumuman Lengkap") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = kategori,
                    onValueChange = { kategori = it },
                    label = { Text("Kategori (misal: Air, Listrik, Kebersihan)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Tingkat Kepentingan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AnnouncementPriority.values().forEach { p ->
                        OutlinedButton(
                            onClick = { prioritas = p },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (prioritas == p) KosBluePrimary else KosWhite
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text(p.label, fontSize = 10.sp, color = if (prioritas == p) KosWhite else KosTextPrimary, maxLines = 1)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (judul.isNotBlank() && isi.isNotBlank()) {
                        onAdd(judul, isi, prioritas, kategori)
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
