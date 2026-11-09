package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSwitcherSheet(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    onDismiss: () -> Unit
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
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(KosOutlineVariant)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Pilih Peran Pengguna",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = KosTextPrimary
            )
            Text(
                text = "Beralih peran untuk menguji pengalaman dari sudut pandang Pemilik, Penghuni, atau Investor.",
                style = MaterialTheme.typography.bodyMedium,
                color = KosTextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            RoleItemCard(
                title = "Pemilik / Pengelola (Admin)",
                subtitle = "Kelola kamar, pantau okupansi, keuangan, dan komplain",
                icon = Icons.Default.AdminPanelSettings,
                isSelected = currentRole == UserRole.ADMIN,
                badgeText = "Akses Penuh",
                tag = "role_admin_option",
                onClick = {
                    onRoleSelected(UserRole.ADMIN)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            RoleItemCard(
                title = "Penghuni Kos (Tenant)",
                subtitle = "Lihat kamar, bayar tagihan (VA/QRIS), lapor komplain",
                icon = Icons.Default.Person,
                isSelected = currentRole == UserRole.PENGHUNI,
                badgeText = "Kamar 101",
                tag = "role_tenant_option",
                onClick = {
                    onRoleSelected(UserRole.PENGHUNI)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            RoleItemCard(
                title = "Investor Properti",
                subtitle = "Pantau dividen bagi hasil, arus kas, & okupansi",
                icon = Icons.Default.TrendingUp,
                isSelected = currentRole == UserRole.INVESTOR,
                badgeText = "Saham 30%",
                tag = "role_investor_option",
                onClick = {
                    onRoleSelected(UserRole.INVESTOR)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun RoleItemCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    badgeText: String,
    tag: String,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) KosBluePrimary else KosOutline
    val bgColor = if (isSelected) KosBlueContainer.copy(alpha = 0.5f) else KosWhite

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) KosBluePrimary else KosBlueContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) KosWhite else KosBluePrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = KosTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(KosBlueContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = KosBlueDark
                        )
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = KosTextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = KosBluePrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
