package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auth.AuthManager
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.repository.ThinkDeeperRepository
import com.example.notification.NotificationHelper
import com.example.ui.AppMode
import com.example.ui.ThinkDeeperViewModel
import com.example.ui.ThinkDeeperViewModelFactory
import com.example.ui.components.AccountMenuDialog
import com.example.ui.components.AutomationsView
import com.example.ui.components.ChatView
import com.example.ui.components.CodeWorkspaceView
import com.example.ui.components.DrawerContent
import com.example.ui.components.ExternalWorkspaceDialog
import com.example.ui.components.MessageInputArea
import com.example.ui.components.ModelSelectorDialog
import com.example.ui.components.NewAutomationDialog
import com.example.ui.components.NewProjectDialog
import com.example.ui.components.PluginsDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TopHeaderBar
import com.example.ui.theme.ThinkDeeperTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = AppDatabase.getInstance(this)
    val preferences = PreferencesManager(this)
    val notificationHelper = NotificationHelper(this)
    val authManager = AuthManager(this, preferences)
    val repository = ThinkDeeperRepository(database, preferences, notificationHelper)
    val viewModelFactory = ThinkDeeperViewModelFactory(repository, preferences, notificationHelper, authManager)

    setContent {
      val viewModel: ThinkDeeperViewModel = viewModel(factory = viewModelFactory)
      val themeMode by viewModel.themeMode.collectAsState()
      val colorPalette by viewModel.colorPalette.collectAsState()
      val fontTheme by viewModel.fontTheme.collectAsState()

      ThinkDeeperTheme(
        themeMode = themeMode,
        colorPalette = colorPalette,
        fontTheme = fontTheme
      ) {
        ThinkDeeperApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun ThinkDeeperApp(viewModel: ThinkDeeperViewModel) {
  val context = LocalContext.current
  val activity = context as? ComponentActivity
  val scope = rememberCoroutineScope()
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

  // Notification permission request for Android 13+
  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { /* result handled */ }

  LaunchedEffect(Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }

  // Observable States
  val activeMode by viewModel.activeMode.collectAsState()
  val conversations by viewModel.conversations.collectAsState()
  val currentConversationId by viewModel.currentConversationId.collectAsState()
  val currentMessages by viewModel.currentMessages.collectAsState()
  val projects by viewModel.projects.collectAsState()
  val automations by viewModel.automations.collectAsState()

  val apiProvider by viewModel.apiProvider.collectAsState()
  val apiBaseUrl by viewModel.apiBaseUrl.collectAsState()
  val apiKey by viewModel.apiKey.collectAsState()
  val selectedModel by viewModel.selectedModel.collectAsState()
  val customModels by viewModel.customModels.collectAsState()
  val temperature by viewModel.temperature.collectAsState()
  val systemPrompt by viewModel.systemPrompt.collectAsState()

  val syncStatus by viewModel.syncStatus.collectAsState()
  val isGenerating by viewModel.isGenerating.collectAsState()
  val liveSteps by viewModel.liveSteps.collectAsState()
  val statusText by viewModel.statusText.collectAsState()
  val isDeepThink by viewModel.isDeepThink.collectAsState()
  val intensity by viewModel.intensity.collectAsState()
  val serverUrl by viewModel.serverUrl.collectAsState()
  val authToken by viewModel.authToken.collectAsState()
  val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()

  val userName by viewModel.userName.collectAsState()
  val nickname by viewModel.nickname.collectAsState()
  val profilePicUrl by viewModel.profilePicUrl.collectAsState()
  val themeMode by viewModel.themeMode.collectAsState()
  val colorPalette by viewModel.colorPalette.collectAsState()
  val fontTheme by viewModel.fontTheme.collectAsState()
  val plugins by viewModel.plugins.collectAsState()

  val currentUser by viewModel.currentUser.collectAsState()
  val isAuthLoading by viewModel.isAuthLoading.collectAsState()

  var isSettingsOpen by remember { mutableStateOf(false) }
  var isModelSelectorOpen by remember { mutableStateOf(false) }
  var isNewProjectOpen by remember { mutableStateOf(false) }
  var isNewAutomationOpen by remember { mutableStateOf(false) }
  var isAccountOpen by remember { mutableStateOf(false) }
  var isPluginsOpen by remember { mutableStateOf(false) }
  var isExternalWorkspaceOpen by remember { mutableStateOf(false) }

  var inputText by remember { mutableStateOf("") }

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      DrawerContent(
        currentMode = activeMode,
        conversations = conversations,
        currentConversationId = currentConversationId,
        projects = projects,
        onSelectMode = { mode -> viewModel.setMode(mode) },
        onSelectConversation = { id -> viewModel.selectConversation(id) },
        onNewConversation = { viewModel.createNewConversation() },
        onDeleteConversation = { id -> viewModel.deleteConversation(id) },
        onRenameConversation = { id, title -> viewModel.renameConversation(id, title) },
        onTogglePinConversation = { id -> viewModel.togglePinConversation(id) },
        onNewProject = { isNewProjectOpen = true },
        onOpenPlugins = { isPluginsOpen = true },
        onOpenExternalWorkspace = { isExternalWorkspaceOpen = true },
        onOpenAutomations = { isNewAutomationOpen = true },
        onCloseDrawer = { scope.launch { drawerState.close() } }
      )
    }
  ) {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = {
        TopHeaderBar(
          selectedModel = selectedModel,
          syncStatus = syncStatus,
          onOpenDrawer = { scope.launch { drawerState.open() } },
          onOpenModelSelector = { isModelSelectorOpen = true },
          onOpenSettings = { isSettingsOpen = true },
          onOpenAccount = { isAccountOpen = true }
        )
      },
      bottomBar = {
        if (activeMode == AppMode.CHAT) {
          MessageInputArea(
            inputText = inputText,
            onInputTextChange = { inputText = it },
            onSend = {
              val text = inputText
              inputText = ""
              viewModel.sendMessage(text)
            },
            onStop = { viewModel.stopGeneration() },
            isGenerating = isGenerating,
            isDeepThink = isDeepThink,
            onToggleDeepThink = { viewModel.toggleDeepThink(it) },
            intensity = intensity,
            onSelectIntensity = { viewModel.setIntensity(it) },
            statusText = statusText
          )
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        when (activeMode) {
          AppMode.CHAT -> {
            ChatView(
              messages = currentMessages,
              isGenerating = isGenerating,
              liveSteps = liveSteps,
              onSelectSuggestion = { suggestion ->
                viewModel.sendMessage(suggestion)
              }
            )
          }

          AppMode.SPEC_AND_PLAN -> {
            ChatView(
              messages = currentMessages,
              isGenerating = isGenerating,
              liveSteps = liveSteps,
              onSelectSuggestion = { suggestion ->
                viewModel.sendMessage("Generate architectural specification and task implementation plan for: $suggestion")
              }
            )
          }

          AppMode.AUTOMATIONS -> {
            AutomationsView(
              automations = automations,
              onNewAutomation = { isNewAutomationOpen = true },
              onDeleteAutomation = { viewModel.deleteAutomation(it) }
            )
          }
        }
      }
    }
  }

  // Dialogs
  if (isSettingsOpen) {
    SettingsDialog(
      currentProvider = apiProvider,
      currentBaseUrl = apiBaseUrl,
      currentApiKey = apiKey,
      currentModel = selectedModel,
      currentServerUrl = serverUrl,
      currentAuthToken = authToken,
      notificationsEnabled = notificationsEnabled,
      currentTemperature = temperature,
      currentSystemPrompt = systemPrompt,
      onSave = { prov, base, key, mdl, srvUrl, tok, notifs, temp, prompt ->
        viewModel.updateSettings(prov, base, key, mdl, srvUrl, tok, notifs, temp, prompt)
      },
      onTestNotification = { viewModel.triggerTestNotification() },
      onDismiss = { isSettingsOpen = false }
    )
  }

  if (isModelSelectorOpen) {
    ModelSelectorDialog(
      currentModel = selectedModel,
      modelLibrary = customModels,
      onSelectModel = { viewModel.setSelectedModel(it) },
      onAddModel = { viewModel.addCustomModel(it) },
      onRemoveModel = { viewModel.removeCustomModel(it) },
      onDismiss = { isModelSelectorOpen = false }
    )
  }

  if (isNewProjectOpen) {
    NewProjectDialog(
      onCreate = { name, type, plan ->
        viewModel.createProject(name, type, plan)
      },
      onDismiss = { isNewProjectOpen = false }
    )
  }

  if (isNewAutomationOpen) {
    NewAutomationDialog(
      onCreate = { name, trigger, action ->
        viewModel.createAutomation(name, trigger, action)
      },
      onDismiss = { isNewAutomationOpen = false }
    )
  }

  if (isPluginsOpen) {
    PluginsDialog(
      plugins = plugins,
      onTogglePlugin = { viewModel.togglePlugin(it) },
      onSavePlugin = { viewModel.savePlugin(it) },
      onDeletePlugin = { viewModel.deletePlugin(it) },
      onDismiss = { isPluginsOpen = false }
    )
  }

  if (isExternalWorkspaceOpen) {
    ExternalWorkspaceDialog(
      onDismiss = { isExternalWorkspaceOpen = false }
    )
  }

  if (isAccountOpen) {
    AccountMenuDialog(
      userName = userName,
      nickname = nickname,
      profilePicUrl = profilePicUrl,
      themeMode = themeMode,
      colorPalette = colorPalette,
      fontTheme = fontTheme,
      currentUser = currentUser,
      isLoading = isAuthLoading,
      onSaveProfile = { name, nick, picUrl ->
        viewModel.updateProfile(name, nick, picUrl)
      },
      onChangeThemeMode = { viewModel.setThemeMode(it) },
      onChangePalette = { viewModel.setColorPalette(it) },
      onChangeFontTheme = { viewModel.setFontTheme(it) },
      onSignInWithGoogle = {
        activity?.let { viewModel.signInWithGoogle(it) }
      },
      onSignOut = { viewModel.signOut() },
      onDismiss = { isAccountOpen = false }
    )
  }
}

// Preserve Greeting for backward compatibility with existing tests
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  ThinkDeeperTheme { Greeting("Android") }
}
