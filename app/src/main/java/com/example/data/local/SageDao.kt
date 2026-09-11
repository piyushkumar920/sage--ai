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
}
