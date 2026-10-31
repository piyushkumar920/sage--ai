package com.example

import com.example.data.attachments.AttachmentContext
import com.example.data.attachments.AttachmentContextType
import com.example.data.attachments.AttachmentLifecycleManager
import com.example.data.attachments.AttachmentProcessingStatus
import com.example.data.attachments.AttachmentUploadStatus
import com.example.data.attachments.StudyAttachmentEntity
import com.example.data.attachments.StudyAttachmentRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StudyAttachmentStage1Test {

    @Test
    fun testValidLifecycleTransitions() {
        // Sequential upload lifecycle
        assertTrue(
            AttachmentLifecycleManager.isValidTransition(
                current = AttachmentUploadStatus.LOCAL_SELECTED,
                next = AttachmentUploadStatus.VALIDATING
            )
        )
        assertTrue(
            AttachmentLifecycleManager.isValidTransition(
                current = AttachmentUploadStatus.VALIDATING,
                next = AttachmentUploadStatus.READY_TO_UPLOAD
            )
        )
        assertTrue(
            AttachmentLifecycleManager.isValidTransition(
                current = AttachmentUploadStatus.READY_TO_UPLOAD,
                next = AttachmentUploadStatus.UPLOADING
            )
        )
        assertTrue(
            AttachmentLifecycleManager.isValidTransition(
                current = AttachmentUploadStatus.UPLOADING,
                next = AttachmentUploadStatus.PROCESSING
            )
        )
        assertTrue(
            AttachmentLifecycleManager.isValidTransition(
                current = AttachmentUploadStatus.PROCESSING,
                next = AttachmentUploadStatus.PROCESSED
            )
        )
        assertTrue(
            AttachmentLifecycleManager.isValidTransition(
                current = AttachmentUploadStatus.PROCESSED,
                next = AttachmentUploadStatus.ATTACHED
            )
        )

        // Retryable transitions from failures
        assertTrue(
            AttachmentLifecycleManager.isValidTransition(
                current = AttachmentUploadStatus.UPLOAD_FAILED,
                next = AttachmentUploadStatus.READY_TO_UPLOAD
            )
        )
        assertTrue(
            AttachmentLifecycleManager.isValidTransition(
                current = AttachmentUploadStatus.PROCESSING_FAILED,
                next = AttachmentUploadStatus.VALIDATING
            )
        )
        assertTrue(
            AttachmentLifecycleManager.isValidTransition(
                current = AttachmentUploadStatus.VALIDATION_FAILED,
                next = AttachmentUploadStatus.VALIDATING
            )
        )
    }

    @Test
    fun testInvalidLifecycleTransitions() {
        // Cannot leap directly from LOCAL_SELECTED to ATTACHED
        assertFalse(
            AttachmentLifecycleManager.isValidTransition(
                current = AttachmentUploadStatus.LOCAL_SELECTED,
                next = AttachmentUploadStatus.ATTACHED
            )
        )
        // Cannot leap directly from LOCAL_SELECTED to UPLOADING without VALIDATING
        assertFalse(
            AttachmentLifecycleManager.isValidTransition(
                current = AttachmentUploadStatus.LOCAL_SELECTED,
                next = AttachmentUploadStatus.UPLOADING
            )
        )
    }

    @Test
    fun testUsabilityRules() {
        // Documents are only usable when fully ATTACHED or PROCESSED
        assertTrue(AttachmentLifecycleManager.isAttachmentUsable(AttachmentUploadStatus.ATTACHED))
        assertTrue(AttachmentLifecycleManager.isAttachmentUsable(AttachmentUploadStatus.PROCESSED))

        // In-progress or failed states are not usable
        assertFalse(AttachmentLifecycleManager.isAttachmentUsable(AttachmentUploadStatus.LOCAL_SELECTED))
        assertFalse(AttachmentLifecycleManager.isAttachmentUsable(AttachmentUploadStatus.VALIDATING))
        assertFalse(AttachmentLifecycleManager.isAttachmentUsable(AttachmentUploadStatus.READY_TO_UPLOAD))
        assertFalse(AttachmentLifecycleManager.isAttachmentUsable(AttachmentUploadStatus.UPLOADING))
        assertFalse(AttachmentLifecycleManager.isAttachmentUsable(AttachmentUploadStatus.PROCESSING))
        assertFalse(AttachmentLifecycleManager.isAttachmentUsable(AttachmentUploadStatus.PROCESSING_FAILED))
        assertFalse(AttachmentLifecycleManager.isAttachmentUsable(AttachmentUploadStatus.UPLOAD_FAILED))
        assertFalse(AttachmentLifecycleManager.isAttachmentUsable(AttachmentUploadStatus.CANCELLED))
    }

    @Test
    fun testRetryAndCancellation() {
        // Failed states are retryable
        assertTrue(AttachmentLifecycleManager.isRetryable(AttachmentUploadStatus.VALIDATION_FAILED))
        assertTrue(AttachmentLifecycleManager.isRetryable(AttachmentUploadStatus.UPLOAD_FAILED))
        assertTrue(AttachmentLifecycleManager.isRetryable(AttachmentUploadStatus.PROCESSING_FAILED))
        assertTrue(AttachmentLifecycleManager.isRetryable(AttachmentUploadStatus.CANCELLED))

        // Normal states are not retryable
        assertFalse(AttachmentLifecycleManager.isRetryable(AttachmentUploadStatus.ATTACHED))
        assertFalse(AttachmentLifecycleManager.isRetryable(AttachmentUploadStatus.PROCESSED))

        // Cancellation checks
        assertTrue(AttachmentLifecycleManager.canCancel(AttachmentUploadStatus.UPLOADING))
        assertTrue(AttachmentLifecycleManager.canCancel(AttachmentUploadStatus.PROCESSING))
        assertFalse(AttachmentLifecycleManager.canCancel(AttachmentUploadStatus.ATTACHED))
    }

    @Test
    fun testContextIsolationKeys() {
        val chatContext1 = AttachmentContext(
            contextType = AttachmentContextType.CHAT_CONVERSATION,
            contextId = "conv_123",
            ownerUid = "user_abc"
        )
        val chatContext2 = AttachmentContext(
            contextType = AttachmentContextType.CHAT_CONVERSATION,
            contextId = "conv_456",
            ownerUid = "user_abc"
        )
        val roadmapContext = AttachmentContext(
            contextType = AttachmentContextType.PERSONAL_ROADMAP,
            contextId = "conv_123", // same ID but different contextType
            ownerUid = "user_abc"
        )
        val userBContext = AttachmentContext(
            contextType = AttachmentContextType.CHAT_CONVERSATION,
            contextId = "conv_123",
            ownerUid = "user_xyz" // different user
        )

        assertEquals("user_abc_CHAT_CONVERSATION_conv_123", chatContext1.uniqueKey)
        assertEquals("user_abc_CHAT_CONVERSATION_conv_456", chatContext2.uniqueKey)
        assertEquals("user_abc_PERSONAL_ROADMAP_conv_123", roadmapContext.uniqueKey)

        // Strict isolation
        assertFalse(chatContext1.uniqueKey == chatContext2.uniqueKey)
        assertFalse(chatContext1.uniqueKey == roadmapContext.uniqueKey)
        assertFalse(chatContext1.uniqueKey == userBContext.uniqueKey)
    }

    @Test
    fun testFileValidationSignaturesAndMimeTypes() {
        // PDF with magic bytes %PDF
        val pdfBytes = "%PDF-1.4 sample study material".toByteArray()
        val pdfResult = StudyAttachmentRepository.validateBytes(
            bytes = pdfBytes,
            fileName = "lecture_notes.pdf",
            declaredMimeType = "application/pdf"
        )
        assertTrue(pdfResult.isValid)
        assertEquals("application/pdf", pdfResult.detectedMimeType)

        // PNG with correct magic bytes
        val pngBytes = byteArrayOf(0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte(), 0x0D, 0x0A, 0x1A, 0x0A)
        val pngResult = StudyAttachmentRepository.validateBytes(
            bytes = pngBytes,
            fileName = "diagram.png",
            declaredMimeType = "image/png"
        )
        assertTrue(pngResult.isValid)
        assertEquals("image/png", pngResult.detectedMimeType)

        // JPEG with SOI marker
        val jpegBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 0x00, 0x10)
        val jpegResult = StudyAttachmentRepository.validateBytes(
            bytes = jpegBytes,
            fileName = "photo.jpg",
            declaredMimeType = "image/jpeg"
        )
        assertTrue(jpegResult.isValid)
        assertEquals("image/jpeg", jpegResult.detectedMimeType)

        // TXT with valid UTF-8
        val txtBytes = "Introduction to Algorithms\nChapter 1".toByteArray(Charsets.UTF_8)
        val txtResult = StudyAttachmentRepository.validateBytes(
            bytes = txtBytes,
            fileName = "syllabus.txt",
            declaredMimeType = "text/plain"
        )
        assertTrue(txtResult.isValid)
        assertEquals("text/plain", txtResult.detectedMimeType)

        // DOCX with PK header
        val zipBytes = byteArrayOf(0x50, 0x4B, 0x03, 0x04, 0x14, 0x00)
        val docxResult = StudyAttachmentRepository.validateBytes(
            bytes = zipBytes,
            fileName = "notes.docx",
            declaredMimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        )
        assertTrue(docxResult.isValid)

        // Empty file rejection
        val emptyResult = StudyAttachmentRepository.validateBytes(
            bytes = ByteArray(0),
            fileName = "empty.pdf",
            declaredMimeType = "application/pdf"
        )
        assertFalse(emptyResult.isValid)
        assertEquals("File is empty (0 bytes)", emptyResult.errorMessage)

        // Corrupted signature
        val fakePdfBytes = "Invalid fake pdf header".toByteArray()
        val fakePdfResult = StudyAttachmentRepository.validateBytes(
            bytes = fakePdfBytes,
            fileName = "evil.pdf",
            declaredMimeType = "application/pdf"
        )
        assertFalse(fakePdfResult.isValid)

        // Unsupported format rejection
        val exeBytes = byteArrayOf(0x4D, 0x5A, 0x90.toByte(), 0x00)
        val exeResult = StudyAttachmentRepository.validateBytes(
            bytes = exeBytes,
            fileName = "program.exe",
            declaredMimeType = "application/x-msdownload"
        )
        assertFalse(exeResult.isValid)
        assertTrue(exeResult.errorMessage?.contains("Unsupported format") == true)
    }

    @Test
    fun testFileValidationSizeLimit() {
        val maxLimit = 15 * 1024 * 1024 // 15MB
        assertEquals(15 * 1024 * 1024L, StudyAttachmentRepository.MAX_FILE_SIZE_BYTES)

        // Over-limit file
        val overLimitSize = maxLimit + 1024L
        val sizeCheck = StudyAttachmentRepository.checkFileSize(overLimitSize)
        assertFalse(sizeCheck.isValid)
        assertTrue(sizeCheck.errorMessage?.contains("15MB") == true)

        // Within limit
        val withinLimitSize = maxLimit.toLong()
        val validSizeCheck = StudyAttachmentRepository.checkFileSize(withinLimitSize)
        assertTrue(validSizeCheck.isValid)
    }

    @Test
    fun testReferenceSafeAttachmentEntity() {
        val entity = StudyAttachmentEntity(
            attachmentId = "att_test_1",
            ownerUid = "user_123",
            documentId = "doc_shared_999",
            contextType = AttachmentContextType.CHAT_CONVERSATION.name,
            contextId = "conv_1",
            originalFileName = "physics.pdf",
            mimeType = "application/pdf",
            fileSize = 1024L,
            uploadStatus = AttachmentUploadStatus.ATTACHED.name,
            processingStatus = AttachmentProcessingStatus.COMPLETED.name,
            isDeleted = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        assertNotNull(entity)
        assertEquals("doc_shared_999", entity.documentId)
        assertFalse(entity.isDeleted)

        // Soft deletion marks isDeleted = true while keeping documentId intact
        val softDeleted = entity.copy(
            isDeleted = true,
            updatedAt = System.currentTimeMillis()
        )
        assertTrue(softDeleted.isDeleted)
        assertEquals("doc_shared_999", softDeleted.documentId)

        // Firestore DTO conversion preserves isolation metadata
        val firestoreDto = softDeleted.toFirestoreModel()
        assertEquals(softDeleted.attachmentId, firestoreDto.attachmentId)
        assertEquals(softDeleted.ownerUid, firestoreDto.ownerUid)
        assertEquals(softDeleted.documentId, firestoreDto.documentId)
        assertTrue(firestoreDto.isDeleted)
    }
}
