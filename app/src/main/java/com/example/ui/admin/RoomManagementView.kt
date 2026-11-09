package com.example.ui.admin

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.data.model.Room
import com.example.data.model.RoomStatus
import com.example.data.repository.KosRepository
import com.example.ui.KosViewModel
import com.example.ui.theme.*

enum class RoomSortOption(val label: String) {
    NUMBER_ASC("Nomor Kamar"),
    PRICE_LOW("Harga Termurah"),
    PRICE_HIGH("Harga Termahal")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomManagementView(
    viewModel: KosViewModel
) {
    val rooms by viewModel.rooms.collectAsState()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<RoomStatus?>(null) }
    var selectedSort by remember { mutableStateOf(RoomSortOption.NUMBER_ASC) }

    // Dialog & Sheet States
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedRoomForDetail by remember { mutableStateOf<Room?>(null) }
    var roomToCheckIn by remember { mutableStateOf<Room?>(null) }
    var roomToEdit by remember { mutableStateOf<Room?>(null) }
    var roomToDeleteConfirm by remember { mutableStateOf<Room?>(null) }
    var roomToCheckOutConfirm by remember { mutableStateOf<Room?>(null) }

    // Filter & Sort Logic
    val processedRooms = remember(rooms, searchQuery, selectedFilter, selectedSort) {
        var result = rooms

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            result = result.filter {
                it.nomorKamar.lowercase().contains(q) ||
                it.tipeKamar.lowercase().contains(q) ||
                (it.penghuniNama?.lowercase()?.contains(q) == true)
            }
        }

        if (selectedFilter != null) {
            result = result.filter { it.status == selectedFilter }
        }

