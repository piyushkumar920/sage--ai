package com.example.data.attachments

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Room Entity representing a Universal Study Attachment binding a document to an academic context.
 */
@Entity(
    tableName = "study_attachments",
    indices = [
        Index(value = ["contextType", "contextId", "isDeleted"]),
        Index(value = ["documentId"]),
        Index(value = ["ownerUid"]),
        Index(value = ["ownerUid", "contextType", "contextId"])
    ]
)
data class StudyAttachmentEntity(
    @PrimaryKey
    val attachmentId: String,
    val ownerUid: String,
    val documentId: String,
    val contextType: String, // CHAT_CONVERSATION, PERSONAL_ROADMAP, ROADMAP_TOPIC, ACADEMIC_TOPIC
    val contextId: String,
    val originalFileName: String,
    val mimeType: String,
    val fileSize: Long,
    val uploadStatus: String = AttachmentUploadStatus.LOCAL_SELECTED.name,
    val processingStatus: String = AttachmentProcessingStatus.PENDING.name,
    val extractionId: String? = null,
    val extractedSummary: String? = null,
    val localUri: String? = null,
    val isDeleted: Boolean = false,
    val syncStatus: String = "LOCAL_ONLY", // LOCAL_ONLY, SYNCED, PENDING_SYNC
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toFirestoreModel(): FirestoreStudyAttachment = FirestoreStudyAttachment(
        attachmentId = attachmentId,
        ownerUid = ownerUid,
        documentId = documentId,
        contextType = contextType,
        contextId = contextId,
        originalFileName = originalFileName,
        mimeType = mimeType,
        fileSize = fileSize,
        uploadStatus = uploadStatus,
        processingStatus = processingStatus,
        extractionId = extractionId,
        extractedSummary = extractedSummary,
        isDeleted = isDeleted,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Firestore DTO for metadata sync into users/{userId}/attachments/{attachmentId}.
 * Strictly metadata only — NO raw binary data is uploaded to Firestore.
 */
@IgnoreExtraProperties
data class FirestoreStudyAttachment(
    val attachmentId: String = "",
    val ownerUid: String = "",
    val documentId: String = "",
    val contextType: String = "",
    val contextId: String = "",
    val originalFileName: String = "",
    val mimeType: String = "",
    val fileSize: Long = 0L,
    val uploadStatus: String = "ATTACHED",
    val processingStatus: String = "COMPLETED",
    val extractionId: String? = null,
    val extractedSummary: String? = null,
    val isDeleted: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    fun toEntity(localUri: String? = null): StudyAttachmentEntity = StudyAttachmentEntity(
        attachmentId = attachmentId,
        ownerUid = ownerUid,
        documentId = documentId,
        contextType = contextType,
        contextId = contextId,
        originalFileName = originalFileName,
        mimeType = mimeType,
        fileSize = fileSize,
        uploadStatus = uploadStatus,
        processingStatus = processingStatus,
        extractionId = extractionId,
        extractedSummary = extractedSummary,
        localUri = localUri,
        isDeleted = isDeleted,
        syncStatus = "SYNCED",
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
