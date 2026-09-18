package com.example.data.curriculum

import android.content.Context
import android.util.Log
import com.example.data.roadmap.DevRoadmapDetail
import com.example.data.roadmap.DevRoadmapSummary
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.util.regex.Pattern

class CurriculumRepository(private val context: Context) {

    private val departments: List<CurriculumDepartment> = listOf(
        CurriculumDepartment(
            id = "cse_aiml",
            name = "Computer Science & Engineering (AI & ML)",
            shortName = "CSE (AI & ML)",
            programme = "B. Tech CSE (AI & ML)",
            regulation = "R25",
            icon = "🤖",
            assetFileName = "cse_aiml_r25_database.json",
            totalCourses = 80,
            availableSemesters = (1..8).toList()
        ),
        CurriculumDepartment(
            id = "it",
            name = "Information Technology",
            shortName = "IT",
            programme = "B.Tech. in Information Technology",
            regulation = "R25",
            icon = "💻",
            assetFileName = "it_r25_database.json",
            totalCourses = 88,
            availableSemesters = (1..8).toList()
        ),
        CurriculumDepartment(
            id = "cst",
            name = "Computer Science and Technology",
            shortName = "CST",
            programme = "B.Tech. in Computer Science and Technology",
            regulation = "R25",
            icon = "🖥️",
            assetFileName = "cst_r25_database.json",
            totalCourses = 80,
            availableSemesters = (1..8).toList()
        ),
        CurriculumDepartment(
            id = "ece",
            name = "Electronics and Communication Engineering",
            shortName = "ECE",
            programme = "B.Tech. in Electronics and Communication Engineering",
            regulation = "R25",
            icon = "📡",
            assetFileName = "ece_r25_database.json",
            totalCourses = 109,
            availableSemesters = (1..8).toList()
        ),
        CurriculumDepartment(
            id = "ee",
            name = "Electrical Engineering",
            shortName = "EE",
            programme = "B.Tech. in Electrical Engineering",
            regulation = "R25",
            icon = "⚡",
            assetFileName = "ee_r25_database.json",
            totalCourses = 87,
            availableSemesters = (1..8).toList()
        ),
        CurriculumDepartment(
            id = "me",
            name = "Mechanical Engineering",
            shortName = "ME",
            programme = "B.Tech in Mechanical Engineering",
            regulation = "R25",
            icon = "⚙️",
            assetFileName = "me_r25_database.json",
            totalCourses = 107,
            availableSemesters = (1..8).toList()
        ),
        CurriculumDepartment(
            id = "ce",
            name = "Civil Engineering",
            shortName = "CE",
            programme = "B.Tech. in Civil Engineering",
            regulation = "R25",
            icon = "🏗️",
            assetFileName = "ce_r25_database.json",
            totalCourses = 89,
            availableSemesters = (1..8).toList()
        ),
        CurriculumDepartment(
            id = "bme",
            name = "Biomedical Engineering",
            shortName = "BME",
            programme = "B.Tech in Biomedical Engineering",
            regulation = "R25",
            icon = "🧬",
            assetFileName = "bme_r25_database.json",
            totalCourses = 76,
            availableSemesters = (1..8).toList()
        ),
        CurriculumDepartment(
            id = "bba",
            name = "Department of Business Administration",
            shortName = "BBA",
            programme = "BBA (General)",
            regulation = "R26",
            icon = "📊",
            assetFileName = "bba_r26_database.json",
            totalCourses = 99,
            availableSemesters = (1..8).toList()
        ),
        CurriculumDepartment(
            id = "mca",
            name = "Department of Computer Application",
            shortName = "MCA",
            programme = "Master of Computer Applications (MCA)",
            regulation = "R25",
            icon = "🎓",
            assetFileName = "mca_r25_database.json",
            totalCourses = 64,
            availableSemesters = (1..4).toList()
        )
    )

    private val departmentCache = mutableMapOf<String, List<CurriculumCourse>>()
    private val allCoursesFlat = mutableListOf<CurriculumCourse>()
    private var allCoursesLoaded = false

    fun getAllDepartments(): List<CurriculumDepartment> = departments

    fun getDepartment(id: String): CurriculumDepartment? =
        departments.find { it.id.equals(id, ignoreCase = true) }

