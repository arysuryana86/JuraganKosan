package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.admin.*
import com.example.ui.components.RoleSwitcherSheet
import com.example.ui.investor.InvestorScreen
import com.example.ui.tenant.TenantScreen
import com.example.ui.theme.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

enum class AdminTab(val label: String) {
    BERANDA("Beranda"),
    KAMAR("Kamar"),
    KEUANGAN("Keuangan"),
    KOMPLAIN("Komplain")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KosConnectApp(
    viewModel: KosViewModel = viewModel(),
    user: com.google.firebase.auth.FirebaseUser? = null
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val summary by viewModel.summaryState.collectAsState()

    var showRoleModal by remember { mutableStateOf(false) }
    var selectedAdminTab by remember { mutableStateOf(AdminTab.BERANDA) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    // Handle back button when inside secondary tab
    BackHandler(enabled = selectedAdminTab != AdminTab.BERANDA) {
        selectedAdminTab = AdminTab.BERANDA
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = KosBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(KosBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apartment,
                                contentDescription = "Logo",
                                tint = KosWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "KosConnect",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = KosBluePrimary
                        )
                    }
                },
                actions = {
                    // Role Switcher Chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = KosBlueContainer,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showRoleModal = true }
                            .testTag("role_switcher_chip")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            val roleIcon = when (currentRole) {
                                UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                                UserRole.PENGHUNI -> Icons.Default.Person
                                UserRole.INVESTOR -> Icons.Default.TrendingUp
                            }
                            Icon(
                                imageVector = roleIcon,
                                contentDescription = null,
                                tint = KosBluePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (currentRole) {
                                    UserRole.ADMIN -> "Admin"
                                    UserRole.PENGHUNI -> "Penghuni"
                                    UserRole.INVESTOR -> "Investor"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = KosBlueDark
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Pilih Role",
                                tint = KosBlueDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Notification Bell
                    IconButton(
                        onClick = { showNotificationDialog = true },
                        modifier = Modifier.testTag("btn_notification")
                    ) {
                        BadgedBox(
                            badge = {
                                if (summary.pendingComplaintsCount > 0 || summary.unpaidCount > 0) {
                                    Badge(containerColor = KosError) {
                                        Text("${summary.pendingComplaintsCount + summary.unpaidCount}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifikasi",
                                tint = KosTextPrimary
                            )
                        }
                    }

                    // Sign Out Button
                    IconButton(
                        onClick = { showSignOutConfirm = true },
                        modifier = Modifier.testTag("btn_logout")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Keluar Akun",
                            tint = KosTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KosWhite,
                    titleContentColor = KosBluePrimary
                )
            )
        },
        bottomBar = {
            if (currentRole == UserRole.ADMIN) {
                NavigationBar(
                    containerColor = KosWhite,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    NavigationBarItem(
                        selected = selectedAdminTab == AdminTab.BERANDA,
                        onClick = { selectedAdminTab = AdminTab.BERANDA },
                        icon = {
                            Icon(
                                imageVector = if (selectedAdminTab == AdminTab.BERANDA) Icons.Default.Home else Icons.Default.HomeWork,
                                contentDescription = "Beranda"
                            )
                        },
                        label = { Text("Beranda", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KosBluePrimary,
                            selectedTextColor = KosBluePrimary,
                            indicatorColor = KosBlueContainer
                        )
                    )
                    NavigationBarItem(
                        selected = selectedAdminTab == AdminTab.KAMAR,
                        onClick = { selectedAdminTab = AdminTab.KAMAR },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.MeetingRoom,
                                contentDescription = "Kamar"
                            )
                        },
                        label = { Text("Kamar", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KosBluePrimary,
                            selectedTextColor = KosBluePrimary,
                            indicatorColor = KosBlueContainer
                        )
                    )
                    NavigationBarItem(
                        selected = selectedAdminTab == AdminTab.KEUANGAN,
                        onClick = { selectedAdminTab = AdminTab.KEUANGAN },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Keuangan"
                            )
                        },
                        label = { Text("Keuangan", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KosBluePrimary,
                            selectedTextColor = KosBluePrimary,
                            indicatorColor = KosBlueContainer
                        )
                    )
                    NavigationBarItem(
                        selected = selectedAdminTab == AdminTab.KOMPLAIN,
                        onClick = { selectedAdminTab = AdminTab.KOMPLAIN },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (summary.pendingComplaintsCount > 0) {
                                        Badge(containerColor = KosError) {
                                            Text("${summary.pendingComplaintsCount}")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReportProblem,
                                    contentDescription = "Komplain"
                                )
                            }
                        },
                        label = { Text("Komplain", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KosBluePrimary,
                            selectedTextColor = KosBluePrimary,
                            indicatorColor = KosBlueContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentRole, label = "role_crossfade") { role ->
                when (role) {
                    UserRole.ADMIN -> {
                        when (selectedAdminTab) {
                            AdminTab.BERANDA -> AdminDashboardScreen(
                                viewModel = viewModel,
                                onNavigateToRooms = { selectedAdminTab = AdminTab.KAMAR },
                                onNavigateToFinance = { selectedAdminTab = AdminTab.KEUANGAN },
                                onNavigateToComplaints = { selectedAdminTab = AdminTab.KOMPLAIN }
                            )
                            AdminTab.KAMAR -> RoomManagementView(viewModel = viewModel)
                            AdminTab.KEUANGAN -> FinanceManagementView(viewModel = viewModel)
                            AdminTab.KOMPLAIN -> ComplaintsManagementView(viewModel = viewModel)
                        }
                    }
                    UserRole.PENGHUNI -> TenantScreen(viewModel = viewModel)
                    UserRole.INVESTOR -> InvestorScreen(viewModel = viewModel)
                }
            }
        }
    }

    if (showRoleModal) {
        RoleSwitcherSheet(
            currentRole = currentRole,
            onRoleSelected = { newRole ->
                viewModel.setRole(newRole)
            },
            onDismiss = { showRoleModal = false }
        )
    }

    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            icon = { Icon(Icons.Default.Notifications, contentDescription = null, tint = KosBluePrimary) },
            title = { Text("Pusat Notifikasi", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (summary.unpaidCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = KosWarningContainer.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showNotificationDialog = false
                                    viewModel.setRole(UserRole.ADMIN)
                                    selectedAdminTab = AdminTab.KEUANGAN
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, tint = KosWarning, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${summary.unpaidCount} Tagihan Belum Dibayar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KosTextPrimary)
                                    Text("Ketuk untuk meninjau dan mengirim pengingat.", fontSize = 10.sp, color = KosTextSecondary)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = KosTextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    if (summary.pendingComplaintsCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = KosErrorContainer.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showNotificationDialog = false
                                    viewModel.setRole(UserRole.ADMIN)
                                    selectedAdminTab = AdminTab.KOMPLAIN
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null, tint = KosError, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${summary.pendingComplaintsCount} Komplain Perlu Penanganan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KosTextPrimary)
                                    Text("Ketuk untuk merespon laporan kerusakan.", fontSize = 10.sp, color = KosTextSecondary)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = KosTextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = KosBlueContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = KosBluePrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Bagi Hasil Investor Aktif", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KosTextPrimary)
                                Text("Laba bersih kosan dikalkulasi otomatis setiap ada transaksi.", fontSize = 10.sp, color = KosTextSecondary)
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = KosBlueContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = KosBluePrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Pengingat WorkManager Aktif", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KosTextPrimary)
                                Text("Sinkronisasi otomatis background setiap 12 jam (H-3 & H-1 sewa).", fontSize = 10.sp, color = KosTextSecondary)
                            }
                        }
                    }

                    val ctx = androidx.compose.ui.platform.LocalContext.current
                    OutlinedButton(
                        onClick = {
                            viewModel.triggerBroadcastReminders(ctx)
                            android.widget.Toast.makeText(ctx, "WorkManager dijalankan: Notifikasi pengingat dikirim!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = KosBluePrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tes Kirim Notifikasi Pengingat ke HP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KosBluePrimary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showNotificationDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = KosBluePrimary)
                ) {
                    Text("Tutup", color = KosWhite)
                }
            }
        )
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            icon = { Icon(Icons.Default.Logout, contentDescription = null, tint = KosError) },
            title = { Text("Konfirmasi Keluar", fontWeight = FontWeight.Bold) },
            text = {
                val email = user?.email ?: "akun Google Anda"
                Text("Apakah Anda yakin ingin keluar dari $email?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirm = false
                        com.google.firebase.Firebase.auth.signOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KosError)
                ) {
                    Text("Keluar", color = KosWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text("Batal", color = KosTextSecondary)
                }
            }
        )
    }
}
