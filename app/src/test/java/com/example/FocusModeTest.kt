package com.example

import com.example.data.focus.FocusRepository
import com.example.data.studytools.AcademicContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FocusModeTest {

    @Test
    fun testDeriveGoalForContextWithFullData() {
        val context = AcademicContext(
            department = "CSE (AI & ML)",
            programme = "B. Tech",
            regulation = "R25",
            semester = 3,
            courseCode = "CS301",
            courseName = "Operating Systems",
            module = "Module 2",
            topic = "Process Scheduling & Deadlocks"
        )
        val goal = FocusRepository.deriveDefaultGoal(context)
        assertEquals("Master Process Scheduling & Deadlocks in Operating Systems", goal)
    }

    @Test
    fun testDeriveGoalForContextWithFallback() {
        val goal = FocusRepository.deriveDefaultGoal(null)
        assertEquals("Complete current curriculum module", goal)
    }

    @Test
    fun testFormatDuration() {
        val formattedZero = FocusRepository.formatDuration(0)
        assertEquals("00:00", formattedZero)

        val formatted25Min = FocusRepository.formatDuration(25 * 60)
        assertEquals("25:00", formatted25Min)

        val formattedWithHours = FocusRepository.formatDuration(3665)
        assertEquals("01:01:05", formattedWithHours)
    }

    @Test
    fun testAcademicContextFormatting() {
        val context = AcademicContext(
            department = "Information Technology",
            programme = "B.Tech. in Information Technology",
            regulation = "R25",
            semester = 4,
            courseCode = "IT401",
            courseName = "Database Management Systems",
            module = "Module 3",
            topic = "Normalization & Normal Forms"
        )
        assertTrue(context.getFormattedHierarchy().contains("Information Technology"))
        assertTrue(context.getFormattedHierarchy().contains("IT401"))
        assertTrue(context.getFormattedHierarchy().contains("Normalization & Normal Forms"))
    }
}
