package com.example.data.documents

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "uploaded_documents")
data class UploadedDocumentEntity(
    @PrimaryKey
    val documentId: String,
    val userId: String = "local_user",
    val fileName: String,
    val mimeType: String,
    val fileSizeBytes: Long,
    val fileSignature: String,
    val uploadedAt: Long = System.currentTimeMillis(),
    val validationStatus: String = "VALIDATED",
    val syncStatus: String = "LOCAL_OFFLINE"
)

@Entity(tableName = "document_extractions")
data class DocumentExtractionEntity(
    @PrimaryKey
    val extractionId: String,
    val documentId: String,
    val confidence: String,
    val structuredCurriculumJson: String,
    val extractedAt: Long = System.currentTimeMillis(),
    val verificationStatus: String = "PENDING_REVIEW"
)

@Entity(tableName = "document_grounded_roadmaps")
data class DocumentGroundedRoadmapEntity(
    @PrimaryKey
    val roadmapId: String,
    val documentId: String,
    val extractionId: String,
    val status: String = "DRAFT", // DRAFT, REVIEW, CONFIRMED, ACTIVE
    val groundedConfidence: String = "HIGH",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