    @Synchronized
    fun getCoursesForDepartment(deptId: String): List<CurriculumCourse> {
        departmentCache[deptId]?.let { return it }

        val dept = getDepartment(deptId) ?: return emptyList()
        val parsed = loadDepartmentCourses(dept)
        departmentCache[deptId] = parsed
        return parsed
    }

    fun getCoursesForSemester(deptId: String, semester: Int): List<CurriculumCourse> {
        return getCoursesForDepartment(deptId).filter { it.semester == semester }
    }

    fun getCourse(deptId: String, courseCode: String): CurriculumCourse? {
        val courses = getCoursesForDepartment(deptId)
        val cleanCode = courseCode.replace(" ", "").uppercase()
        return courses.find { it.code.replace(" ", "").equals(cleanCode, ignoreCase = true) }
    }

    fun getCourseByRoadmapId(roadmapId: String): CurriculumCourse? {
        if (!roadmapId.startsWith("curriculum_")) return null
        val rest = roadmapId.removePrefix("curriculum_")
        // Match department by id prefix to handle departments with underscores (e.g. cse_aiml)
        val matchingDept = departments.find { rest.startsWith("${it.id}_") }
        if (matchingDept != null) {
            val courses = getCoursesForDepartment(matchingDept.id)
            val direct = courses.find { it.roadmapId.equals(roadmapId, ignoreCase = true) }
            if (direct != null) return direct
            val courseCode = rest.removePrefix("${matchingDept.id}_")
            return getCourse(matchingDept.id, courseCode)
        }
        return null
    }

    fun toRoadmapDetail(course: CurriculumCourse): DevRoadmapDetail {
        val dept = getDepartment(course.departmentId)
        val icon = dept?.icon ?: "📘"
        return course.toDevRoadmapDetail(icon)
    }

    @Synchronized
    fun getAllCourses(): List<CurriculumCourse> {
        if (allCoursesLoaded) return allCoursesFlat

        allCoursesFlat.clear()
        for (dept in departments) {
            allCoursesFlat.addAll(getCoursesForDepartment(dept.id))
        }
        allCoursesLoaded = true
        return allCoursesFlat
    }

    fun searchCourses(query: String, deptFilter: String? = null): List<CurriculumCourse> {
        val trimmed = query.trim().lowercase()
        val source = if (!deptFilter.isNullOrBlank() && !deptFilter.equals("all", ignoreCase = true)) {
            getCoursesForDepartment(deptFilter)
        } else {
            getAllCourses()
        }

        if (trimmed.isEmpty()) {
            return source
        }

        return source.filter { course ->
            course.code.lowercase().contains(trimmed) ||
                    course.title.lowercase().contains(trimmed) ||
                    course.departmentName.lowercase().contains(trimmed) ||
                    course.category.lowercase().contains(trimmed) ||
                    course.modules.any { it.title.lowercase().contains(trimmed) || it.content.lowercase().contains(trimmed) }
        }
    }

    fun getAllCurriculumRoadmapSummaries(): List<DevRoadmapSummary> {
        val list = mutableListOf<DevRoadmapSummary>()
        for (dept in departments) {
            val courses = getCoursesForDepartment(dept.id)
            for (c in courses) {
                list.add(c.toDevRoadmapSummary(dept.icon))
            }
        }
        return list
    }

    private fun loadDepartmentCourses(dept: CurriculumDepartment): List<CurriculumCourse> {
        try {
            val fullPath = "curriculum/${dept.assetFileName}"
            val jsonString = context.assets.open(fullPath).bufferedReader().use { it.readText() }
            val rootObj = JSONObject(jsonString)

            return when (dept.id) {
                "bba" -> parseBba(rootObj, dept)
                "bme" -> parseBme(rootObj, dept)
                "cse_aiml" -> parseCseAiml(rootObj, dept)
                "mca" -> parseMca(rootObj, dept)
                else -> parseFlatCourses(rootObj, dept)
            }
        } catch (e: Exception) {
            Log.e("CurriculumRepository", "Error loading ${dept.assetFileName}", e)
            return emptyList()
        }
    }

