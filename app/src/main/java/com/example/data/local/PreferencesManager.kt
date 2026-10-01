package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sage_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CUSTOM_BACKEND_URL = "custom_backend_url"
        private const val KEY_CONNECTION_VERIFIED = "connection_verified"
        private const val KEY_STREAK = "learning_streak"
        private const val KEY_LONGEST_STREAK = "longest_streak"
        private const val KEY_LAST_ACTIVE_DATE = "last_active_date"
        private const val KEY_LAST_REQUEST_SUCCESS = "last_request_success"
        private const val KEY_LAST_ERROR = "last_error"
        private const val KEY_LAST_LATENCY = "last_latency_ms"
        private const val KEY_ACTIVE_TOPIC_ID = "active_topic_id"
        private const val KEY_ACTIVE_ROADMAP_ID = "active_roadmap_id"
        private const val KEY_ACTIVE_ROADMAP_TITLE = "active_roadmap_title"
        private const val KEY_CURRENT_TOPIC_ID = "current_topic_id"
        private const val KEY_CURRENT_TOPIC_TITLE = "current_topic_title"
        private const val KEY_STUDY_MINUTES = "study_minutes"
        private const val KEY_HAS_ACADEMIC_PROFILE = "has_academic_profile"
        private const val KEY_ACADEMIC_DEPT_ID = "academic_dept_id"
        private const val KEY_ACADEMIC_DEPT_NAME = "academic_dept_name"
        private const val KEY_ACADEMIC_PROGRAMME_ID = "academic_programme_id"
        private const val KEY_ACADEMIC_PROGRAMME_NAME = "academic_programme_name"
        private const val KEY_ACADEMIC_REGULATION = "academic_regulation"
        private const val KEY_ACADEMIC_SEMESTER = "academic_semester"
        private const val KEY_ACADEMIC_UPDATED_AT = "academic_updated_at"
        private const val KEY_PYQS_ATTEMPTED = "pyqs_attempted_count"
    }

    init {
        // Safe production migration: clear out legacy Cloud Run, dev, or localhost URLs
        val saved = prefs.getString(KEY_CUSTOM_BACKEND_URL, "") ?: ""
        if (saved.isNotEmpty() && !saved.contains("sage-backend-ai.onrender.com", ignoreCase = true)) {
            prefs.edit().remove(KEY_CUSTOM_BACKEND_URL).apply()
        }
    }

    // --- ACADEMIC PROFILE (Phase C2.5) ---
    var hasAcademicProfile: Boolean
        get() = prefs.getBoolean(KEY_HAS_ACADEMIC_PROFILE, false)
        set(value) = prefs.edit().putBoolean(KEY_HAS_ACADEMIC_PROFILE, value).apply()

    var academicDepartmentId: String
        get() = prefs.getString(KEY_ACADEMIC_DEPT_ID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ACADEMIC_DEPT_ID, value).apply()

    var academicDepartmentName: String
        get() = prefs.getString(KEY_ACADEMIC_DEPT_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ACADEMIC_DEPT_NAME, value).apply()

    var academicProgrammeId: String
        get() = prefs.getString(KEY_ACADEMIC_PROGRAMME_ID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ACADEMIC_PROGRAMME_ID, value).apply()

    var academicProgrammeName: String
        get() = prefs.getString(KEY_ACADEMIC_PROGRAMME_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ACADEMIC_PROGRAMME_NAME, value).apply()

    var academicRegulation: String
        get() = prefs.getString(KEY_ACADEMIC_REGULATION, "R25") ?: "R25"
        set(value) = prefs.edit().putString(KEY_ACADEMIC_REGULATION, value).apply()

    var academicSemester: Int
        get() = prefs.getInt(KEY_ACADEMIC_SEMESTER, 1)
        set(value) = prefs.edit().putInt(KEY_ACADEMIC_SEMESTER, value).apply()

    var academicProfileUpdatedAt: Long
        get() = prefs.getLong(KEY_ACADEMIC_UPDATED_AT, 0L)
        set(value) = prefs.edit().putLong(KEY_ACADEMIC_UPDATED_AT, value).apply()

    fun saveAcademicProfile(profile: com.example.data.profile.AcademicProfile) {
        prefs.edit()
            .putBoolean(KEY_HAS_ACADEMIC_PROFILE, true)
            .putString(KEY_ACADEMIC_DEPT_ID, profile.departmentId)
            .putString(KEY_ACADEMIC_DEPT_NAME, profile.departmentName)
            .putString(KEY_ACADEMIC_PROGRAMME_ID, profile.programmeId)
            .putString(KEY_ACADEMIC_PROGRAMME_NAME, profile.programmeName)
            .putString(KEY_ACADEMIC_REGULATION, profile.regulation)
            .putInt(KEY_ACADEMIC_SEMESTER, profile.semester)
            .putLong(KEY_ACADEMIC_UPDATED_AT, profile.updatedAt)
            .apply()
    }

    fun getAcademicProfile(): com.example.data.profile.AcademicProfile? {
        if (!hasAcademicProfile) return null
        val deptId = academicDepartmentId
        if (deptId.isBlank()) return null
        return com.example.data.profile.AcademicProfile(
            departmentId = deptId,
            departmentName = academicDepartmentName.ifBlank { deptId },
            programmeId = academicProgrammeId.ifBlank { deptId },
            programmeName = academicProgrammeName.ifBlank { academicDepartmentName },
            regulationId = academicRegulation.lowercase(),
            regulation = academicRegulation,
            semester = academicSemester,
            updatedAt = academicProfileUpdatedAt
        )
    }

    fun clearAcademicProfile() {
        prefs.edit()
            .putBoolean(KEY_HAS_ACADEMIC_PROFILE, false)
            .remove(KEY_ACADEMIC_DEPT_ID)
            .remove(KEY_ACADEMIC_DEPT_NAME)
            .remove(KEY_ACADEMIC_PROGRAMME_ID)
            .remove(KEY_ACADEMIC_PROGRAMME_NAME)
            .remove(KEY_ACADEMIC_REGULATION)
            .remove(KEY_ACADEMIC_SEMESTER)
            .remove(KEY_ACADEMIC_UPDATED_AT)
            .apply()
    }

    var customBackendUrl: String
        get() {
            val url = prefs.getString(KEY_CUSTOM_BACKEND_URL, "") ?: ""
            if (url.isNotEmpty() && !url.contains("sage-backend-ai.onrender.com", ignoreCase = true)) {
                prefs.edit().remove(KEY_CUSTOM_BACKEND_URL).apply()
                return ""
            }
            return url
        }
        set(value) {
            val trimmed = value.trim()
            if (trimmed.isEmpty() || trimmed.contains("sage-backend-ai.onrender.com", ignoreCase = true)) {
                prefs.edit().putString(KEY_CUSTOM_BACKEND_URL, trimmed).apply()
            }
        }

    var isConnectionVerified: Boolean
        get() = prefs.getBoolean(KEY_CONNECTION_VERIFIED, false)
        set(value) = prefs.edit().putBoolean(KEY_CONNECTION_VERIFIED, value).apply()

    var activeTopicId: Long
        get() = prefs.getLong(KEY_ACTIVE_TOPIC_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_ACTIVE_TOPIC_ID, value).apply()

    var activeRoadmapId: String
        get() = prefs.getString(KEY_ACTIVE_ROADMAP_ID, "curriculum_cse_aiml_CS101") ?: "curriculum_cse_aiml_CS101"
        set(value) = prefs.edit().putString(KEY_ACTIVE_ROADMAP_ID, value).apply()

    var activeRoadmapTitle: String
        get() = prefs.getString(KEY_ACTIVE_ROADMAP_TITLE, "CS101: Introduction to Programming and Problem Solving") ?: "CS101: Introduction to Programming and Problem Solving"
        set(value) = prefs.edit().putString(KEY_ACTIVE_ROADMAP_TITLE, value).apply()

    var currentTopicId: String
        get() = prefs.getString(KEY_CURRENT_TOPIC_ID, "curriculum_cse_aiml_CS101_mod_0") ?: "curriculum_cse_aiml_CS101_mod_0"
        set(value) = prefs.edit().putString(KEY_CURRENT_TOPIC_ID, value).apply()

    var currentTopicTitle: String
        get() = prefs.getString(KEY_CURRENT_TOPIC_TITLE, "Module 1: Basics of Computing & Number Representation") ?: "Module 1: Basics of Computing & Number Representation"
        set(value) = prefs.edit().putString(KEY_CURRENT_TOPIC_TITLE, value).apply()

    var lastRequestSuccess: Boolean
        get() = prefs.getBoolean(KEY_LAST_REQUEST_SUCCESS, true)
        set(value) = prefs.edit().putBoolean(KEY_LAST_REQUEST_SUCCESS, value).apply()

    var lastErrorMessage: String
        get() = prefs.getString(KEY_LAST_ERROR, "None") ?: "None"
        set(value) = prefs.edit().putString(KEY_LAST_ERROR, value).apply()

    var lastLatencyMs: Long
        get() = prefs.getLong(KEY_LAST_LATENCY, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_LATENCY, value).apply()

    var streakDays: Int
        get() = prefs.getInt(KEY_STREAK, 3)
        set(value) = prefs.edit().putInt(KEY_STREAK, value).apply()

    var longestStreak: Int
        get() = prefs.getInt(KEY_LONGEST_STREAK, 12)
        set(value) = prefs.edit().putInt(KEY_LONGEST_STREAK, value).apply()

    var studyMinutes: Int
        get() = prefs.getInt(KEY_STUDY_MINUTES, 45)
        set(value) = prefs.edit().putInt(KEY_STUDY_MINUTES, value).apply()

    var pyqsAttemptedCount: Int
        get() = prefs.getInt(KEY_PYQS_ATTEMPTED, 12)
        set(value) = prefs.edit().putInt(KEY_PYQS_ATTEMPTED, value).apply()

    fun incrementPyqsAttempted(count: Int = 1) {
        pyqsAttemptedCount = pyqsAttemptedCount + count
    }

    fun addStudyMinutes(minutes: Int) {
        val current = studyMinutes
        prefs.edit().putInt(KEY_STUDY_MINUTES, current + minutes).apply()
    }

    fun updateStreak() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = sdf.format(Date())
        val lastDate = prefs.getString(KEY_LAST_ACTIVE_DATE, "")

        if (lastDate == today) return

        if (lastDate.isNullOrEmpty()) {
            streakDays = 1
            if (longestStreak < 1) longestStreak = 1
        } else {
            try {
                val lastTime = sdf.parse(lastDate)?.time ?: 0L
                val cal = Calendar.getInstance()
                cal.time = Date()
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterday = sdf.format(cal.time)

                if (lastDate == yesterday) {
                    val newStreak = streakDays + 1
                    streakDays = newStreak
                    if (newStreak > longestStreak) longestStreak = newStreak
                } else {
                    // Missed one or more days
                    streakDays = 1
                }
            } catch (e: Exception) {
                streakDays = 1
            }
        }
        prefs.edit().putString(KEY_LAST_ACTIVE_DATE, today).apply()
    }
}
