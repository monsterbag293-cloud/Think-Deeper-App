package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
  @PrimaryKey val id: String,
  val title: String,
  val created: Long,
  val updated: Long,
  val projectId: String? = null,
  val isPinned: Boolean = false,
  val needsAiTitle: Boolean = false
)

@Entity(
  tableName = "messages",
  foreignKeys = [
    ForeignKey(
      entity = ConversationEntity::class,
      parentColumns = ["id"],
      childColumns = ["conversationId"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index("conversationId")]
)
data class MessageEntity(
  @PrimaryKey val id: String,
  val conversationId: String,
  val role: String, // "user", "assistant"
  val content: String,
  val displayContent: String? = null,
  val timestamp: Long,
  val thinkMs: Long = 0L,
  val activitiesJson: String = "[]",
  val attachmentsJson: String = "[]"
)

@Entity(tableName = "projects")
data class ProjectEntity(
  @PrimaryKey val id: String,
  val name: String,
  val type: String = "Web app",
  val plan: String = "",
  val created: Long
)

@Entity(tableName = "automations")
data class AutomationEntity(
  @PrimaryKey val id: String,
  val name: String,
  val enabled: Boolean = true,
  val triggerType: String, // "onAiReply", "onUserSend", "clock", "onBroadcast"
  val actionType: String,  // "store", "broadcast", "notify", "playAudio", "sendSilent"
  val configJson: String = "{}",
  val created: Long
)

data class ActivityStep(
  val agent: String = "Agent 1",
  val name: String = "Step",
  val status: String = "done", // "running", "done", "error"
  val detail: String = ""
)
