package com.example.data.mission

import android.content.Context
import com.example.data.curriculum.CurriculumRepository
import com.example.data.local.SageDao
import com.example.data.local.TopicProgressEntity
import com.example.data.local.WeakConceptEntity
import com.example.data.roadmap.DevRoadmapNode
import com.example.data.studytools.AcademicContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DailyMissionRepository(
    private val context: Context,
    private val dao: SageDao,
    private val curriculumRepository: CurriculumRepository
) {
    val allMissions: Flow<List<DailyMissionEntity>> = dao.getAllDailyMissions()

    fun getTodayDateKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    fun getDailyMissionFlow(dateKey: String = getTodayDateKey()): Flow<DailyMissionEntity?> {
        return dao.getDailyMissionByDate(dateKey)
    }

    fun getTodayMissionFlow(): Flow<DailyMissionEntity?> {
        return dao.getDailyMissionByDate(getTodayDateKey())
    }

    fun getRecentMissions(limit: Int = 10): Flow<List<DailyMissionEntity>> {
        return dao.getRecentDailyMissions(limit)
    }

    /**
     * Gets existing mission for today or deterministically generates a new one based on priority.
     * If an established AcademicProfile is provided, the mission is strictly constrained to that profile's curriculum.
     */
    suspend fun getOrCreateTodayMission(
        academicProfile: com.example.data.profile.AcademicProfile? = null,
        activeAcademicContext: AcademicContext? = null,
        activeRoadmapId: String? = null,
        activeTopicTitle: String? = null
    ): DailyMissionEntity = withContext(Dispatchers.IO) {
        val todayKey = getTodayDateKey()
        val existing = dao.getDailyMissionByDateOnce(todayKey)
        if (existing != null) {
            // Check if existing mission matches academic profile if provided
            if (academicProfile != null && !existing.completed) {
                val matchesDept = existing.department.contains(academicProfile.departmentName, ignoreCase = true) ||
                        existing.department.contains(academicProfile.departmentId, ignoreCase = true)
                val matchesSem = existing.semester == academicProfile.semester
                if (!matchesDept || !matchesSem) {
                    // Profile changed today and previous mission was incomplete -> replace it with new profile mission
                    dao.deleteIncompleteDailyMissionByDate(todayKey)
                    val newMission = generateMissionForDate(
                        dateKey = todayKey,
                        academicProfile = academicProfile,
                        activeAcademicContext = activeAcademicContext,
                        activeRoadmapId = activeRoadmapId,
                        activeTopicTitle = activeTopicTitle
                    )
                    dao.insertOrUpdateDailyMission(newMission)
                    return@withContext newMission
                }
            }
            return@withContext existing
        }

        // Generate deterministically from profile / existing local data by priority
        val newMission = generateMissionForDate(
            dateKey = todayKey,
            academicProfile = academicProfile,
            activeAcademicContext = activeAcademicContext,
            activeRoadmapId = activeRoadmapId,
            activeTopicTitle = activeTopicTitle
        )
        dao.insertOrUpdateDailyMission(newMission)
        newMission
    }

    /**
     * Handles an explicit Academic Profile change.
     * If today's mission is incomplete and belongs to an older profile, it is replaced with a new mission.
     * If today's mission was already completed, it is preserved in history without being rewritten.
     */
    suspend fun handleAcademicProfileChange(newProfile: com.example.data.profile.AcademicProfile): DailyMissionEntity = withContext(Dispatchers.IO) {
        val todayKey = getTodayDateKey()
        val existing = dao.getDailyMissionByDateOnce(todayKey)

        if (existing != null && existing.completed) {
            // Historical completed mission is preserved in history
            return@withContext existing
        }

        // Invalidate previous incomplete mission if any and generate for the new profile
        dao.deleteIncompleteDailyMissionByDate(todayKey)
        val newMission = generateMissionForDate(
            dateKey = todayKey,
            academicProfile = newProfile,
            activeAcademicContext = null,
            activeRoadmapId = null
        )
        dao.insertOrUpdateDailyMission(newMission)
        newMission
    }

    /**
     * Deterministic mission generator strictly prioritizing existing local data and constrained by AcademicProfile:
     * 1. Active academic topic (if within user profile or explicit temporary exploration)
     * 2. Current learning topic from recent topics / active topic
     * 3. Recommended / in-progress topic from profile's semester courses
     * 4. Weak concept from quiz mistakes in profile's courses
     * 5. Next available unstarted topic in profile's semester courses
     * 6. Fallback to the first topic of the first course in the student's Academic Profile
     */
    suspend fun generateMissionForDate(
        dateKey: String,
        academicProfile: com.example.data.profile.AcademicProfile? = null,
        activeAcademicContext: AcademicContext? = null,
        activeRoadmapId: String? = null,
        activeTopicTitle: String? = null
    ): DailyMissionEntity = withContext(Dispatchers.IO) {
        // Priority 1: Active academic context if valid
        if (activeAcademicContext != null && activeAcademicContext.topic.isNotBlank() && activeAcademicContext.courseName.isNotBlank()) {
            return@withContext buildMissionFromContext(
                dateKey = dateKey,
                context = activeAcademicContext,
                sourcePriority = "Active Academic Context"
            )
        }

        // Priority 2: Current learning topic from recent topics / active topic
        val latestTopic = dao.getLatestTopic()
        if (latestTopic != null && latestTopic.mode == "LEARNING" && latestTopic.title.isNotBlank()) {
            val parsedContext = parseContextFromTopicTitle(latestTopic.title)
            if (parsedContext != null) {
                // If profile is set, ensure it belongs to the profile or allow if explicitly studied
                if (academicProfile == null || parsedContext.semester == academicProfile.semester ||
                    parsedContext.department.contains(academicProfile.departmentName, ignoreCase = true)
                ) {
                    return@withContext buildMissionFromContext(
                        dateKey = dateKey,
                        context = parsedContext,
                        sourcePriority = "Current Learning Topic"
                    )
                }
            }
        }

        // If academicProfile is present, constrain all curriculum selections to that profile
        val deptId = academicProfile?.departmentId
        val semester = academicProfile?.semester

        val profileCourses = if (!deptId.isNullOrBlank() && semester != null) {
            curriculumRepository.getCoursesForSemester(deptId, semester)
        } else if (!deptId.isNullOrBlank()) {
            curriculumRepository.getCoursesForDepartment(deptId)
        } else if (!activeRoadmapId.isNullOrBlank()) {
            val c = curriculumRepository.getCourseByRoadmapId(activeRoadmapId)
            if (c != null) listOf(c) else emptyList()
        } else {
            emptyList()
        }

        // Priority 3: In-progress topic from profile courses
        if (profileCourses.isNotEmpty()) {
            for (course in profileCourses) {
                val progressList = dao.getTopicProgressForRoadmapOnce(course.roadmapId)
                val inProgressItem = progressList.find { it.status == "IN_PROGRESS" || it.status == "NEEDS_REVIEW" }
                if (inProgressItem != null) {
                    val ctx = buildContextFromRoadmapItem(course.roadmapId, inProgressItem)
                    if (ctx != null) {
                        return@withContext buildMissionFromContext(
                            dateKey = dateKey,
                            context = ctx,
                            sourcePriority = "In-Progress Course Topic"
                        )
                    }
                }
            }
        }

        // Priority 4: Weak concept from previous quiz failures in profile courses
        val weakConcepts = dao.getActiveWeakConceptsOnce()
        val topWeakConcept = weakConcepts.firstOrNull()
        if (topWeakConcept != null && topWeakConcept.concept.isNotBlank() && profileCourses.isNotEmpty()) {
            val primaryCourse = profileCourses.first()
            val ctx = buildContextFromWeakConcept(topWeakConcept, primaryCourse.roadmapId)
            if (ctx != null) {
                return@withContext buildMissionFromContext(
                    dateKey = dateKey,
                    context = ctx,
                    goalOverride = "Master weak concept: ${topWeakConcept.concept}",
                    sourcePriority = "Weak Concept Remediation"
                )
            }
        }

        // Priority 5: Next available unstarted module in profile semester courses
        if (profileCourses.isNotEmpty()) {
            for (course in profileCourses) {
                val progressList = dao.getTopicProgressForRoadmapOnce(course.roadmapId)
                val completedNodeIds = progressList.filter { it.status == "COMPLETED" }.map { it.nodeId }.toSet()
                for (mod in course.modules) {
                    val modNodeId = "mod_${mod.moduleNumber.lowercase().replace(" ", "_")}"
                    if (!completedNodeIds.contains(modNodeId)) {
                        val topicName = mod.title.ifBlank { "${mod.moduleNumber}: Core Syllabus" }
                        val ctx = AcademicContext(
                            department = course.departmentName,
                            programme = course.programme,
                            regulation = course.regulation,
                            semester = course.semester,
                            courseCode = course.code,
                            courseName = course.title,
                            module = mod.moduleNumber,
                            topic = topicName,
                            officialSyllabusContent = mod.content
                        )
                        return@withContext buildMissionFromContext(
                            dateKey = dateKey,
                            context = ctx,
                            sourcePriority = "Next Curriculum Module"
                        )
                    }
                }
            }

            // If all modules in all courses completed, take the first module of the first course for continuous mastery
            val primaryCourse = profileCourses.first()
            val firstModule = primaryCourse.modules.firstOrNull()
            val ctx = AcademicContext(
                department = primaryCourse.departmentName,
                programme = primaryCourse.programme,
                regulation = primaryCourse.regulation,
                semester = primaryCourse.semester,
                courseCode = primaryCourse.code,
                courseName = primaryCourse.title,
                module = firstModule?.moduleNumber ?: "Module 1",
                topic = firstModule?.title ?: "Core Syllabus Mastery",
                officialSyllabusContent = firstModule?.content ?: ""
            )
            return@withContext buildMissionFromContext(
                dateKey = dateKey,
                context = ctx,
                sourcePriority = "Semester Curriculum Review"
            )
        }

        // Priority 6: Fallback when no academic profile has been selected yet
        val defaultDept = if (!deptId.isNullOrBlank()) curriculumRepository.getDepartment(deptId) else curriculumRepository.getAllDepartments().firstOrNull()
        val defaultCourse = if (defaultDept != null) curriculumRepository.getCoursesForDepartment(defaultDept.id).firstOrNull() else null
        val defaultModule = defaultCourse?.modules?.firstOrNull()
        val fallbackContext = AcademicContext(
            department = defaultDept?.name ?: "Academic Curriculum",
            programme = defaultDept?.programme ?: "Undergraduate Programme",
            regulation = defaultDept?.regulation ?: "R25",
            semester = defaultCourse?.semester ?: 1,
            courseCode = defaultCourse?.code ?: "CORE101",
            courseName = defaultCourse?.title ?: "Introduction to Curriculum",
            module = defaultModule?.moduleNumber ?: "Module 1",
            topic = defaultModule?.title ?: "Fundamental Concepts",
            officialSyllabusContent = defaultModule?.content ?: ""
        )

        buildMissionFromContext(
            dateKey = dateKey,
            context = fallbackContext,
            sourcePriority = "Default Academic Curriculum"
        )
    }

    private fun buildMissionFromContext(
        dateKey: String,
        context: AcademicContext,
        goalOverride: String? = null,
        sourcePriority: String = ""
    ): DailyMissionEntity {
        val topicName = context.topic.ifBlank { context.module }.ifBlank { "Core Concepts" }
        val courseName = context.courseName.ifBlank { context.courseCode }.ifBlank { "Curriculum Course" }
        val goal = goalOverride ?: "Understand $topicName in $courseName"

        // 3 Small, achievable tasks (~20 minutes)
        val tasks = listOf(
            MissionTask(
                id = "task_learn_${dateKey}",
                type = "LEARN",
                title = "Learn the concept",
                description = "Understand $topicName principles with Sage",
                actionRoute = "CHAT",
                isCompleted = false
            ),
            MissionTask(
                id = "task_practice_${dateKey}",
                type = "PRACTICE",
                title = "Practice 5 flashcards",
                description = "Test active recall for $topicName",
                actionRoute = "FLASHCARDS",
                isCompleted = false
            ),
            MissionTask(
                id = "task_pyq_${dateKey}",
                type = "PYQ",
                title = "Solve 1 PYQ",
                description = "Solve 1 previous-year exam question on $topicName",
                actionRoute = "PYQ",
                isCompleted = false
            )
        )

        return DailyMissionEntity(
            id = "mission_$dateKey",
            dateKey = dateKey,
            createdAt = System.currentTimeMillis(),
            department = context.department,
            programme = context.programme,
            regulation = context.regulation,
            semester = context.semester,
            courseCode = context.courseCode,
            courseName = context.courseName,
            module = context.module,
            topic = topicName,
            officialSyllabusContent = context.officialSyllabusContent,
            goal = goal,
            estimatedMinutes = 20,
            tasksJson = DailyMissionEntity.serializeTasks(tasks),
            completed = false,
            completedAt = null,
            actualSpentSeconds = 0
        )
    }

    private fun parseContextFromTopicTitle(topicTitle: String): AcademicContext? {
        // e.g. "CS101: Problem Solving & Algorithms"
        val parts = topicTitle.split(":")
        if (parts.size >= 2) {
            val code = parts[0].trim()
            val topic = parts[1].trim()
            val allCourses = curriculumRepository.searchCourses(code)
            val course = allCourses.firstOrNull()
            if (course != null) {
                return AcademicContext(
                    department = course.departmentName,
                    programme = course.programme,
                    regulation = course.regulation,
                    semester = course.semester,
                    courseCode = course.code,
                    courseName = course.title,
                    module = topic,
                    topic = topic
                )
            }
        }
        return null
    }

    private fun buildContextFromRoadmapItem(roadmapId: String, item: TopicProgressEntity): AcademicContext? {
        val course = curriculumRepository.getCourseByRoadmapId(roadmapId) ?: return null
        val mod = course.modules.find { item.nodeTitle.contains(it.title, ignoreCase = true) || item.nodeTitle.contains(it.moduleNumber, ignoreCase = true) }
        return AcademicContext(
            department = course.departmentName,
            programme = course.programme,
            regulation = course.regulation,
            semester = course.semester,
            courseCode = course.code,
            courseName = course.title,
            module = mod?.moduleNumber ?: item.nodeTitle,
            topic = item.nodeTitle,
            officialSyllabusContent = mod?.content ?: ""
        )
    }

    private fun buildContextFromWeakConcept(weak: WeakConceptEntity, roadmapId: String): AcademicContext? {
        val course = curriculumRepository.getCourseByRoadmapId(roadmapId)
        return AcademicContext(
            department = course?.departmentName ?: "CSE (AI & ML)",
            programme = course?.programme ?: "B. Tech",
            regulation = course?.regulation ?: "R25",
            semester = course?.semester ?: 1,
            courseCode = course?.code ?: "CS101",
            courseName = course?.title ?: "Academic Curriculum",
            module = "Weak Concept Remediation",
            topic = weak.concept
        )
    }

    /**
     * Toggles or updates task completion state.
     * Automatically completes the entire mission when all tasks are complete.
     */
    suspend fun toggleTaskCompletion(missionId: String, taskId: String): DailyMissionEntity? = withContext(Dispatchers.IO) {
        val mission = dao.getDailyMissionById(missionId) ?: return@withContext null
        val tasks = mission.parseTasks().toMutableList()
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index == -1) return@withContext mission

        val currentTask = tasks[index]
        val newStatus = !currentTask.isCompleted
        tasks[index] = currentTask.copy(
            isCompleted = newStatus,
            completedAt = if (newStatus) System.currentTimeMillis() else null
        )

        val allCompleted = tasks.all { it.isCompleted }
        val updatedMission = mission.copy(
            tasksJson = DailyMissionEntity.serializeTasks(tasks),
            completed = allCompleted,
            completedAt = if (allCompleted) (mission.completedAt ?: System.currentTimeMillis()) else null
        )
        dao.insertOrUpdateDailyMission(updatedMission)
        updatedMission
    }

    /**
     * Marks an entire mission as completed (e.g. from completion button / Focus summary).
     */
    suspend fun completeMission(missionId: String, spentSeconds: Int = 0): DailyMissionEntity? = withContext(Dispatchers.IO) {
        val mission = dao.getDailyMissionById(missionId) ?: return@withContext null
        val tasks = mission.parseTasks().map {
            it.copy(isCompleted = true, completedAt = it.completedAt ?: System.currentTimeMillis())
        }
        val updated = mission.copy(
            tasksJson = DailyMissionEntity.serializeTasks(tasks),
            completed = true,
            completedAt = System.currentTimeMillis(),
            actualSpentSeconds = if (spentSeconds > 0) spentSeconds else mission.actualSpentSeconds
        )
        dao.insertOrUpdateDailyMission(updated)
        updated
    }

    /**
     * Records additional focus seconds spent on a mission.
     */
    suspend fun recordMissionFocusTime(missionId: String, seconds: Int) = withContext(Dispatchers.IO) {
        val mission = dao.getDailyMissionById(missionId) ?: return@withContext
        val updated = mission.copy(actualSpentSeconds = mission.actualSpentSeconds + seconds)
        dao.insertOrUpdateDailyMission(updated)
    }
}
