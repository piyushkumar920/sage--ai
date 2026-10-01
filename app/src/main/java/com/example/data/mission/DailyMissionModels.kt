package com.example.data.mission

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.studytools.AcademicContext
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Task Types supported within a Daily Mission.
 */
enum class MissionTaskType(val label: String, val icon: String) {
    LEARN("Learn", "📖"),
    PRACTICE("Practice", "🗂️"),
    PYQ("PYQ", "📑"),
    REVIEW("Review", "📝")
}

/**
 * Individual action item / task inside a Daily Mission.
 */
@JsonClass(generateAdapter = true)
data class MissionTask(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "type") val type: String = "LEARN", // LEARN, PRACTICE, PYQ, REVIEW
    @field:Json(name = "title") val title: String,
    @field:Json(name = "description") val description: String = "",
    @field:Json(name = "actionRoute") val actionRoute: String = "CHAT", // CHAT, FLASHCARDS, PYQ, REVISION, SCAN
    @field:Json(name = "isCompleted") val isCompleted: Boolean = false,
    @field:Json(name = "completedAt") val completedAt: Long? = null
) {
    val taskType: MissionTaskType
        get() = try {
            MissionTaskType.valueOf(type)
        } catch (_: Exception) {
            MissionTaskType.LEARN
        }
}

/**
 * Room Entity representing a stable daily academic mission.
 */
@Entity(tableName = "daily_missions")
data class DailyMissionEntity(
    @PrimaryKey
    val id: String, // format: "mission_yyyy-MM-dd"
    val dateKey: String, // format: "yyyy-MM-dd"
    val createdAt: Long = System.currentTimeMillis(),
    val department: String = "",
    val programme: String = "B. Tech",
    val regulation: String = "R25",
    val semester: Int? = null,
    val courseCode: String = "",
    val courseName: String = "",
    val module: String = "",
    val topic: String = "",
    val officialSyllabusContent: String = "",
    val goal: String = "",
    val estimatedMinutes: Int = 20,
    val tasksJson: String = "[]",
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val actualSpentSeconds: Int = 0
) {
    fun parseTasks(moshi: Moshi = defaultMoshi): List<MissionTask> {
        return try {
            val type = Types.newParameterizedType(List::class.java, MissionTask::class.java)
            val adapter = moshi.adapter<List<MissionTask>>(type)
            adapter.fromJson(tasksJson) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    val completedTasksCount: Int
        get() = parseTasks().count { it.isCompleted }

    val totalTasksCount: Int
        get() = parseTasks().size

    val progressFraction: Float
        get() {
            val total = totalTasksCount
            return if (total > 0) completedTasksCount.toFloat() / total.toFloat() else 0f
        }

    fun toAcademicContext(): AcademicContext {
        return AcademicContext(
            department = department,
            programme = programme,
            regulation = regulation,
            semester = semester,
            courseCode = courseCode,
            courseName = courseName,
            module = module,
            topic = topic,
            officialSyllabusContent = officialSyllabusContent
        )
    }

    companion object {
        private val defaultMoshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        fun serializeTasks(tasks: List<MissionTask>, moshi: Moshi = defaultMoshi): String {
            val type = Types.newParameterizedType(List::class.java, MissionTask::class.java)
            val adapter = moshi.adapter<List<MissionTask>>(type)
            return adapter.toJson(tasks)
        }
    }
}
