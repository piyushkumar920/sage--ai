package com.example.data.attachments

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Defines the strict context boundary for an attachment.
 * Attachments are strictly isolated by contextType and contextId for an ownerUid.
 */
data class AttachmentContext(
    val contextType: AttachmentContextType,
    val contextId: String,
    val ownerUid: String
) {
    init {
        require(contextId.isNotBlank()) { "contextId must not be blank" }
        require(ownerUid.isNotBlank()) { "ownerUid must not be blank" }
    }

    val uniqueKey: String
        get() = "${ownerUid}_${contextType.name}_$contextId"
}

data class AttachmentValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val errorCode: String? = null,
    val detectedMimeType: String? = null,
    val fileSizeBytes: Long = 0L
)

@JsonClass(generateAdapter = true)
data class AttachmentProcessingResult(
    @field:Json(name = "attachmentId") val attachmentId: String,
    @field:Json(name = "documentId") val documentId: String,
    @field:Json(name = "extractionId") val extractionId: String? = null,
    @field:Json(name = "processingStatus") val processingStatus: String = "COMPLETED",
    @field:Json(name = "extractedTextAvailability") val extractedTextAvailability: Boolean = false,
    @field:Json(name = "extractionSummary") val extractionSummary: String? = null,
    @field:Json(name = "warnings") val warnings: List<String> = emptyList(),
    @field:Json(name = "errorCode") val errorCode: String? = null
)

// --- Retrofit & Moshi DTOs for Backend Communication ---

@JsonClass(generateAdapter = true)
data class AttachmentUploadRequest(
    @field:Json(name = "documentName") val documentName: String,
    @field:Json(name = "mimeType") val mimeType: String,
    @field:Json(name = "fileBase64") val fileBase64: String,
    @field:Json(name = "textContent") val textContent: String = "",
    @field:Json(name = "contextType") val contextType: String,
    @field:Json(name = "contextId") val contextId: String
)

@JsonClass(generateAdapter = true)
data class AttachmentUploadResponse(
    @field:Json(name = "success") val success: Boolean,
    @field:Json(name = "attachmentId") val attachmentId: String? = null,
    @field:Json(name = "documentId") val documentId: String? = null,
    @field:Json(name = "extractionId") val extractionId: String? = null,
    @field:Json(name = "status") val status: String? = null,
    @field:Json(name = "summary") val summary: String? = null,
    @field:Json(name = "error") val error: String? = null,
    @field:Json(name = "code") val code: String? = null
)

@JsonClass(generateAdapter = true)
data class AttachmentAttachRequest(
    @field:Json(name = "documentId") val documentId: String,
    @field:Json(name = "contextType") val contextType: String,
    @field:Json(name = "contextId") val contextId: String
)

@JsonClass(generateAdapter = true)
data class AttachmentAttachResponse(
    @field:Json(name = "success") val success: Boolean,
    @field:Json(name = "attachmentId") val attachmentId: String? = null,
    @field:Json(name = "error") val error: String? = null
)

@JsonClass(generateAdapter = true)
data class AttachmentItemDto(
    @field:Json(name = "attachmentId") val attachmentId: String,
    @field:Json(name = "documentId") val documentId: String,
    @field:Json(name = "ownerUid") val ownerUid: String,
    @field:Json(name = "contextType") val contextType: String,
    @field:Json(name = "contextId") val contextId: String,
    @field:Json(name = "originalFileName") val originalFileName: String,
    @field:Json(name = "mimeType") val mimeType: String,
    @field:Json(name = "fileSize") val fileSize: Long,
    @field:Json(name = "uploadStatus") val uploadStatus: String,
    @field:Json(name = "processingStatus") val processingStatus: String,
    @field:Json(name = "extractionId") val extractionId: String? = null,
    @field:Json(name = "summary") val summary: String? = null,
    @field:Json(name = "createdAt") val createdAt: Long,
    @field:Json(name = "updatedAt") val updatedAt: Long
)

@JsonClass(generateAdapter = true)
data class AttachmentListResponse(
    @field:Json(name = "success") val success: Boolean,
    @field:Json(name = "attachments") val attachments: List<AttachmentItemDto> = emptyList(),
    @field:Json(name = "error") val error: String? = null
)
