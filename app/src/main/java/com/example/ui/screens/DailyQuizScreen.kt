package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.DailyQuizRecordEntity
import com.example.data.repository.DailyQuizDashboardStats
import com.example.data.repository.DailyQuizHistoryItem
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
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailyQuizScreen(
    dailyQuiz: DailyQuizRecordEntity?,
    history: List<DailyQuizHistoryItem>,
    stats: DailyQuizDashboardStats?,
    onSubmitAnswer: (Int) -> Unit,
    onBack: () -> Unit,
    onPracticeWeakConcept: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOptionIndex by remember { mutableIntStateOf(-1) }

    val options = remember(dailyQuiz?.optionsJson) {
        val list = mutableListOf<String>()
        if (dailyQuiz != null) {
            try {
                val array = JSONArray(dailyQuiz.optionsJson)
                for (i in 0 until array.length()) {
                    list.add(array.getString(i))
                }
            } catch (e: Exception) {
                list.addAll(listOf("Option A", "Option B", "Option C", "Option D"))
            }
        }
        list
    }

    val todayFormatted = remember {
        SimpleDateFormat("MMMM d, yyyy", Locale.US).format(Date())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SageBackground)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Navigation Header
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
                        .testTag("daily_quiz_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SageTextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "DAILY ASSESSMENT DASHBOARD",
                        color = SagePrimaryLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Today's Challenge",
                        color = SageTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Streak Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_streak_banner"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SageSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SageGold.copy(alpha = 0.15f))
                                .border(1.dp, SageGold.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = null,
                                tint = SageGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "${stats?.currentStreak ?: 3} Day Streak",
                                color = SageTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = todayFormatted,
                                color = SageTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Best Streak",
                            color = SageTextMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${stats?.longestStreak ?: 12} days",
                            color = SageGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section: TODAY'S QUIZ
        item {
            Text(
                text = "TODAY'S QUIZ",
                color = SagePrimaryLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        if (dailyQuiz != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("daily_quiz_card"),
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
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SageRaisedSurface)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = dailyQuiz.topicTitle,
                                    color = SagePrimaryLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "Difficulty: Adaptive",
                                color = SageTextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = dailyQuiz.question,
                            color = SageTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp
                        )

                        // Options
                        options.forEachIndexed { index, opt ->
                            val isCompleted = dailyQuiz.isCompleted
                            val isChosen = if (isCompleted) dailyQuiz.userAnswerIndex == index else selectedOptionIndex == index
                            val isCorrectOpt = index == dailyQuiz.correctIndex

                            val (bgColor, borderColor) = when {
                                isCompleted && isCorrectOpt -> SageSuccess.copy(alpha = 0.2f) to SageSuccess
                                isCompleted && isChosen && !isCorrectOpt -> SageError.copy(alpha = 0.2f) to SageError
                                !isCompleted && isChosen -> SagePrimary.copy(alpha = 0.15f) to SagePrimaryLight
                                else -> SageRaisedSurface to SageCardBorder
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isCompleted) {
                                        selectedOptionIndex = index
                                    }
                                    .testTag("daily_quiz_option_$index"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = bgColor),
                                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(if (isChosen || (isCompleted && isCorrectOpt)) borderColor else SageSurface)
                                            .border(1.dp, borderColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = ('A' + index).toString(),
                                            color = if (isChosen || (isCompleted && isCorrectOpt)) Color.White else SageTextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Text(
                                        text = opt,
                                        color = SageTextPrimary,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Submit Button or Evaluation
                        if (!dailyQuiz.isCompleted) {
                            Button(
                                onClick = {
                                    if (selectedOptionIndex >= 0) {
                                        onSubmitAnswer(selectedOptionIndex)
                                    }
                                },
                                enabled = selectedOptionIndex >= 0,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_daily_quiz_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                            ) {
                                Text("Submit Answer", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // After Answering: Evaluation Banner
                            val isCorrect = dailyQuiz.isCorrect == true
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isCorrect) SageSuccess.copy(alpha = 0.12f) else SageError.copy(alpha = 0.12f))
                                    .border(1.dp, if (isCorrect) SageSuccess else SageError, RoundedCornerShape(12.dp))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (isCorrect) SageSuccess else SageError,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isCorrect) "Correct! +100 XP" else "Incorrect",
                                        color = if (isCorrect) SageSuccess else SageError,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = dailyQuiz.explanation,
                                    color = SageTextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: QUIZ HISTORY
        item {
            Text(
                text = "QUIZ HISTORY",
                color = SageTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // History Card (Past Days List)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quiz_history_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SageSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    history.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.displayDate,
                                color = SageTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            when (item.status) {
                                "CORRECT" -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "Passed", color = SageSuccess, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "✓", color = SageSuccess, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                "INCORRECT" -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "Failed", color = SageError, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "✗", color = SageError, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                "MISSED" -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "Missed", color = SageTextMuted, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "—", color = SageTextMuted, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                else -> {
                                    Text(text = "Pending", color = SageGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: HISTORICAL STATS DASHBOARD
        item {
            Text(
                text = "CUMULATIVE PERFORMANCE",
                color = SageTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_quiz_stats_card"),
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
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Current Streak", color = SageTextMuted, fontSize = 11.sp)
                            Text(
                                text = "${stats?.currentStreak ?: 3} days",
                                color = SageTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(text = "Longest Streak", color = SageTextMuted, fontSize = 11.sp)
                            Text(
                                text = "${stats?.longestStreak ?: 12} days",
                                color = SageGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(text = "Accuracy", color = SageTextMuted, fontSize = 11.sp)
                            Text(
                                text = "${stats?.accuracyPercent ?: 84}%",
                                color = SageSuccess,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Answered", color = SageTextMuted, fontSize = 11.sp)
                            Text(
                                text = "${stats?.answeredDays ?: 25} days",
                                color = SageTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column {
                            Text(text = "Missed", color = SageTextMuted, fontSize = 11.sp)
                            Text(
                                text = "${stats?.missedDays ?: 5} days",
                                color = SageError,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Remediation Action
        item {
            Button(
                onClick = onPracticeWeakConcept,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("practice_weak_concept_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SageRaisedSurface)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = SagePrimaryLight, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Practice Weak Concepts with Sage", color = SagePrimaryLight, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
