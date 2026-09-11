package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sage_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CUSTOM_BACKEND_URL = "custom_backend_url"
        private const val KEY_CONNECTION_VERIFIED = "connection_verified"
        private const val KEY_STREAK = "learning_streak"
        private const val KEY_LAST_ACTIVE_DATE = "last_active_date"
        private const val KEY_LAST_REQUEST_SUCCESS = "last_request_success"
        private const val KEY_LAST_ERROR = "last_error"
        private const val KEY_LAST_LATENCY = "last_latency_ms"
        private const val KEY_ACTIVE_TOPIC_ID = "active_topic_id"
    }

    var customBackendUrl: String
        get() = prefs.getString(KEY_CUSTOM_BACKEND_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_BACKEND_URL, value.trim()).apply()

    var isConnectionVerified: Boolean
        get() = prefs.getBoolean(KEY_CONNECTION_VERIFIED, false)
        set(value) = prefs.edit().putBoolean(KEY_CONNECTION_VERIFIED, value).apply()

    var activeTopicId: Long
        get() = prefs.getLong(KEY_ACTIVE_TOPIC_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_ACTIVE_TOPIC_ID, value).apply()

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
        get() = prefs.getInt(KEY_STREAK, 1)
        set(value) = prefs.edit().putInt(KEY_STREAK, value).apply()

    fun updateStreak() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val lastDate = prefs.getString(KEY_LAST_ACTIVE_DATE, "")
        if (lastDate != today) {
            val currentStreak = streakDays
            prefs.edit()
                .putString(KEY_LAST_ACTIVE_DATE, today)
                .putInt(KEY_STREAK, if (lastDate.isNullOrEmpty()) 1 else currentStreak + 1)
                .apply()
        }
    }
}
