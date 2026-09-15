package com.example.data.quiz

enum class QuizQuestionType {
    MCQ,
    TRUE_FALSE,
    CODE_OUTPUT,
    SCENARIO,
    SHORT_ANSWER
}

data class QuizQuestion(
    val id: Int,
    val type: QuizQuestionType,
    val question: String,
    val codeSnippet: String? = null,
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val sampleAnswer: String = "",
    val explanation: String,
    val conceptTested: String
)

data class UserQuestionAnswer(
    val question: QuizQuestion,
    val selectedIndex: Int,
    val textAnswer: String,
    val isCorrect: Boolean
)

data class QuizSubmissionResult(
    val topicTitle: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val scorePercent: Int,
    val answers: List<UserQuestionAnswer>,
    val weakConcepts: List<String>,
    val passed: Boolean,
    val recommendation: String
)
