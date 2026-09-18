package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.quiz.QuizQuestion
import com.example.data.quiz.QuizQuestionType
import com.example.data.quiz.QuizSubmissionResult
import com.example.data.quiz.UserQuestionAnswer
import com.example.data.roadmap.DevRoadmapNode
import com.example.ui.components.AmbientGlowBackground
import com.example.ui.components.GlassBadge
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassChip
import com.example.ui.components.GlassLevel
import com.example.ui.theme.SageBackground
import com.example.ui.theme.SageCardBorder
import com.example.ui.theme.SageError
import com.example.ui.theme.SageGlassBorder
import com.example.ui.theme.SageGlassBorderLight
import com.example.ui.theme.SageGlassL1
import com.example.ui.theme.SageGlassL2
import com.example.ui.theme.SageGlassL3
import com.example.ui.theme.SageGold
import com.example.ui.theme.SageGlowPrimary
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
fun QuizScreen(
    node: DevRoadmapNode,
    roadmapTitle: String,
    onLoadQuiz: suspend () -> List<QuizQuestion>,
    onSubmitResult: (scorePercent: Int, total: Int, weakList: List<String>) -> Unit,
    onBack: () -> Unit,
    onStartLearning: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(true) }
    var questions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }

    // Map questionIndex -> selectedOptionIndex
    val userSelectedOptions = remember { mutableStateMapOf<Int, Int>() }
    // Map questionIndex -> text input
    val userTextAnswers = remember { mutableStateMapOf<Int, String>() }

    var submissionResult by remember { mutableStateOf<QuizSubmissionResult?>(null) }

    LaunchedEffect(node.id) {
        isLoading = true
        questions = onLoadQuiz()
        isLoading = false
    }

    if (isLoading) {
        AmbientGlowBackground(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    modifier = Modifier.padding(28.dp),
                    level = GlassLevel.L2,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = SagePrimaryLight,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Generating ${node.title} Quiz via Gemini...",
                            color = SageTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
        return
    }

    // Results View
    if (submissionResult != null) {
        QuizResultView(
            result = submissionResult!!,
            node = node,
            roadmapTitle = roadmapTitle,
            onBack = onBack,
            onReviewWithSage = onStartLearning,
            onRetake = {
                submissionResult = null
                userSelectedOptions.clear()
                userTextAnswers.clear()
                currentQuestionIndex = 0
            },
            modifier = modifier
        )
        return
    }

    if (questions.isEmpty()) {
        AmbientGlowBackground(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    modifier = Modifier.padding(20.dp),
                    level = GlassLevel.L2,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "No questions available for this topic.", color = SageTextSecondary)
                        Spacer(modifier = Modifier.height(16.dp))
                        GlassButton(
                            text = "Go Back",
                            onClick = onBack,
                            variant = GlassButtonVariant.Primary
                        )
                    }
                }
            }
        }
        return
    }

    val currentQuestion = questions[currentQuestionIndex]
    val totalQuestions = questions.size

    val progressAnimated by animateFloatAsState(
        targetValue = (currentQuestionIndex + 1).toFloat() / totalQuestions,
        label = "quiz_progress"
    )

    AmbientGlowBackground(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(SageGlassL2)
                            .border(1.dp, SageGlassBorder, CircleShape)
                            .testTag("quiz_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SageTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "TOPIC QUIZ • $roadmapTitle".uppercase(),
                            color = SagePrimaryLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = node.title,
                            color = SageTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Progress Bar & Counter
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    level = GlassLevel.L1,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Question ${currentQuestionIndex + 1} of $totalQuestions",
                                color = SageTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            GlassBadge(
                                text = currentQuestion.type.name.replace("_", " "),
                                color = SageGold
                            )
                        }

                        LinearProgressIndicator(
                            progress = { progressAnimated },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = SagePrimaryLight,
                            trackColor = SageGlassL3
                        )
                    }
                }
            }

            // Question Card
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_question_card"),
                    level = GlassLevel.L2,
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = currentQuestion.question,
                            color = SageTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp
                        )

                        if (currentQuestion.codeSnippet != null && currentQuestion.codeSnippet.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0A0A14))
                                    .border(1.dp, SageGlassBorder, RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = currentQuestion.codeSnippet,
                                    color = SagePrimaryLight,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Options or Input
            if (currentQuestion.type == QuizQuestionType.SHORT_ANSWER && currentQuestion.options.isEmpty()) {
                item {
                    val currentText = userTextAnswers[currentQuestionIndex] ?: ""
                    OutlinedTextField(
                        value = currentText,
                        onValueChange = { userTextAnswers[currentQuestionIndex] = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quiz_short_answer_input"),
                        placeholder = { Text("Type your answer here...", color = SageTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SageTextPrimary,
                            unfocusedTextColor = SageTextPrimary,
                            focusedBorderColor = SagePrimaryLight,
                            unfocusedBorderColor = SageGlassBorder,
                            focusedContainerColor = SageGlassL2,
                            unfocusedContainerColor = SageGlassL1
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            } else {
                // Options list
                itemsIndexed(currentQuestion.options) { index, option ->
                    val isSelected = userSelectedOptions[currentQuestionIndex] == index
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quiz_option_$index"),
                        level = if (isSelected) GlassLevel.L3 else GlassLevel.L1,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.2.dp,
                            if (isSelected) SagePrimaryLight else SageGlassBorder
                        ),
                        onClick = { userSelectedOptions[currentQuestionIndex] = index }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) SagePrimary else SageGlassL3)
                                    .border(
                                        1.dp,
                                        if (isSelected) SagePrimaryLight else SageGlassBorder,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ('A' + index).toString(),
                                    color = if (isSelected) Color.White else SageTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Text(
                                text = option,
                                color = if (isSelected) SageTextPrimary else SageTextSecondary,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Navigation / Submit Buttons
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentQuestionIndex > 0) {
                        GlassButton(
                            text = "Previous",
                            onClick = { currentQuestionIndex-- },
                            variant = GlassButtonVariant.Secondary,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        )
                    }

                    val hasAnswer = userSelectedOptions.containsKey(currentQuestionIndex) ||
                            !userTextAnswers[currentQuestionIndex].isNullOrBlank()

                    GlassButton(
                        text = if (currentQuestionIndex < totalQuestions - 1) "Next" else "Submit Quiz",
                        onClick = {
                            if (currentQuestionIndex < totalQuestions - 1) {
                                currentQuestionIndex++
                            } else {
                                // Compute Submission Result
                                val answers = questions.mapIndexed { idx, q ->
                                    val selectedIdx = userSelectedOptions[idx] ?: -1
                                    val textAns = userTextAnswers[idx] ?: ""
                                    val isCorrect = if (q.type == QuizQuestionType.SHORT_ANSWER && q.options.isEmpty()) {
                                        textAns.isNotBlank() && q.sampleAnswer.contains(textAns, ignoreCase = true)
                                    } else {
                                        selectedIdx == q.correctIndex
                                    }
                                    UserQuestionAnswer(
                                        question = q,
                                        selectedIndex = selectedIdx,
                                        textAnswer = textAns,
                                        isCorrect = isCorrect
                                    )
                                }

                                val correctCount = answers.count { it.isCorrect }
                                val scorePercent = (correctCount * 100) / totalQuestions
                                val passed = scorePercent >= 70

                                val weakList = answers.filter { !it.isCorrect }.map { it.question.conceptTested }

                                val rec = if (passed) {
                                    "Excellent mastery of ${node.title}! You are ready to proceed to the next module in your roadmap."
                                } else {
                                    "Score below 70%. Marked as 'NEEDS REVIEW' so Sage can strengthen your foundational comprehension before proceeding."
                                }

                                val result = QuizSubmissionResult(
                                    topicTitle = node.title,
                                    totalQuestions = totalQuestions,
                                    correctCount = correctCount,
                                    scorePercent = scorePercent,
                                    answers = answers,
                                    weakConcepts = weakList,
                                    passed = passed,
                                    recommendation = rec
                                )

                                submissionResult = result
                                onSubmitResult(scorePercent, totalQuestions, weakList)
                            }
                        },
                        variant = GlassButtonVariant.Primary,
                        enabled = hasAnswer,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("quiz_next_submit_button")
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
fun QuizResultView(
    result: QuizSubmissionResult,
    node: DevRoadmapNode,
    roadmapTitle: String,
    onBack: () -> Unit,
    onReviewWithSage: () -> Unit,
    onRetake: () -> Unit,
    modifier: Modifier = Modifier
) {
    AmbientGlowBackground(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(SageGlassL2)
                            .border(1.dp, SageGlassBorder, CircleShape)
                            .testTag("result_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SageTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "ASSESSMENT SUMMARY",
                            color = SagePrimaryLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = result.topicTitle,
                            color = SageTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Score Hero Card
            item {
                val scoreColor = if (result.passed) SageSuccess else SageWarning
                val statusLabel = if (result.passed) "TOPIC COMPLETED ✓" else "NEEDS REVIEW ↻"

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_score_hero_card"),
                    level = GlassLevel.L2,
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, scoreColor.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "${result.scorePercent}%",
                            color = scoreColor,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        GlassBadge(
                            text = statusLabel,
                            color = scoreColor
                        )

                        Text(
                            text = "${result.correctCount} of ${result.totalQuestions} Questions Correct",
                            color = SageTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = result.recommendation,
                            color = SageTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Action Buttons
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!result.passed) {
                        GlassButton(
                            text = "Review Weak Concepts with Sage AI",
                            onClick = onReviewWithSage,
                            icon = Icons.Default.AutoAwesome,
                            variant = GlassButtonVariant.Primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("review_with_sage_button")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassButton(
                            text = "Retake Quiz",
                            onClick = onRetake,
                            icon = Icons.Default.Refresh,
                            variant = GlassButtonVariant.Secondary,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("retake_quiz_button")
                        )

                        GlassButton(
                            text = "Back to Roadmap",
                            onClick = onBack,
                            variant = GlassButtonVariant.Secondary,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("done_quiz_button")
                        )
                    }
                }
            }

            // Weak Concepts section if any
            if (result.weakConcepts.isNotEmpty()) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        level = GlassLevel.L1,
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SageWarning.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "IDENTIFIED WEAK CONCEPTS",
                                color = SageWarning,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            result.weakConcepts.forEach { weak ->
                                Text(
                                    text = "• $weak",
                                    color = SageTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Detailed Answers Breakdown
            item {
                Text(
                    text = "QUESTION BREAKDOWN & EXPLANATIONS",
                    color = SageTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            itemsIndexed(result.answers) { index, ans ->
                val icon = if (ans.isCorrect) Icons.Default.Check else Icons.Default.Close
                val iconTint = if (ans.isCorrect) SageSuccess else SageError

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    level = GlassLevel.L1,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Q${index + 1}: ${ans.question.conceptTested}",
                                color = SageTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = ans.question.question,
                            color = SageTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (ans.question.options.isNotEmpty() && ans.selectedIndex in ans.question.options.indices) {
                            Text(
                                text = "Your Answer: ${ans.question.options[ans.selectedIndex]}",
                                color = if (ans.isCorrect) SageSuccess else SageError,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (!ans.isCorrect && ans.question.correctIndex in ans.question.options.indices) {
                                Text(
                                    text = "Correct Answer: ${ans.question.options[ans.question.correctIndex]}",
                                    color = SageSuccess,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Text(
                            text = "Explanation: ${ans.question.explanation}",
                            color = SageTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
