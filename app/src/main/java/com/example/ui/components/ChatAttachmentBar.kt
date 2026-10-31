package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.attachments.AttachmentProcessingStatus
import com.example.data.attachments.AttachmentUploadStatus
import com.example.data.attachments.StudyAttachmentEntity
import com.example.ui.theme.SageBackground
import com.example.ui.theme.SageGlassBorder
import com.example.ui.theme.SageGlassBorderGlow
import com.example.ui.theme.SageGlassL1
import com.example.ui.theme.SageGlassL2
import com.example.ui.theme.SageGlassL3
import com.example.ui.theme.SageGold
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryLight
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary

fun getAttachmentIcon(mimeType: String): ImageVector {
    val lower = mimeType.lowercase()
    return when {
        lower.contains("pdf") -> Icons.Default.PictureAsPdf
        lower.contains("image") -> Icons.Default.Image
        lower.contains("presentation") || lower.contains("powerpoint") -> Icons.Default.Slideshow
        lower.contains("word") || lower.contains("officedocument.wordprocessing") -> Icons.Default.Description
        else -> Icons.Default.Article
    }
}

fun getAttachmentFormatColor(mimeType: String): Color {
    val lower = mimeType.lowercase()
    return when {
        lower.contains("pdf") -> Color(0xFFEF5350) // Red
        lower.contains("image") -> Color(0xFF26A69A) // Teal
        lower.contains("presentation") || lower.contains("powerpoint") -> Color(0xFFFFB74D) // Amber
        lower.contains("word") || lower.contains("officedocument.wordprocessing") -> Color(0xFF42A5F5) // Blue
        else -> Color(0xFFB0BEC5) // Blue-gray
    }
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1.0 -> String.format("%.1f MB", mb)
        kb >= 1.0 -> String.format("%.0f KB", kb)
        else -> "$bytes B"
    }
}

@Composable
fun ChatAttachmentChip(
    attachment: StudyAttachmentEntity,
    onRemove: () -> Unit,
    onRetry: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUploadingOrProcessing = attachment.uploadStatus == AttachmentUploadStatus.UPLOADING.name ||
            attachment.uploadStatus == AttachmentUploadStatus.PROCESSING.name ||
            attachment.uploadStatus == AttachmentUploadStatus.VALIDATING.name
    val isFailed = attachment.uploadStatus.contains("FAILED") || attachment.processingStatus == AttachmentProcessingStatus.FAILED.name
    val isReady = (attachment.uploadStatus == AttachmentUploadStatus.ATTACHED.name ||
            attachment.uploadStatus == AttachmentUploadStatus.PROCESSED.name) && !isFailed

    val icon = getAttachmentIcon(attachment.mimeType)
    val formatColor = getAttachmentFormatColor(attachment.mimeType)

    Row(
        modifier = modifier
            .widthIn(min = 140.dp, max = 240.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SageGlassL3)
            .border(
                1.dp,
                if (isFailed) Color(0xFFE57373).copy(alpha = 0.6f) else SageGlassBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("attachment_chip_${attachment.attachmentId}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with colored badge
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(formatColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = attachment.mimeType,
                tint = formatColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Name and status
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = attachment.originalFileName,
                color = SageTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatFileSize(attachment.fileSize),
                    color = SageTextMuted,
                    fontSize = 10.sp
                )

                if (isUploadingOrProcessing) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "• Processing",
                        color = SagePrimaryLight,
                        fontSize = 10.sp
                    )
                } else if (isFailed) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "• Failed",
                        color = Color(0xFFEF5350),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else if (isReady) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "• Ready",
                        color = Color(0xFF81C784),
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Trailing action: Spinner, Retry or Remove
        if (isUploadingOrProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = SagePrimaryLight,
                strokeWidth = 2.dp
            )
        } else if (isFailed) {
            IconButton(
                onClick = onRetry,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("retry_attachment_${attachment.attachmentId}")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry upload",
                    tint = SageGold,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .size(24.dp)
                .testTag("remove_attachment_${attachment.attachmentId}")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove attachment",
                tint = SageTextMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun ChatAttachmentPreviewRow(
    attachments: List<StudyAttachmentEntity>,
    onRemove: (String) -> Unit,
    onRetry: (String) -> Unit,
    onOpenManagement: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = attachments.isNotEmpty(),
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(SageGlassL1)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(SageGlassBorderGlow.copy(alpha = 0.2f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
                .padding(vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ATTACHED STUDY DOCUMENTS (${attachments.size})",
                    color = SageGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                Text(
                    text = "Manage All",
                    color = SagePrimaryLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { onOpenManagement() }
                        .padding(4.dp)
                        .testTag("manage_attachments_button")
                )
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(attachments, key = { it.attachmentId }) { att ->
                    ChatAttachmentChip(
                        attachment = att,
                        onRemove = { onRemove(att.attachmentId) },
                        onRetry = { onRetry(att.attachmentId) },
                        onClick = onOpenManagement
                    )
                }
            }
        }
    }
}

@Composable
fun ChatAttachmentManagementDialog(
    attachments: List<StudyAttachmentEntity>,
    onDismiss: () -> Unit,
    onRemoveFromChat: (String) -> Unit,
    onDeletePermanently: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SageBackground,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = SageGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Conversation Attachments",
                    color = SageTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            if (attachments.isEmpty()) {
                Text(
                    text = "No study documents are currently attached to this conversation.",
                    color = SageTextMuted,
                    fontSize = 13.sp
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(attachments, key = { it.attachmentId }) { att ->
                        val formatColor = getAttachmentFormatColor(att.mimeType)
                        val icon = getAttachmentIcon(att.mimeType)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SageGlassL2)
                                .border(1.dp, SageGlassBorder, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(formatColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = formatColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = att.originalFileName,
                                        color = SageTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${formatFileSize(att.fileSize)} • ${att.mimeType.substringAfter('/')}",
                                        color = SageTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (!att.extractedSummary.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "AI Study Summary: ${att.extractedSummary}",
                                    color = SageTextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SageGlassL3)
                                        .padding(6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { onRemoveFromChat(att.attachmentId) },
                                    modifier = Modifier.testTag("dialog_remove_from_chat_${att.attachmentId}")
                                ) {
                                    Text(
                                        text = "Remove from Chat",
                                        color = SageTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }

                                TextButton(
                                    onClick = { onDeletePermanently(att.attachmentId) },
                                    modifier = Modifier.testTag("dialog_delete_permanently_${att.attachmentId}")
                                ) {
                                    Text(
                                        text = "Delete File",
                                        color = Color(0xFFEF5350),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_attachment_dialog")
            ) {
                Text("Close", color = SagePrimaryLight, fontWeight = FontWeight.Bold)
            }
        }
    )
}
