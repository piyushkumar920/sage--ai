package com.example.data.studytools

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Supported Study Tool Types
 */
enum class StudyToolType(val id: String, val title: String, val icon: String, val description: String) {
    SCAN_AND_SOLVE("solve_image", "Scan & Solve", "📷", "Upload or photograph any question to get step-by-step educational solutions"),
    GENERATE_NOTES("notes", "Generate Notes", "📝", "Synthesize comprehensive, syllabus-grounded academic notes"),
    FLASHCARDS("flashcards", "Flashcards", "🗂", "Interactive flip cards for active recall and spaced revision"),
    MIND_MAP("mindmap", "Mind Map", "🧠", "Hierarchical, visual node tree with zoom, pan, and concept drills"),
    REVISION_SHEET("revision", "Revision Sheet", "📄", "High-yield core formulas, definitions, misconceptions, and quick points"),
    FORMULA_SHEET("formulas", "Formula Sheet", "🧮", "Equations, variable meanings, SI units, and worked derivations")
}

/**
 * Room Database Entity for Stored Study Tool Artefacts
 */
@Entity(tableName = "saved_study_tools")
data class SavedStudyToolEntity(
    @PrimaryKey
    val id: String, // e.g. "notes_1672345678"
    val type: String, // "notes", "flashcards", "mindmap", "revision", "formulas", "solve_image"
    val title: String,
    val subject: String = "",
    val topic: String = "",
    val syllabusContext: String = "",
    val contentJson: String, // Serialized structured model
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ==========================================
// 1. NOTES CONTRACT
// ==========================================
@JsonClass(generateAdapter = true)
data class StudyNotesSection(
    @field:Json(name = "heading") val heading: String = "",
    @field:Json(name = "content") val content: String = "",
    @field:Json(name = "keyPoints") val keyPoints: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class StudyNotesData(
    @field:Json(name = "type") val type: String = "notes",
    @field:Json(name = "title") val title: String = "",
    @field:Json(name = "summary") val summary: String = "",
    @field:Json(name = "sections") val sections: List<StudyNotesSection> = emptyList(),
    @field:Json(name = "examples") val examples: List<String> = emptyList(),
    @field:Json(name = "importantTerms") val importantTerms: List<String> = emptyList(),
    @field:Json(name = "beyondSyllabus") val beyondSyllabus: Boolean = false
)

// ==========================================
// 2. FLASHCARDS CONTRACT
// ==========================================
@JsonClass(generateAdapter = true)
data class StudyFlashcard(
    @field:Json(name = "front") val front: String = "",
    @field:Json(name = "back") val back: String = "",
    @field:Json(name = "hint") val hint: String = ""
)

@JsonClass(generateAdapter = true)
data class StudyFlashcardsData(
    @field:Json(name = "type") val type: String = "flashcards",
    @field:Json(name = "title") val title: String = "",
    @field:Json(name = "cards") val cards: List<StudyFlashcard> = emptyList()
)

// ==========================================
// 3. MIND MAP CONTRACT
// ==========================================
@JsonClass(generateAdapter = true)
data class MindMapNode(
    @field:Json(name = "id") val id: String = "",
    @field:Json(name = "label") val label: String = "",
    @field:Json(name = "description") val description: String = "",
    @field:Json(name = "children") val children: List<MindMapNode> = emptyList()
)

@JsonClass(generateAdapter = true)
data class StudyMindMapData(
    @field:Json(name = "type") val type: String = "mindmap",
    @field:Json(name = "title") val title: String = "",
    @field:Json(name = "root") val root: MindMapNode = MindMapNode()
)

// ==========================================
// 4. REVISION SHEET CONTRACT
// ==========================================
@JsonClass(generateAdapter = true)
data class StudyRevisionSheetData(
    @field:Json(name = "type") val type: String = "revision",
    @field:Json(name = "title") val title: String = "",
    @field:Json(name = "coreConcepts") val coreConcepts: List<String> = emptyList(),
    @field:Json(name = "definitions") val definitions: List<String> = emptyList(),
    @field:Json(name = "keyFacts") val keyFacts: List<String> = emptyList(),
    @field:Json(name = "importantFormulas") val importantFormulas: List<String> = emptyList(),
    @field:Json(name = "commonMistakes") val commonMistakes: List<String> = emptyList(),
    @field:Json(name = "quickExamples") val quickExamples: List<String> = emptyList(),
    @field:Json(name = "lastMinuteRevisionPoints") val lastMinuteRevisionPoints: List<String> = emptyList(),
    @field:Json(name = "beyondSyllabus") val beyondSyllabus: Boolean = false
)

// ==========================================
// 5. FORMULA SHEET CONTRACT
// ==========================================
@JsonClass(generateAdapter = true)
data class FormulaItem(
    @field:Json(name = "name") val name: String = "",
    @field:Json(name = "formula") val formula: String = "",
    @field:Json(name = "variables") val variables: List<String> = emptyList(),
    @field:Json(name = "units") val units: String = "",
    @field:Json(name = "usageExplanation") val usageExplanation: String = "",
    @field:Json(name = "example") val example: String = ""
)

@JsonClass(generateAdapter = true)
data class StudyFormulaSheetData(
    @field:Json(name = "type") val type: String = "formulas",
    @field:Json(name = "title") val title: String = "",
    @field:Json(name = "hasFormulas") val hasFormulas: Boolean = true,
    @field:Json(name = "formulas") val formulas: List<FormulaItem> = emptyList(),
    @field:Json(name = "notes") val notes: String = ""
)

// ==========================================
// 6. SCAN & SOLVE SOLUTION CONTRACT
// ==========================================
@JsonClass(generateAdapter = true)
data class SolutionStep(
    @field:Json(name = "step") val step: Int = 1,
    @field:Json(name = "title") val title: String = "",
    @field:Json(name = "explanation") val explanation: String = ""
)

@JsonClass(generateAdapter = true)
data class StudySolutionData(
    @field:Json(name = "type") val type: String = "solution",
    @field:Json(name = "isClear") val isClear: Boolean = true,
    @field:Json(name = "problem") val problem: String = "",
    @field:Json(name = "subject") val subject: String = "",
    @field:Json(name = "answer") val answer: String = "",
    @field:Json(name = "steps") val steps: List<SolutionStep> = emptyList(),
    @field:Json(name = "finalAnswer") val finalAnswer: String = "",
    @field:Json(name = "needsVerification") val needsVerification: Boolean = false
)

// ==========================================
// BACKEND REQUEST / RESPONSE WRAPPERS
// ==========================================
@JsonClass(generateAdapter = true)
data class AcademicContext(
    @field:Json(name = "department") val department: String = "",
    @field:Json(name = "programme") val programme: String = "",
    @field:Json(name = "regulation") val regulation: String = "",
    @field:Json(name = "semester") val semester: Int? = null,
    @field:Json(name = "courseCode") val courseCode: String = "",
    @field:Json(name = "courseName") val courseName: String = "",
    @field:Json(name = "module") val module: String = "",
    @field:Json(name = "topic") val topic: String = "",
    @field:Json(name = "officialSyllabusContent") val officialSyllabusContent: String = ""
)

@JsonClass(generateAdapter = true)
data class SageStudyToolsRequest(
    @field:Json(name = "operation") val operation: String,
    @field:Json(name = "topic") val topic: String = "",
    @field:Json(name = "subject") val subject: String = "",
    @field:Json(name = "syllabusContext") val syllabusContext: String = "",
    @field:Json(name = "imageBase64") val imageBase64: String = "",
    @field:Json(name = "imageMimeType") val imageMimeType: String = "image/jpeg",
    @field:Json(name = "prompt") val prompt: String = "",
    @field:Json(name = "academicContext") val academicContext: AcademicContext? = null
)

@JsonClass(generateAdapter = true)
data class SageStudyToolsResponse(
    @field:Json(name = "ok") val ok: Boolean? = null,
    @field:Json(name = "success") val success: Boolean = false,
    @field:Json(name = "operation") val operation: String? = null,
    @field:Json(name = "data") val data: Map<String, Any?>? = null,
    @field:Json(name = "error") val error: String? = null,
    @field:Json(name = "code") val code: String? = null,
    @field:Json(name = "requestId") val requestId: String? = null,
    @field:Json(name = "timestamp") val timestamp: String? = null
)

// ==========================================
// TYPED STUDY TOOL ERRORS
// ==========================================
sealed class StudyToolError(val userMessage: String, val diagnostic: String = "") {
    data class RouteNotFound(val raw: String = "") :
        StudyToolError("Study service is temporarily unavailable. Please try again.", raw)

    data class InvalidRequest(val raw: String = "") :
        StudyToolError("Invalid study tool request. Please check inputs and try again.", raw)

    data class Unauthorized(val raw: String = "") :
        StudyToolError("Service access error. Please try again later.", raw)

    data class RateLimited(val raw: String = "") :
        StudyToolError("Sage has reached its temporary AI request limit. Please try again shortly.", raw)

    data class ServerError(val raw: String = "") :
        StudyToolError("Sage couldn't generate this material right now. Please try again.", raw)

    data class GeminiUnavailable(val raw: String = "") :
        StudyToolError("Sage couldn't generate this material right now. Please try again.", raw)

    data class Network(val raw: String = "") :
        StudyToolError("You're offline. Connect to the internet to generate new study material.", raw)

    data class InvalidResponse(val raw: String = "") :
        StudyToolError("Sage couldn't parse the generated study material. Please retry.", raw)

    companion object {
        fun from(error: com.example.data.api.GeminiResult.Error): StudyToolError {
            val diag = error.diagnosticMessage ?: error.message
            return when {
                diag.contains("404") || error.message.contains("route not found", ignoreCase = true) ||
                    error.message.contains("temporarily unavailable", ignoreCase = true) ->
                    RouteNotFound(diag)

                error.isRateLimit || diag.contains("429") || diag.contains("RATE_LIMIT", ignoreCase = true) ->
                    RateLimited(diag)

                error.isAuthError || diag.contains("401") || diag.contains("403") ||
                    diag.contains("AUTH_OR_API_KEY_ERROR", ignoreCase = true) ->
                    Unauthorized(diag)

                error.isNetworkError || error.isTimeout || diag.contains("offline", ignoreCase = true) ||
                    diag.contains("UnknownHost", ignoreCase = true) || diag.contains("ConnectException") ||
                    diag.contains("SocketTimeout") ->
                    Network(diag)

                diag.contains("502") || diag.contains("503") ||
                    diag.contains("GEMINI_SERVICE_UNAVAILABLE", ignoreCase = true) ->
                    GeminiUnavailable(diag)

                diag.contains("400") || diag.contains("INVALID_REQUEST", ignoreCase = true) ->
                    InvalidRequest(diag)

                error.message.contains("parse", ignoreCase = true) ||
                    error.message.contains("JSON", ignoreCase = true) ->
                    InvalidResponse(diag)

                else ->
                    ServerError(diag)
            }
        }
    }
}

