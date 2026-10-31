package com.example.ui.screens.documents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.attachments.StudyAttachmentEntity
import com.example.ui.components.AmbientGlowBackground
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLevel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentManagerScreen(
    attachments: List<StudyAttachmentEntity>,
    onBack: () -> Unit,
    onRemoveFromContext: (String) -> Unit,
    onDeletePermanently: (String) -> Unit,
    onRetry: (String) -> Unit,
    onUploadNew: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, PDF, IMAGE, WORD, PPTX, TEXT
    var viewingSummaryAttachment by remember { mutableStateOf<StudyAttachmentEntity?>(null) }

    val filteredAttachments = remember(attachments, searchQuery, selectedFilter) {
        attachments.filter { att ->
            val matchesSearch = searchQuery.isBlank() || att.originalFileName.contains(searchQuery, ignoreCase = true)
            val mime = att.mimeType.lowercase()
            val matchesFilter = when (selectedFilter) {
                "PDF" -> mime.contains("pdf")
                "IMAGE" -> mime.contains("image")
                "WORD" -> mime.contains("word") || mime.contains("docx")
                "PPTX" -> mime.contains("presentation") || mime.contains("pptx")
                "TEXT" -> mime.contains("text")
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    AmbientGlowBackground(modifier = modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "UNIVERSAL STUDY ATTACHMENTS",
                                color = SageGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Document Manager",
                                color = SageTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .testTag("doc_manager_back_button")
                                .padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SageTextPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search attached documents by filename...", color = SageTextMuted, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SagePrimaryLight) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search", tint = SageTextMuted)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("doc_manager_search_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SageGlassL2,
                        unfocusedContainerColor = SageGlassL1,
                        focusedBorderColor = SagePrimaryLight,
                        unfocusedBorderColor = SageGlassBorder,
                        focusedTextColor = SageTextPrimary,
                        unfocusedTextColor = SageTextPrimary
                    )
                )

                // Filter row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filters = listOf(
                        "ALL" to "All Files",
                        "PDF" to "PDFs",
                        "IMAGE" to "Images",
                        "WORD" to "DOCX",
                        "PPTX" to "PPTX",
                        "TEXT" to "Text"
                    )
                    items(filters) { (key, label) ->
                        val isSelected = selectedFilter == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) SagePrimary.copy(alpha = 0.3f) else SageGlassL1)
                                .border(1.dp, if (isSelected) SagePrimaryLight else SageGlassBorder, RoundedCornerShape(20.dp))
                                .clickable { selectedFilter = key }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("doc_filter_$key")
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) SagePrimaryLight else SageTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                // Stats Banner
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    level = GlassLevel.L1,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${filteredAttachments.size} Document(s) Managed",
                                color = SageTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Reference-aware isolation across conversations & roadmap topics.",
                                color = SageTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                if (filteredAttachments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = SageTextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = if (searchQuery.isNotBlank()) "No documents match '$searchQuery'" else "No study attachments found",
                                color = SageTextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(filteredAttachments, key = { it.attachmentId }) { att ->
                            DocumentItemCard(
                                attachment = att,
                                onViewSummary = { viewingSummaryAttachment = att },
                                onRemoveFromContext = { onRemoveFromContext(att.attachmentId) },
                                onDeletePermanently = { onDeletePermanently(att.attachmentId) },
                                onRetry = { onRetry(att.attachmentId) }
                            )
                        }
                    }
                }
            }
        }

        // Summary Dialog
        viewingSummaryAttachment?.let { att ->
            AlertDialog(
                onDismissRequest = { viewingSummaryAttachment = null },
                containerColor = Color(0xFF0F172A),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = SagePrimaryLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = att.originalFileName,
                            color = SageTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Format: ${att.mimeType} | Size: ${formatSize(att.fileSize)}",
                            color = SageGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Context: ${att.contextType}:${att.contextId}",
                            color = SageTextMuted,
                            fontSize = 11.sp
                        )
                        HorizontalDivider(color = SageGlassBorder)
                        Text(
                            text = if (!att.extractedSummary.isNullOrBlank()) att.extractedSummary else "Document extracted and verified for Gemini AI grounding context.",
                            color = SageTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewingSummaryAttachment = null }) {
                        Text("Close", color = SagePrimaryLight, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
private fun DocumentItemCard(
    attachment: StudyAttachmentEntity,
    onViewSummary: () -> Unit,
    onRemoveFromContext: () -> Unit,
    onDeletePermanently: () -> Unit,
    onRetry: () -> Unit
) {
    val (badgeText, badgeColor) = when {
        attachment.mimeType.contains("pdf") -> "PDF" to Color(0xFFE53935)
        attachment.mimeType.contains("image") -> "IMG" to Color(0xFF1E88E5)
        attachment.mimeType.contains("word") || attachment.mimeType.contains("docx") -> "DOCX" to Color(0xFF1565C0)
        attachment.mimeType.contains("presentation") || attachment.mimeType.contains("pptx") -> "PPTX" to Color(0xFFE65100)
        else -> "TXT" to Color(0xFF43A047)
    }

    val isProcessed = attachment.uploadStatus == "ATTACHED" || attachment.processingStatus == "COMPLETED"
    val isFailed = attachment.uploadStatus.contains("FAILED") || attachment.processingStatus.contains("FAILED")

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("doc_item_${attachment.attachmentId}"),
        level = GlassLevel.L1,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeColor.copy(alpha = 0.2f))
                            .border(1.dp, badgeColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = attachment.originalFileName,
                            color = SageTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${formatSize(attachment.fileSize)} • ${attachment.contextType}:${attachment.contextId}",
                            color = SageTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                // Status tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isProcessed) SageSuccess.copy(alpha = 0.15f) else if (isFailed) SageError.copy(alpha = 0.15f) else SageGold.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isProcessed) "READY" else if (isFailed) "FAILED" else "UPLOADING",
                        color = if (isProcessed) SageSuccess else if (isFailed) SageError else SageGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isProcessed) {
                    TextButton(onClick = onViewSummary) {
                        Text("Summary", color = SagePrimaryLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (isFailed) {
                    TextButton(onClick = onRetry) {
                        Text("Retry", color = SageGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                TextButton(onClick = onRemoveFromContext) {
                    Text("Detach", color = SageTextSecondary, fontSize = 12.sp)
                }

                TextButton(onClick = onDeletePermanently) {
                    Text("Delete", color = SageError, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}
