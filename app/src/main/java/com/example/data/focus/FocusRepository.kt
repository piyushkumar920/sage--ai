package com.example.data.focus

import com.example.data.local.FocusSessionEntity
import com.example.data.local.SageDao
import com.example.data.studytools.AcademicContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar

data class FocusStats(
    val todayMinutes: Int = 0,
    val thisWeekFormatted: String = "0m",
    val totalSessionsCount: Int = 0
)

class FocusRepository(
    private val dao: SageDao
) {
    val allSessions: Flow<List<FocusSessionEntity>> = dao.getAllFocusSessions()

    val focusStats: Flow<FocusStats> = allSessions.map { sessions ->
        calculateFocusStats(sessions)
    }

    suspend fun recordFocusSession(session: FocusSessionEntity): Long = withContext(Dispatchers.IO) {
        dao.insertFocusSession(session)
    }

    companion object {
        /**
         * Automatically derives a simple curriculum learning goal from academic context.
         */
        fun deriveDefaultGoal(context: AcademicContext?): String {
            val topic = context?.topic?.trim()
            val module = context?.module?.trim()
            val course = context?.courseName?.trim()

            return when {
                !topic.isNullOrBlank() -> "Understand $topic and solve one core practice example."
                !module.isNullOrBlank() -> "Master key concepts in $module and review key definitions."
                !course.isNullOrBlank() -> "Study core principles of $course and practice active recall."
                else -> "Complete focused study session and review key takeaways."
            }
        }

        /**
         * Formats seconds into MM:SS.
         */
        fun formatTimerSeconds(totalSeconds: Int): String {
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }

        /**
         * Calculates focus analytics.
         */
        fun calculateFocusStats(sessions: List<FocusSessionEntity>): FocusStats {
            if (sessions.isEmpty()) {
                return FocusStats(todayMinutes = 0, thisWeekFormatted = "0m", totalSessionsCount = 0)
            }

            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startOfToday = cal.timeInMillis

            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            val startOfWeek = cal.timeInMillis

            var todaySeconds = 0
            var weekSeconds = 0

            for (s in sessions) {
                if (s.endedAt >= startOfToday) {
                    todaySeconds += s.actualFocusedSeconds
                }
                if (s.endedAt >= startOfWeek) {
                    weekSeconds += s.actualFocusedSeconds
                }
            }

            val todayMinutes = todaySeconds / 60
            val weekMinutes = weekSeconds / 60
            val weekHours = weekMinutes / 60
            val remainingWeekMins = weekMinutes % 60

            val weekFormatted = if (weekHours > 0) {
                "${weekHours}h ${remainingWeekMins}m"
            } else {
                "${weekMinutes}m"
            }

            return FocusStats(
                todayMinutes = todayMinutes,
                thisWeekFormatted = weekFormatted,
                totalSessionsCount = sessions.size
            )
        }
    }
}
