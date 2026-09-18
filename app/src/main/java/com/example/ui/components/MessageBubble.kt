package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MessageEntity
import com.example.ui.theme.SageAccent
import com.example.ui.theme.SageAiBubble
import com.example.ui.theme.SageCardBorder
import com.example.ui.theme.SageError
import com.example.ui.theme.SageGold
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryLight
import com.example.ui.theme.SagePrimaryStart
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary
import com.example.ui.theme.SageUserBubble
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    message: MessageEntity,
    onRetry: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val context = LocalContext.current

    if (message.status == "SENDING" && !isUser) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            SageAvatar()
            Spacer(modifier = Modifier.width(8.dp))
            TypingIndicator()
        }
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            SageAvatar()
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            if (message.status == "FAILED") {
                // Error card with retry button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(SageAiBubble)
                        .border(1.dp, SageError.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = SageError,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Couldn't reach Sage",
                                color = SageError,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = message.content.ifEmpty { "Connection problem. Please check your internet connection or Gemini configuration." },
                            color = SageTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onRetry(message.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SagePrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("retry_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Try Again", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                // Normal message content
                val shape = if (isUser) {
                    RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
                } else {
                    RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
                }

                val bubbleBorderBrush = if (isUser) {
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.25f),
                            SagePrimaryStart.copy(alpha = 0.6f),
                            Color.White.copy(alpha = 0.08f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.14f),
                            SagePrimaryLight.copy(alpha = 0.25f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(shape)
                        .background(if (isUser) SageUserBubble else SageAiBubble)
                        .border(
                            width = 1.dp,
                            brush = bubbleBorderBrush,
                            shape = shape
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column {
                        FormattedContent(text = message.content)

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))
                            Text(
                                text = timeStr,
                                color = SageTextMuted,
                                fontSize = 11.sp
                            )

                            if (!isUser) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy message",
                                    tint = SageTextMuted,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Sage Response", message.content)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            UserAvatar()
        }
    }
}

@Composable
fun SageAvatar() {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(listOf(SagePrimaryStart, SagePrimary, SageAccent))
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.45f),
                        SagePrimaryLight.copy(alpha = 0.6f)
                    )
                ),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "Sage AI",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun UserAvatar() {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(SageCardBorder)
            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "User",
            tint = SageTextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun FormattedContent(text: String) {
    val blocks = parseContentBlocks(text)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        blocks.forEach { block ->
            when (block) {
                is ContentBlock.Code -> {
                    CodeBlockView(code = block.code, language = block.language)
                }
                is ContentBlock.Text -> {
                    val annotatedString = buildAnnotatedString {
                        val raw = block.content
                        var lastIndex = 0
                        val boldPattern = Regex("\\*\\*(.*?)\\*\\*")
                        boldPattern.findAll(raw).forEach { match ->
                            append(raw.substring(lastIndex, match.range.first))
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = SageTextPrimary)) {
                                append(match.groupValues[1])
                            }
                            lastIndex = match.range.last + 1
                        }
                        if (lastIndex < raw.length) {
                            append(raw.substring(lastIndex))
                        }
                    }
                    Text(
                        text = annotatedString,
                        color = SageTextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CodeBlockView(code: String, language: String) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF07070D))
            .border(1.dp, SageCardBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            if (language.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = language.uppercase(),
                        color = SagePrimaryLight,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Code snippet", code)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = SageTextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = code,
                color = Color(0xFFE2E8F0),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

sealed class ContentBlock {
    data class Text(val content: String) : ContentBlock()
    data class Code(val code: String, val language: String) : ContentBlock()
}

fun parseContentBlocks(content: String): List<ContentBlock> {
    val list = mutableListOf<ContentBlock>()
    val codeFenceRegex = Regex("```([a-zA-Z0-9_-]*)\\n?([\\s\\S]*?)```")
    var lastIndex = 0

    codeFenceRegex.findAll(content).forEach { match ->
        val textBefore = content.substring(lastIndex, match.range.first)
        if (textBefore.trim().isNotEmpty()) {
            list.add(ContentBlock.Text(textBefore.trim()))
        }
        val lang = match.groupValues[1]
        val code = match.groupValues[2].trimEnd()
        list.add(ContentBlock.Code(code = code, language = lang))
        lastIndex = match.range.last + 1
    }

    if (lastIndex < content.length) {
        val remaining = content.substring(lastIndex)
        if (remaining.trim().isNotEmpty()) {
            list.add(ContentBlock.Text(remaining.trim()))
        }
    }

    if (list.isEmpty() && content.isNotEmpty()) {
        list.add(ContentBlock.Text(content))
    }

    return list
}
