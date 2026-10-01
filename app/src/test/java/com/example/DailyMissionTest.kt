package com.example

import com.example.data.mission.DailyMissionEntity
import com.example.data.mission.MissionTask
import com.example.data.mission.MissionTaskType
import com.example.data.studytools.AcademicContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DailyMissionTest {

    @Test
    fun testTaskSerializationAndParsing() {
        val tasks = listOf(
            MissionTask(
                id = "task_1",
                title = "Understand Pointers & Dynamic Memory",
                description = "Read core concept breakdown and syntax",
                estimatedMinutes = 10,
                taskType = MissionTaskType.LEARN,
                isCompleted = false
            ),
            MissionTask(
                id = "task_2",
                title = "Solve Pointer Arithmetic Exercise",
                description = "Practice memory address calculations",
                estimatedMinutes = 10,
                taskType = MissionTaskType.PRACTICE,
                isCompleted = true,
                completedAt = 1700000000L
            )
        )

        val json = DailyMissionEntity.serializeTasks(tasks)
        assertNotNull(json)
        assertTrue(json.contains("task_1"))
        assertTrue(json.contains("task_2"))

        val parsed = DailyMissionEntity.deserializeTasks(json)
        assertEquals(2, parsed.size)
        assertEquals("task_1", parsed[0].id)
        assertFalse(parsed[0].isCompleted)
        assertEquals(MissionTaskType.LEARN, parsed[0].taskType)

        assertEquals("task_2", parsed[1].id)
        assertTrue(parsed[1].isCompleted)
        assertEquals(1700000000L, parsed[1].completedAt)
        assertEquals(MissionTaskType.PRACTICE, parsed[1].taskType)
    }

    @Test
    fun testMissionEntityAcademicContextConversion() {
        val mission = DailyMissionEntity(
            id = "mission_2026-09-20",
            dateKey = "2026-09-20",
            department = "CSE (AI & ML)",
            programme = "B. Tech CSE (AI & ML)",
            regulation = "R25",
            semester = 2,
            courseCode = "CS201",
            courseName = "Data Structures",
            module = "Module 2",
            topic = "Binary Search Trees",
            goal = "Master Binary Search Tree Operations in Data Structures",
            estimatedMinutes = 20,
            tasksJson = "[]"
        )

        val context = mission.toAcademicContext()
        assertEquals("CSE (AI & ML)", context.department)
        assertEquals("B. Tech CSE (AI & ML)", context.programme)
        assertEquals("R25", context.regulation)
        assertEquals(2, context.semester)
        assertEquals("CS201", context.courseCode)
        assertEquals("Data Structures", context.courseName)
        assertEquals("Module 2", context.module)
        assertEquals("Binary Search Trees", context.topic)
    }

    @Test
    fun testTaskCompletionCalculations() {
        val tasks = listOf(
            MissionTask(id = "1", title = "Task 1", estimatedMinutes = 5, isCompleted = true),
            MissionTask(id = "2", title = "Task 2", estimatedMinutes = 10, isCompleted = true),
            MissionTask(id = "3", title = "Task 3", estimatedMinutes = 5, isCompleted = false)
        )

        val mission = DailyMissionEntity(
            id = "test_mission",
            dateKey = "2026-09-20",
            department = "CSE",
            programme = "B.Tech",
            regulation = "R25",
            semester = 1,
            courseCode = "CS101",
            courseName = "Intro to Programming",
            module = "Module 1",
            topic = "Conditionals",
            goal = "Understand Conditionals",
            estimatedMinutes = 20,
            tasksJson = DailyMissionEntity.serializeTasks(tasks),
            completed = false
        )

        val parsedTasks = mission.parseTasks()
        val completedCount = parsedTasks.count { it.isCompleted }
        assertEquals(2, completedCount)
        assertEquals(3, parsedTasks.size)
        assertFalse(mission.completed)
    }

    @Test
    fun testAcademicProfileConstrainedMission() {
        val profile = com.example.data.profile.AcademicProfile(
            departmentId = "it",
            departmentName = "Information Technology",
            programmeId = "btech_it",
            programmeName = "B. Tech Information Technology",
            regulation = "R25",
            semester = 3
        )

        assertEquals("it", profile.departmentId)
        assertEquals("Information Technology", profile.departmentName)
        assertEquals(3, profile.semester)
        assertEquals("R25", profile.regulation)

        val entity = profile.toEntity()
        assertEquals(profile.departmentId, entity.departmentId)
        assertEquals(profile.semester, entity.semester)

        val restored = entity.toDomain()
        assertEquals(profile.departmentName, restored.departmentName)
        assertEquals(profile.semester, restored.semester)
    }
}
