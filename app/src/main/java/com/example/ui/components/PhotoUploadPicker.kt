package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*
import com.example.util.MediaHelper

@Composable
fun PhotoUploadPicker(
    label: String,
    imagePath: String?,
    onImageSelected: (String?) -> Unit,
    sampleTitle: String = "Bukti Dokumen",
    sampleType: String = "RECEIPT",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = MediaHelper.savePickedImageToInternalStorage(
                context = context,
                sourceUri = uri,
                prefix = sampleType.lowercase()
            )
            if (savedPath != null) {
                onImageSelected(savedPath)
                Toast.makeText(context, "Foto berhasil diunggah", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal memproses foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = KosTextPrimary,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        if (imagePath.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(KosSurfaceVariant)
                    .border(1.dp, KosOutline, RoundedCornerShape(12.dp))
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .testTag("btn_pick_photo"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(KosBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = KosBluePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ketuk untuk Memilih Foto (Galeri)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = KosBluePrimary
                    )

                    Text(
                        text = "PNG, JPG hingga 10MB",
                        fontSize = 10.sp,
                        color = KosTextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Emulator fast testing fallback
            TextButton(
                onClick = {
                    val sample = MediaHelper.createSampleReceiptOrDamageImage(
                        context = context,
                        title = sampleTitle,
                        subtitle = "Kamar 101 • Ahmad Pratama",
                        type = sampleType
                    )
                    if (sample != null) {
                        onImageSelected(sample)
                        Toast.makeText(context, "Sampel foto otomatis dibuat", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.align(Alignment.End),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoFixHigh,
                    contentDescription = null,
                    tint = KosBluePrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Gunakan Sampel Foto Instan", fontSize = 11.sp, color = KosBluePrimary, fontWeight = FontWeight.SemiBold)
            }
        } else {
            // Preview card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = KosWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.05f))
                    ) {
                        AsyncImage(
                            model = imagePath,
                            contentDescription = label,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = KosSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Foto Siap Disimpan", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = KosSuccess)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Ganti", fontSize = 11.sp, color = KosBluePrimary, fontWeight = FontWeight.Bold)
                            }

                            TextButton(
                                onClick = { onImageSelected(null) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Hapus", fontSize = 11.sp, color = KosError, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
