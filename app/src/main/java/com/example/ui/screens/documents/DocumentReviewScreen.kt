package com.example.ui.screens.documents

import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.documents.DocumentExtractionResponse
import com.example.data.documents.StructuredCurriculumData
import com.example.ui.components.AmbientGlowBackground
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassButtonVariant
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassLevel
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.data.curriculum.CurriculumCourse
import com.example.data.curriculum.CurriculumComparisonEngine
import com.example.data.curriculum.ComparisonLabel

enum class DocumentReviewStep {
    UPLOAD,
    PROCESSING,
    SUMMARY,
    CURRICULUM_REVIEW,
    COMPARISON,
    VERIFICATION
}

@Composable
fun DocumentReviewScreen(
    onExtractSyllabus: suspend (name: String, mimeType: String, base64: String, text: String) -> DocumentExtractionResponse?,
    onRoadmapApproved: (StructuredCurriculumData) -> Unit,
    officialCourses: List<CurriculumCourse> = emptyList(),
    officialDeptName: String = "Engineering",
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentStep by remember { mutableStateOf(DocumentReviewStep.UPLOAD) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedMimeType by remember { mutableStateOf("application/pdf") }
    var pastedTextContent by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var processingStageIndex by remember { mutableStateOf(0) }
    var extractionResult by remember { mutableStateOf<DocumentExtractionResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val processingStages = listOf(
        "Uploading and validating document...",
        "Checking MIME signature and file size...",
        "Extracting document text & structure via Gemini...",
        "Mapping curriculum modules, topics & source references...",
        "Validating structured JSON & confidence assessment..."
    )

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "Uploaded_Document.pdf"
            selectedFileName = name
            val mime = context.contentResolver.getType(uri) ?: "application/pdf"
            selectedMimeType = mime
        }
    }

    AmbientGlowBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentStep == DocumentReviewStep.UPLOAD) onBack()
                        else currentStep = DocumentReviewStep.UPLOAD
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SageGlassL2)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = SageTextPrimary)
                }

                Text(
                    text = "DOCUMENT INTELLIGENCE",
                    color = SagePrimaryLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.size(40.dp))
            }

            when (currentStep) {
                DocumentReviewStep.UPLOAD -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                level = GlassLevel.L2,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(SagePrimaryStart.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "📄", fontSize = 28.sp)
                                    }

                                    Text(
                                        text = "Upload Syllabus or Study Guide",
                                        color = SageTextPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = "Supported formats: PDF, JPG, PNG, TXT, DOCX, PPTX (Max 15MB). Gemini will securely extract semesters, subjects, modules, and source page references.",
                                        color = SageTextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    GlassButton(
                                        text = if (selectedFileName != null) "Change Document" else "Browse & Select Document",
                                        onClick = {
                                            filePickerLauncher.launch(arrayOf("application/pdf", "image/*", "text/plain", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.openxmlformats-officedocument.presentationml.presentation"))
                                        },
                                        variant = GlassButtonVariant.Primary,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (selectedFileName != null) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(SageGlassL1)
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = "📎", fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(text = selectedFileName ?: "", color = SageTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                    Text(text = selectedMimeType, color = SageTextSecondary, fontSize = 10.sp)
                                                }
                                            }
                                            IconButton(onClick = { selectedFileName = null; selectedFileUri = null }) {
                                                Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = SageError)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Fallback text input option
                        item {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                level = GlassLevel.L1,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = "Or Paste Syllabus Text directly", color = SagePrimaryLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    OutlinedTextField(
                                        value = pastedTextContent,
                                        onValueChange = { pastedTextContent = it },
                                        placeholder = { Text("Paste syllabus outline, course modules, or topics here...", color = SageTextMuted, fontSize = 12.sp) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = SagePrimary,
                                            unfocusedBorderColor = SageGlassBorder,
                                            focusedTextColor = SageTextPrimary,
                                            unfocusedTextColor = SageTextPrimary
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }

                        if (errorMessage != null) {
                            item {
                                Text(text = errorMessage ?: "", color = SageError, fontSize = 12.sp)
                            }
                        }

                        item {
                            GlassButton(
                                text = "Extract & Analyze Curriculum",
                                onClick = {
                                    if (selectedFileUri == null && pastedTextContent.isBlank()) {
                                        errorMessage = "Please select a document or paste syllabus text."
                                        return@GlassButton
                                    }
                                    errorMessage = null
                                    currentStep = DocumentReviewStep.PROCESSING
                                    isProcessing = true
                                    processingStageIndex = 0

                                    coroutineScope.launch(Dispatchers.IO) {
                                        try {
                                            var base64Str = ""
                                            if (selectedFileUri != null) {
                                                val inputStream = context.contentResolver.openInputStream(selectedFileUri!!)
                                                val bytes = inputStream?.readBytes() ?: byteArrayOf()
                                                base64Str = Base64.encodeToString(bytes, Base64.NO_WRAP)
                                                inputStream?.close()
                                            }

                                            withContext(Dispatchers.Main) { processingStageIndex = 1 }
                                            delay(600)
                                            withContext(Dispatchers.Main) { processingStageIndex = 2 }

                                            val result = onExtractSyllabus(
                                                selectedFileName ?: "Pasted_Syllabus.txt",
                                                selectedMimeType,
                                                base64Str,
                                                pastedTextContent
                                            )

                                            withContext(Dispatchers.Main) { processingStageIndex = 3 }
                                            delay(500)
                                            withContext(Dispatchers.Main) { processingStageIndex = 4 }
                                            delay(400)

                                            withContext(Dispatchers.Main) {
                                                isProcessing = false
                                                if (result != null && result.success) {
                                                    extractionResult = result
                                                    currentStep = DocumentReviewStep.SUMMARY
                                                } else {
                                                    errorMessage = result?.error ?: "Extraction failed."
                                                    currentStep = DocumentReviewStep.UPLOAD
                                                }
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                isProcessing = false
                                                errorMessage = "Error: ${e.message}"
                                                currentStep = DocumentReviewStep.UPLOAD
                                            }
                                        }
                                    }
                                },
                                variant = GlassButtonVariant.Primary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                DocumentReviewStep.PROCESSING -> {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        level = GlassLevel.L2,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            CircularProgressIndicator(color = SagePrimary, modifier = Modifier.size(56.dp))
                            Text(text = "Analyzing Document Intelligence", color = SageTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = processingStages.getOrNull(processingStageIndex) ?: "Processing...",
                                color = SagePrimaryLight,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                DocumentReviewStep.SUMMARY -> {
                    val data = extractionResult?.structuredData
                    val meta = data?.documentMetadata
                    val summary = data?.extractionSummary

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                level = GlassLevel.L2,
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(text = meta?.documentTitle ?: "Extracted Syllabus", color = SageTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "Institution: ${meta?.institution ?: "Not Specified"} • Dept: ${meta?.department ?: "General"}", color = SageTextSecondary, fontSize = 12.sp)
                                    Text(text = "Confidence: ${summary?.confidence ?: "HIGH"} • Status: ${summary?.status ?: "SUCCESS"}", color = SageSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                GlassButton(
                                    text = "Compare with Official Curriculum",
                                    onClick = { currentStep = DocumentReviewStep.COMPARISON },
                                    variant = GlassButtonVariant.Primary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    GlassButton(
                                        text = "Curriculum Hierarchy",
                                        onClick = { currentStep = DocumentReviewStep.CURRICULUM_REVIEW },
                                        variant = GlassButtonVariant.Secondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    GlassButton(
                                        text = "Verify & Approve",
                                        onClick = { currentStep = DocumentReviewStep.VERIFICATION },
                                        variant = GlassButtonVariant.Secondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                DocumentReviewStep.COMPARISON -> {
                    val data = extractionResult?.structuredData
                    val report = remember(data, officialCourses) {
                        if (data != null && officialCourses.isNotEmpty()) {
                            CurriculumComparisonEngine.compare(data, officialCourses, officialDeptName)
                        } else null
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(text = "OFFICIAL CURRICULUM COMPARISON", color = SagePrimaryLight, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }

                        // Protection Notice
                        item {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                level = GlassLevel.L1,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Protected", tint = SageSuccess)
                                    Text(
                                        text = "Official Academic Records are Read-Only & Protected. Uploaded differences will be annotated without mutating institutional curricula.",
                                        color = SageTextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        // Summary Metrics
                        if (report != null) {
                            item {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    level = GlassLevel.L2,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "Curriculum Alignment", color = SageTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(text = "${report.alignmentPercentage}%", color = SagePrimaryLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(text = "MATCHED: ${report.matchedCount}", color = SageSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text(text = "•", color = SageTextSecondary, fontSize = 10.sp)
                                            Text(text = "MISMATCH: ${report.nameMismatchCount}", color = SagePrimaryLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text(text = "•", color = SageTextSecondary, fontSize = 10.sp)
                                            Text(text = "ADDITIONAL: ${report.additionalInUploadCount}", color = SagePurple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text(text = "•", color = SageTextSecondary, fontSize = 10.sp)
                                            Text(text = "MISSING: ${report.missingFromOfficialCount}", color = SageError, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            items(report.topicResults) { itemResult ->
                                val (badgeColor, labelText) = when (itemResult.comparisonLabel) {
                                    ComparisonLabel.MATCHED -> Pair(SageSuccess, "MATCHED")
                                    ComparisonLabel.NAME_MISMATCH -> Pair(SagePrimaryLight, "NAME_MISMATCH")
                                    ComparisonLabel.ADDITIONAL_IN_UPLOAD -> Pair(SagePurple, "ADDITIONAL_IN_UPLOAD")
                                    ComparisonLabel.MISSING_FROM_OFFICIAL -> Pair(SageError, "MISSING_FROM_OFFICIAL")
                                    ComparisonLabel.NEEDS_MANUAL_REVIEW -> Pair(SageGold, "NEEDS_MANUAL_REVIEW")
                                }

                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    level = GlassLevel.L1,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = itemResult.topicName,
                                                color = SageTextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Surface(
                                                color = badgeColor.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = labelText,
                                                    color = badgeColor,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = itemResult.explanation,
                                            color = SageTextSecondary,
                                            fontSize = 11.sp
                                        )
                                        if (itemResult.matchedOfficialTopic != null) {
                                            Text(
                                                text = "Matched Official: ${itemResult.matchedOfficialTopic}",
                                                color = SagePrimaryLight,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            item {
                                Text(
                                    text = "Comparing extracted syllabus against standard courses...",
                                    color = SageTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        item {
                            GlassButton(
                                text = "Proceed to Verification & Approval",
                                onClick = { currentStep = DocumentReviewStep.VERIFICATION },
                                variant = GlassButtonVariant.Primary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                DocumentReviewStep.CURRICULUM_REVIEW -> {
                    val data = extractionResult?.structuredData
                    val semesters = data?.semesters ?: emptyList()

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(text = "EXTRACTED CURRICULUM HIERARCHY", color = SagePrimaryLight, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }

                        items(semesters) { sem ->
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                level = GlassLevel.L1,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = "Semester ${sem.semesterNumber}", color = SageGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    sem.subjects?.forEach { sub ->
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "• ${sub.subjectName} (${sub.subjectCode ?: "N/A"})", color = SageTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        sub.modules?.forEach { mod ->
                                            Text(text = "   - Module ${mod.moduleNumber}: ${mod.moduleTitle}", color = SageTextSecondary, fontSize = 12.sp)
                                            mod.topics?.forEach { top ->
                                                val srcRef = top.sourceReferences?.firstOrNull()
                                                val pageStr = if (srcRef?.pageNumber != null) " [Page ${srcRef.pageNumber}]" else ""
                                                val classStr = if (srcRef?.sourceType != null) " (${srcRef.sourceType})" else ""
                                                Text(text = "      * ${top.topicName}$pageStr$classStr", color = SagePrimaryLight, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                GlassButton(
                                    text = "Compare Official",
                                    onClick = { currentStep = DocumentReviewStep.COMPARISON },
                                    variant = GlassButtonVariant.Secondary,
                                    modifier = Modifier.weight(1f)
                                )
                                GlassButton(
                                    text = "Proceed to Approval",
                                    onClick = { currentStep = DocumentReviewStep.VERIFICATION },
                                    variant = GlassButtonVariant.Primary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                DocumentReviewStep.VERIFICATION -> {
                    val data = extractionResult?.structuredData

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        level = GlassLevel.L2,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(text = "VERIFICATION & APPROVAL", color = SageSuccess, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text(
                                text = "All curriculum records have been mapped with source traceability (EXPLICIT / INFERRED). Official institutional records remain protected. Approved extractions can be used to ground Personal AI Roadmaps.",
                                color = SageTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            GlassButton(
                                text = "Approve & Generate Grounded Roadmap",
                                onClick = {
                                    if (data != null) {
                                        onRoadmapApproved(data)
                                    }
                                },
                                variant = GlassButtonVariant.Primary,
                                modifier = Modifier.fillMaxWidth()
                            )

                            GlassButton(
                                text = "Back to Comparison",
                                onClick = { currentStep = DocumentReviewStep.COMPARISON },
                                variant = GlassButtonVariant.Secondary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
