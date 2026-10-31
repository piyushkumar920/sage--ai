package com.example.data.documents

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DocumentExtractionRequest(
    val documentName: String,
    val mimeType: String,
    val fileBase64: String,
    val textContent: String = ""
)

@JsonClass(generateAdapter = true)
data class DocumentExtractionResponse(
    val success: Boolean,
    val documentId: String?,
    val extractionId: String?,
    val structuredData: StructuredCurriculumData?,
    val processingStatus: String?,
    val error: String?
)

@JsonClass(generateAdapter = true)
data class StructuredCurriculumData(
    val documentMetadata: DocumentMetadata?,
    val extractionSummary: ExtractionSummary?,
    val semesters: List<SemesterExtraction>?,
    val supplementarySuggestions: List<String>?,
    val verificationWarnings: List<String>?
)

@JsonClass(generateAdapter = true)
data class DocumentMetadata(
    val documentTitle: String?,
    val documentType: String?,
    val institution: String?,
    val department: String?,
    val programme: String?,
    val regulation: String?,
    val academicYear: String?,
    val totalPages: Int?
)

@JsonClass(generateAdapter = true)
data class ExtractionSummary(
    val status: String?,
    val confidence: String?, // HIGH, MEDIUM, LOW, INSUFFICIENT
    val warnings: List<String>?,
    val unreadableSections: List<String>?
)

@JsonClass(generateAdapter = true)
data class SemesterExtraction(
    val semesterNumber: Int,
    val subjects: List<SubjectExtraction>?
)

@JsonClass(generateAdapter = true)
data class SubjectExtraction(
    val subjectName: String,
    val subjectCode: String?,
    val credits: Int?,
    val modules: List<ModuleExtraction>?
)

@JsonClass(generateAdapter = true)
data class ModuleExtraction(
    val moduleNumber: Int,
    val moduleTitle: String,
    val topics: List<TopicExtraction>?
)

@JsonClass(generateAdapter = true)
data class TopicExtraction(
    val topicName: String,
    val subtopics: List<String>?,
    val learningObjectives: List<String>?,
    val sourceReferences: List<SourceReference>?,
    val confidence: String?
)

@JsonClass(generateAdapter = true)
data class SourceReference(
    val pageNumber: Int?,
    val sectionTitle: String?,
    val sourceType: String? // EXPLICIT, INFERRED, AI_SUPPLEMENTARY
)
