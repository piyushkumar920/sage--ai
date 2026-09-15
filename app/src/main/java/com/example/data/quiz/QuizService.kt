package com.example.data.quiz

import com.example.data.api.GeminiClient
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiResult
import com.example.data.roadmap.DevRoadmapNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class QuizService(private val geminiClient: GeminiClient) {

    suspend fun generateQuizForTopic(node: DevRoadmapNode, roadmapTitle: String): List<QuizQuestion> = withContext(Dispatchers.IO) {
        val prompt = """
You are generating a 4-question quiz for the roadmap topic: "${node.title}" (Roadmap: $roadmapTitle).
Description: ${node.description}
Difficulty: ${node.difficulty ?: "Intermediate"}

Output a JSON array containing exactly 4 questions with mixed types:
1. MCQ (Multiple Choice with 4 options)
2. TRUE_FALSE (2 options: "True", "False")
3. CODE_OUTPUT or SCENARIO (Practical question with 4 options)
4. SHORT_ANSWER or MCQ

FORMAT ONLY AS RAW JSON (no markdown fences, no extra text):
[
  {
    "type": "MCQ",
    "question": "question text",
    "codeSnippet": null,
    "options": ["opt1", "opt2", "opt3", "opt4"],
    "correctIndex": 0,
    "explanation": "why this is correct",
    "conceptTested": "specific concept name"
  },
  {
    "type": "TRUE_FALSE",
    "question": "statement to evaluate",
    "codeSnippet": null,
    "options": ["True", "False"],
    "correctIndex": 0,
    "explanation": "explanation",
    "conceptTested": "specific concept name"
  },
  {
    "type": "SCENARIO",
    "question": "scenario question",
    "codeSnippet": "optional code snippet",
    "options": ["opt1", "opt2", "opt3", "opt4"],
    "correctIndex": 1,
    "explanation": "explanation",
    "conceptTested": "specific concept name"
  },
  {
    "type": "MCQ",
    "question": "practical question",
    "codeSnippet": null,
    "options": ["opt1", "opt2", "opt3", "opt4"],
    "correctIndex": 2,
    "explanation": "explanation",
    "conceptTested": "specific concept name"
  }
]
        """.trimIndent()

        val history = listOf(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = prompt))
            )
        )

        try {
            val result = geminiClient.generateContent(
                history = history,
                systemPrompt = "You are an expert technical examiner. Return valid raw JSON array only.",
                mode = "normal"
            )

            if (result is GeminiResult.Success) {
                val cleaned = cleanJsonResponse(result.data)
                val array = JSONArray(cleaned)
                val list = mutableListOf<QuizQuestion>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val typeStr = obj.optString("type", "MCQ")
                    val type = when (typeStr.uppercase()) {
                        "TRUE_FALSE" -> QuizQuestionType.TRUE_FALSE
                        "CODE_OUTPUT" -> QuizQuestionType.CODE_OUTPUT
                        "SCENARIO" -> QuizQuestionType.SCENARIO
                        "SHORT_ANSWER" -> QuizQuestionType.SHORT_ANSWER
                        else -> QuizQuestionType.MCQ
                    }

                    val optArray = obj.optJSONArray("options")
                    val options = mutableListOf<String>()
                    if (optArray != null) {
                        for (o in 0 until optArray.length()) {
                            options.add(optArray.getString(o))
                        }
                    }

                    list.add(
                        QuizQuestion(
                            id = i + 1,
                            type = type,
                            question = obj.optString("question", "Question ${i + 1}"),
                            codeSnippet = if (obj.has("codeSnippet") && !obj.isNull("codeSnippet")) obj.getString("codeSnippet") else null,
                            options = if (options.isEmpty()) listOf("Option A", "Option B", "Option C", "Option D") else options,
                            correctIndex = obj.optInt("correctIndex", 0),
                            sampleAnswer = obj.optString("sampleAnswer", ""),
                            explanation = obj.optString("explanation", "Correct understanding of ${node.title}."),
                            conceptTested = obj.optString("conceptTested", node.title)
                        )
                    )
                }
                if (list.size >= 2) return@withContext list
            }
        } catch (e: Exception) {
            // Fall back to robust pre-configured questions for this topic
        }

        return@withContext getFallbackQuizForTopic(node)
    }

    fun getFallbackQuizForTopic(node: DevRoadmapNode): List<QuizQuestion> {
        val title = node.title
        val desc = node.description

        return listOf(
            QuizQuestion(
                id = 1,
                type = QuizQuestionType.MCQ,
                question = "What is the primary conceptual responsibility of $title?",
                options = listOf(
                    desc.ifBlank { "Provides modular structure and efficient lifecycle coordination" },
                    "Forces all operations to execute synchronously on the main UI thread",
                    "Disables error reporting and validation checks in production",
                    "Eliminates the need for memory management and unit testing"
                ),
                correctIndex = 0,
                explanation = "In modern architecture, $title is designed to provide clear modular separation and robust maintainability.",
                conceptTested = "$title Core Purpose"
            ),
            QuizQuestion(
                id = 2,
                type = QuizQuestionType.TRUE_FALSE,
                question = "True or False: In $title, understanding underlying prerequisites and lifecycle flow prevents severe performance bottlenecks.",
                options = listOf("True", "False"),
                correctIndex = 0,
                explanation = "Understanding foundational execution mechanics and lifecycle boundaries is essential for reliable implementations of $title.",
                conceptTested = "$title Lifecycle Flow"
            ),
            QuizQuestion(
                id = 3,
                type = QuizQuestionType.SCENARIO,
                question = "Scenario: While deploying a solution with $title, sudden traffic bursts cause high latency. What is the most effective initial mitigation?",
                options = listOf(
                    "Profile bottlenecks, introduce caching, and handle tasks asynchronously",
                    "Restart the server on every incoming connection",
                    "Convert all data structures into unindexed global variables",
                    "Disable automated testing suites"
                ),
                correctIndex = 0,
                explanation = "Profiling bottlenecks and applying asynchronous processing with intelligent caching restores responsiveness during load spikes in $title.",
                conceptTested = "$title Performance Optimization"
            ),
            QuizQuestion(
                id = 4,
                type = QuizQuestionType.CODE_OUTPUT,
                question = "Which pattern represents best practice error resilience when working with $title?",
                options = listOf(
                    "Structured try/catch with informative diagnostic feedback and safe fallback state",
                    "Silently suppressing all runtime exceptions without logging",
                    "Crash the application immediately without saving progress",
                    "Ignore network timeouts and block execution indefinitely"
                ),
                correctIndex = 0,
                explanation = "Production-grade resilience demands structured error propagation, informative diagnostics, and graceful recovery fallbacks.",
                conceptTested = "$title Error Resilience"
            )
        )
    }

    private fun cleanJsonResponse(raw: String): String {
        var clean = raw.trim()
        if (clean.startsWith("```json")) {
            clean = clean.removePrefix("```json")
        } else if (clean.startsWith("```")) {
            clean = clean.removePrefix("```")
        }
        if (clean.endsWith("```")) {
            clean = clean.removeSuffix("```")
        }
        return clean.trim()
    }
}
