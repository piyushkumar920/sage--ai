package com.example

import com.example.data.studytools.AcademicContext
import com.example.ui.screens.pyq.OFFICIAL_PYQ_DRIVE_URL
import com.example.ui.screens.pyq.generateQrBitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PYQTest {

    @Test
    fun testOfficialPyqDriveUrl() {
        assertEquals(
            "https://drive.google.com/drive/folders/1ErNTEtC5OxzpA-YU1W6e8Uv_ZpxbbTaC",
            OFFICIAL_PYQ_DRIVE_URL
        )
    }

    @Test
    fun testGenerateQrBitmap() {
        val bitmap = generateQrBitmap(OFFICIAL_PYQ_DRIVE_URL, 256)
        assertNotNull("QR Bitmap should be generated successfully", bitmap)
        assertEquals(256, bitmap?.width)
        assertEquals(256, bitmap?.height)
    }

    @Test
    fun testAcademicContextIntegration() {
        val context = AcademicContext(
            department = "CSE (AI & ML)",
            programme = "B. Tech",
            regulation = "R25",
            semester = 3,
            courseCode = "CS301",
            courseName = "Operating Systems",
            topic = "Process Scheduling Algorithms"
        )
        assertEquals("Operating Systems", context.courseName)
        assertEquals("CSE (AI & ML)", context.department)
        assertEquals(3, context.semester)
    }
}
