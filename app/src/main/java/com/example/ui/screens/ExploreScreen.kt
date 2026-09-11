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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.ui.theme.SageBackground
import com.example.ui.theme.SageCardBorder
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryLight
import com.example.ui.theme.SagePrimaryStart
import com.example.ui.theme.SageRaisedSurface
import com.example.ui.theme.SageSurface
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary

data class ExploreDomainItem(
    val emoji: String,
    val category: String,
    val title: String,
    val description: String
)

@Composable
fun ExploreScreen(
    onStartTrack: (String) -> Unit,
    onCustomTrack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf(
        "All",
        "Technology & Engineering",
        "Creative Arts",
        "Business & Strategy",
        "Science & Math"
    )

    val allTracks = remember {
        listOf(
            ExploreDomainItem(
                emoji = "\uD83E\uDDE0", // brain
                category = "TECHNOLOGY & ENGINEERING",
                title = "Machine Learning & AI",
                description = "Neural networks, supervised learning, gradient descent & LLMs"
            ),
            ExploreDomainItem(
                emoji = "\uD83C\uDF10", // globe
                category = "TECHNOLOGY & ENGINEERING",
                title = "Web Development",
                description = "Modern frontend, backend architectures, APIs & full-stack"
            ),
            ExploreDomainItem(
                emoji = "\uD83D\uDC0D", // snake
                category = "TECHNOLOGY & ENGINEERING",
                title = "Python & Data Science",
                description = "Pandas, NumPy, statistical modeling & data pipelines"
            ),
            ExploreDomainItem(
                emoji = "\uD83D\uDEE1\uFE0F", // shield
                category = "TECHNOLOGY & ENGINEERING",
                title = "Cybersecurity",
                description = "Network defense, cryptography, threat modeling & penetration testing"
            ),
            ExploreDomainItem(
                emoji = "\u2601\uFE0F", // cloud
                category = "TECHNOLOGY & ENGINEERING",
                title = "Cloud Computing (GCP / AWS)",
                description = "Serverless, containers, IAM, microservices & infrastructure as code"
            ),
            ExploreDomainItem(
                emoji = "\uD83C\uDFA8", // palette
                category = "CREATIVE ARTS",
                title = "Design Systems & UI/UX",
                description = "Visual hierarchy, typography, design systems & creative coding"
            ),
            ExploreDomainItem(
                emoji = "\uD83D\uDCCA", // chart
                category = "BUSINESS & STRATEGY",
                title = "Product Strategy & Metrics",
                description = "User research, prioritization frameworks, unit economics & roadmaps"
            ),
            ExploreDomainItem(
                emoji = "\u269B\uFE0F", // atom
                category = "SCIENCE & MATH",
                title = "Quantum Computing",
                description = "Qubits, quantum superposition, quantum gates & algorithms"
            )
        )
    }

    val filteredTracks = remember(searchQuery, selectedCategory) {
        allTracks.filter { track ->
            val matchesCategory = selectedCategory == "All" || track.category.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    track.title.contains(searchQuery, ignoreCase = true) ||
                    track.description.contains(searchQuery, ignoreCase = true) ||
                    track.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Curated Domains",
                        color = SagePrimaryLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Explore Learning Tracks",
                        color = SageTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // "+ Custom" Button
                Button(
                    onClick = onCustomTrack,
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("custom_track_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Custom",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Custom", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                    .testTag("explore_search_input"),
                placeholder = {
                    Text(
                        text = "Search topics, skills, frameworks...",
                        color = SageTextMuted,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = SageTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SageSurface,
                    unfocusedContainerColor = SageSurface,
                    focusedBorderColor = SagePrimary,
                    unfocusedBorderColor = SageCardBorder,
                    focusedTextColor = SageTextPrimary,
                    unfocusedTextColor = SageTextPrimary,
                    cursorColor = SagePrimaryLight
                )
            )
        }

        // Horizontal Category Chips
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
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) SagePrimary else SageSurface)
                            .border(
                                1.dp,
                                if (isSelected) SagePrimaryLight else SageCardBorder,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("category_chip_$category")
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.White else SageTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Track Cards List
        items(filteredTracks) { track ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("explore_card_${track.title}"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SageSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Emoji / Icon Avatar
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(SageRaisedSurface)
                            .border(1.dp, SageCardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = track.emoji,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Track Info
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = track.category,
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

                        Text(
                            text = track.description,
                            color = SageTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // "Start" Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SageRaisedSurface)
                            .border(1.dp, SagePrimary.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .clickable { onStartTrack(track.title) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("start_track_${track.title}")
                    ) {
                        Text(
                            text = "Start",
                            color = SagePrimaryLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
