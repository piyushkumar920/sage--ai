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
    }

    init {
        // Safe production migration: clear out legacy Cloud Run, dev, or localhost URLs
        val saved = prefs.getString(KEY_CUSTOM_BACKEND_URL, "") ?: ""
        if (saved.isNotEmpty() && !saved.contains("sage-backend-ai.onrender.com", ignoreCase = true)) {
            prefs.edit().remove(KEY_CUSTOM_BACKEND_URL).apply()
        }
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
