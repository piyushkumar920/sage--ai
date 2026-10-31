package com.example.data.curriculum

import com.example.data.documents.SemesterExtraction
import com.example.data.documents.StructuredCurriculumData
import com.squareup.moshi.JsonClass
import java.util.Locale

/**
 * Status labels required for official curriculum comparison.
 * Official curriculum records are strictly immutable reference data loaded from assets.
 */
enum class ComparisonLabel {
    MATCHED,               // Concept and scope exist in official curriculum with high confidence
    MISSING_FROM_OFFICIAL, // Topic is in the uploaded syllabus but absent from standard official curriculum
    ADDITIONAL_IN_UPLOAD,  // Upload introduces additional/elective material beyond standard syllabus
    NAME_MISMATCH,         // Concept is identical or equivalent, but named differently
    NEEDS_MANUAL_REVIEW    // Low confidence or ambiguous match requiring student review
}

@JsonClass(generateAdapter = true)
data class TopicComparisonResult(
    val topicName: String,
    val moduleTitle: String,
    val subjectName: String,
    val comparisonLabel: ComparisonLabel,
    val matchedOfficialTopic: String? = null,
    val similarityScore: Float = 0f,
    val explanation: String,
    val sourcePage: Int? = null,
    val isVerifiedByStudent: Boolean = false
)

@JsonClass(generateAdapter = true)
data class CurriculumComparisonReport(
    val totalUploadedTopics: Int,
    val matchedCount: Int,
    val missingFromOfficialCount: Int,
    val additionalInUploadCount: Int,
    val nameMismatchCount: Int,
    val needsManualReviewCount: Int,
    val alignmentPercentage: Int,
    val topicResults: List<TopicComparisonResult>,
    val officialCurriculumId: String,
    val officialCurriculumTitle: String
)

object CurriculumComparisonEngine {

    /**
     * Compare structured syllabus extraction against official department curriculum courses.
     * Guaranteed: NEVER modifies any official curriculum records.
     */
    fun compare(
        extractedData: StructuredCurriculumData,
        officialCourses: List<CurriculumCourse>,
        officialDeptName: String
    ): CurriculumComparisonReport {
        val topicResults = mutableListOf<TopicComparisonResult>()

        val officialTopicBank = officialCourses.flatMap { course ->
            course.modules.map { mod ->
                Triple(course.title, mod.moduleNumber, mod.title)
            }
        }

        val semesters = extractedData.semesters ?: emptyList()
        for (sem in semesters) {
            val subjects = sem.subjects ?: emptyList()
            for (subject in subjects) {
                val modules = subject.modules ?: emptyList()
                for (module in modules) {
                    val topics = module.topics ?: emptyList()
                    for (topic in topics) {
                        val topicName = topic.topicName
                        val page = topic.sourceReferences?.firstOrNull()?.pageNumber

                        val bestMatch = findBestOfficialMatch(topicName, officialTopicBank)

                        val (label, matchedName, score, explanation) = when {
                            bestMatch == null -> {
                                Quad(
                                    ComparisonLabel.MISSING_FROM_OFFICIAL,
                                    null,
                                    0f,
                                    "No corresponding topic found in official $officialDeptName curriculum."
                                )
                            }
                            bestMatch.second >= 0.85f -> {
                                Quad(
                                    ComparisonLabel.MATCHED,
                                    bestMatch.first.third,
                                    bestMatch.second,
                                    "Matches official course topic '${bestMatch.first.third}' in ${bestMatch.first.first}."
                                )
                            }
                            bestMatch.second >= 0.65f -> {
                                Quad(
                                    ComparisonLabel.NAME_MISMATCH,
                                    bestMatch.first.third,
                                    bestMatch.second,
                                    "Concept matches official '${bestMatch.first.third}' under a different title."
                                )
                            }
                            bestMatch.second >= 0.40f -> {
                                Quad(
                                    ComparisonLabel.ADDITIONAL_IN_UPLOAD,
                                    bestMatch.first.third,
                                    bestMatch.second,
                                    "Uploaded topic introduces supplementary material related to '${bestMatch.first.third}'."
                                )
                            }
                            else -> {
                                Quad(
                                    ComparisonLabel.NEEDS_MANUAL_REVIEW,
                                    bestMatch.first.third,
                                    bestMatch.second,
                                    "Low similarity (${(bestMatch.second * 100).toInt()}%); requires student verification."
                                )
                            }
                        }

                        topicResults.add(
                            TopicComparisonResult(
                                topicName = topicName,
                                moduleTitle = module.moduleTitle,
                                subjectName = subject.subjectName,
                                comparisonLabel = label,
                                matchedOfficialTopic = matchedName,
                                similarityScore = score,
                                explanation = explanation,
                                sourcePage = page
                            )
                        )
                    }
                }
            }
        }

        val total = topicResults.size.coerceAtLeast(1)
        val matched = topicResults.count { it.comparisonLabel == ComparisonLabel.MATCHED }
        val missing = topicResults.count { it.comparisonLabel == ComparisonLabel.MISSING_FROM_OFFICIAL }
        val additional = topicResults.count { it.comparisonLabel == ComparisonLabel.ADDITIONAL_IN_UPLOAD }
        val nameMismatch = topicResults.count { it.comparisonLabel == ComparisonLabel.NAME_MISMATCH }
        val manualReview = topicResults.count { it.comparisonLabel == ComparisonLabel.NEEDS_MANUAL_REVIEW }

        val alignment = ((matched + nameMismatch) * 100) / total

        return CurriculumComparisonReport(
            totalUploadedTopics = topicResults.size,
            matchedCount = matched,
            missingFromOfficialCount = missing,
            additionalInUploadCount = additional,
            nameMismatchCount = nameMismatch,
            needsManualReviewCount = manualReview,
            alignmentPercentage = alignment,
            topicResults = topicResults,
            officialCurriculumId = officialDeptName,
            officialCurriculumTitle = "Official $officialDeptName R25 Curriculum"
        )
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    private fun findBestOfficialMatch(
        uploadedTopic: String,
        bank: List<Triple<String, String, String>>
    ): Pair<Triple<String, String, String>, Float>? {
        if (bank.isEmpty()) return null
        val cleanUploaded = normalize(uploadedTopic)
        var bestScore = 0f
        var bestItem: Triple<String, String, String>? = null

        for (item in bank) {
            val cleanOfficial = normalize(item.third)
            val score = calculateTokenJaccardSimilarity(cleanUploaded, cleanOfficial)
            if (score > bestScore) {
                bestScore = score
                bestItem = item
            }
        }

        return if (bestItem != null && bestScore > 0.1f) Pair(bestItem, bestScore) else null
    }

    private fun normalize(str: String): String {
        return str.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9 ]"), " ")
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() && it !in STOP_WORDS }
            .joinToString(" ")
    }

    private fun calculateTokenJaccardSimilarity(s1: String, s2: String): Float {
        if (s1 == s2) return 1.0f
        val tokens1 = s1.split(" ").filter { it.isNotBlank() }.toSet()
        val tokens2 = s2.split(" ").filter { it.isNotBlank() }.toSet()
        if (tokens1.isEmpty() || tokens2.isEmpty()) return 0f
        val intersection = tokens1.intersect(tokens2).size
        val union = tokens1.union(tokens2).size
        if (union == 0) return 0f
        return intersection.toFloat() / union.toFloat()
    }

    private val STOP_WORDS = setOf(
        "and", "or", "the", "a", "an", "in", "on", "of", "to", "for", "with",
        "introduction", "overview", "basics", "fundamentals", "unit", "module"
    )
}
