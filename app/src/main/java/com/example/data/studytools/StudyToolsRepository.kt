package com.example.data.studytools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.data.api.GeminiClient
import com.example.data.api.GeminiResult
import com.example.data.local.SageDao
import com.example.data.sync.FirestoreStudyToolItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID

class StudyToolsRepository(
    private val context: Context,
    private val dao: SageDao,
    private val geminiClient: GeminiClient,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val allSavedTools: Flow<List<SavedStudyToolEntity>> = dao.getAllSavedStudyTools()

    fun getSavedToolsByType(type: String): Flow<List<SavedStudyToolEntity>> {
        return dao.getSavedStudyToolsByType(type)
    }

    suspend fun getSavedToolById(id: String): SavedStudyToolEntity? {
        return dao.getSavedStudyToolById(id)
    }

    suspend fun saveStudyTool(tool: SavedStudyToolEntity) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateSavedStudyTool(tool)
        // Fire-and-forget sync to Firestore if user logged in
        syncToolToFirestore(tool)
    }

    suspend fun deleteStudyTool(id: String) = withContext(Dispatchers.IO) {
        dao.deleteSavedStudyTool(id)
        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                firestore.collection("users").document(uid)
                    .collection("studyTools").document(id)
                    .delete().await()
            } catch (_: Exception) {}
        }
    }

    private suspend fun syncToolToFirestore(tool: SavedStudyToolEntity) {
        val uid = auth.currentUser?.uid ?: return
        try {
            val firestoreItem = FirestoreStudyToolItem(
                id = tool.id,
                type = tool.type,
                title = tool.title,
                subject = tool.subject,
                topic = tool.topic,
                syllabusContext = tool.syllabusContext,
                contentJson = tool.contentJson,
                createdAt = tool.createdAt,
                updatedAt = tool.updatedAt
            )
            firestore.collection("users").document(uid)
                .collection("studyTools").document(tool.id)
                .set(firestoreItem, SetOptions.merge()).await()
        } catch (_: Exception) {
            // Offline or unauthenticated; safely ignore
        }
    }

    // ==========================================
    // STUDY TOOL GENERATION
    // ==========================================

    suspend fun generateNotes(
        topic: String,
        subject: String,
        syllabusContext: String = "",
        prompt: String = "",
        academicContext: AcademicContext? = null
    ): GeminiResult<StudyNotesData> = withContext(Dispatchers.IO) {
        val res = geminiClient.generateStudyTool(
            operation = "notes",
            topic = topic,
            subject = subject,
            syllabusContext = syllabusContext,
            prompt = prompt,
            academicContext = academicContext
        )
        when (res) {
            is GeminiResult.Success -> {
                try {
                    val adapter = moshi.adapter(StudyNotesData::class.java)
                    val data = adapter.fromJson(res.data)
                    if (data != null) GeminiResult.Success(data)
                    else GeminiResult.Error("Could not parse notes response.")
                } catch (e: Exception) {
                    GeminiResult.Error("Failed to parse notes: ${e.message}")
                }
            }
            is GeminiResult.Error -> res
        }
    }

    suspend fun generateFlashcards(
        topic: String,
        subject: String,
        syllabusContext: String = "",
        prompt: String = "",
        academicContext: AcademicContext? = null
    ): GeminiResult<StudyFlashcardsData> = withContext(Dispatchers.IO) {
        val res = geminiClient.generateStudyTool(
            operation = "flashcards",
            topic = topic,
            subject = subject,
            syllabusContext = syllabusContext,
            prompt = prompt,
            academicContext = academicContext
        )
        when (res) {
            is GeminiResult.Success -> {
                try {
                    val adapter = moshi.adapter(StudyFlashcardsData::class.java)
                    val data = adapter.fromJson(res.data)
                    if (data != null) GeminiResult.Success(data)
                    else GeminiResult.Error("Could not parse flashcards response.")
                } catch (e: Exception) {
                    GeminiResult.Error("Failed to parse flashcards: ${e.message}")
                }
            }
            is GeminiResult.Error -> res
        }
    }

    suspend fun generateMindMap(
        topic: String,
        subject: String,
        syllabusContext: String = "",
        prompt: String = "",
        academicContext: AcademicContext? = null
    ): GeminiResult<StudyMindMapData> = withContext(Dispatchers.IO) {
        val res = geminiClient.generateStudyTool(
            operation = "mindmap",
            topic = topic,
            subject = subject,
            syllabusContext = syllabusContext,
            prompt = prompt,
            academicContext = academicContext
        )
        when (res) {
            is GeminiResult.Success -> {
                try {
                    val adapter = moshi.adapter(StudyMindMapData::class.java)
                    val data = adapter.fromJson(res.data)
                    if (data != null) GeminiResult.Success(data)
                    else GeminiResult.Error("Could not parse mind map response.")
                } catch (e: Exception) {
                    GeminiResult.Error("Failed to parse mind map: ${e.message}")
                }
            }
            is GeminiResult.Error -> res
        }
    }

    suspend fun generateRevisionSheet(
        topic: String,
        subject: String,
        syllabusContext: String = "",
        prompt: String = "",
        academicContext: AcademicContext? = null
    ): GeminiResult<StudyRevisionSheetData> = withContext(Dispatchers.IO) {
        val res = geminiClient.generateStudyTool(
            operation = "revision",
            topic = topic,
            subject = subject,
            syllabusContext = syllabusContext,
            prompt = prompt,
            academicContext = academicContext
        )
        when (res) {
            is GeminiResult.Success -> {
                try {
                    val adapter = moshi.adapter(StudyRevisionSheetData::class.java)
                    val data = adapter.fromJson(res.data)
                    if (data != null) GeminiResult.Success(data)
                    else GeminiResult.Error("Could not parse revision sheet response.")
                } catch (e: Exception) {
                    GeminiResult.Error("Failed to parse revision sheet: ${e.message}")
                }
            }
            is GeminiResult.Error -> res
        }
    }

    suspend fun generateFormulaSheet(
        topic: String,
        subject: String,
        syllabusContext: String = "",
        prompt: String = "",
        academicContext: AcademicContext? = null
    ): GeminiResult<StudyFormulaSheetData> = withContext(Dispatchers.IO) {
        val res = geminiClient.generateStudyTool(
            operation = "formulas",
            topic = topic,
            subject = subject,
            syllabusContext = syllabusContext,
            prompt = prompt,
            academicContext = academicContext
        )
        when (res) {
            is GeminiResult.Success -> {
                try {
                    val adapter = moshi.adapter(StudyFormulaSheetData::class.java)
                    val data = adapter.fromJson(res.data)
                    if (data != null) GeminiResult.Success(data)
                    else GeminiResult.Error("Could not parse formula sheet response.")
                } catch (e: Exception) {
                    GeminiResult.Error("Failed to parse formula sheet: ${e.message}")
                }
            }
            is GeminiResult.Error -> res
        }
    }

    // ==========================================
    // SCAN & SOLVE (IMAGE PROCESSING)
    // ==========================================

    suspend fun solveImage(
        imageUri: Uri,
        prompt: String = "",
        academicContext: AcademicContext? = null
    ): GeminiResult<StudySolutionData> = withContext(Dispatchers.IO) {
        try {
            val (base64String, mimeType) = processAndCompressImage(imageUri)
                ?: return@withContext GeminiResult.Error("Could not read or process image.")

            val res = geminiClient.generateStudyTool(
                operation = "solve_image",
                imageBase64 = base64String,
                imageMimeType = mimeType,
                prompt = prompt,
                academicContext = academicContext
            )

            when (res) {
                is GeminiResult.Success -> {
                    val adapter = moshi.adapter(StudySolutionData::class.java)
                    val data = adapter.fromJson(res.data)
                    if (data != null) GeminiResult.Success(data)
                    else GeminiResult.Error("Could not parse solution response.")
                }
                is GeminiResult.Error -> res
            }
        } catch (e: Exception) {
            GeminiResult.Error("Failed to solve image: ${e.message}")
        }
    }

    private fun processAndCompressImage(uri: Uri): Pair<String, String>? {
        var inputStream: InputStream? = null
        return try {
            inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null

            // Resize down to max 1280px on the longest edge to stay fast and low-bandwidth
            val maxDimension = 1280
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scaledBitmap = if (width > maxDimension || height > maxDimension) {
                val ratio = width.toFloat() / height.toFloat()
                val newW: Int
                val newH: Int
                if (width > height) {
                    newW = maxDimension
                    newH = (maxDimension / ratio).toInt()
                } else {
                    newH = maxDimension
                    newW = (maxDimension * ratio).toInt()
                }
                Bitmap.createScaledBitmap(originalBitmap, newW, newH, true)
            } else {
                originalBitmap
            }

            val baos = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
            val bytes = baos.toByteArray()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            Pair(base64, "image/jpeg")
        } catch (e: Exception) {
            null
        } finally {
            inputStream?.close()
        }
    }
}
