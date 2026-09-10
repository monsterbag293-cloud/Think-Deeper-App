package com.example.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthManager
import com.example.data.local.ActivityStep
import com.example.data.local.AutomationEntity
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.local.PluginItem
import com.example.data.local.PreferencesManager
import com.example.data.local.ProjectEntity
import com.example.data.repository.ServerSyncStatus
import com.example.data.repository.ThinkDeeperRepository
import com.example.notification.NotificationHelper
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppMode {
  CHAT,
  SPEC_AND_PLAN,
  AUTOMATIONS
}

data class VirtualFile(
  val path: String,
  val name: String,
  val content: String,
  val language: String = "javascript"
)

class ThinkDeeperViewModel(
  private val repository: ThinkDeeperRepository,
  private val preferences: PreferencesManager,
  private val notificationHelper: NotificationHelper,
  private val authManager: AuthManager
) : ViewModel() {

  val conversations: StateFlow<List<ConversationEntity>> = repository.allConversations
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val projects: StateFlow<List<ProjectEntity>> = repository.allProjects
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val automations: StateFlow<List<AutomationEntity>> = repository.allAutomations
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val syncStatus: StateFlow<ServerSyncStatus> = repository.syncStatus

  // Settings & Preferences
  val apiProvider: StateFlow<String> = preferences.apiProvider
  val apiBaseUrl: StateFlow<String> = preferences.apiBaseUrl
  val apiKey: StateFlow<String> = preferences.apiKey
  val selectedModel: StateFlow<String> = preferences.selectedModel
  val customModels: StateFlow<List<String>> = preferences.customModels
  val talkModel: StateFlow<String> = preferences.talkModel
  val codeModel: StateFlow<String> = preferences.codeModel
  val reasonModel: StateFlow<String> = preferences.reasonModel
  val temperature: StateFlow<Float> = preferences.temperature
  val systemPrompt: StateFlow<String> = preferences.systemPrompt

  val serverUrl: StateFlow<String> = preferences.serverUrl
  val authToken: StateFlow<String> = preferences.authToken
  val intensity: StateFlow<String> = preferences.intensity
  val isDeepThink: StateFlow<Boolean> = preferences.isDeepThink
  val notificationsEnabled: StateFlow<Boolean> = preferences.notificationsEnabled

  // User Profile & Themes
  val userName: StateFlow<String> = preferences.userName
  val nickname: StateFlow<String> = preferences.nickname
  val profilePicUrl: StateFlow<String> = preferences.profilePicUrl
  val themeMode: StateFlow<String> = preferences.themeMode
  val colorPalette: StateFlow<String> = preferences.colorPalette
  val fontTheme: StateFlow<String> = preferences.fontTheme
  val plugins: StateFlow<List<PluginItem>> = preferences.plugins

  // Firebase Auth
  val currentUser: StateFlow<FirebaseUser?> = authManager.currentUser
  val authStatus: StateFlow<String> = authManager.authStatus
  val isAuthLoading: StateFlow<Boolean> = authManager.isLoading

  private val _currentConversationId = MutableStateFlow<String?>(null)
  val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val currentMessages: StateFlow<List<MessageEntity>> = _currentConversationId
    .flatMapLatest { id ->
      if (id == null) flowOf(emptyList()) else repository.getMessagesForConversation(id)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _activeMode = MutableStateFlow(AppMode.CHAT)
  val activeMode: StateFlow<AppMode> = _activeMode.asStateFlow()

  private val _isGenerating = MutableStateFlow(false)
  val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

  private val _liveSteps = MutableStateFlow<List<ActivityStep>>(emptyList())
  val liveSteps: StateFlow<List<ActivityStep>> = _liveSteps.asStateFlow()

  private val _statusText = MutableStateFlow("Ready")
  val statusText: StateFlow<String> = _statusText.asStateFlow()

  private var activeGenerationJob: Job? = null

  init {
    viewModelScope.launch {
      conversations.collect { list ->
        if (_currentConversationId.value == null && list.isNotEmpty()) {
          _currentConversationId.value = list.first().id
        }
      }
    }
  }

  fun setMode(mode: AppMode) {
    _activeMode.value = mode
  }

  fun selectConversation(id: String) {
    _currentConversationId.value = id
  }

  fun createNewConversation() {
    viewModelScope.launch {
      val newId = repository.createConversation()
      _currentConversationId.value = newId
    }
  }

  fun deleteConversation(id: String) {
    viewModelScope.launch {
      repository.deleteConversation(id)
      if (_currentConversationId.value == id) {
        val remaining = conversations.value.filter { it.id != id }
        _currentConversationId.value = remaining.firstOrNull()?.id
      }
    }
  }

  fun renameConversation(id: String, newTitle: String) {
    viewModelScope.launch {
      repository.renameConversation(id, newTitle)
    }
  }

  fun togglePinConversation(id: String) {
    viewModelScope.launch {
      val conv = conversations.value.find { it.id == id } ?: return@launch
      repository.pinConversation(id, !conv.isPinned)
    }
  }

  fun toggleDeepThink(enabled: Boolean) {
    preferences.setDeepThink(enabled)
  }

  fun setIntensity(mode: String) {
    preferences.setIntensity(mode)
  }

  fun setSelectedModel(model: String) {
    preferences.setSelectedModel(model)
  }

  fun addCustomModel(model: String) {
    preferences.addModelToLibrary(model)
  }

  fun removeCustomModel(model: String) {
    preferences.removeModelFromLibrary(model)
  }

  fun setRoleModels(talk: String, code: String, reason: String) {
    preferences.setRoleModels(talk, code, reason)
  }

  fun setTemperature(temp: Float) {
    preferences.setTemperature(temp)
  }

  fun setSystemPrompt(prompt: String) {
    preferences.setSystemPrompt(prompt)
  }

  fun updateSettings(
    provider: String,
    base: String,
    key: String,
    model: String,
    serverUrl: String,
    authToken: String,
    notifications: Boolean,
    temp: Float,
    sysPrompt: String
  ) {
    preferences.setApiProvider(provider)
    preferences.setApiBaseUrl(base)
    preferences.setApiKey(key)
    preferences.setSelectedModel(model)
    preferences.setServerUrl(serverUrl)
    preferences.setAuthToken(authToken)
    preferences.setNotificationsEnabled(notifications)
    preferences.setTemperature(temp)
    preferences.setSystemPrompt(sysPrompt)

    viewModelScope.launch {
      repository.syncWithVercelServer()
    }
  }

  fun updateProfile(name: String, nick: String, picUrl: String) {
    preferences.setUserName(name)
    preferences.setNickname(nick)
    preferences.setProfilePicUrl(picUrl)
  }

  fun setThemeMode(mode: String) {
    preferences.setThemeMode(mode)
  }

  fun setColorPalette(palette: String) {
    preferences.setColorPalette(palette)
  }

  fun setFontTheme(font: String) {
    preferences.setFontTheme(font)
  }

  fun togglePlugin(id: String) {
    preferences.togglePlugin(id)
  }

  fun savePlugin(plugin: PluginItem) {
    preferences.savePlugin(plugin)
  }

  fun deletePlugin(id: String) {
    preferences.deletePlugin(id)
  }

  fun signInWithGoogle(activity: Activity) {
    viewModelScope.launch {
      authManager.signInWithGoogle(activity)
    }
  }

  fun signOut() {
    authManager.signOut()
  }

  fun createProject(name: String, type: String, plan: String) {
    viewModelScope.launch {
      repository.createProject(name, type, plan)
    }
  }

  fun createAutomation(name: String, triggerType: String, actionType: String) {
    viewModelScope.launch {
      repository.createAutomation(name, triggerType, actionType)
      notificationHelper.notifyAutomationTriggered(name, "Rule registered for $triggerType")
    }
  }

  fun deleteAutomation(id: String) {
    viewModelScope.launch {
      repository.deleteAutomation(id)
    }
  }

  fun triggerTestNotification() {
    notificationHelper.notifyTaskComplete(
      title = "Think Deeper • Push Test",
      message = "Dynamic push notification channel verified for ${preferences.serverUrl.value}"
    )
  }

  fun sendMessage(prompt: String) {
    if (prompt.isBlank() || _isGenerating.value) return

    viewModelScope.launch {
      var convId = _currentConversationId.value
      if (convId == null) {
        convId = repository.createConversation()
        _currentConversationId.value = convId
      }

      _isGenerating.value = true
      _statusText.value = if (preferences.isDeepThink.value) "Deep thinking..." else "Reasoning..."
      _liveSteps.value = emptyList()

      activeGenerationJob = launch {
        try {
          repository.sendMessage(
            conversationId = convId,
            text = prompt,
            onStepUpdate = { step ->
              val currentList = _liveSteps.value.toMutableList()
              val idx = currentList.indexOfFirst { it.name == step.name }
              if (idx >= 0) {
                currentList[idx] = step
              } else {
                currentList.add(step)
              }
              _liveSteps.value = currentList
            }
          )
        } finally {
          _isGenerating.value = false
          _statusText.value = "Ready"
        }
      }
    }
  }

  fun stopGeneration() {
    activeGenerationJob?.cancel()
    _isGenerating.value = false
    _statusText.value = "Stopped"
  }
}

class ThinkDeeperViewModelFactory(
  private val repository: ThinkDeeperRepository,
  private val preferences: PreferencesManager,
  private val notificationHelper: NotificationHelper,
  private val authManager: AuthManager
) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(ThinkDeeperViewModel::class.java)) {
      return ThinkDeeperViewModel(repository, preferences, notificationHelper, authManager) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