    private fun parseCseAiml(root: JSONObject, dept: CurriculumDepartment): List<CurriculumCourse> {
        val list = mutableListOf<CurriculumCourse>()
        val coursesArray = root.optJSONArray("courses")
        val coursesObj = root.optJSONObject("courses")

        if (coursesArray != null) {
            for (i in 0 until coursesArray.length()) {
                val obj = coursesArray.optJSONObject(i) ?: continue
                parseCseAimlCourse(obj, dept)?.let { list.add(it) }
            }
        } else if (coursesObj != null) {
            val keys = coursesObj.keys()
            val sortedKeys = mutableListOf<String>()
            while (keys.hasNext()) {
                sortedKeys.add(keys.next())
            }
            sortedKeys.sortBy { it.toIntOrNull() ?: 999 }
            for (key in sortedKeys) {
                val obj = coursesObj.optJSONObject(key) ?: continue
                parseCseAimlCourse(obj, dept)?.let { list.add(it) }
            }
        }
        return list
    }

    private fun parseCseAimlCourse(obj: JSONObject, dept: CurriculumDepartment): CurriculumCourse? {
        val code = obj.optString("code", "").trim()
        val title = obj.optString("title", "").trim()
        if (code.isEmpty() && title.isEmpty()) return null

        val sem = obj.optInt("semester", 1)
        val year = obj.optInt("year", (sem + 1) / 2)
        val credit = obj.optString("credit", obj.optString("credits_stated_in_course", "3"))
        val contact = obj.optString("contact", "")
        val category = obj.optString("category", obj.optString("broad_category", "Core"))
        val courseType = obj.optString("course_type", "Theory").replaceFirstChar { it.uppercase() }
        val prerequisites = obj.optString("prerequisites", "")
        val objectives = obj.optString("objectives", "")
        val outcomes = obj.optString("outcomes", "")
        val syllabusText = obj.optString("syllabus", obj.optString("raw_course_section", ""))

        val modules = parseTextToModules("${dept.id}_$code", syllabusText)

        return CurriculumCourse(
            id = "${dept.id}_$code",
            code = code,
            title = title,
            departmentId = dept.id,
            departmentName = dept.shortName,
            programme = dept.programme,
            regulation = dept.regulation,
            semester = sem,
            year = year,
            credits = credit,
            contact = contact,
            category = category,
            courseType = courseType,
            prerequisites = prerequisites,
            objectives = objectives,
            outcomes = outcomes,
            modules = modules,
            rawSyllabus = syllabusText
        )
    }

    private fun parseBba(root: JSONObject, dept: CurriculumDepartment): List<CurriculumCourse> {
        val list = mutableListOf<CurriculumCourse>()
        val coursesArray = root.optJSONArray("detailed_courses") ?: return emptyList()

        for (i in 0 until coursesArray.length()) {
            val obj = coursesArray.optJSONObject(i) ?: continue
            val code = obj.optString("course_code", "").trim()
            val title = obj.optString("title", "").trim()
            if (code.isEmpty() && title.isEmpty()) continue

            val semStr = obj.optString("semester", "1")
            val sem = semStr.filter { it.isDigit() }.toIntOrNull() ?: 1
            val year = (sem + 1) / 2
            val credit = obj.optString("credit", "4")
            val category = obj.optString("course_category", "Major")
            val kind = obj.optString("kind", "Theory")

            val modulesArray = obj.optJSONArray("modules")
            val modules = mutableListOf<CurriculumModule>()
            if (modulesArray != null) {
                for (m in 0 until modulesArray.length()) {
                    val mObj = modulesArray.optJSONObject(m) ?: continue
                    val mLabel = mObj.optString("module", "Module ${m + 1}").trim()
                    val content = mObj.optString("content", "").trim()
                    val hours = mObj.optDouble("hours", 0.0)
                    modules.add(
                        CurriculumModule(
                            id = "${dept.id}_${code}_m$m",
                            moduleIndex = m,
                            moduleNumber = mLabel,
                            title = "$mLabel: " + content.lines().firstOrNull()?.take(55)?.trim().orEmpty(),
                            content = content,
                            hours = if (hours > 0) "${hours.toInt()} Hours" else null
                        )
                    )
                }
            }

            list.add(
                CurriculumCourse(
                    id = "${dept.id}_${code.replace(" ", "")}",
                    code = code,
                    title = title,
                    departmentId = dept.id,
                    departmentName = dept.shortName,
                    programme = dept.programme,
                    regulation = dept.regulation,
                    semester = sem,
                    year = year,
                    credits = credit,
                    category = category,
                    courseType = kind,
                    modules = modules
                )
            )
        }
        return list
    }

