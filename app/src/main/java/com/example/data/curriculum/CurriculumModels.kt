package com.example.data.curriculum

import com.example.data.roadmap.DevRoadmapDetail
import com.example.data.roadmap.DevRoadmapNode
import com.example.data.roadmap.DevRoadmapResource
import com.example.data.roadmap.DevRoadmapSummary

/**
 * Academic Department information corresponding to an official curriculum database file.
 */
data class CurriculumDepartment(
    val id: String,
    val name: String,
    val shortName: String,
    val programme: String,
    val regulation: String,
    val institution: String = "JIS College of Engineering",
    val icon: String,
    val assetFileName: String,
    val totalCourses: Int,
    val availableSemesters: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7, 8)
)

/**
 * An individual module or syllabus unit within an official academic course.
 */
data class CurriculumModule(
    val id: String,
    val moduleIndex: Int,
    val moduleNumber: String, // e.g. "Module 1", "M1"
    val title: String,        // e.g. "Module 1: Basics of Computing & Number Representation (7L)"
    val content: String,      // Exact syllabus topic description
    val hours: String? = null,
    val topics: List<String> = emptyList()
)

/**
 * Complete official course record from the regulatory curriculum database.
 */
data class CurriculumCourse(
    val id: String,                  // e.g. "cse_aiml_CS101"
    val code: String,                // e.g. "CS101"
    val title: String,               // e.g. "Introduction to Programming and Problem Solving"
    val departmentId: String,        // e.g. "cse_aiml"
    val departmentName: String,      // e.g. "CSE (AI & ML)"
    val programme: String,           // e.g. "B. Tech CSE (AI & ML)"
    val regulation: String,          // e.g. "R25"
    val semester: Int,               // 1..8
    val year: Int,                   // 1..4
    val credits: String,             // e.g. "3" or "4"
    val contact: String = "",        // e.g. "3:0:0"
    val category: String = "Core",   // e.g. "Professional Core", "Major", "Engineering Science"
    val courseType: String = "Theory", // "Theory", "Practical", "Sessional", "Mandatory"
    val prerequisites: String = "",
    val objectives: String = "",
    val outcomes: String = "",
    val textbooks: String = "",
    val referenceBooks: String = "",
    val modules: List<CurriculumModule> = emptyList(),
    val rawSyllabus: String = ""
) {
    /**
     * Unique deterministic roadmap identifier matching the roadmap engine routing convention.
     */
    val roadmapId: String
        get() = "curriculum_${departmentId}_$code"

    /**
     * Formatted course display label with code and title.
     */
    val displayTitle: String
        get() = "$code: $title"

    /**
     * Transform this official syllabus record deterministically into an interactive DevRoadmapDetail.
     * "THE SYLLABUS IS THE ROADMAP."
     */
    fun toDevRoadmapDetail(deptIcon: String): DevRoadmapDetail {
        val courseCategories = if (modules.isNotEmpty()) {
            modules.map { it.moduleNumber }.distinct()
        } else {
            listOf("Syllabus Units")
        }

        val parsedNodes: List<DevRoadmapNode> = if (modules.isNotEmpty()) {
            modules.mapIndexed { index, mod ->
                val nextModuleId = if (index + 1 < modules.size) {
                    "${id}_mod_${index + 1}"
                } else null

                val resources = buildCourseResources(textbooks, referenceBooks, mod.title)

                DevRoadmapNode(
                    id = "${id}_mod_$index",
                    title = mod.title.ifBlank { "${mod.moduleNumber}: Core Syllabus" },
                    icon = when (index % 4) {
                        0 -> "📘"
                        1 -> "💡"
                        2 -> "⚙️"
                        else -> "🎯"
                    },
                    category = mod.moduleNumber,
                    description = mod.content.ifBlank {
                        "Official syllabus module for $code ($title). Refer to course outline."
                    },
                    resources = resources,
                    children = if (nextModuleId != null) listOf(nextModuleId) else emptyList(),
                    difficulty = when (year) {
                        1 -> "Foundational"
                        2 -> "Core"
                        3 -> "Advanced"
                        else -> "Specialized"
                    }
                )
            }
        } else {
            // Single fallback node containing the official raw syllabus or notice
            listOf(
                DevRoadmapNode(
                    id = "${id}_mod_0",
                    title = "$code: Complete Course Syllabus",
                    icon = "📘",
                    category = "Syllabus",
                    description = rawSyllabus.ifBlank {
                        "Official syllabus details for $code ($title). Refer to institutional department notice for laboratory or sessional breakdown."
                    },
                    resources = buildCourseResources(textbooks, referenceBooks, title),
                    children = emptyList(),
                    difficulty = when (year) {
                        1 -> "Foundational"
                        2 -> "Core"
                        3 -> "Advanced"
                        else -> "Specialized"
                    }
                )
            )
        }

        val descriptionText = buildString {
            appendLine("$departmentName • $programme ($regulation)")
            appendLine("Semester $semester (Year $year) • Credits: $credits • Contact: ${contact.ifBlank { "As prescribed" }}")
            if (prerequisites.isNotBlank()) {
                appendLine("Prerequisites: $prerequisites")
            }
            if (objectives.isNotBlank()) {
                appendLine("Objectives: $objectives")
            }
        }

        return DevRoadmapDetail(
            id = roadmapId,
            title = displayTitle,
            description = descriptionText.trim(),
            icon = deptIcon,
            categories = courseCategories,
            nodes = parsedNodes
        )
    }

    /**
     * Convert course into a DevRoadmapSummary representation.
     */
    fun toDevRoadmapSummary(deptIcon: String): DevRoadmapSummary {
        val nodeCount = if (modules.isNotEmpty()) modules.size else 1
        return DevRoadmapSummary(
            id = roadmapId,
            title = displayTitle,
            icon = deptIcon,
            nodes = nodeCount,
            resources = if (textbooks.isNotBlank() || referenceBooks.isNotBlank()) 2 else 1,
            difficulty = when (year) {
                1 -> "Foundational"
                2 -> "Core"
                3 -> "Advanced"
                else -> "Specialized"
            },
            time = "Semester $semester"
        )
    }

    private fun buildCourseResources(
        textbooks: String,
        referenceBooks: String,
        topicQuery: String
    ): List<DevRoadmapResource> {
        val list = mutableListOf<DevRoadmapResource>()
        if (textbooks.isNotBlank()) {
            val primary = textbooks.lines().firstOrNull { it.isNotBlank() } ?: textbooks
            list.add(
                DevRoadmapResource(
                    title = "Prescribed Text: ${primary.trim()}",
                    url = "https://scholar.google.com/scholar?q=${primary.trim()}",
                    type = "textbook",
                    free = true
                )
            )
        }
        if (referenceBooks.isNotBlank()) {
            val ref = referenceBooks.lines().firstOrNull { it.isNotBlank() } ?: referenceBooks
            list.add(
                DevRoadmapResource(
                    title = "Reference: ${ref.trim()}",
                    url = "https://scholar.google.com/scholar?q=${ref.trim()}",
                    type = "reference",
                    free = true
                )
            )
        }
        if (list.isEmpty()) {
            list.add(
                DevRoadmapResource(
                    title = "Academic Literature on $title",
                    url = "https://scholar.google.com/scholar?q=$code+$title",
                    type = "docs",
                    free = true
                )
            )
        }
        return list
    }
}
