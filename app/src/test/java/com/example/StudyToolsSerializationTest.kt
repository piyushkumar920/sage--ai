package com.example.studytools

import com.example.data.studytools.AcademicContext
import com.example.data.studytools.FormulaItem
import com.example.data.studytools.MindMapNode
import com.example.data.studytools.SageStudyToolsRequest
import com.example.data.studytools.SageStudyToolsResponse
import com.example.data.studytools.SolutionStep
import com.example.data.studytools.StudyFlashcard
import com.example.data.studytools.StudyFlashcardsData
import com.example.data.studytools.StudyFormulaSheetData
import com.example.data.studytools.StudyMindMapData
import com.example.data.studytools.StudyNotesData
import com.example.data.studytools.StudyNotesSection
import com.example.data.studytools.StudyRevisionSheetData
import com.example.data.studytools.StudySolutionData
import com.example.data.studytools.StudyToolType
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StudyToolsSerializationTest {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun `test notes data model and json serialization`() {
        val notes = StudyNotesData(
            type = "notes",
            title = "Binary Search Trees",
            summary = "A binary search tree is a rooted binary tree data structure.",
            sections = listOf(
                StudyNotesSection(
                    heading = "Properties",
                    content = "Left child is smaller, right child is greater.",
                    keyPoints = listOf("Left subtree < root", "Right subtree > root")
                )
            ),
            examples = listOf("BST search of key 42"),
            importantTerms = listOf("In-order traversal", "Balanced BST"),
            beyondSyllabus = false
        )
        val adapter = moshi.adapter(StudyNotesData::class.java)
        val json = adapter.toJson(notes)
        assertNotNull(json)
        val parsed = adapter.fromJson(json)
        assertNotNull(parsed)
        assertEquals("Binary Search Trees", parsed?.title)
        assertEquals(1, parsed?.sections?.size)
        assertEquals(2, parsed?.sections?.first()?.keyPoints?.size)
    }

    @Test
    fun `test flashcards model serialization`() {
        val flashcards = StudyFlashcardsData(
            type = "flashcards",
            title = "Discrete Math Sets",
            cards = listOf(
                StudyFlashcard(front = "What is a power set?", back = "Set of all subsets", hint = "Includes null set"),
                StudyFlashcard(front = "What is set cardinality?", back = "Number of elements in the set", hint = "Size")
            )
        )
        val adapter = moshi.adapter(StudyFlashcardsData::class.java)
        val json = adapter.toJson(flashcards)
        assertNotNull(json)
        val parsed = adapter.fromJson(json)
        assertEquals(2, parsed?.cards?.size)
        assertEquals("What is a power set?", parsed?.cards?.get(0)?.front)
        assertEquals("Set of all subsets", parsed?.cards?.get(0)?.back)
    }

    @Test
    fun `test mind map model serialization`() {
        val mindMap = StudyMindMapData(
            type = "mindmap",
            title = "Operating Systems",
            root = MindMapNode(
                id = "root",
                label = "Operating Systems",
                description = "Core software managing hardware and processes",
                children = listOf(
                    MindMapNode(id = "proc", label = "Process Management", description = "Scheduling & threads"),
                    MindMapNode(id = "mem", label = "Memory Management", description = "Paging & segmentation")
                )
            )
        )
        val adapter = moshi.adapter(StudyMindMapData::class.java)
        val json = adapter.toJson(mindMap)
        assertNotNull(json)
        val parsed = adapter.fromJson(json)
        assertEquals("Operating Systems", parsed?.title)
        assertEquals(2, parsed?.root?.children?.size)
        assertEquals("Process Management", parsed?.root?.children?.get(0)?.label)
    }

    @Test
    fun `test revision sheet model serialization`() {
        val revision = StudyRevisionSheetData(
            type = "revision",
            title = "Thermodynamics",
            coreConcepts = listOf("First Law: Energy Conservation", "Second Law: Entropy"),
            definitions = listOf("Enthalpy: H = U + PV"),
            keyFacts = listOf("Carnot cycle efficiency is maximal"),
            importantFormulas = listOf("ΔU = Q - W"),
            commonMistakes = listOf("Confusing sign convention for work done"),
            quickExamples = listOf("Calculate work done in isobaric process: W = PΔV"),
            lastMinuteRevisionPoints = listOf("Remember state functions vs path functions"),
            beyondSyllabus = false
        )
        val adapter = moshi.adapter(StudyRevisionSheetData::class.java)
        val json = adapter.toJson(revision)
        assertNotNull(json)
        val parsed = adapter.fromJson(json)
        assertEquals("Thermodynamics", parsed?.title)
        assertEquals(2, parsed?.coreConcepts?.size)
        assertEquals("ΔU = Q - W", parsed?.importantFormulas?.first())
    }

    @Test
    fun `test formula sheet model serialization`() {
        val formulas = StudyFormulaSheetData(
            type = "formulas",
            title = "Linear Algebra",
            hasFormulas = true,
            formulas = listOf(
                FormulaItem(
                    name = "Determinant 2x2",
                    formula = "ad - bc",
                    variables = listOf("a, b, c, d: Matrix elements"),
                    units = "Dimensionless",
                    usageExplanation = "Invertibility test",
                    example = "det([[2, 1], [3, 4]]) = 8 - 3 = 5"
                )
            ),
            notes = "Determinant non-zero implies invertible matrix"
        )
        val adapter = moshi.adapter(StudyFormulaSheetData::class.java)
        val json = adapter.toJson(formulas)
        assertNotNull(json)
        val parsed = adapter.fromJson(json)
        assertEquals("ad - bc", parsed?.formulas?.get(0)?.formula)
        assertEquals("Linear Algebra", parsed?.title)
    }

    @Test
    fun `test scan and solve model serialization`() {
        val solve = StudySolutionData(
            type = "solution",
            isClear = true,
            problem = "Solve 2x + 4 = 10",
            subject = "Algebra",
            answer = "x = 3",
            steps = listOf(
                SolutionStep(step = 1, title = "Subtract 4", explanation = "Subtract 4 from both sides: 2x = 6"),
                SolutionStep(step = 2, title = "Divide by 2", explanation = "Divide both sides by 2: x = 3")
            ),
            finalAnswer = "x = 3",
            needsVerification = false
        )
        val adapter = moshi.adapter(StudySolutionData::class.java)
        val json = adapter.toJson(solve)
        assertNotNull(json)
        val parsed = adapter.fromJson(json)
        assertEquals("x = 3", parsed?.finalAnswer)
        assertEquals(2, parsed?.steps?.size)
        assertEquals("Subtract 4", parsed?.steps?.first()?.title)
    }

    @Test
    fun `test study tool request with academic context serialization`() {
        val request = SageStudyToolsRequest(
            operation = "notes",
            topic = "Heaps and Priority Queues",
            subject = "Data Structures & Algorithms",
            syllabusContext = "Binary heaps, min-heap, max-heap, heapify, heapsort",
            prompt = "Focus on heapify runtime derivation",
            academicContext = AcademicContext(
                department = "CSE (AI & ML)",
                programme = "B. Tech CSE (AI & ML)",
                regulation = "R25",
                semester = 3,
                courseCode = "CS301",
                courseName = "Data Structures & Algorithms",
                module = "Module 4: Trees and Heaps",
                topic = "Heaps and Priority Queues",
                officialSyllabusContent = "Binary heaps, min-heap, max-heap, heapify, heapsort"
            )
        )
        val adapter = moshi.adapter(SageStudyToolsRequest::class.java)
        val json = adapter.toJson(request)
        assertNotNull(json)
        val parsed = adapter.fromJson(json)
        assertEquals("notes", parsed?.operation)
        assertEquals("CS301", parsed?.academicContext?.courseCode)
        assertEquals(3, parsed?.academicContext?.semester)
    }

    @Test
    fun `test study tool types enum values`() {
        assertEquals(6, StudyToolType.values().size)
        assertTrue(StudyToolType.values().any { it.id == "solve_image" })
        assertTrue(StudyToolType.values().any { it.id == "notes" })
        assertTrue(StudyToolType.values().any { it.id == "flashcards" })
        assertTrue(StudyToolType.values().any { it.id == "mindmap" })
        assertTrue(StudyToolType.values().any { it.id == "revision" })
        assertTrue(StudyToolType.values().any { it.id == "formulas" })
    }
}
