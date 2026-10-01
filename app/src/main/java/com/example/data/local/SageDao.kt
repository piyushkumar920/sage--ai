package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SageDao {
    @Query("SELECT * FROM topics ORDER BY updatedAt DESC")
    fun getAllTopics(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE id = :topicId LIMIT 1")
    suspend fun getTopicById(topicId: Long): TopicEntity?

    @Query("SELECT * FROM topics ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLatestTopic(): TopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: TopicEntity): Long

    @Update
    suspend fun updateTopic(topic: TopicEntity)

    @Delete
    suspend fun deleteTopic(topic: TopicEntity)

    @Query("SELECT * FROM messages WHERE topicId = :topicId ORDER BY timestamp ASC")
    fun getMessagesForTopic(topicId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE topicId = :topicId ORDER BY timestamp ASC")
    suspend fun getMessagesForTopicOnce(topicId: Long): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: Long)

    @Query("SELECT * FROM messages WHERE id = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: Long): MessageEntity?

    @Query("DELETE FROM messages WHERE topicId = :topicId AND id > :messageId")
    suspend fun deleteMessagesAfterId(topicId: Long, messageId: Long)

    @Query("DELETE FROM messages WHERE topicId = :topicId")
    suspend fun deleteMessagesForTopic(topicId: Long)

    // --- Roadmap Progress ---
    @Query("SELECT * FROM roadmap_progress WHERE isCurrent = 1 LIMIT 1")
    fun getCurrentRoadmap(): Flow<RoadmapProgressEntity?>

    @Query("SELECT * FROM roadmap_progress WHERE isCurrent = 1 LIMIT 1")
    suspend fun getCurrentRoadmapOnce(): RoadmapProgressEntity?

    @Query("SELECT * FROM roadmap_progress ORDER BY updatedAt DESC")
    fun getAllRoadmapProgress(): Flow<List<RoadmapProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRoadmapProgress(roadmap: RoadmapProgressEntity)

    @Query("UPDATE roadmap_progress SET isCurrent = CASE WHEN roadmapId = :roadmapId THEN 1 ELSE 0 END")
    suspend fun setCurrentRoadmap(roadmapId: String)

    // --- Topic Progress ---
    @Query("SELECT * FROM topic_progress WHERE roadmapId = :roadmapId")
    fun getTopicProgressForRoadmap(roadmapId: String): Flow<List<TopicProgressEntity>>

    @Query("SELECT * FROM topic_progress WHERE roadmapId = :roadmapId")
    suspend fun getTopicProgressForRoadmapOnce(roadmapId: String): List<TopicProgressEntity>

    @Query("SELECT * FROM topic_progress WHERE `key` = :key LIMIT 1")
    suspend fun getTopicProgressByKey(key: String): TopicProgressEntity?

    @Query("SELECT * FROM topic_progress WHERE roadmapId = :roadmapId AND (studiedInChat = 1 OR sessionsCount > 0 OR status != 'NOT_STARTED') ORDER BY lastStudiedAt DESC, updatedAt DESC")
    fun getStudiedTopicsForRoadmap(roadmapId: String): Flow<List<TopicProgressEntity>>

    @Query("SELECT * FROM topic_progress WHERE roadmapId = :roadmapId AND (studiedInChat = 1 OR sessionsCount > 0 OR status != 'NOT_STARTED') ORDER BY lastStudiedAt DESC, updatedAt DESC")
    suspend fun getStudiedTopicsForRoadmapOnce(roadmapId: String): List<TopicProgressEntity>

    @Query("SELECT * FROM topic_progress WHERE status = 'COMPLETED' ORDER BY completedAt DESC LIMIT :limit")
    fun getRecentlyCompletedTopics(limit: Int = 5): Flow<List<TopicProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTopicProgress(topicProgress: TopicProgressEntity)

    // --- Quiz Results ---
    @Query("SELECT * FROM quiz_results ORDER BY timestamp DESC")
    fun getAllQuizResults(): Flow<List<QuizResultEntity>>

    @Query("SELECT * FROM quiz_results WHERE roadmapId = :roadmapId ORDER BY timestamp DESC")
    fun getQuizResultsForRoadmap(roadmapId: String): Flow<List<QuizResultEntity>>

    @Query("SELECT AVG(score) FROM quiz_results")
    fun getAverageQuizScore(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM quiz_results")
    fun getTotalQuizzesCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResult(result: QuizResultEntity): Long

    // --- Daily Quiz Records ---
    @Query("SELECT * FROM daily_quiz_records WHERE dateStr = :dateStr LIMIT 1")
    fun getDailyQuizForDate(dateStr: String): Flow<DailyQuizRecordEntity?>

    @Query("SELECT * FROM daily_quiz_records WHERE dateStr = :dateStr LIMIT 1")
    suspend fun getDailyQuizForDateOnce(dateStr: String): DailyQuizRecordEntity?

    @Query("SELECT * FROM daily_quiz_records ORDER BY dateStr DESC LIMIT :limit")
    fun getRecentDailyQuizzes(limit: Int = 14): Flow<List<DailyQuizRecordEntity>>

    @Query("SELECT * FROM daily_quiz_records ORDER BY dateStr DESC")
    suspend fun getAllDailyQuizzesOnce(): List<DailyQuizRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyQuiz(record: DailyQuizRecordEntity)

    // --- Weak Concepts ---
    @Query("SELECT * FROM weak_concepts WHERE isResolved = 0 ORDER BY mistakeCount DESC, lastTestedAt DESC")
    fun getActiveWeakConcepts(): Flow<List<WeakConceptEntity>>

    @Query("SELECT * FROM weak_concepts WHERE isResolved = 0 ORDER BY mistakeCount DESC, lastTestedAt DESC")
    suspend fun getActiveWeakConceptsOnce(): List<WeakConceptEntity>

    @Query("SELECT * FROM weak_concepts WHERE concept = :concept LIMIT 1")
    suspend fun getWeakConceptByName(concept: String): WeakConceptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWeakConcept(weakConcept: WeakConceptEntity)

    @Query("UPDATE weak_concepts SET isResolved = 1 WHERE id = :id")
    suspend fun resolveWeakConcept(id: Long)

    // --- Saved Study Tools ---
    @Query("SELECT * FROM saved_study_tools ORDER BY updatedAt DESC")
    fun getAllSavedStudyTools(): Flow<List<com.example.data.studytools.SavedStudyToolEntity>>

    @Query("SELECT * FROM saved_study_tools WHERE type = :type ORDER BY updatedAt DESC")
    fun getSavedStudyToolsByType(type: String): Flow<List<com.example.data.studytools.SavedStudyToolEntity>>

    @Query("SELECT * FROM saved_study_tools WHERE id = :id LIMIT 1")
    suspend fun getSavedStudyToolById(id: String): com.example.data.studytools.SavedStudyToolEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSavedStudyTool(tool: com.example.data.studytools.SavedStudyToolEntity)

    @Query("DELETE FROM saved_study_tools WHERE id = :id")
    suspend fun deleteSavedStudyTool(id: String)

    // --- Focus Sessions ---
    @Query("SELECT * FROM focus_sessions ORDER BY endedAt DESC")
    fun getAllFocusSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE endedAt >= :sinceTimestamp ORDER BY endedAt DESC")
    fun getFocusSessionsSince(sinceTimestamp: Long): Flow<List<FocusSessionEntity>>

    @Query("SELECT SUM(actualFocusedSeconds) FROM focus_sessions")
    fun getTotalFocusSeconds(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity): Long

    // --- Daily Missions ---
    @Query("SELECT * FROM daily_missions WHERE dateKey = :dateKey LIMIT 1")
    fun getDailyMissionByDate(dateKey: String): Flow<com.example.data.mission.DailyMissionEntity?>

    @Query("SELECT * FROM daily_missions WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getDailyMissionByDateOnce(dateKey: String): com.example.data.mission.DailyMissionEntity?

    @Query("SELECT * FROM daily_missions WHERE id = :id LIMIT 1")
    suspend fun getDailyMissionById(id: String): com.example.data.mission.DailyMissionEntity?

    @Query("SELECT * FROM daily_missions ORDER BY dateKey DESC")
    fun getAllDailyMissions(): Flow<List<com.example.data.mission.DailyMissionEntity>>

    @Query("SELECT * FROM daily_missions ORDER BY dateKey DESC LIMIT :limit")
    fun getRecentDailyMissions(limit: Int): Flow<List<com.example.data.mission.DailyMissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyMission(mission: com.example.data.mission.DailyMissionEntity)

    @Query("DELETE FROM daily_missions WHERE dateKey = :dateKey AND completed = 0")
    suspend fun deleteIncompleteDailyMissionByDate(dateKey: String)

    // --- Academic Profile (Phase C2.5) ---
    @Query("SELECT * FROM academic_profile WHERE id = 'primary' LIMIT 1")
    fun getAcademicProfileFlow(): Flow<com.example.data.profile.AcademicProfileEntity?>

    @Query("SELECT * FROM academic_profile WHERE id = 'primary' LIMIT 1")
    suspend fun getAcademicProfileOnce(): com.example.data.profile.AcademicProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAcademicProfile(profile: com.example.data.profile.AcademicProfileEntity)

    @Query("DELETE FROM academic_profile WHERE id = 'primary'")
    suspend fun clearAcademicProfile()
}
