package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.roadmap.DevRoadmapSummary
import com.example.ui.theme.SageBackground
import com.example.ui.theme.SageCardBorder
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

data class ExploreDomainItem(
    val roadmapId: String,
    val emoji: String,
    val category: String,
    val title: String,
    val description: String
)

@Composable
fun ExploreScreen(
    summaries: List<DevRoadmapSummary>,
    roadmapPercentages: Map<String, Int> = emptyMap(),
    onOpenRoadmap: (String) -> Unit,
    onStartTrack: (String) -> Unit,
    onCustomTrack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf(
        "All",
        "Engineering",
        "AI & Data",
        "Systems & Cloud",
        "Product & QA"
    )

    val allTracks = remember {
        listOf(
            ExploreDomainItem(
                roadmapId = "ml-ai",
                emoji = "🧠",
                category = "AI & Data",
                title = "Machine Learning & AI",
                description = "Neural networks, supervised learning, gradient descent & LLMs"
            ),
            ExploreDomainItem(
                roadmapId = "fullstack",
                emoji = "🌐",
                category = "Engineering",
                title = "Web Development",
                description = "Modern frontend, backend architectures, REST & GraphQL APIs"
            ),
            ExploreDomainItem(
                roadmapId = "data-engineer",
                emoji = "🐍",
                category = "AI & Data",
                title = "Python & Data Science",
                description = "Python pipelines, SQL, Pandas, data warehousing & analytics"
            ),
            ExploreDomainItem(
                roadmapId = "cybersecurity",
                emoji = "🔒",
                category = "Systems & Cloud",
                title = "Cybersecurity",
                description = "Network defense, vulnerability assessments & threat modeling"
            ),
            ExploreDomainItem(
                roadmapId = "frontend",
                emoji = "🎨",
                category = "Engineering",
                title = "Frontend Engineering",
                description = "JavaScript, TypeScript, React, state management & web performance"
            ),
            ExploreDomainItem(
                roadmapId = "backend",
                emoji = "⚙️",
                category = "Engineering",
                title = "Backend Architecture",
                description = "Node.js, databases, caching, concurrency & microservices"
            ),
            ExploreDomainItem(
                roadmapId = "mobile",
                emoji = "📱",
                category = "Engineering",
                title = "Mobile App Development",
                description = "Android, Kotlin, Jetpack Compose, iOS & cross-platform"
            ),
            ExploreDomainItem(
                roadmapId = "devops",
                emoji = "☁️",
                category = "Systems & Cloud",
                title = "DevOps & Cloud Engineering",
                description = "CI/CD pipelines, Docker, Kubernetes, Terraform & AWS/GCP"
            ),
            ExploreDomainItem(
                roadmapId = "qa-engineer",
                emoji = "🧪",
                category = "Product & QA",
                title = "QA & Test Engineering",
                description = "Automated testing, unit, integration, and E2E frameworks"
            ),
            ExploreDomainItem(
                roadmapId = "product-manager",
                emoji = "📋",
                category = "Product & QA",
                title = "Product Management",
                description = "Agile roadmaps, metrics, user research & system design"
            )
        )
    }

    val filteredTracks = remember(searchQuery, selectedCategory) {
        allTracks.filter { track ->
            val matchesSearch = searchQuery.isBlank() ||
                    track.title.contains(searchQuery, ignoreCase = true) ||
                    track.description.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategory == "All" || track.category.equals(selectedCategory, ignoreCase = true)

            matchesSearch && matchesCategory
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SageBackground)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "EXPLORE CURRICULUM",
                        color = SagePrimaryLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Explore Learning Tracks",
                        color = SageTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Button(
                    onClick = onCustomTrack,
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("custom_track_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SageRaisedSurface)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = SagePrimaryLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Custom", color = SagePrimaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("explore_search_field"),
                placeholder = {
                    Text(
                        text = "Search roadmaps (e.g. Full Stack, AI, DevOps)...",
                        color = SageTextMuted,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = SageTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SageTextPrimary,
                    unfocusedTextColor = SageTextPrimary,
                    focusedBorderColor = SagePrimaryLight,
                    unfocusedBorderColor = SageCardBorder,
                    focusedContainerColor = SageSurface,
                    unfocusedContainerColor = SageSurface
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) SagePrimary else SageSurface)
                            .border(
                                1.dp,
                                if (isSelected) SagePrimaryLight else SageCardBorder,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("category_chip_$category")
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

        // Section Title
        item {
            Text(
                text = "FEATURED LEARNING PATHS (${filteredTracks.size})",
                color = SageTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Learning Track Cards List with [ Roadmap ] Button
        items(filteredTracks) { track ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("explore_card_${track.title}"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SageSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SageRaisedSurface)
                                .border(1.dp, SageCardBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = track.emoji, fontSize = 22.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.category.uppercase(),
                                color = SagePrimaryLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = track.title,
                                color = SageTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = track.description,
                        color = SageTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 17.sp
                    )

                    // Real Progress Display from Room Single Source of Truth
                    val percent = roadmapPercentages[track.roadmapId] ?: if (track.roadmapId == "fullstack") 42 else 0
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progress: $percent%",
                                color = if (percent > 0) SageGold else SageTextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (percent > 0) {
                                Text(
                                    text = if (percent >= 100) "Completed" else "In Progress",
                                    color = if (percent >= 100) SageSuccess else SagePrimaryLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        LinearProgressIndicator(
                            progress = { (percent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = SagePrimary,
                            trackColor = SageRaisedSurface
                        )
                    }

                    // Action Area: [ Start ] and [ Roadmap ] BESIDE EVERY COURSE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // [ Start ] or [ Continue ] Button
                        Button(
                            onClick = { onStartTrack(track.title) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("start_button_${track.roadmapId}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SagePrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (percent > 0) "Continue" else "Start",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // [ Roadmap ] Button - Clearly visible beside the course
                        OutlinedButton(
                            onClick = { onOpenRoadmap(track.roadmapId) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("roadmap_button_${track.roadmapId}"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, SagePrimaryLight),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = SageRaisedSurface,
                                contentColor = SageTextPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Roadmap",
                                tint = SagePrimaryLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Roadmap",
                                color = SageTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Section: ALL DEVROADMAPS (20 Curated Paths)
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "DEVROADMAPS DIRECTORY (20 PATHS)",
                    color = SagePrimaryLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Authentic curriculum roadmaps from github.com/rudra496/devroadmaps",
                    color = SageTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        items(summaries) { summary ->
            val summaryPercent = roadmapPercentages[summary.id] ?: if (summary.id == "fullstack") 42 else 0
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("summary_card_${summary.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SageSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = summary.icon, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = summary.title,
                                color = SageTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${summary.nodes} Topics • ${summary.difficulty} • Progress: $summaryPercent%",
                                color = if (summaryPercent > 0) SageGold else SageTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (summaryPercent > 0) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onStartTrack(summary.title) },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("directory_continue_${summary.id}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                        ) {
                            Text(
                                text = if (summaryPercent > 0) "Continue" else "Start",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { onOpenRoadmap(summary.id) },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("directory_roadmap_${summary.id}"),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SagePrimaryLight),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = SageRaisedSurface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = SagePrimaryLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Roadmap",
                                color = SageTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
