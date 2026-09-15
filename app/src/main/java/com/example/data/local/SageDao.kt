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

    @Query("SELECT * FROM weak_concepts WHERE concept = :concept LIMIT 1")
    suspend fun getWeakConceptByName(concept: String): WeakConceptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWeakConcept(weakConcept: WeakConceptEntity)

    @Query("UPDATE weak_concepts SET isResolved = 1 WHERE id = :id")
    suspend fun resolveWeakConcept(id: Long)
}
