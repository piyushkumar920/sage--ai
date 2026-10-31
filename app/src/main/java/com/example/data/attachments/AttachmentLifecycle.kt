package com.example.data.attachments

/**
 * Universal Study Attachments - Lifecycle Statuses
 *
 * Enforces strict sequential transitions:
 * LOCAL_SELECTED -> VALIDATING -> READY_TO_UPLOAD -> UPLOADING -> PROCESSING -> PROCESSED -> ATTACHED
 * Failure states: VALIDATION_FAILED, UPLOAD_FAILED, PROCESSING_FAILED, CANCELLED
 * Deletion states: ACTIVE -> DELETE_REQUESTED -> DELETED
 */
enum class AttachmentUploadStatus {
    LOCAL_SELECTED,
    VALIDATING,
    READY_TO_UPLOAD,
    UPLOADING,
    PROCESSING,
    PROCESSED,
    ATTACHED,
    VALIDATION_FAILED,
    UPLOAD_FAILED,
    PROCESSING_FAILED,
    CANCELLED
}

enum class AttachmentProcessingStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}

enum class AttachmentDeletionStatus {
    ACTIVE,
    DELETE_REQUESTED,
    DELETED
}

enum class AttachmentContextType {
    CHAT_CONVERSATION,
    PERSONAL_ROADMAP,
    ROADMAP_TOPIC,
    ACADEMIC_TOPIC
}

object AttachmentLifecycleManager {

    /**
     * Checks if transitioning from [current] to [next] is legally permitted in the state machine.
     * Prevents invalid leaps (e.g. LOCAL_SELECTED directly to ATTACHED without validation).
     */
    fun isValidTransition(current: AttachmentUploadStatus, next: AttachmentUploadStatus): Boolean {
        if (current == next) return true

        return when (current) {
            AttachmentUploadStatus.LOCAL_SELECTED -> {
                next == AttachmentUploadStatus.VALIDATING ||
                next == AttachmentUploadStatus.CANCELLED
            }
            AttachmentUploadStatus.VALIDATING -> {
                next == AttachmentUploadStatus.READY_TO_UPLOAD ||
                next == AttachmentUploadStatus.VALIDATION_FAILED ||
                next == AttachmentUploadStatus.CANCELLED
            }
            AttachmentUploadStatus.READY_TO_UPLOAD -> {
                next == AttachmentUploadStatus.UPLOADING ||
                next == AttachmentUploadStatus.CANCELLED
            }
            AttachmentUploadStatus.UPLOADING -> {
                next == AttachmentUploadStatus.PROCESSING ||
                next == AttachmentUploadStatus.UPLOAD_FAILED ||
                next == AttachmentUploadStatus.CANCELLED
            }
            AttachmentUploadStatus.PROCESSING -> {
                next == AttachmentUploadStatus.PROCESSED ||
                next == AttachmentUploadStatus.PROCESSING_FAILED ||
                next == AttachmentUploadStatus.CANCELLED
            }
            AttachmentUploadStatus.PROCESSED -> {
                next == AttachmentUploadStatus.ATTACHED ||
                next == AttachmentUploadStatus.CANCELLED
            }
            AttachmentUploadStatus.ATTACHED -> {
                // Attached documents can be re-processed if needed or cancelled
                next == AttachmentUploadStatus.PROCESSING ||
                next == AttachmentUploadStatus.CANCELLED
            }
            AttachmentUploadStatus.VALIDATION_FAILED,
            AttachmentUploadStatus.UPLOAD_FAILED,
            AttachmentUploadStatus.PROCESSING_FAILED,
            AttachmentUploadStatus.CANCELLED -> {
                // Retryable states can reset back to VALIDATING or READY_TO_UPLOAD
                next == AttachmentUploadStatus.VALIDATING ||
                next == AttachmentUploadStatus.READY_TO_UPLOAD ||
                next == AttachmentUploadStatus.UPLOADING
            }
        }
    }

    /**
     * Confirms whether the attachment is in a usable state for AI context or study sessions.
     * Documents MUST NOT be used until they have been fully processed and attached.
     */
    fun isAttachmentUsable(status: AttachmentUploadStatus): Boolean {
        return status == AttachmentUploadStatus.ATTACHED || status == AttachmentUploadStatus.PROCESSED
    }

    /**
     * Confirms whether the state allows cancellation.
     */
    fun canCancel(status: AttachmentUploadStatus): Boolean {
        return status != AttachmentUploadStatus.ATTACHED &&
               status != AttachmentUploadStatus.CANCELLED
    }

    /**
     * Confirms whether the state is retryable.
     */
    fun isRetryable(status: AttachmentUploadStatus): Boolean {
        return status == AttachmentUploadStatus.VALIDATION_FAILED ||
               status == AttachmentUploadStatus.UPLOAD_FAILED ||
               status == AttachmentUploadStatus.PROCESSING_FAILED ||
               status == AttachmentUploadStatus.CANCELLED
    }
}
