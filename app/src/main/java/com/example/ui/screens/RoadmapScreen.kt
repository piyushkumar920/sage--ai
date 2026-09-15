package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TopicProgressEntity
import com.example.data.roadmap.DevRoadmapDetail
import com.example.data.roadmap.DevRoadmapNode
import com.example.data.roadmap.TopicStatus
import com.example.ui.theme.SageBackground
import com.example.ui.theme.SageCardBorder
import com.example.ui.theme.SageError
import com.example.ui.theme.SageGold
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryLight
import com.example.ui.theme.SagePrimaryStart
import com.example.ui.theme.SageRaisedSurface
import com.example.ui.theme.SageSuccess
import com.example.ui.theme.SageSurface
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary
import com.example.ui.theme.SageWarning

@Composable
fun RoadmapScreen(
    roadmapDetail: DevRoadmapDetail,
    progressList: List<TopicProgressEntity>,
    onBack: () -> Unit,
    onSelectNode: (DevRoadmapNode) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }

    // Map of nodeId -> status
    val statusMap = remember(progressList) {
        progressList.associate { it.nodeId to it.status }
    }

    val totalNodes = roadmapDetail.nodes.size
    val completedCount = progressList.count { it.status == "COMPLETED" }
    val inProgressCount = progressList.count { it.status == "IN_PROGRESS" }
    val needsReviewCount = progressList.count { it.status == "NEEDS_REVIEW" }
    val remainingCount = (totalNodes - completedCount).coerceAtLeast(0)
    val percentComplete = if (totalNodes > 0) (completedCount * 100) / totalNodes else 0

    // Current topic
    val currentTopicNode = roadmapDetail.nodes.find { statusMap[it.id] == "IN_PROGRESS" }
        ?: roadmapDetail.nodes.find { statusMap[it.id] == "NEEDS_REVIEW" }
        ?: roadmapDetail.nodes.find { statusMap[it.id] != "COMPLETED" }
        ?: roadmapDetail.nodes.firstOrNull()

    // Next topic
    val nextTopicNode = roadmapDetail.nodes.find { node ->
        node.id != currentTopicNode?.id && (statusMap[node.id] == null || statusMap[node.id] == "NOT_STARTED")
    }

    val categories = remember(roadmapDetail) {
        listOf("All") + roadmapDetail.categories.map { it.replaceFirstChar { char -> char.uppercase() } }
    }

    val filteredNodes = remember(selectedCategory, roadmapDetail) {
        if (selectedCategory.equals("All", ignoreCase = true)) {
            roadmapDetail.nodes
        } else {
            roadmapDetail.nodes.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SageBackground)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Navigation Bar
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SageSurface)
                        .border(1.dp, SageCardBorder, CircleShape)
                        .testTag("roadmap_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SageTextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LEARNING ROADMAP",
                        color = SagePrimaryLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${roadmapDetail.icon} ${roadmapDetail.title}",
                        color = SageTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Source Attribution Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/rudra496/devroadmaps"))
                            context.startActivity(intent)
                        } catch (e: Exception) {}
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SageRaisedSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = SagePrimaryLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Curated from devroadmaps (github.com/rudra496/devroadmaps)",
                            color = SageTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "View Repo",
                        tint = SageTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Progress Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("roadmap_summary_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SageSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overall Progress",
                            color = SageTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$percentComplete% Complete",
                            color = SagePrimaryLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LinearProgressIndicator(
                        progress = { percentComplete / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SagePrimary,
                        trackColor = SageRaisedSurface
                    )

                    // 4 Metric Counters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricCountItem(label = "Completed", count = completedCount, color = SageSuccess, symbol = "✓")
                        MetricCountItem(label = "In Progress", count = inProgressCount, color = SagePrimaryLight, symbol = "→")
                        MetricCountItem(label = "Review", count = needsReviewCount, color = SageWarning, symbol = "↻")
                        MetricCountItem(label = "Remaining", count = remainingCount, color = SageTextMuted, symbol = "○")
                    }

                    // Current & Next Topic Preview
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SageRaisedSurface)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (currentTopicNode != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Current Topic: ",
                                    color = SageTextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${currentTopicNode.icon} ${currentTopicNode.title}",
                                    color = SageTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (nextTopicNode != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Recommended Next: ",
                                    color = SageTextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${nextTopicNode.icon} ${nextTopicNode.title}",
                                    color = SagePrimaryLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Learning Stages / Category Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory.equals(category, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) SagePrimary else SageSurface)
                            .border(1.dp, if (isSelected) SagePrimaryLight else SageCardBorder, RoundedCornerShape(16.dp))
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("stage_chip_$category")
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.White else SageTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Section Title: Interactive Roadmap Nodes
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ROADMAP TOPICS (${filteredNodes.size})",
                    color = SageTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Tap node for actions",
                    color = SageTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        // Nodes List
        items(filteredNodes) { node ->
            val statusStr = statusMap[node.id] ?: "NOT_STARTED"
            val status = when (statusStr) {
                "COMPLETED" -> TopicStatus.COMPLETED
                "IN_PROGRESS" -> TopicStatus.IN_PROGRESS
                "NEEDS_REVIEW" -> TopicStatus.NEEDS_REVIEW
                else -> TopicStatus.NOT_STARTED
            }

            RoadmapNodeCard(
                node = node,
                status = status,
                onClick = { onSelectNode(node) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun MetricCountItem(
    label: String,
    count: Int,
    color: Color,
    symbol: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = symbol,
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = count.toString(),
                color = SageTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = label,
            color = SageTextMuted,
            fontSize = 11.sp
        )
    }
}

data class NodeStatusStyle(
    val bg: Color,
    val border: Color,
    val color: Color,
    val label: String
)

@Composable
fun RoadmapNodeCard(
    node: DevRoadmapNode,
    status: TopicStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val style = when (status) {
        TopicStatus.COMPLETED -> NodeStatusStyle(
            bg = SageSuccess.copy(alpha = 0.15f),
            border = SageSuccess,
            color = SageSuccess,
            label = "✓ COMPLETED"
        )
        TopicStatus.IN_PROGRESS -> NodeStatusStyle(
            bg = SagePrimary.copy(alpha = 0.2f),
            border = SagePrimaryLight,
            color = SagePrimaryLight,
            label = "→ IN PROGRESS"
        )
        TopicStatus.NEEDS_REVIEW -> NodeStatusStyle(
            bg = SageWarning.copy(alpha = 0.2f),
            border = SageWarning,
            color = SageWarning,
            label = "↻ NEEDS REVIEW"
        )
        TopicStatus.NOT_STARTED -> NodeStatusStyle(
            bg = SageRaisedSurface,
            border = SageCardBorder,
            color = SageTextMuted,
            label = "○ NOT STARTED"
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("roadmap_node_${node.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SageSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (status == TopicStatus.IN_PROGRESS) SagePrimaryLight else SageCardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon Indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(style.bg)
                    .border(1.dp, style.border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = node.icon.ifBlank { status.symbol },
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(style.bg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = style.label,
                            color = style.color,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (node.difficulty != null) {
                        Text(
                            text = node.difficulty,
                            color = SageTextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Text(
                    text = node.title,
                    color = SageTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                if (node.description.isNotBlank()) {
                    Text(
                        text = node.description,
                        color = SageTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 2,
                        lineHeight = 15.sp
                    )
                }

                if (node.resources.isNotEmpty()) {
                    Text(
                        text = "${node.resources.size} curated resources",
                        color = SagePrimaryLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open Topic",
                tint = SageTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
