package com.example.ui.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.ui.AdminSummaryState
import com.example.ui.theme.*
import com.example.util.AiSmartService
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: String = "Sekarang"
)

@Composable
fun AiAssistantDialog(
    summary: AdminSummaryState,
    namaKos: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var isStreaming by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                isUser = false,
                text = "Halo Juragan! Saya asisten pintar AI KosConnect. Ada yang bisa saya bantu untuk operasional, tagihan, aturan kos, atau strategi bisnis ${namaKos} hari ini?"
            )
        )
    }

    val contextInfo = remember(summary, namaKos) {
        """
            Nama Kos: $namaKos
            Total Kamar: ${summary.totalRooms}
            Terisi: ${summary.occupiedRooms} (Okupansi: ${summary.occupancyRate}%)
            Kamar Kosong: ${summary.availableRooms}
            Kamar Perbaikan: ${summary.repairRooms}
            Tagihan Tertunggak: ${summary.unpaidCount} kamar
            Komplain Belum Selesai: ${summary.pendingComplaintsCount}
        """.trimIndent()
    }

    val quickPrompts = listOf(
        "Draft pesan WA tagih sewa tegas & sopan",
        "Tips cepat isi kamar kosong",
        "Saran aturan jam malam & tamu lawan jenis",
        "Cara hemat token listrik AC kosan"
    )

    fun sendMessage(promptText: String) {
        val q = promptText.trim()
        if (q.isBlank() || isStreaming) return

        messages.add(ChatMessage(isUser = true, text = q))
        inputText = ""

        // Placeholder for AI streaming message
        val aiMessageIndex = messages.size
        messages.add(ChatMessage(isUser = false, text = ""))
        isStreaming = true

        coroutineScope.launch {
            try {
                AiSmartService.askKosAssistantStream(q, contextInfo).collect { chunk ->
                    val currentText = messages[aiMessageIndex].text
                    messages[aiMessageIndex] = messages[aiMessageIndex].copy(text = currentText + chunk)
                    listState.animateScrollToItem(messages.size - 1)
                }
            } catch (e: Exception) {
                val currentText = messages[aiMessageIndex].text
                messages[aiMessageIndex] = messages[aiMessageIndex].copy(text = currentText + "\n[Gagal memuat: ${e.localizedMessage}]")
            } finally {
                isStreaming = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(22.dp)),
            color = KosWhite
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
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
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = KosWhite,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Asisten Pintar Kos",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KosWhite
                                )
                                Text(
                                    text = "Didukung Gemini 3.8 Flash • Online",
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

                // Chat Messages List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        ChatBubbleItem(
                            message = msg,
                            onCopy = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("KosConnect AI", msg.text))
                                Toast.makeText(context, "Teks disalin", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    if (isStreaming && messages.lastOrNull()?.text.isNullOrBlank()) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = KosBluePrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Asisten sedang berpikir...", fontSize = 12.sp, color = KosTextMuted)
                            }
                        }
                    }
                }

                // Quick Prompt Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KosSurfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickPrompts) { prompt ->
                        SuggestionChip(
                            onClick = { sendMessage(prompt) },
                            label = { Text(prompt, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = KosWhite,
                                labelColor = KosBluePrimary
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = KosBluePrimary.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                // Input Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = KosWhite,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Tanyakan apa saja seputar kos...", fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_ai_chat"),
                            maxLines = 3,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KosBluePrimary,
                                unfocusedBorderColor = KosOutline
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { sendMessage(inputText) },
                            enabled = inputText.isNotBlank() && !isStreaming,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (inputText.isNotBlank() && !isStreaming) KosBluePrimary else KosOutline)
                                .testTag("btn_send_ai_chat")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Kirim",
                                tint = KosWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessage,
    onCopy: () -> Unit
) {
    val isUser = message.isUser
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bgColor = if (isUser) KosBluePrimary else KosSurfaceVariant
    val textColor = if (isUser) KosWhite else KosTextPrimary
    val shape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(if (isUser) 0.85f else 0.92f),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(KosBlueContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = KosBluePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(bgColor)
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = message.text.ifBlank { "..." },
                        fontSize = 13.sp,
                        color = textColor,
                        lineHeight = 20.sp
                    )

                    if (!isUser && message.text.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .clickable(onClick = onCopy)
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Salin",
                                tint = KosTextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salin", fontSize = 10.sp, color = KosTextMuted, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
