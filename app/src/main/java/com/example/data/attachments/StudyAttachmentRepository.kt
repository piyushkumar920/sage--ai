package com.example.data.attachments

import android.content.Context
import android.util.Log
import com.example.data.api.GeminiClient
import com.example.data.api.GeminiResult
import com.example.data.auth.FirebaseAuthRepository
import com.example.data.documents.UploadedDocumentEntity
import com.example.data.local.SageDao
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

class StudyAttachmentRepository(
    private val context: Context,
    private val dao: SageDao,
    private val geminiClient: GeminiClient,
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "StudyAttachmentRepo"
        const val MAX_FILE_SIZE_BYTES = 15L * 1024 * 1024 // 15MB
        val ALLOWED_MIME_TYPES = setOf(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png",
            "text/plain",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        )
        fun checkFileSize(sizeBytes: Long): AttachmentValidationResult {
            if (sizeBytes <= 0) {
                return AttachmentValidationResult(false, "File is empty (0 bytes)", "EMPTY_FILE")
            }
            if (sizeBytes > MAX_FILE_SIZE_BYTES) {
                val sizeMb = sizeBytes / (1024 * 1024)
                return AttachmentValidationResult(
                    false,
                    "File size (${sizeMb}MB) exceeds the 15MB limit.",
                    "FILE_TOO_LARGE",
                    fileSizeBytes = sizeBytes
                )
            }
            return AttachmentValidationResult(isValid = true, fileSizeBytes = sizeBytes)
        }

        fun validateBytes(
            bytes: ByteArray,
            fileName: String,
            declaredMimeType: String
        ): AttachmentValidationResult {
            if (fileName.isBlank()) {
                return AttachmentValidationResult(false, "Filename cannot be blank.", "EMPTY_FILENAME")
            }
            val sizeCheck = checkFileSize(bytes.size.toLong())
            if (!sizeCheck.isValid) return sizeCheck

            val lowerMime = declaredMimeType.trim().lowercase()
            if (!ALLOWED_MIME_TYPES.contains(lowerMime)) {
                return AttachmentValidationResult(
                    false,
                    "Unsupported format ($declaredMimeType). Allowed: PDF, JPG, PNG, TXT, DOCX, PPTX.",
                    "UNSUPPORTED_FORMAT",
                    declaredMimeType,
                    bytes.size.toLong()
                )
            }

            if (bytes.size >= 4) {
                val hex = bytes.take(4).joinToString("") { "%02x".format(it) }
                val signatureValid = when {
                    lowerMime == "application/pdf" -> hex == "25504446"
                    lowerMime == "image/png" -> hex == "89504e47"
                    lowerMime == "image/jpeg" || lowerMime == "image/jpg" -> hex.startsWith("ffd8")
                    lowerMime.contains("openxmlformats") || lowerMime.contains("wordprocessingml") || lowerMime.contains("presentationml") -> hex == "504b0304"
                    lowerMime == "text/plain" -> true
                    else -> false
                }

                if (!signatureValid) {
                    return AttachmentValidationResult(
                        false,
                        "File signature does not match claimed format ($declaredMimeType). File appears corrupted or invalid header.",
                        "INVALID_FILE_SIGNATURE",
                        lowerMime,
                        bytes.size.toLong()
                    )
                }
            }

            return AttachmentValidationResult(
                isValid = true,
                detectedMimeType = lowerMime,
                fileSizeBytes = bytes.size.toLong()
            )
        }
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // --- 1. VALIDATION ---

    fun validateDocument(
        fileName: String,
        mimeType: String,
        sizeBytes: Long,
        firstBytes: ByteArray? = null
    ): AttachmentValidationResult {
        if (fileName.isBlank()) {
            return AttachmentValidationResult(false, "Filename cannot be blank.", "EMPTY_FILENAME")
        }
        val sizeCheck = checkFileSize(sizeBytes)
        if (!sizeCheck.isValid) return sizeCheck

        val lowerMime = mimeType.trim().lowercase()
        if (!ALLOWED_MIME_TYPES.contains(lowerMime)) {
            return AttachmentValidationResult(
                false,
                "Unsupported format ($mimeType). Allowed: PDF, JPG, PNG, TXT, DOCX, PPTX.",
                "UNSUPPORTED_FORMAT",
                mimeType,
                sizeBytes
            )
        }

        if (firstBytes != null && firstBytes.size >= 4) {
            val hex = firstBytes.take(4).joinToString("") { "%02x".format(it) }
            val signatureValid = when {
                lowerMime == "application/pdf" -> hex == "25504446"
                lowerMime == "image/png" -> hex == "89504e47"
                lowerMime == "image/jpeg" || lowerMime == "image/jpg" -> hex.startsWith("ffd8")
                lowerMime.contains("openxmlformats") || lowerMime.contains("wordprocessingml") || lowerMime.contains("presentationml") -> hex == "504b0304"
                lowerMime == "text/plain" -> true
                else -> false
            }

            if (!signatureValid) {
                return AttachmentValidationResult(
                    false,
                    "File signature does not match claimed format ($mimeType). File appears corrupted or invalid header.",
                    "INVALID_FILE_SIGNATURE",
                    mimeType,
                    sizeBytes
                )
            }
        }

        return AttachmentValidationResult(
            isValid = true,
            detectedMimeType = lowerMime,
            fileSizeBytes = sizeBytes
        )
    }

    // --- 2. DRAFT CREATION & CONTEXT BINDING ---

    suspend fun createAttachmentDraft(
        context: AttachmentContext,
        fileName: String,
        mimeType: String,
        fileSize: Long,
        localUri: String? = null,
        documentId: String = "doc_${UUID.randomUUID()}"
    ): StudyAttachmentEntity = withContext(Dispatchers.IO) {
        val attachmentId = "att_${UUID.randomUUID()}"
        val entity = StudyAttachmentEntity(
            attachmentId = attachmentId,
            ownerUid = context.ownerUid,
            documentId = documentId,
            contextType = context.contextType.name,
            contextId = context.contextId,
            originalFileName = fileName,
            mimeType = mimeType,
            fileSize = fileSize,
            uploadStatus = AttachmentUploadStatus.LOCAL_SELECTED.name,
            processingStatus = AttachmentProcessingStatus.PENDING.name,
            localUri = localUri,
            isDeleted = false,
            syncStatus = "LOCAL_ONLY"
        )

        // Save underlying uploaded document record if not present
        val existingDoc = dao.getUploadedDocumentById(documentId)
        if (existingDoc == null) {
            dao.insertUploadedDocument(
                UploadedDocumentEntity(
                    documentId = documentId,
                    userId = context.ownerUid,
                    fileName = fileName,
                    mimeType = mimeType,
                    fileSizeBytes = fileSize,
                    fileSignature = mimeType,
                    validationStatus = "VALIDATED",
                    syncStatus = "LOCAL_OFFLINE"
                )
            )
        }

        dao.insertAttachment(entity)
        entity
    }

    // --- 3. LIFECYCLE PROGRESSION & UPLOAD PIPELINE ---

    suspend fun uploadAndProcessAttachment(
        attachmentId: String,
        fileBase64: String,
        textContent: String = "",
        onProgress: (AttachmentUploadStatus) -> Unit = {}
    ): GeminiResult<AttachmentProcessingResult> = withContext(Dispatchers.IO) {
        val attachment = dao.getAttachmentById(attachmentId)
            ?: return@withContext GeminiResult.Error("Attachment not found: $attachmentId")

        try {
            // Step 1: VALIDATING
            dao.updateAttachmentStatus(attachmentId, AttachmentUploadStatus.VALIDATING.name, AttachmentProcessingStatus.PENDING.name)
            onProgress(AttachmentUploadStatus.VALIDATING)

            val validation = validateDocument(attachment.originalFileName, attachment.mimeType, attachment.fileSize)
            if (!validation.isValid) {
                dao.updateAttachmentStatus(attachmentId, AttachmentUploadStatus.VALIDATION_FAILED.name, AttachmentProcessingStatus.FAILED.name)
                onProgress(AttachmentUploadStatus.VALIDATION_FAILED)
                return@withContext GeminiResult.Error(validation.errorMessage ?: "Validation failed", diagnosticMessage = validation.errorCode ?: "")
            }

            // Step 2: READY_TO_UPLOAD
            dao.updateAttachmentStatus(attachmentId, AttachmentUploadStatus.READY_TO_UPLOAD.name, AttachmentProcessingStatus.PENDING.name)
            onProgress(AttachmentUploadStatus.READY_TO_UPLOAD)

            // Step 3: UPLOADING
            dao.updateAttachmentStatus(attachmentId, AttachmentUploadStatus.UPLOADING.name, AttachmentProcessingStatus.PROCESSING.name)
            onProgress(AttachmentUploadStatus.UPLOADING)

            // Fetch Firebase ID Token for server verification
            val idToken = authRepository.getIdToken(forceRefresh = false)

            val baseUrl = geminiClient.getEffectiveBackendUrl()
            val url = "${baseUrl}api/attachments/upload"

            val uploadRequest = AttachmentUploadRequest(
                documentName = attachment.originalFileName,
                mimeType = attachment.mimeType,
                fileBase64 = fileBase64,
                textContent = textContent,
                contextType = attachment.contextType,
                contextId = attachment.contextId
            )

            val jsonAdapter = moshi.adapter(AttachmentUploadRequest::class.java)
            val jsonBody = jsonAdapter.toJson(uploadRequest)
            val body = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

            val requestBuilder = Request.Builder()
                .url(url)
                .post(body)

            if (!idToken.isNullOrBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $idToken")
            }
            requestBuilder.addHeader("x-user-uid", attachment.ownerUid)

            // Step 4: PROCESSING
            dao.updateAttachmentStatus(attachmentId, AttachmentUploadStatus.PROCESSING.name, AttachmentProcessingStatus.PROCESSING.name)
            onProgress(AttachmentUploadStatus.PROCESSING)

            httpClient.newCall(requestBuilder.build()).execute().use { response ->
                val responseString = response.body?.string() ?: ""
                if (response.isSuccessful && responseString.isNotBlank()) {
                    val respAdapter = moshi.adapter(AttachmentUploadResponse::class.java)
                    val parsed = respAdapter.fromJson(responseString)

                    if (parsed != null && parsed.success) {
                        // Step 5 & 6: PROCESSED -> ATTACHED
                        val updated = attachment.copy(
                            uploadStatus = AttachmentUploadStatus.ATTACHED.name,
                            processingStatus = AttachmentProcessingStatus.COMPLETED.name,
                            documentId = parsed.documentId ?: attachment.documentId,
                            extractionId = parsed.extractionId,
                            extractedSummary = parsed.summary,
                            updatedAt = System.currentTimeMillis()
                        )
                        dao.updateAttachment(updated)
                        onProgress(AttachmentUploadStatus.ATTACHED)

                        // Fire-and-forget sync to Firestore
                        syncAttachmentToFirestore(updated)

                        val result = AttachmentProcessingResult(
                            attachmentId = attachmentId,
                            documentId = updated.documentId,
                            extractionId = parsed.extractionId,
                            processingStatus = "COMPLETED",
                            extractedTextAvailability = !parsed.summary.isNullOrBlank(),
                            extractionSummary = parsed.summary
                        )
                        GeminiResult.Success(result)
                    } else {
                        dao.updateAttachmentStatus(attachmentId, AttachmentUploadStatus.PROCESSING_FAILED.name, AttachmentProcessingStatus.FAILED.name)
                        onProgress(AttachmentUploadStatus.PROCESSING_FAILED)
                        GeminiResult.Error(parsed?.error ?: "Document processing failed on backend.")
                    }
                } else {
                    dao.updateAttachmentStatus(attachmentId, AttachmentUploadStatus.UPLOAD_FAILED.name, AttachmentProcessingStatus.FAILED.name)
                    onProgress(AttachmentUploadStatus.UPLOAD_FAILED)
                    GeminiResult.Error("Server returned error HTTP ${response.code}: $responseString")
                }
            }
        } catch (e: CancellationException) {
            dao.updateAttachmentStatus(attachmentId, AttachmentUploadStatus.CANCELLED.name, AttachmentProcessingStatus.FAILED.name)
            onProgress(AttachmentUploadStatus.CANCELLED)
            throw e
        } catch (e: Exception) {
            dao.updateAttachmentStatus(attachmentId, AttachmentUploadStatus.UPLOAD_FAILED.name, AttachmentProcessingStatus.FAILED.name)
            onProgress(AttachmentUploadStatus.UPLOAD_FAILED)
            GeminiResult.Error("Network error during attachment upload: ${e.message}", isNetworkError = true)
        }
    }

    // --- 4. ATTACH EXISTING DOCUMENT TO NEW CONTEXT ---

    suspend fun attachExistingDocument(
        sourceDocumentId: String,
        targetContext: AttachmentContext
    ): GeminiResult<StudyAttachmentEntity> = withContext(Dispatchers.IO) {
        val existingDoc = dao.getUploadedDocumentById(sourceDocumentId)
            ?: return@withContext GeminiResult.Error("Source document does not exist: $sourceDocumentId")

        val newAttachmentId = "att_${UUID.randomUUID()}"
        val newAttachment = StudyAttachmentEntity(
            attachmentId = newAttachmentId,
            ownerUid = targetContext.ownerUid,
            documentId = sourceDocumentId,
            contextType = targetContext.contextType.name,
            contextId = targetContext.contextId,
            originalFileName = existingDoc.fileName,
            mimeType = existingDoc.mimeType,
            fileSize = existingDoc.fileSizeBytes,
            uploadStatus = AttachmentUploadStatus.ATTACHED.name,
            processingStatus = AttachmentProcessingStatus.COMPLETED.name,
            isDeleted = false,
            syncStatus = "LOCAL_ONLY"
        )

        dao.insertAttachment(newAttachment)
        syncAttachmentToFirestore(newAttachment)

        // Notify backend of context mapping
        try {
            val idToken = authRepository.getIdToken()
            val baseUrl = geminiClient.getEffectiveBackendUrl()
            val url = "${baseUrl}api/attachments/attach"

            val attachReq = AttachmentAttachRequest(
                documentId = sourceDocumentId,
                contextType = targetContext.contextType.name,
                contextId = targetContext.contextId
            )
            val jsonAdapter = moshi.adapter(AttachmentAttachRequest::class.java)
            val body = jsonAdapter.toJson(attachReq).toRequestBody("application/json; charset=utf-8".toMediaType())

            val req = Request.Builder()
                .url(url)
                .post(body)
                .apply {
                    if (!idToken.isNullOrBlank()) addHeader("Authorization", "Bearer $idToken")
                    addHeader("x-user-uid", targetContext.ownerUid)
                }
                .build()

            httpClient.newCall(req).execute().close()
        } catch (e: Exception) {
            Log.w(TAG, "Backend attach sync deferred (offline): ${e.message}")
        }

        GeminiResult.Success(newAttachment)
    }

    // --- 5. CONTEXT-SCOPED RETRIEVAL (FLOW) ---

    fun getAttachmentsForContext(context: AttachmentContext): Flow<List<StudyAttachmentEntity>> {
        return dao.getAttachmentsForContext(context.contextType.name, context.contextId)
    }

    suspend fun getAttachmentsForContextOnce(context: AttachmentContext): List<StudyAttachmentEntity> = withContext(Dispatchers.IO) {
        dao.getAttachmentsForContextOnce(context.contextType.name, context.contextId)
    }

    fun getAllAttachmentsForOwner(ownerUid: String): Flow<List<StudyAttachmentEntity>> {
        return dao.getAllAttachmentsForOwner(ownerUid)
    }

    suspend fun getAttachmentById(attachmentId: String): StudyAttachmentEntity? = withContext(Dispatchers.IO) {
        dao.getAttachmentById(attachmentId)
    }

    // --- 6. REFERENCE-AWARE DELETION ---

    /**
     * Removes attachment from current context.
     * Guaranteed: NEVER deletes the source document if another roadmap or conversation still references it!
     */
    suspend fun removeFromContext(attachmentId: String): Boolean = withContext(Dispatchers.IO) {
        val attachment = dao.getAttachmentById(attachmentId) ?: return@withContext false
        val documentId = attachment.documentId

        // Soft delete attachment for this context
        dao.softDeleteAttachment(attachmentId)

        // Sync deletion to Firestore
        try {
            if (authRepository.isUserAuthenticated) {
                val uid = attachment.ownerUid
                firestore.collection("users")
                    .document(uid)
                    .collection("attachments")
                    .document(attachmentId)
                    .update("isDeleted", true)
                    .await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore attachment soft delete deferred: ${e.message}")
        }

        // Notify backend
        notifyBackendDeletion(attachmentId, permanent = false)

        // Check if other contexts still reference this document
        val remainingReferences = dao.countActiveAttachmentsForDocument(documentId)
        if (remainingReferences == 0) {
            Log.d(TAG, "Document $documentId has no remaining context references. Safe to purge if requested.")
        }
        true
    }

    /**
     * Permanently purges document and all its attachments across all contexts for the owner.
     */
    suspend fun deletePermanently(documentId: String, ownerUid: String): Boolean = withContext(Dispatchers.IO) {
        dao.deleteUploadedDocument(documentId)

        // Notify backend of permanent deletion
        notifyBackendDeletion(documentId, permanent = true)
        true
    }

    /**
     * Permanently purges a single attachment by ID.
     */
    suspend fun deletePermanently(attachmentId: String): Boolean = withContext(Dispatchers.IO) {
        val attachment = dao.getAttachmentById(attachmentId)
        if (attachment != null) {
            dao.hardDeleteAttachment(attachmentId)
            notifyBackendDeletion(attachmentId, permanent = true)
            val remaining = dao.countActiveAttachmentsForDocument(attachment.documentId)
            if (remaining == 0) {
                dao.deleteUploadedDocument(attachment.documentId)
            }
        } else {
            notifyBackendDeletion(attachmentId, permanent = true)
        }
        true
    }

    // --- 7. FIRESTORE METADATA SYNCHRONIZATION ---

    suspend fun syncAttachmentToFirestore(attachment: StudyAttachmentEntity) = withContext(Dispatchers.IO) {
        if (!authRepository.isUserAuthenticated) return@withContext
        val uid = attachment.ownerUid
        try {
            val firestoreModel = attachment.toFirestoreModel()
            firestore.collection("users")
                .document(uid)
                .collection("attachments")
                .document(attachment.attachmentId)
                .set(firestoreModel)
                .await()

            dao.updateAttachment(attachment.copy(syncStatus = "SYNCED"))
        } catch (e: Exception) {
            Log.w(TAG, "Firestore attachment metadata sync error: ${e.message}")
        }
    }

    private suspend fun notifyBackendDeletion(id: String, permanent: Boolean) {
        try {
            val idToken = authRepository.getIdToken()
            val baseUrl = geminiClient.getEffectiveBackendUrl()
            val url = "${baseUrl}api/attachments/$id?permanent=$permanent"

            val req = Request.Builder()
                .url(url)
                .delete()
                .apply {
                    if (!idToken.isNullOrBlank()) addHeader("Authorization", "Bearer $idToken")
                }
                .build()

            httpClient.newCall(req).execute().close()
        } catch (e: Exception) {
            Log.w(TAG, "Backend delete notification deferred: ${e.message}")
        }
    }

    // --- 8. URI IMPORT & RE-TRY UTILITIES ---

    data class UriMetadata(
        val fileName: String,
        val mimeType: String,
        val sizeBytes: Long
    )

    fun readUriMetadata(uri: Uri): UriMetadata {
        var name = "attachment"
        var size = 0L
        var mime = context.contentResolver.getType(uri) ?: "application/octet-stream"

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
                    if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                }
            }
        } catch (_: Exception) {}

        // Fallback MIME detection by filename extension if generic
        if (mime == "application/octet-stream" || mime == "*/*") {
            val ext = name.substringAfterLast('.', "").lowercase()
            mime = when (ext) {
                "pdf" -> "application/pdf"
                "png" -> "image/png"
                "jpg", "jpeg" -> "image/jpeg"
                "txt" -> "text/plain"
                "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                else -> mime
            }
        }

        return UriMetadata(name, mime, size)
    }

    suspend fun addAttachmentFromUri(
        attachmentContext: AttachmentContext,
        uri: Uri,
        onProgress: (AttachmentUploadStatus) -> Unit = {}
    ): GeminiResult<StudyAttachmentEntity> = withContext(Dispatchers.IO) {
        try {
            val meta = readUriMetadata(uri)
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext GeminiResult.Error("Could not read file contents from system picker.")

            val effectiveSize = if (meta.sizeBytes > 0) meta.sizeBytes else bytes.size.toLong()
            val validation = validateBytes(bytes, meta.fileName, meta.mimeType)
            if (!validation.isValid) {
                return@withContext GeminiResult.Error(
                    message = validation.errorMessage ?: "Invalid document format or size.",
                    diagnosticMessage = validation.errorCode ?: "VALIDATION_FAILED"
                )
            }

            val draft = createAttachmentDraft(
                context = attachmentContext,
                fileName = meta.fileName,
                mimeType = validation.detectedMimeType ?: meta.mimeType,
                fileSize = effectiveSize,
                localUri = uri.toString()
            )

            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val uploadRes = uploadAndProcessAttachment(
                attachmentId = draft.attachmentId,
                fileBase64 = base64,
                onProgress = onProgress
            )

            when (uploadRes) {
                is GeminiResult.Success -> {
                    val updated = dao.getAttachmentById(draft.attachmentId) ?: draft
                    GeminiResult.Success(updated)
                }
                is GeminiResult.Error -> {
                    GeminiResult.Error(uploadRes.message, uploadRes.diagnosticMessage)
                }
            }
        } catch (e: Exception) {
            GeminiResult.Error("Failed to add attachment: ${e.message}")
        }
    }

    suspend fun retryAttachmentUpload(
        attachmentId: String,
        onProgress: (AttachmentUploadStatus) -> Unit = {}
    ): GeminiResult<StudyAttachmentEntity> = withContext(Dispatchers.IO) {
        val attachment = dao.getAttachmentById(attachmentId)
            ?: return@withContext GeminiResult.Error("Attachment not found: $attachmentId")

        val uriStr = attachment.localUri
            ?: return@withContext GeminiResult.Error("Original file URI not available to retry.")

        val uri = Uri.parse(uriStr)
        val bytes = try {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (_: Exception) {
            null
        } ?: return@withContext GeminiResult.Error("Could not access file to retry.")

        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        val uploadRes = uploadAndProcessAttachment(
            attachmentId = attachment.attachmentId,
            fileBase64 = base64,
            onProgress = onProgress
        )

        when (uploadRes) {
            is GeminiResult.Success -> {
                val updated = dao.getAttachmentById(attachmentId) ?: attachment
                GeminiResult.Success(updated)
            }
            is GeminiResult.Error -> {
                GeminiResult.Error(uploadRes.message, uploadRes.diagnosticMessage)
            }
        }
    }
}
