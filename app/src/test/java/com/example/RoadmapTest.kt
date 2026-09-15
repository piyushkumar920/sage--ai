package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.roadmap.RoadmapLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoadmapTest {

    @Test
    fun testRoadmapsLoadFromAssets() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val loader = RoadmapLoader(context)
        val summaries = loader.getSummaries()
        assertTrue("Expected devroadmaps summaries to be non-empty", summaries.isNotEmpty())
        assertEquals(20, summaries.size)

        val fullstack = loader.getRoadmapDetail("fullstack")
        assertNotNull("Expected fullstack roadmap detail to load", fullstack)
        assertEquals("fullstack", fullstack?.id)
        assertTrue("Expected fullstack nodes to be non-empty", fullstack?.nodes?.isNotEmpty() == true)
        assertTrue("Expected fullstack categories to be non-empty", fullstack?.categories?.isNotEmpty() == true)
    }
}
