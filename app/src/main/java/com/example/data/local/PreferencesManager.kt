package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class PluginItem(
  val id: String,
  val name: String,
  val description: String,
  val instructions: String,
  val knowledge: String,
  val icon: String = "🧩",
  val enabled: Boolean = true,
  val isTraining: Boolean = false
)

class PreferencesManager(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("think_deeper_prefs", Context.MODE_PRIVATE)

  // API Provider: "openai", "groq", "openrouter", "gemini", "custom"
  private val _apiProvider = MutableStateFlow(
    prefs.getString(KEY_API_PROVIDER, "openai") ?: "openai"
  )
  val apiProvider: StateFlow<String> = _apiProvider.asStateFlow()

  // API Base URL
  private val _apiBaseUrl = MutableStateFlow(
    prefs.getString(KEY_API_BASE_URL, DEFAULT_OPENAI_BASE) ?: DEFAULT_OPENAI_BASE
  )
  val apiBaseUrl: StateFlow<String> = _apiBaseUrl.asStateFlow()

  // API Key
  private val _apiKey = MutableStateFlow(
    prefs.getString(KEY_API_KEY, "") ?: ""
  )
  val apiKey: StateFlow<String> = _apiKey.asStateFlow()

  // Main Active Model
  private val _selectedModel = MutableStateFlow(
    prefs.getString(KEY_SELECTED_MODEL, "") ?: ""
  )
  val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

  // Custom added models in library
  private val _customModels = MutableStateFlow(
    prefs.getStringSet(KEY_CUSTOM_MODELS, DEFAULT_MODEL_SET)?.toList() ?: DEFAULT_MODEL_LIST
  )
  val customModels: StateFlow<List<String>> = _customModels.asStateFlow()

  // Role Models
  private val _talkModel = MutableStateFlow(
    prefs.getString(KEY_TALK_MODEL, "") ?: ""
  )
  val talkModel: StateFlow<String> = _talkModel.asStateFlow()

  private val _codeModel = MutableStateFlow(
    prefs.getString(KEY_CODE_MODEL, "") ?: ""
  )
  val codeModel: StateFlow<String> = _codeModel.asStateFlow()

  private val _reasonModel = MutableStateFlow(
    prefs.getString(KEY_REASON_MODEL, "") ?: ""
  )
  val reasonModel: StateFlow<String> = _reasonModel.asStateFlow()

  // Temperature
  private val _temperature = MutableStateFlow(
    prefs.getFloat(KEY_TEMPERATURE, 0.7f)
  )
  val temperature: StateFlow<Float> = _temperature.asStateFlow()

  // System Style Prompt / Persona
  private val _systemPrompt = MutableStateFlow(
    prefs.getString(KEY_SYSTEM_PROMPT, DEFAULT_SYSTEM_PROMPT) ?: DEFAULT_SYSTEM_PROMPT
  )
  val systemPrompt: StateFlow<String> = _systemPrompt.asStateFlow()

  // Vercel Server & Auth
  private val _serverUrl = MutableStateFlow(
    prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
  )
  val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

  private val _authToken = MutableStateFlow(
    prefs.getString(KEY_AUTH_TOKEN, "") ?: ""
  )
  val authToken: StateFlow<String> = _authToken.asStateFlow()

  // Reasoning options
  private val _intensity = MutableStateFlow(
    prefs.getString(KEY_INTENSITY, "medium") ?: "medium"
  )
  val intensity: StateFlow<String> = _intensity.asStateFlow()

  private val _isDeepThink = MutableStateFlow(
    prefs.getBoolean(KEY_DEEP_THINK, false)
  )
  val isDeepThink: StateFlow<Boolean> = _isDeepThink.asStateFlow()

  private val _notificationsEnabled = MutableStateFlow(
    prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
  )
  val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

  // Account / User profile settings (Item #6)
  private val _userName = MutableStateFlow(
    prefs.getString(KEY_USER_NAME, "You") ?: "You"
  )
  val userName: StateFlow<String> = _userName.asStateFlow()

  private val _nickname = MutableStateFlow(
    prefs.getString(KEY_NICKNAME, "Thinker") ?: "Thinker"
  )
  val nickname: StateFlow<String> = _nickname.asStateFlow()

  private val _profilePicUrl = MutableStateFlow(
    prefs.getString(KEY_PROFILE_PIC_URL, "") ?: ""
  )
  val profilePicUrl: StateFlow<String> = _profilePicUrl.asStateFlow()

  // Appearance & Theme settings
  // "dark", "light"
  private val _themeMode = MutableStateFlow(
    prefs.getString(KEY_THEME_MODE, "dark") ?: "dark"
  )
  val themeMode: StateFlow<String> = _themeMode.asStateFlow()

  // "default", "red", "pink", "green", "orange", "yellow", "blue", "violet"
  private val _colorPalette = MutableStateFlow(
    prefs.getString(KEY_COLOR_PALETTE, "default") ?: "default"
  )
  val colorPalette: StateFlow<String> = _colorPalette.asStateFlow()

  // "default", "serif", "mono", "round"
  private val _fontTheme = MutableStateFlow(
    prefs.getString(KEY_FONT_THEME, "default") ?: "default"
  )
  val fontTheme: StateFlow<String> = _fontTheme.asStateFlow()

  // Plugins list
  private val _plugins = MutableStateFlow(
    loadPlugins()
  )
  val plugins: StateFlow<List<PluginItem>> = _plugins.asStateFlow()

  private fun loadPlugins(): List<PluginItem> {
    val raw = prefs.getString(KEY_PLUGINS, null) ?: return defaultPlugins()
    return try {
      val array = JSONArray(raw)
      val list = mutableListOf<PluginItem>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          PluginItem(
            id = obj.optString("id", i.toString()),
            name = obj.optString("name", "Plugin"),
            description = obj.optString("description", ""),
            instructions = obj.optString("instructions", ""),
            knowledge = obj.optString("knowledge", ""),
            icon = obj.optString("icon", "🧩"),
            enabled = obj.optBoolean("enabled", true),
            isTraining = obj.optBoolean("isTraining", false)
          )
        )
      }
      if (list.isEmpty()) defaultPlugins() else list
    } catch (e: Exception) {
      defaultPlugins()
    }
  }

  private fun defaultPlugins(): List<PluginItem> {
    return listOf(
      PluginItem(
        id = "math-latex",
        name = "Math & LaTeX",
        description = "Renders formulas with KaTeX notation",
        instructions = "Wrap math formulas in inline \$...\$ or block \$\$...\$\$ with aligned equations.",
        knowledge = "Math parsing rule: inline \$x^2\$, display \$\$\\nabla f(\\mathbf{x})=\\lambda\\nabla g(\\mathbf{x})\$\$.",
        icon = "∑",
        enabled = true
      ),
      PluginItem(
        id = "clean-code",
        name = "Clean Coding",
        description = "Ensures robust structure, types and edge case handling",
        instructions = "Write clean modular code with explicit types, error handling, and unit test patterns.",
        knowledge = "Always review edge cases, syntax, and verify code blocks.",
        icon = "</>",
        enabled = true
      )
    )
  }

  private fun savePlugins(list: List<PluginItem>) {
    try {
      val array = JSONArray()
      for (p in list) {
        val obj = JSONObject().apply {
          put("id", p.id)
          put("name", p.name)
          put("description", p.description)
          put("instructions", p.instructions)
          put("knowledge", p.knowledge)
          put("icon", p.icon)
          put("enabled", p.enabled)
          put("isTraining", p.isTraining)
        }
        array.put(obj)
      }
      prefs.edit().putString(KEY_PLUGINS, array.toString()).apply()
      _plugins.value = list
    } catch (_: Exception) {}
  }

  fun savePlugin(plugin: PluginItem) {
    val current = _plugins.value.toMutableList()
    val index = current.indexOfFirst { it.id == plugin.id }
    if (index >= 0) {
      current[index] = plugin
    } else {
      current.add(plugin)
    }
    savePlugins(current)
  }

  fun deletePlugin(id: String) {
    val current = _plugins.value.filter { it.id != id }
    savePlugins(current)
  }

  fun togglePlugin(id: String) {
    val current = _plugins.value.map {
      if (it.id == id) it.copy(enabled = !it.enabled) else it
    }
    savePlugins(current)
  }

  fun setApiProvider(provider: String) {
    val baseUrl = when (provider) {
      "openai" -> DEFAULT_OPENAI_BASE
      "groq" -> DEFAULT_GROQ_BASE
      "openrouter" -> DEFAULT_OPENROUTER_BASE
      "gemini" -> DEFAULT_GEMINI_BASE
      else -> _apiBaseUrl.value
    }
    prefs.edit()
      .putString(KEY_API_PROVIDER, provider)
      .putString(KEY_API_BASE_URL, baseUrl)
      .apply()
    _apiProvider.value = provider
    _apiBaseUrl.value = baseUrl
  }

  fun setApiBaseUrl(url: String) {
    val clean = url.trim().removeSuffix("/")
    prefs.edit().putString(KEY_API_BASE_URL, clean).apply()
    _apiBaseUrl.value = clean
  }

  fun setApiKey(key: String) {
    val clean = key.trim()
    prefs.edit().putString(KEY_API_KEY, clean).apply()
    _apiKey.value = clean
  }

  fun setSelectedModel(model: String) {
    val clean = model.trim()
    prefs.edit().putString(KEY_SELECTED_MODEL, clean).apply()
    _selectedModel.value = clean
    addModelToLibrary(clean)
  }

  fun addModelToLibrary(model: String) {
    val clean = model.trim()
    if (clean.isBlank()) return
    val current = _customModels.value.toMutableList()
    if (!current.contains(clean)) {
      current.add(0, clean)
      prefs.edit().putStringSet(KEY_CUSTOM_MODELS, current.toSet()).apply()
      _customModels.value = current
    }
  }

  fun removeModelFromLibrary(model: String) {
    val current = _customModels.value.toMutableList()
    if (current.remove(model)) {
      prefs.edit().putStringSet(KEY_CUSTOM_MODELS, current.toSet()).apply()
      _customModels.value = current
    }
  }

  fun setRoleModels(talk: String, code: String, reason: String) {
    prefs.edit()
      .putString(KEY_TALK_MODEL, talk.trim())
      .putString(KEY_CODE_MODEL, code.trim())
      .putString(KEY_REASON_MODEL, reason.trim())
      .apply()
    _talkModel.value = talk.trim()
    _codeModel.value = code.trim()
    _reasonModel.value = reason.trim()
  }

  fun setTemperature(temp: Float) {
    prefs.edit().putFloat(KEY_TEMPERATURE, temp).apply()
    _temperature.value = temp
  }

  fun setSystemPrompt(prompt: String) {
    prefs.edit().putString(KEY_SYSTEM_PROMPT, prompt).apply()
    _systemPrompt.value = prompt
  }

  fun setServerUrl(url: String) {
    val clean = url.trim().removeSuffix("/")
    prefs.edit().putString(KEY_SERVER_URL, clean).apply()
    _serverUrl.value = clean
  }

  fun setAuthToken(token: String) {
    val clean = token.trim()
    prefs.edit().putString(KEY_AUTH_TOKEN, clean).apply()
    _authToken.value = clean
  }

  fun setIntensity(intensity: String) {
    prefs.edit().putString(KEY_INTENSITY, intensity).apply()
    _intensity.value = intensity
  }

  fun setDeepThink(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_DEEP_THINK, enabled).apply()
    _isDeepThink.value = enabled
  }

  fun setNotificationsEnabled(enabled: Boolean) {
    prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    _notificationsEnabled.value = enabled
  }

  fun setUserName(name: String) {
    prefs.edit().putString(KEY_USER_NAME, name).apply()
    _userName.value = name
  }

  fun setNickname(name: String) {
    prefs.edit().putString(KEY_NICKNAME, name).apply()
    _nickname.value = name
  }

  fun setProfilePicUrl(url: String) {
    prefs.edit().putString(KEY_PROFILE_PIC_URL, url).apply()
    _profilePicUrl.value = url
  }

  fun setThemeMode(mode: String) {
    prefs.edit().putString(KEY_THEME_MODE, mode).apply()
    _themeMode.value = mode
  }

  fun setColorPalette(palette: String) {
    prefs.edit().putString(KEY_COLOR_PALETTE, palette).apply()
    _colorPalette.value = palette
  }

  fun setFontTheme(font: String) {
    prefs.edit().putString(KEY_FONT_THEME, font).apply()
    _fontTheme.value = font
  }

  companion object {
    const val DEFAULT_SERVER_URL = "https://think-deeper-app.vercel.app"
    const val DEFAULT_OPENAI_BASE = "https://api.openai.com/v1"
    const val DEFAULT_GROQ_BASE = "https://api.groq.com/openai/v1"
    const val DEFAULT_OPENROUTER_BASE = "https://openrouter.ai/api/v1"
    const val DEFAULT_GEMINI_BASE = "https://generativelanguage.googleapis.com/v1beta/openai"
    const val DEFAULT_SYSTEM_PROMPT = "You are Think Deeper, an advanced agentic intelligence that reasons through complex problems systematically with verified steps and rigorous logic."

    private val DEFAULT_MODEL_LIST = emptyList<String>()
    private val DEFAULT_MODEL_SET = emptySet<String>()

    private const val KEY_API_PROVIDER = "api_provider"
    private const val KEY_API_BASE_URL = "api_base_url"
    private const val KEY_API_KEY = "api_key"
    private const val KEY_SELECTED_MODEL = "selected_model"
    private const val KEY_CUSTOM_MODELS = "custom_models"
    private const val KEY_TALK_MODEL = "talk_model"
    private const val KEY_CODE_MODEL = "code_model"
    private const val KEY_REASON_MODEL = "reason_model"
    private const val KEY_TEMPERATURE = "temperature"
    private const val KEY_SYSTEM_PROMPT = "system_prompt"
    private const val KEY_SERVER_URL = "server_url"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_INTENSITY = "intensity"
    private const val KEY_DEEP_THINK = "deep_think"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_NICKNAME = "nickname"
    private const val KEY_PROFILE_PIC_URL = "profile_pic_url"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_COLOR_PALETTE = "color_palette"
    private const val KEY_FONT_THEME = "font_theme"
    private const val KEY_PLUGINS = "plugins"
  }
}
