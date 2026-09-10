package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
  @Query("SELECT * FROM conversations ORDER BY isPinned DESC, updated DESC")
  fun getAllConversations(): Flow<List<ConversationEntity>>

  @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
  suspend fun getConversationById(id: String): ConversationEntity?

  @Query("SELECT * FROM conversations WHERE projectId = :projectId ORDER BY updated DESC")
  fun getConversationsByProject(projectId: String): Flow<List<ConversationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(conversation: ConversationEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(conversations: List<ConversationEntity>)

  @Update
  suspend fun update(conversation: ConversationEntity)

  @Query("DELETE FROM conversations WHERE id = :id")
  suspend fun deleteById(id: String)

  @Query("DELETE FROM conversations")
  suspend fun deleteAll()
}

@Dao
interface MessageDao {
  @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
  fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>

  @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
  suspend fun getMessagesList(conversationId: String): List<MessageEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(message: MessageEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(messages: List<MessageEntity>)

  @Update
  suspend fun update(message: MessageEntity)

  @Query("DELETE FROM messages WHERE id = :id")
  suspend fun deleteById(id: String)

  @Query("DELETE FROM messages WHERE conversationId = :conversationId")
  suspend fun deleteForConversation(conversationId: String)
}

@Dao
interface ProjectDao {
  @Query("SELECT * FROM projects ORDER BY created DESC")
  fun getAllProjects(): Flow<List<ProjectEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(project: ProjectEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(project: ProjectEntity)

  @Query("DELETE FROM projects WHERE id = :id")
  suspend fun deleteById(id: String)
}

@Dao
interface AutomationDao {
  @Query("SELECT * FROM automations ORDER BY created DESC")
  fun getAllAutomations(): Flow<List<AutomationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(automation: AutomationEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(automation: AutomationEntity)

  @Update
  suspend fun update(automation: AutomationEntity)

  @Query("DELETE FROM automations WHERE id = :id")
  suspend fun deleteById(id: String)
}
