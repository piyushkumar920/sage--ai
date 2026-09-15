package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(SageBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CircularProgressIndicator(color = SagePrimary)
                Text(
                    text = "Generating ${node.title} Quiz via Gemini...",
                    color = SageTextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
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
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(SageBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "No questions available for this topic.", color = SageTextSecondary)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBack) { Text("Go Back") }
            }
        }
        return
    }

    val currentQuestion = questions[currentQuestionIndex]
    val totalQuestions = questions.size

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SageSurface)
                        .border(1.dp, SageCardBorder, CircleShape)
                        .testTag("quiz_back_button")
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
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                    Text(
                        text = currentQuestion.type.name.replace("_", " "),
                        color = SageGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { (currentQuestionIndex + 1).toFloat() / totalQuestions },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = SagePrimary,
                    trackColor = SageRaisedSurface
                )
            }
        }

        // Question Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quiz_question_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SageSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = currentQuestion.question,
                        color = SageTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )

                    if (currentQuestion.codeSnippet != null && currentQuestion.codeSnippet.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SageRaisedSurface)
                                .border(1.dp, SageCardBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp)
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
                        unfocusedBorderColor = SageCardBorder,
                        focusedContainerColor = SageSurface,
                        unfocusedContainerColor = SageSurface
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
            }
        } else {
            // Options list
            itemsIndexed(currentQuestion.options) { index, option ->
                val isSelected = userSelectedOptions[currentQuestionIndex] == index
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { userSelectedOptions[currentQuestionIndex] = index }
                        .testTag("quiz_option_$index"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) SagePrimary.copy(alpha = 0.15f) else SageSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) SagePrimaryLight else SageCardBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) SagePrimary else SageRaisedSurface)
                                .border(1.dp, if (isSelected) SagePrimaryLight else SageCardBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ('A' + index).toString(),
                                color = if (isSelected) Color.White else SageTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

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
                    OutlinedButton(
                        onClick = { currentQuestionIndex-- },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SageSurface)
                    ) {
                        Text("Previous", color = SageTextSecondary)
                    }
                }

                val hasAnswer = userSelectedOptions.containsKey(currentQuestionIndex) ||
                        !userTextAnswers[currentQuestionIndex].isNullOrBlank()

                Button(
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
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("quiz_next_submit_button"),
                    enabled = hasAnswer,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                ) {
                    Text(
                        text = if (currentQuestionIndex < totalQuestions - 1) "Next" else "Submit Quiz",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
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
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SageBackground)
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SageSurface)
                        .border(1.dp, SageCardBorder, CircleShape)
                        .testTag("result_back_button")
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

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quiz_score_hero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SageSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, scoreColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "${result.scorePercent}%",
                        color = scoreColor,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(scoreColor.copy(alpha = 0.15f))
                            .border(1.dp, scoreColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            color = scoreColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${result.correctCount} of ${result.totalQuestions} Questions Correct",
                        color = SageTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = result.recommendation,
                        color = SageTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
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
                    Button(
                        onClick = onReviewWithSage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("review_with_sage_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Review Weak Concepts with Sage AI", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onRetake,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("retake_quiz_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SageSurface)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = SageTextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retake Quiz", color = SageTextSecondary)
                    }

                    Button(
                        onClick = onBack,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("done_quiz_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SageRaisedSurface)
                    ) {
                        Text("Back to Roadmap", color = SageTextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Weak Concepts section if any
        if (result.weakConcepts.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SageRaisedSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SageWarning.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SageSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SageCardBorder)
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
