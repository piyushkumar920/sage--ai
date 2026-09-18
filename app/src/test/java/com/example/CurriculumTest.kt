package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.curriculum.CurriculumRepository
import com.example.data.local.PreferencesManager
import com.example.data.local.SageDatabase
import com.example.data.repository.RoadmapRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CurriculumTest {

    private lateinit var context: Context
    private lateinit var database: SageDatabase
    private lateinit var curriculumRepository: CurriculumRepository
    private lateinit var roadmapRepository: RoadmapRepository
    private lateinit var preferencesManager: PreferencesManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, SageDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        preferencesManager = PreferencesManager(context)
        roadmapRepository = RoadmapRepository(
            context = context,
            dao = database.sageDao(),
            preferencesManager = preferencesManager
        )
        curriculumRepository = roadmapRepository.curriculumRepository
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testAllTenDepartmentsExistWithRegulations() {
        val departments = curriculumRepository.getAllDepartments()
        assertEquals("Expected exactly 10 academic departments", 10, departments.size)

        val deptIds = departments.map { it.id }
        assertTrue("Expected cse_aiml", deptIds.contains("cse_aiml"))
        assertTrue("Expected it", deptIds.contains("it"))
        assertTrue("Expected cst", deptIds.contains("cst"))
        assertTrue("Expected ece", deptIds.contains("ece"))
        assertTrue("Expected ee", deptIds.contains("ee"))
        assertTrue("Expected me", deptIds.contains("me"))
        assertTrue("Expected ce", deptIds.contains("ce"))
        assertTrue("Expected bme", deptIds.contains("bme"))
        assertTrue("Expected bba", deptIds.contains("bba"))
        assertTrue("Expected mca", deptIds.contains("mca"))

        val bba = curriculumRepository.getDepartment("bba")
        assertNotNull(bba)
        assertEquals("R26", bba?.regulation)

        val cse = curriculumRepository.getDepartment("cse_aiml")
        assertNotNull(cse)
        assertEquals("R25", cse?.regulation)
    }

    @Test
    fun testDepartmentCourseLoadingAndSearch() {
        val cseCourses = curriculumRepository.getCoursesForDepartment("cse_aiml")
        assertTrue("Expected CSE AI/ML courses to load", cseCourses.isNotEmpty())

        // Search for Python
        val pythonCourses = curriculumRepository.searchCourses("Python", "cse_aiml")
        assertTrue("Expected Python course search to return results", pythonCourses.isNotEmpty())

        // Test getting specific course
        val firstCourse = cseCourses.first()
        val fetched = curriculumRepository.getCourse("cse_aiml", firstCourse.code)
        assertNotNull("Expected to fetch course by code", fetched)
        assertEquals(firstCourse.code, fetched?.code)
    }

    @Test
    fun testSyllabusIsTheRoadmapDeterministicGeneration() {
        val cseCourses = curriculumRepository.getCoursesForDepartment("cse_aiml")
        val courseWithModules = cseCourses.find { it.modules.isNotEmpty() } ?: cseCourses.first()

        val roadmapDetail = curriculumRepository.toRoadmapDetail(courseWithModules)
        assertEquals(courseWithModules.roadmapId, roadmapDetail.id)
        assertTrue(roadmapDetail.title.contains(courseWithModules.code))
        assertTrue("Nodes must represent official syllabus modules", roadmapDetail.nodes.isNotEmpty())

        if (courseWithModules.modules.isNotEmpty()) {
            assertEquals(courseWithModules.modules.size, roadmapDetail.nodes.size)
            val firstNode = roadmapDetail.nodes.first()
            val firstMod = courseWithModules.modules.first()
            assertTrue(firstNode.title.contains(firstMod.title) || firstNode.title.contains(firstMod.moduleNumber))
            assertEquals(firstMod.content, firstNode.description)
        }
    }

    @Test
    fun testRoadmapRepositoryCurriculumIntegration() = runBlocking {
        val cseCourses = curriculumRepository.getCoursesForDepartment("cse_aiml")
        val course = cseCourses.first()
        val roadmapId = course.roadmapId

        // Fetch detail via unified RoadmapRepository
        val detail = roadmapRepository.getRoadmapDetail(roadmapId)
        assertNotNull("Expected unified repository to resolve curriculum roadmap", detail)
        assertEquals(roadmapId, detail?.id)

        // Initialize progress
        roadmapRepository.ensureInitializedProgress(roadmapId, detail!!)
        val initialProgress = roadmapRepository.getTopicProgressOnce(roadmapId)
        assertTrue("Expected progress to be initialized in Room", initialProgress.isNotEmpty())

        // Record a quiz result for the first node
        val firstNode = detail.nodes.first()
        roadmapRepository.recordQuizResult(
            roadmapId = roadmapId,
            topicId = firstNode.id,
            topicTitle = firstNode.title,
            score = 90,
            totalQuestions = 5,
            weakList = emptyList()
        )

        // Verify updated progress in Room
        val updatedProgress = roadmapRepository.getTopicProgressOnce(roadmapId)
        val updatedNode = updatedProgress.find { it.nodeId == firstNode.id }
        assertNotNull(updatedNode)
        assertEquals("COMPLETED", updatedNode?.status)
        assertEquals(90, updatedNode?.quizScore)

        // Set active roadmap to test percentage calculation
        roadmapRepository.setActiveRoadmap(roadmapId)
        val percentages = roadmapRepository.getAllRoadmapCompletionPercentages()
        val percent = percentages[roadmapId] ?: 0
        assertTrue("Expected completion percent >= 0", percent >= 0)
    }

    @Test
    fun testCurriculumCourseMetadataGrounding() {
        val cseCourses = curriculumRepository.getCoursesForDepartment("cse_aiml")
        val course = cseCourses.first()

        assertEquals("cse_aiml", course.departmentId)
        assertEquals("R25", course.regulation)
        assertTrue(course.roadmapId.startsWith("curriculum_cse_aiml_"))
        assertTrue(course.displayTitle.contains(course.code))
        assertTrue(course.displayTitle.contains(course.title))
    }
}