    private fun parseBme(root: JSONObject, dept: CurriculumDepartment): List<CurriculumCourse> {
        val list = mutableListOf<CurriculumCourse>()
        val curriculumArray = root.optJSONArray("curriculum") ?: JSONArray()
        val syllabiArray = root.optJSONArray("course_syllabi") ?: JSONArray()

        val syllabiMap = mutableMapOf<String, JSONObject>()
        for (s in 0 until syllabiArray.length()) {
            val sObj = syllabiArray.optJSONObject(s) ?: continue
            val code = sObj.optString("code", sObj.optString("course_code", "")).trim()
            if (code.isNotEmpty()) {
                syllabiMap[code] = sObj
            }
        }

        for (c in 0 until curriculumArray.length()) {
            val cObj = curriculumArray.optJSONObject(c) ?: continue
            val code = cObj.optString("course_code", "").trim()
            val syllabusObj = syllabiMap[code]

            val title = cObj.optString("course_title", "").ifBlank {
                syllabusObj?.optString("name", "") ?: code
            }.trim()

            val sem = cObj.optInt("semester", syllabusObj?.optInt("semester", 1) ?: 1)
            val year = cObj.optInt("year", (sem + 1) / 2)
            val credits = cObj.optString("credits", syllabusObj?.optString("credit", "3"))
            val category = cObj.optString("category", "Core")
            val contact = cObj.optString("hours_per_week", syllabusObj?.optString("contact", ""))
            val courseContent = syllabusObj?.optString("course_content", "") ?: ""
            val textbooks = syllabusObj?.optString("textbooks", "") ?: ""
            val referenceBooks = syllabusObj?.optString("reference_books", "") ?: ""
            val prereq = syllabusObj?.optString("prerequisites", "") ?: ""
            val obj = syllabusObj?.optString("objectives", "") ?: ""
            val outcomes = syllabusObj?.optString("outcomes", "") ?: ""

            val modules = parseTextToModules("${dept.id}_$code", courseContent)

            list.add(
                CurriculumCourse(
                    id = "${dept.id}_$code",
                    code = code,
                    title = title,
                    departmentId = dept.id,
                    departmentName = dept.shortName,
                    programme = dept.programme,
                    regulation = dept.regulation,
                    semester = sem,
                    year = year,
                    credits = credits,
                    contact = contact,
                    category = category,
                    courseType = if (code.contains("19") || code.contains("91")) "Practical" else "Theory",
                    prerequisites = prereq,
                    objectives = obj,
                    outcomes = outcomes,
                    textbooks = textbooks,
                    referenceBooks = referenceBooks,
                    modules = modules,
                    rawSyllabus = courseContent
                )
            )
        }
        return list
    }

    private fun parseMca(root: JSONObject, dept: CurriculumDepartment): List<CurriculumCourse> {
        val list = mutableListOf<CurriculumCourse>()
        val coursesArray = root.optJSONArray("detailed_courses") ?: return emptyList()

        for (i in 0 until coursesArray.length()) {
            val obj = coursesArray.optJSONObject(i) ?: continue
            val code = obj.optString("course_code", "").trim()
            val title = obj.optString("title", "").trim()
            if (code.isEmpty() && title.isEmpty()) continue

            val sem = obj.optInt("semester", 1)
            val year = (sem + 1) / 2
            val credit = obj.optString("credit", "3")
            val contact = obj.optString("contact", "")
            val prereq = obj.optString("prerequisite", "")

            val modulesArray = obj.optJSONArray("modules")
            val modules = mutableListOf<CurriculumModule>()
            if (modulesArray != null) {
                for (m in 0 until modulesArray.length()) {
                    val mObj = modulesArray.optJSONObject(m) ?: continue
                    val mNum = mObj.optString("module", "Module ${m + 1}").trim()
                    val mTitle = mObj.optString("title", "").trim()
                    val content = mObj.optString("content", "").trim()
                    val hours = mObj.optDouble("hours", 0.0)

                    val displayTitle = if (mTitle.isNotBlank()) "$mNum: $mTitle" else "$mNum: " + content.lines().firstOrNull()?.take(50).orEmpty()
                    modules.add(
                        CurriculumModule(
                            id = "${dept.id}_${code}_m$m",
                            moduleIndex = m,
                            moduleNumber = mNum,
                            title = displayTitle,
                            content = content,
                            hours = if (hours > 0) "${hours.toInt()} Hours" else null
                        )
                    )
                }
            }

            list.add(
                CurriculumCourse(
                    id = "${dept.id}_$code",
                    code = code,
                    title = title,
                    departmentId = dept.id,
                    departmentName = dept.shortName,
                    programme = dept.programme,
                    regulation = dept.regulation,
                    semester = sem,
                    year = year,
                    credits = credit,
                    contact = contact,
                    prerequisites = prereq,
                    modules = modules
                )
            )
        }
        return list
    }