        when (selectedSort) {
            RoomSortOption.NUMBER_ASC -> result.sortedBy { it.nomorKamar }
            RoomSortOption.PRICE_LOW -> result.sortedBy { it.hargaSewaDasar }
            RoomSortOption.PRICE_HIGH -> result.sortedByDescending { it.hargaSewaDasar }
        }
    }

    val totalRooms = rooms.size
    val occupiedRooms = rooms.count { it.status == RoomStatus.TERISI }
    val availableRooms = rooms.count { it.status == RoomStatus.TERSEDIA }
    val repairRooms = rooms.count { it.status == RoomStatus.PERBAIKAN }
    val potentialRevenue = rooms.sumOf { it.hargaSewaDasar }
    val currentRealized = rooms.filter { it.status == RoomStatus.TERISI }.sumOf { it.hargaSewaDasar }

    Scaffold(
        containerColor = KosBackground,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = KosBluePrimary,
                contentColor = KosWhite,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("fab_add_room")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tambah Kamar", fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Header Stats Overview
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Manajemen Kamar Kos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = KosTextPrimary
                    )
                    Text(
                        text = "Pantau keterisian, check-in penghuni baru, dan kelola tarif serta fasilitas kamar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = KosTextSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // Stats Banner Card
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
                                MiniMetric(label = "Total Kamar", value = "$totalRooms", color = KosTextPrimary)
                                MiniMetric(label = "Terisi", value = "$occupiedRooms", color = KosBluePrimary)
                                MiniMetric(label = "Kosong", value = "$availableRooms", color = KosSuccess)
                                MiniMetric(label = "Perbaikan", value = "$repairRooms", color = KosWarning)
                            }

                            Divider(color = KosOutline, modifier = Modifier.padding(vertical = 12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Pendapatan Sewa Riil", fontSize = 11.sp, color = KosTextSecondary)
                                    Text(
                                        text = KosRepository.formatRupiah(currentRealized),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KosBluePrimary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Potensi Maksimal (100%)", fontSize = 11.sp, color = KosTextSecondary)
                                    Text(
                                        text = KosRepository.formatRupiah(potentialRevenue),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = KosTextMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_rooms"),
                        placeholder = { Text("Cari nomor kamar, tipe, atau nama...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = KosTextMuted)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = KosTextMuted)
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
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter & Sort Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedFilter == null,
                                    onClick = { selectedFilter = null },
                                    label = { Text("Semua ($totalRooms)", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KosBluePrimary,
                                        selectedLabelColor = KosWhite
                                    )
                                )
                            }
                            items(RoomStatus.values()) { status ->
                                val count = rooms.count { it.status == status }
                                FilterChip(
                                    selected = selectedFilter == status,
                                    onClick = { selectedFilter = if (selectedFilter == status) null else status },
                                    label = { Text("${status.label} ($count)", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KosBluePrimary,
                                        selectedLabelColor = KosWhite
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Room Items
            if (processedRooms.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = KosTextMuted,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tidak ada kamar yang sesuai",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = KosTextPrimary
                            )
                            Text(
                                text = "Coba ubah kata kunci pencarian atau filter status.",
                                fontSize = 12.sp,
                                color = KosTextSecondary
                            )
                        }
                    }
                }
            } else {
                items(processedRooms, key = { it.id }) { room ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                        RoomEnhancedCard(
                            room = room,
                            onClick = { selectedRoomForDetail = room },
                            onCheckInClick = { roomToCheckIn = room },
                            onCheckOutClick = { roomToCheckOutConfirm = room },
                            onContactClick = {
                                if (!room.penghuniHp.isNullOrBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${room.penghuniHp}"))
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Kontak: ${room.penghuniHp}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal: Detail Kamar Komprehensif
    if (selectedRoomForDetail != null) {
        RoomDetailBottomSheet(
            room = selectedRoomForDetail!!,
            onDismiss = { selectedRoomForDetail = null },
            onCheckIn = {
                val r = selectedRoomForDetail!!
                selectedRoomForDetail = null
                roomToCheckIn = r
            },
            onCheckOut = {
                val r = selectedRoomForDetail!!
                selectedRoomForDetail = null
                roomToCheckOutConfirm = r
            },
            onEdit = {
                val r = selectedRoomForDetail!!
                selectedRoomForDetail = null
                roomToEdit = r
            },
            onToggleRepair = {
                val r = selectedRoomForDetail!!
                val newStatus = if (r.status == RoomStatus.PERBAIKAN) RoomStatus.TERSEDIA else RoomStatus.PERBAIKAN
                viewModel.updateRoomStatus(r.id, newStatus)
                selectedRoomForDetail = null
            },
            onDelete = {
                val r = selectedRoomForDetail!!
                selectedRoomForDetail = null
                roomToDeleteConfirm = r
            }
        )
    }

    // Modal: Check-In Penghuni Baru
    if (roomToCheckIn != null) {
        CheckInTenantDialog(
            room = roomToCheckIn!!,
            onDismiss = { roomToCheckIn = null },
            onCheckIn = { nama, noHp, deposit ->
                viewModel.checkInTenant(roomToCheckIn!!.id, nama, noHp, deposit)
                roomToCheckIn = null
                Toast.makeText(context, "Penghuni $nama berhasil check-in ke Kamar ${roomToCheckIn!!.nomorKamar}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal: Edit Info Kamar
    if (roomToEdit != null) {
        EditRoomDetailsDialog(
            room = roomToEdit!!,
            onDismiss = { roomToEdit = null },
            onSave = { no, tipe, harga, fas ->
                viewModel.updateRoom(roomToEdit!!.id, no, tipe, harga, fas)
                roomToEdit = null
            }
        )
    }

    // Modal: Tambah Kamar Baru
    if (showAddDialog) {
        AddRoomDialog(
            onDismiss = { showAddDialog = false },
            onAddRoom = { no, tipe, harga, fas ->
                viewModel.addRoom(no, tipe, harga, fas)
                showAddDialog = false
            }
        )
    }

    // Confirm Checkout Dialog
    if (roomToCheckOutConfirm != null) {
        val r = roomToCheckOutConfirm!!
        AlertDialog(
            onDismissRequest = { roomToCheckOutConfirm = null },
            icon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = KosWarning) },
            title = { Text("Check-out Penghuni", fontWeight = FontWeight.Bold) },
            text = {
                Text("Konfirmasi untuk mengosongkan Kamar ${r.nomorKamar} (${r.penghuniNama ?: "Penghuni"}). Status kamar akan kembali Tersedia.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.checkOutTenant(r.id)
                        roomToCheckOutConfirm = null
                        Toast.makeText(context, "Kamar ${r.nomorKamar} berhasil dikosongkan.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KosWarning)
                ) {
                    Text("Check-out & Kosongkan", color = KosWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { roomToCheckOutConfirm = null }) {
                    Text("Batal", color = KosTextSecondary)
                }
            }
        )
    }

    // Confirm Delete Room Dialog
    if (roomToDeleteConfirm != null) {
        val r = roomToDeleteConfirm!!
        AlertDialog(
            onDismissRequest = { roomToDeleteConfirm = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = KosError) },
            title = { Text("Hapus Kamar ${r.nomorKamar}", fontWeight = FontWeight.Bold) },
            text = {
                Text("Apakah Anda yakin ingin menghapus kamar ini dari sistem KosConnect? Tindakan ini tidak dapat dibatalkan.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRoom(r.id)
                        roomToDeleteConfirm = null
                        Toast.makeText(context, "Kamar ${r.nomorKamar} telah dihapus.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KosError)
                ) {
                    Text("Hapus Kamar", color = KosWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { roomToDeleteConfirm = null }) {
                    Text("Batal", color = KosTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun MiniMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = KosTextSecondary)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}

@Composable
private fun RoomEnhancedCard(
    room: Room,
    onClick: () -> Unit,
    onCheckInClick: () -> Unit,
    onCheckOutClick: () -> Unit,
    onContactClick: () -> Unit
) {
    val (statusBg, statusFg) = when (room.status) {
        RoomStatus.TERSEDIA -> Pair(KosSuccessContainer, KosSuccess)
        RoomStatus.TERISI -> Pair(KosBlueContainer, KosBluePrimary)
        RoomStatus.PERBAIKAN -> Pair(KosWarningContainer, KosWarning)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("room_card_${room.nomorKamar}"),
        colors = CardDefaults.cardColors(containerColor = KosWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Room Number Box
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(KosBlueContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("KAMAR", fontSize = 9.sp, color = KosBlueDark, fontWeight = FontWeight.Bold)
                        Text(
                            text = room.nomorKamar,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = KosBluePrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = room.tipeKamar,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = KosTextPrimary
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = room.status.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusFg
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${KosRepository.formatRupiah(room.hargaSewaDasar)} / bulan",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = KosBluePrimary
                    )
                }
            }

            // Facilities tags
            if (room.fasilitas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(room.fasilitas) { fas ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(KosSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(fas, fontSize = 10.sp, color = KosTextSecondary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Divider(color = KosOutline, modifier = Modifier.padding(vertical = 12.dp))

            // Tenant info or Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (room.status == RoomStatus.TERISI) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = KosBluePrimary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = room.penghuniNama ?: "Penghuni",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = KosTextPrimary
                            )
                        }
                        Text(
                            text = room.penghuniHp ?: "-",
                            fontSize = 11.sp,
                            color = KosTextSecondary,
                            modifier = Modifier.padding(start = 19.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onContactClick,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(KosBlueContainer)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Hubungi", tint = KosBluePrimary, modifier = Modifier.size(16.dp))
                        }

                        OutlinedButton(
                            onClick = onCheckOutClick,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Check-out", fontSize = 11.sp, color = KosTextSecondary)
                        }
                    }
                } else if (room.status == RoomStatus.TERSEDIA) {
                    Text(
                        text = "Siap untuk dihuni",
                        fontSize = 12.sp,
                        color = KosSuccess,
                        fontWeight = FontWeight.Medium
                    )

                    Button(
                        onClick = onCheckInClick,
                        colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = KosWhite, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Check-in Penghuni", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KosWhite)
                    }
                } else {
                    Text(
                        text = "Sedang tahap pemeliharaan",
                        fontSize = 12.sp,
                        color = KosWarning,
                        fontWeight = FontWeight.Medium
                    )

                    OutlinedButton(
                        onClick = onClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Detail Status", fontSize = 11.sp, color = KosBluePrimary)
                    }
                }
            }
        }
    }
}

// Modal Detail Kamar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailBottomSheet(
    room: Room,
    onDismiss: () -> Unit,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit,
    onEdit: () -> Unit,
    onToggleRepair: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = KosWhite,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Detail Kamar ${room.nomorKamar}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = KosTextPrimary
                    )
                    Text(
                        text = room.tipeKamar,
                        style = MaterialTheme.typography.bodyMedium,
                        color = KosBluePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Kamar", tint = KosBluePrimary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pricing & Status Row
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = KosBlueContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Harga Sewa Bulanan", fontSize = 11.sp, color = KosTextSecondary)
                        Text(
                            KosRepository.formatRupiah(room.hargaSewaDasar),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = KosBlueDark
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (room.status) {
                                    RoomStatus.TERSEDIA -> KosSuccessContainer
                                    RoomStatus.TERISI -> KosBlueContainer
                                    RoomStatus.PERBAIKAN -> KosWarningContainer
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = room.status.label,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = when (room.status) {
                                RoomStatus.TERSEDIA -> KosSuccess
                                RoomStatus.TERISI -> KosBluePrimary
                                RoomStatus.PERBAIKAN -> KosWarning
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Facilities
            Text("Fasilitas Tersedia:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KosTextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                room.fasilitas.forEach { fas ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(KosSurfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(fas, fontSize = 11.sp, color = KosTextPrimary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Tenant Section
            if (room.status == RoomStatus.TERISI) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = KosWhite),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Data Penghuni Aktif", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KosTextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Nama: ${room.penghuniNama ?: "-"}", fontSize = 13.sp, color = KosTextPrimary)
                        Text("Kontak WhatsApp: ${room.penghuniHp ?: "-"}", fontSize = 13.sp, color = KosTextSecondary, modifier = Modifier.padding(top = 2.dp))
                        Text("Tanggal Masuk: 1 Oktober 2026", fontSize = 12.sp, color = KosTextMuted, modifier = Modifier.padding(top = 2.dp))
                        Text("Deposit Sewa: Rp 500.000 (Tersimpan)", fontSize = 12.sp, color = KosTextMuted, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            if (room.status == RoomStatus.TERSEDIA) {
                Button(
                    onClick = onCheckIn,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = KosWhite)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Check-in Penghuni Baru", fontWeight = FontWeight.Bold, color = KosWhite)
                }
                Spacer(modifier = Modifier.height(10.dp))
            } else if (room.status == RoomStatus.TERISI) {
                OutlinedButton(
                    onClick = onCheckOut,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = KosWarning)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Check-out / Kosongkan Kamar", color = KosWarning, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            OutlinedButton(
                onClick = onToggleRepair,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Build, contentDescription = null, tint = KosBluePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (room.status == RoomStatus.PERBAIKAN) "Tandai Selesai Perbaikan" else "Tandai Dalam Perbaikan",
                    color = KosBluePrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = KosError, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Hapus Kamar Ini", color = KosError, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Dialog: Check-in Penghuni Baru
@Composable
fun CheckInTenantDialog(
    room: Room,
    onDismiss: () -> Unit,
    onCheckIn: (String, String, Long) -> Unit
) {
    var nama by remember { mutableStateOf("") }
    var noHp by remember { mutableStateOf("") }
    var deposit by remember { mutableStateOf("500000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Check-in Kamar ${room.nomorKamar}", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Tipe: ${room.tipeKamar} • Tarif: ${KosRepository.formatRupiah(room.hargaSewaDasar)}/bln",
                    fontSize = 12.sp,
                    color = KosTextSecondary
                )

                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Lengkap Penghuni") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_checkin_name")
                )

                OutlinedTextField(
                    value = noHp,
                    onValueChange = { noHp = it },
                    label = { Text("Nomor WhatsApp Penghuni") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_checkin_phone")
                )

                OutlinedTextField(
                    value = deposit,
                    onValueChange = { deposit = it.filter { c -> c.isDigit() } },
                    label = { Text("Uang Deposit Awal (Rp)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_checkin_deposit")
                )

                Text(
                    text = "*Sistem akan langsung membuat kontrak aktif dan tagihan sewa pertama secara otomatis.",
                    fontSize = 11.sp,
                    color = KosTextMuted
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nama.isNotBlank() && noHp.isNotBlank()) {
                        onCheckIn(nama, noHp, deposit.toLongOrNull() ?: 500000L)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary),
                modifier = Modifier.testTag("submit_checkin_btn")
            ) {
                Text("Konfirmasi Check-in", color = KosWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = KosTextSecondary)
            }
        }
    )
}

// Dialog: Edit Detail Kamar
@Composable
fun EditRoomDetailsDialog(
    room: Room,
    onDismiss: () -> Unit,
    onSave: (String, String, Long, List<String>) -> Unit
) {
    var nomor by remember { mutableStateOf(room.nomorKamar) }
    var tipe by remember { mutableStateOf(room.tipeKamar) }
    var harga by remember { mutableStateOf(room.hargaSewaDasar.toString()) }

    var fasAc by remember { mutableStateOf(room.fasilitas.contains("AC")) }
    var fasKm by remember { mutableStateOf(room.fasilitas.contains("Kamar Mandi Dalam")) }
    var fasWifi by remember { mutableStateOf(room.fasilitas.contains("WiFi")) }
    var fasBalkon by remember { mutableStateOf(room.fasilitas.contains("Balkon")) }
    var fasWaterHeater by remember { mutableStateOf(room.fasilitas.contains("Water Heater")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Kamar ${room.nomorKamar}", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nomor,
                    onValueChange = { nomor = it },
                    label = { Text("Nomor Kamar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tipe,
                    onValueChange = { tipe = it },
                    label = { Text("Tipe Kamar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = harga,
                    onValueChange = { harga = it.filter { c -> c.isDigit() } },
                    label = { Text("Tarif Sewa (Rp)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Fasilitas Kamar:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = fasAc, onCheckedChange = { fasAc = it })
                    Text("AC", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Checkbox(checked = fasKm, onCheckedChange = { fasKm = it })
                    Text("KM Dalam", fontSize = 12.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = fasWifi, onCheckedChange = { fasWifi = it })
                    Text("WiFi", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Checkbox(checked = fasBalkon, onCheckedChange = { fasBalkon = it })
                    Text("Balkon", fontSize = 12.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = fasWaterHeater, onCheckedChange = { fasWaterHeater = it })
                    Text("Water Heater", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nomor.isNotBlank()) {
                        val fasList = mutableListOf<String>()
                        if (fasAc) fasList.add("AC")
                        if (fasKm) fasList.add("Kamar Mandi Dalam")
                        if (fasWifi) fasList.add("WiFi")
                        if (fasBalkon) fasList.add("Balkon")
                        if (fasWaterHeater) fasList.add("Water Heater")
                        onSave(nomor, tipe, harga.toLongOrNull() ?: room.hargaSewaDasar, fasList)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary)
            ) {
                Text("Simpan Perubahan", color = KosWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = KosTextSecondary)
            }
        }
    )
}
