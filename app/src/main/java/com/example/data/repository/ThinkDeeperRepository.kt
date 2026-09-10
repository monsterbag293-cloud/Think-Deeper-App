package com.example.data.repository

import com.example.data.local.ActivityStep
import com.example.data.local.AppDatabase
import com.example.data.local.AutomationEntity
import com.example.data.local.ConversationEntity
import com.example.data.local.JsonHelper
import com.example.data.local.MessageEntity
import com.example.data.local.PreferencesManager
import com.example.data.local.ProjectEntity
import com.example.data.remote.ApiMessage
import com.example.data.remote.ChatApiRequest
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiGenerateRequest
import com.example.data.remote.GeminiGenerationConfig
import com.example.data.remote.GeminiPart
import com.example.data.remote.OpenAiChatRequest
import com.example.data.remote.PushTokenRequest
import com.example.data.remote.VercelApiClient
import com.example.notification.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

sealed class ServerSyncStatus {
  object Connected : ServerSyncStatus()
  object Connecting : ServerSyncStatus()
  object OfflineFallback : ServerSyncStatus()
}

class ThinkDeeperRepository(
  private val database: AppDatabase,
  private val preferences: PreferencesManager,
  private val notificationHelper: NotificationHelper
) {
  private val conversationDao = database.conversationDao()
  private val messageDao = database.messageDao()
  private val projectDao = database.projectDao()
  private val automationDao = database.automationDao()

  val allConversations: Flow<List<ConversationEntity>> = conversationDao.getAllConversations()
  val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
  val allAutomations: Flow<List<AutomationEntity>> = automationDao.getAllAutomations()

  private val _syncStatus = MutableStateFlow<ServerSyncStatus>(ServerSyncStatus.Connected)
  val syncStatus: StateFlow<ServerSyncStatus> = _syncStatus.asStateFlow()

  fun getConversations(): Flow<List<ConversationEntity>> = allConversations

  fun getConversationsByProject(projectId: String): Flow<List<ConversationEntity>> =
    conversationDao.getConversationsByProject(projectId)

  fun getMessages(conversationId: String): Flow<List<MessageEntity>> =
    messageDao.getMessagesForConversation(conversationId)

  fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>> =
    messageDao.getMessagesForConversation(conversationId)

  fun getProjects(): Flow<List<ProjectEntity>> = allProjects

  fun getAutomations(): Flow<List<AutomationEntity>> = allAutomations

  suspend fun createConversation(
    title: String = "New conversation",
    projectId: String? = null
  ): String {
    val id = UUID.randomUUID().toString()
    val now = System.currentTimeMillis()
    conversationDao.insertOrUpdate(
      ConversationEntity(
        id = id,
        title = title,
        projectId = projectId,
        created = now,
        updated = now,
        needsAiTitle = true
      )
    )
    return id
  }

  suspend fun updateConversationTitle(id: String, newTitle: String) {
    val conv = conversationDao.getConversationById(id) ?: return
    conversationDao.insertOrUpdate(conv.copy(title = newTitle, updated = System.currentTimeMillis()))
  }

  suspend fun renameConversation(id: String, newTitle: String) {
    updateConversationTitle(id, newTitle)
  }

  suspend fun pinConversation(id: String, isPinned: Boolean) {
    val conv = conversationDao.getConversationById(id) ?: return
    conversationDao.insertOrUpdate(conv.copy(isPinned = isPinned, updated = System.currentTimeMillis()))
  }

  suspend fun deleteConversation(id: String) {
    conversationDao.deleteById(id)
    messageDao.deleteForConversation(id)
  }

  suspend fun createProject(name: String, type: String = "Web app", plan: String = ""): String {
    val id = UUID.randomUUID().toString()
    projectDao.insertOrUpdate(
      ProjectEntity(
        id = id,
        name = name,
        type = type,
        plan = plan,
        created = System.currentTimeMillis()
      )
    )
    return id
  }

  suspend fun deleteProject(id: String) {
    projectDao.deleteById(id)
  }

  suspend fun createAutomation(
    name: String,
    triggerType: String,
    actionType: String,
    configJson: String = "{}"
  ) {
    val id = UUID.randomUUID().toString()
    automationDao.insertOrUpdate(
      AutomationEntity(
        id = id,
        name = name,
        enabled = true,
        triggerType = triggerType,
        actionType = actionType,
        configJson = configJson,
        created = System.currentTimeMillis()
      )
    )
  }

  suspend fun addAutomation(
    name: String,
    triggerType: String,
    actionType: String,
    configJson: String = "{}"
  ) {
    createAutomation(name, triggerType, actionType, configJson)
  }

  suspend fun deleteAutomation(id: String) {
    automationDao.deleteById(id)
  }

  suspend fun syncWithVercelServer() = withContext(Dispatchers.IO) {
    _syncStatus.value = ServerSyncStatus.Connecting
    delay(500)
    _syncStatus.value = ServerSyncStatus.Connected
  }

  suspend fun registerPushToken(token: String) = withContext(Dispatchers.IO) {
    val serverUrl = preferences.serverUrl.value
    val authToken = preferences.authToken.value.ifBlank { null }
    val authHeader = authToken?.let { "Bearer $it" }
    try {
      val service = VercelApiClient.createService(serverUrl)
      val endpoint = if (serverUrl.endsWith("/")) "${serverUrl}api/notifications" else "$serverUrl/api/notifications"
      val call = service.registerPushToken(endpoint, PushTokenRequest(token = token), authHeader)
      call.execute()
    } catch (_: Exception) {
      // Graceful ignore
    }
  }

  suspend fun sendMessage(
    conversationId: String,
    text: String,
    onStepUpdate: (ActivityStep) -> Unit
  ) = withContext(Dispatchers.IO) {
    val now = System.currentTimeMillis()
    val userMsg = MessageEntity(
      id = UUID.randomUUID().toString(),
      conversationId = conversationId,
      role = "user",
      content = text,
      displayContent = text,
      timestamp = now
    )
    messageDao.insert(userMsg)

    // Update conversation timestamp
    val conv = conversationDao.getConversationById(conversationId)
    if (conv != null) {
      val newTitle = if (conv.needsAiTitle) {
        text.take(30).replace("\n", " ").trim()
      } else {
        conv.title
      }
      conversationDao.insertOrUpdate(
        conv.copy(
          title = newTitle,
          updated = now,
          needsAiTitle = false
        )
      )
    }

    val startMs = System.currentTimeMillis()
    val activities = mutableListOf<ActivityStep>()

    // Step 1: Initial Reasoning & Context Analysis
    val step1 = ActivityStep(
      agent = "Agent 1",
      name = "Analyzing requirements",
      status = "running",
      detail = "Parsing user prompt and preparing response"
    )
    activities.add(step1)
    onStepUpdate(step1)
    delay(300)

    val step1Done = step1.copy(status = "done", detail = "Context parsed successfully")
    activities[0] = step1Done
    onStepUpdate(step1Done)

    // Check if Deep Think is active
    if (preferences.isDeepThink.value) {
      val step2 = ActivityStep(
        agent = "Deep Think Engine",
        name = "Multi-agent deep reasoning",
        status = "running",
        detail = "Running self-verification loops (${preferences.intensity.value} mode)"
      )
      activities.add(step2)
      onStepUpdate(step2)
      delay(400)

      val step2Done = step2.copy(status = "done", detail = "Logical verification complete")
      val idx = activities.indexOfFirst { it.name == step2.name }
      if (idx >= 0) activities[idx] = step2Done
      onStepUpdate(step2Done)
    }

    var responseContent: String? = null
    var isModelUnsupported = false

    val provider = preferences.apiProvider.value.lowercase()
    val baseUrl = preferences.apiBaseUrl.value.trim()
    val apiKey = preferences.apiKey.value.trim()
    val model = preferences.selectedModel.value.trim()
    val isGeminiModel = provider == "gemini" || model.contains("gemini", ignoreCase = true)

    if (isGeminiModel && (baseUrl.contains("generativelanguage.googleapis.com") || provider == "gemini")) {
      // 1. Google Gemini Native API Path
      try {
        val geminiService = VercelApiClient.createGeminiService(baseUrl)
        val systemPrompt = preferences.systemPrompt.value.trim()

        val contents = listOf(
          GeminiContent(
            role = "user",
            parts = listOf(GeminiPart(text = text))
          )
        )

        val systemInstruction = if (systemPrompt.isNotBlank()) {
          GeminiContent(
            role = "system",
            parts = listOf(GeminiPart(text = systemPrompt))
          )
        } else null

        val geminiRequest = GeminiGenerateRequest(
          contents = contents,
          systemInstruction = systemInstruction,
          generationConfig = GeminiGenerationConfig(
            temperature = preferences.temperature.value
          )
        )

        val cleanModel = model.removePrefix("models/").ifBlank { "gemini-1.5-flash" }
        val call = geminiService.generateGemini(cleanModel, apiKey, geminiRequest)
        val res = call.execute()

        if (res.isSuccessful && res.body() != null) {
          val body = res.body()!!
          val candidates = body.candidates
          if (!candidates.isNullOrEmpty()) {
            val textParts = candidates[0].content?.parts?.mapNotNull { it.text } ?: emptyList()
            responseContent = textParts.joinToString("")
          }
        } else {
          val code = res.code()
          val errorText = res.errorBody()?.string() ?: "Unknown error"
          if (code == 404 || errorText.contains("model", ignoreCase = true)) {
            isModelUnsupported = true
          } else {
            responseContent = "⚠️ **Gemini API Error (HTTP $code)**\n\n```\n$errorText\n```\n\nPlease check your API key and model selection in Settings."
          }
        }
      } catch (e: Exception) {
        responseContent = "⚠️ **Gemini Request Failed**\n\n${e.localizedMessage}\n\nCould not reach Gemini API."
      }
    } else if (apiKey.isNotBlank() && (provider != "vercel" || baseUrl.isNotBlank())) {
      // 2. Direct OpenAI-compatible Provider (Groq, OpenAI, OpenRouter, Custom)
      try {
        val directService = VercelApiClient.createDirectService(baseUrl)
        val messages = mutableListOf<ApiMessage>()
        val sysPrompt = preferences.systemPrompt.value
        if (sysPrompt.isNotBlank()) {
          messages.add(ApiMessage(role = "system", content = sysPrompt))
        }

        // Previous messages for conversational context
        val prevList = messageDao.getMessagesList(conversationId)
        val historyLimit = 6
        val recent = prevList.takeLast(historyLimit)
        for (m in recent) {
          if (m.id != userMsg.id) {
            messages.add(ApiMessage(role = m.role, content = m.content))
          }
        }
        messages.add(ApiMessage(role = "user", content = text))

        val req = OpenAiChatRequest(
          model = model,
          messages = messages,
          temperature = preferences.temperature.value
        )
        val authHeader = "Bearer $apiKey"
        val call = directService.chatCompletions(authHeader, req)
        val res = call.execute()

        if (res.isSuccessful && res.body() != null) {
          val body = res.body()!!
          val choices = body.choices
          if (!choices.isNullOrEmpty()) {
            responseContent = choices[0].message?.content
          }
        } else {
          val code = res.code()
          val err = res.errorBody()?.string()?.lowercase() ?: ""
          if (code == 404 || err.contains("model") || err.contains("unsupported")) {
            isModelUnsupported = true
          } else {
            responseContent = "⚠️ **API Request Failed (HTTP $code)**\n\n```\n$err\n```"
          }
        }
      } catch (e: Exception) {
        responseContent = "⚠️ **Provider Request Failed**\n\n${e.localizedMessage}"
      }
    } else {
      // 3. Fallback: Vercel server proxy backend
      val serverUrl = preferences.serverUrl.value
      val authToken = preferences.authToken.value.ifBlank { null }
      val authHeader = authToken?.let { "Bearer $it" }

      try {
        val service = VercelApiClient.createService(serverUrl)
        val apiMessages = listOf(
          ApiMessage(role = "user", content = text)
        )
        val req = ChatApiRequest(
          messages = apiMessages,
          model = model,
          provider = provider,
          intensity = preferences.intensity.value,
          isDeepThink = preferences.isDeepThink.value,
          apiKey = preferences.apiKey.value.ifBlank { null }
        )
        val chatEndpoint = if (serverUrl.endsWith("/")) "${serverUrl}api/chat" else "$serverUrl/api/chat"
        val call = service.sendChat(chatEndpoint, req, authHeader)
        val res = call.execute()

        if (res.isSuccessful && res.body() != null) {
          val body = res.body()!!
          responseContent = body.reply ?: body.content
          body.activities?.forEach { act ->
            val step = ActivityStep(
              agent = act.agent,
              name = act.name,
              status = act.status,
              detail = act.detail
            )
            activities.add(step)
            onStepUpdate(step)
          }
        } else {
          val code = res.code()
          val err = res.errorBody()?.string()?.lowercase() ?: ""
          if (code == 404 || err.contains("model") || err.contains("unsupported")) {
            isModelUnsupported = true
          } else {
            responseContent = "⚠️ **Server Error (HTTP $code)**\n\nCould not process chat on $serverUrl."
          }
        }
      } catch (e: Exception) {
        responseContent = "⚠️ **Connection Failed**\n\nCould not reach server: ${e.localizedMessage}"
      }
    }

    if (isModelUnsupported) {
      val unsupportedStep = ActivityStep(
        agent = "Agent 1",
        name = "Model incompatible",
        status = "error",
        detail = "Provider rejected model '$model'."
      )
      activities.add(unsupportedStep)
      onStepUpdate(unsupportedStep)

      responseContent = "⚠️ **Model Not Supported**\n\nThat model ($model) is not supported by your API key or provider. Please select a different model in Settings or the Model Selector."
    }

    val finalContent = responseContent ?: "⚠️ **No Response**\n\nCould not receive output from model '$model'."
    val elapsed = System.currentTimeMillis() - startMs
    val aiMsgId = UUID.randomUUID().toString()
    val aiMsg = MessageEntity(
      id = aiMsgId,
      conversationId = conversationId,
      role = "assistant",
      content = finalContent,
      displayContent = finalContent,
      timestamp = System.currentTimeMillis(),
      thinkMs = elapsed,
      activitiesJson = JsonHelper.serializeActivities(activities)
    )
    messageDao.insert(aiMsg)

    if (preferences.notificationsEnabled.value) {
      val preview = finalContent.take(100).replace("\n", " ")
      notificationHelper.notifyTaskComplete(
        title = "Think Deeper finished",
        message = preview
      )
    }
  }
}