    private fun parseFlatCourses(root: JSONObject, dept: CurriculumDepartment): List<CurriculumCourse> {
        val list = mutableListOf<CurriculumCourse>()
        val coursesArray = root.optJSONArray("courses_flat") ?: return emptyList()

        for (i in 0 until coursesArray.length()) {
            val obj = coursesArray.optJSONObject(i) ?: continue
            val code = obj.optString("course_code", "").trim()
            val title = obj.optString("title", "").trim()
            if (code.isEmpty() && title.isEmpty()) continue

            val sem = obj.optInt("semester", 1)
            val year = obj.optInt("year", (sem + 1) / 2)
            val credits = obj.optString("credits", "3")
            val category = obj.optString("category", "Core")
            val type = obj.optString("type", "Theory")
            val syllabusText = obj.optString("syllabus_text", obj.optString("raw_text", ""))

            val modulesArray = obj.optJSONArray("modules")
            val modules = mutableListOf<CurriculumModule>()

            if (modulesArray != null && modulesArray.length() > 0) {
                for (m in 0 until modulesArray.length()) {
                    val mObj = modulesArray.optJSONObject(m) ?: continue
                    val mTitle = mObj.optString("module_title", mObj.optString("module", "Module ${m + 1}")).trim()
                    val content = mObj.optString("content", "").trim()
                    val mNum = "Module ${m + 1}"
                    modules.add(
                        CurriculumModule(
                            id = "${dept.id}_${code}_m$m",
                            moduleIndex = m,
                            moduleNumber = mNum,
                            title = mTitle.ifBlank { "$mNum: Core Topics" },
                            content = content
                        )
                    )
                }
            } else if (syllabusText.isNotBlank()) {
                modules.addAll(parseTextToModules("${dept.id}_$code", syllabusText))
            }

            list.add(
                CurriculumCourse(
                    id = "${dept.id}_$code",
                    code = code,
                    title = title,
                    departmentId = dept.id,
                    departmentName = dept.shortName,
                    programme = dept.programme,
                    regulation = dept.regulation,
                    semester = sem,
                    year = year,
                    credits = credits,
                    category = category,
                    courseType = type,
                    modules = modules,
                    rawSyllabus = syllabusText
                )
            )
        }
        return list
    }

    private fun parseTextToModules(coursePrefix: String, text: String): List<CurriculumModule> {
        if (text.isBlank()) return emptyList()

        val pattern = Pattern.compile(
            "(?=(?:Module\\s*[-–—:]*\\s*[0-9IVXLCDM]+|MODULE\\s*[-–—:]*\\s*[0-9IVXLCDM]+|Unit\\s*[-–—:]*\\s*[0-9IVXLCDM]+|UNIT\\s*[-–—:]*\\s*[0-9IVXLCDM]+))"
        )
        val chunks = pattern.split(text).map { it.trim() }.filter { it.isNotBlank() }

        val modules = mutableListOf<CurriculumModule>()
        var moduleIdx = 0

        for (chunk in chunks) {
            val isModuleHeader = chunk.startsWith("Module", ignoreCase = true) || chunk.startsWith("Unit", ignoreCase = true)
            if (!isModuleHeader && chunks.size > 1 && moduleIdx == 0) {
                // Header preamble (e.g. objectives or prerequisites)
                continue
            }

            val lines = chunk.lines().map { it.trim() }.filter { it.isNotBlank() }
            val headerLine = lines.firstOrNull() ?: "Module ${moduleIdx + 1}"
            val contentBody = if (lines.size > 1) lines.drop(1).joinToString("\n") else headerLine

            val modNum = "Module ${moduleIdx + 1}"
            modules.add(
                CurriculumModule(
                    id = "${coursePrefix}_m$moduleIdx",
                    moduleIndex = moduleIdx,
                    moduleNumber = modNum,
                    title = headerLine.take(80),
                    content = contentBody
                )
            )
            moduleIdx++
        }

        return modules
    }
}
