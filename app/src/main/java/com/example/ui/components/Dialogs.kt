package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.PluginItem
import com.example.data.local.PreferencesManager
import com.google.firebase.auth.FirebaseUser

@Composable
fun SettingsDialog(
  currentProvider: String,
  currentBaseUrl: String,
  currentApiKey: String,
  currentModel: String,
  currentServerUrl: String,
  currentAuthToken: String,
  notificationsEnabled: Boolean,
  currentTemperature: Float,
  currentSystemPrompt: String,
  onSave: (
    provider: String,
    baseUrl: String,
    apiKey: String,
    model: String,
    serverUrl: String,
    authToken: String,
    notifications: Boolean,
    temperature: Float,
    systemPrompt: String
  ) -> Unit,
  onTestNotification: () -> Unit,
  onDismiss: () -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  var provider by remember { mutableStateOf(currentProvider) }
  var baseUrl by remember { mutableStateOf(currentBaseUrl) }
  var apiKey by remember { mutableStateOf(currentApiKey) }
  var model by remember { mutableStateOf(currentModel) }
  var showApiKey by remember { mutableStateOf(false) }

  var serverUrl by remember { mutableStateOf(currentServerUrl) }
  var authToken by remember { mutableStateOf(currentAuthToken) }
  var showToken by remember { mutableStateOf(false) }
  var notifsEnabled by remember { mutableStateOf(notificationsEnabled) }
  var temperature by remember { mutableFloatStateOf(currentTemperature) }
  var systemPrompt by remember { mutableStateOf(currentSystemPrompt) }

  val tabs = listOf("API & Models", "Agent", "Personality", "Server")

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
      tonalElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 680.dp)
        .testTag("settings_dialog")
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
          )
          IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Mobile Tabs
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          contentColor = MaterialTheme.colorScheme.primary,
          modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
          tabs.forEachIndexed { index, title ->
            Tab(
              selected = selectedTab == index,
              onClick = { selectedTab = index },
              text = {
                Text(
                  text = title,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                  ),
                  maxLines = 1
                )
              }
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Scrollable content area
        Column(
          modifier = Modifier
            .weight(1f, fill = false)
            .verticalScroll(rememberScrollState())
        ) {
          when (selectedTab) {
            0 -> {
              // TAB 0: API & Models
              Text(
                text = "PROVIDER PRESET",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.height(6.dp))

              val presets = listOf(
                "gemini" to "Google Gemini",
                "openai" to "OpenAI",
                "groq" to "Groq",
                "openrouter" to "OpenRouter",
                "custom" to "Custom API"
              )

              LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                items(presets) { (key, label) ->
                  val isSelected = provider == key
                  Surface(
                    modifier = Modifier
                      .clip(RoundedCornerShape(10.dp))
                      .clickable {
                        provider = key
                        baseUrl = when (key) {
                          "gemini" -> PreferencesManager.DEFAULT_GEMINI_BASE
                          "openai" -> PreferencesManager.DEFAULT_OPENAI_BASE
                          "groq" -> PreferencesManager.DEFAULT_GROQ_BASE
                          "openrouter" -> PreferencesManager.DEFAULT_OPENROUTER_BASE
                          else -> baseUrl
                        }
                        if (key == "gemini" && (model.isBlank() || model.contains("gpt"))) {
                          model = "gemini-1.5-flash"
                        } else if (key == "openai" && (model.isBlank() || model.contains("gemini"))) {
                          model = "gpt-4o-mini"
                        }
                      },
                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                    else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                      1.dp,
                      if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                  ) {
                    Text(
                      text = label,
                      style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                      ),
                      color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              // API Key
              OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key") },
                placeholder = {
                  Text(
                    when (provider) {
                      "gemini" -> "AIzaSy..."
                      "openai" -> "sk-..."
                      "groq" -> "gsk_..."
                      "openrouter" -> "sk-or-..."
                      else -> "Enter provider API key"
                    }
                  )
                },
                trailingIcon = {
                  IconButton(onClick = { showApiKey = !showApiKey }) {
                    Icon(
                      imageVector = if (showApiKey) Icons.Default.Key else Icons.Default.Security,
                      contentDescription = "Toggle visibility",
                      modifier = Modifier.size(18.dp)
                    )
                  }
                },
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("settings_api_key_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
              )

              Spacer(modifier = Modifier.height(12.dp))

              // Model ID
              OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("Model ID") },
                placeholder = { Text("gemini-1.5-flash, gpt-4o, claude-3-5-sonnet...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
              )

              Spacer(modifier = Modifier.height(12.dp))

              // Base URL
              OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("Base URL Endpoint") },
                placeholder = { Text("https://api.openai.com/v1") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
              )
            }
            1 -> {
              // TAB 1: Agent Parameters
              Text(
                text = "REASONING & SAMPLING",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Sampling Temperature",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = String.format("%.2f", temperature),
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.primary
                )
              }

              Slider(
                value = temperature,
                onValueChange = { temperature = it },
                valueRange = 0.0f..1.5f,
                steps = 14,
                modifier = Modifier.fillMaxWidth()
              )

              Spacer(modifier = Modifier.height(12.dp))

              Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "Push Notifications",
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "Alert when long-horizon thinking finishes",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                  Switch(
                    checked = notifsEnabled,
                    onCheckedChange = { notifsEnabled = it },
                    colors = SwitchDefaults.colors(
                      checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                      checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                  )
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              OutlinedButton(
                onClick = onTestNotification,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send Test Notification", fontSize = 13.sp)
              }
            }
            2 -> {
              // TAB 2: Personality & Custom Instructions
              Text(
                text = "CUSTOM AGENT INSTRUCTIONS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.height(8.dp))

              OutlinedTextField(
                value = systemPrompt,
                onValueChange = { systemPrompt = it },
                label = { Text("System Persona & Logic Prompt") },
                placeholder = { Text("Describe how you want Think Deeper to reason and format responses…") },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(180.dp),
                maxLines = 8,
                shape = RoundedCornerShape(12.dp)
              )
            }
            3 -> {
              // TAB 3: Vercel Server & Auth
              Text(
                text = "CLOUD WORKSPACE ENDPOINT",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.height(8.dp))

              OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("Vercel Server URL") },
                placeholder = { Text("https://think-deeper-app.vercel.app") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
              )

              Spacer(modifier = Modifier.height(12.dp))

              OutlinedTextField(
                value = authToken,
                onValueChange = { authToken = it },
                label = { Text("Vercel Auth Token (Optional)") },
                placeholder = { Text("Bearer token for server routes") },
                trailingIcon = {
                  IconButton(onClick = { showToken = !showToken }) {
                    Icon(
                      imageVector = if (showToken) Icons.Default.Key else Icons.Default.Security,
                      contentDescription = null,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                },
                visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button
        Button(
          onClick = {
            onSave(provider, baseUrl, apiKey, model, serverUrl, authToken, notifsEnabled, temperature, systemPrompt)
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("save_settings_btn")
            .height(50.dp),
          shape = RoundedCornerShape(14.dp)
        ) {
          Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Save & Apply Settings", fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}

@Composable
fun AccountMenuDialog(
  userName: String,
  nickname: String,
  profilePicUrl: String,
  themeMode: String,
  colorPalette: String,
  fontTheme: String,
  currentUser: FirebaseUser?,
  isLoading: Boolean,
  onSaveProfile: (name: String, nickname: String, picUrl: String) -> Unit,
  onChangeThemeMode: (String) -> Unit,
  onChangePalette: (String) -> Unit,
  onChangeFontTheme: (String) -> Unit,
  onSignInWithGoogle: () -> Unit,
  onSignOut: () -> Unit,
  onDismiss: () -> Unit
) {
  var name by remember { mutableStateOf(userName) }
  var nick by remember { mutableStateOf(nickname) }
  var picUrl by remember { mutableStateOf(profilePicUrl) }

  val palettes = listOf(
    "default" to ("Default" to Color(0xFFD97757)),
    "red" to ("Red" to Color(0xFFDC2626)),
    "pink" to ("Pink" to Color(0xFFDB2777)),
    "green" to ("Green" to Color(0xFF16A34A)),
    "orange" to ("Orange" to Color(0xFFEA580C)),
    "yellow" to ("Yellow" to Color(0xFFCA8A04)),
    "blue" to ("Blue" to Color(0xFF2563EB)),
    "violet" to ("Violet" to Color(0xFF7C3AED))
  )

  val fonts = listOf(
    "default" to "Anthropic Default",
    "serif" to "Editorial Serif",
    "mono" to "Monospace Code",
    "round" to "Clean Sans"
  )

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
      tonalElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 680.dp)
        .testTag("account_menu_dialog")
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Account & Appearance",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
          )
          IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Profile Avatar Card
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = CircleShape,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(52.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                val initial = (name.firstOrNull() ?: 'U').uppercaseChar()
                Text(
                  text = "$initial",
                  style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onPrimary
                )
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = name.ifBlank { "Thinker" },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = currentUser?.email ?: "Personal Workspace",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Name & Nickname inputs
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Your Name") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = nick,
          onValueChange = { nick = it },
          label = { Text("AI Addressing Nickname") },
          placeholder = { Text("e.g. Master, Alex, Thinker") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = picUrl,
          onValueChange = { picUrl = it },
          label = { Text("Avatar Image URL (Optional)") },
          placeholder = { Text("https://...") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // APPEARANCE & THEME
        Text(
          text = "THEME & COLOR PALETTE",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Dark / Light toggle row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("dark" to "Dark Canvas", "light" to "Light Warm").forEach { (mode, label) ->
            val isSelected = themeMode == mode
            Surface(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .clickable { onChangeThemeMode(mode) },
              color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
              else MaterialTheme.colorScheme.surfaceVariant,
              border = BorderStroke(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 10.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Color Swatches
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(palettes) { (key, pair) ->
            val (label, color) = pair
            val isSelected = colorPalette == key
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { onChangePalette(key) }
                .padding(4.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .background(color, CircleShape)
                  .padding(2.dp),
                contentAlignment = Alignment.Center
              ) {
                if (isSelected) {
                  Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // FONT THEME
        Text(
          text = "TYPOGRAPHY THEME",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(fonts) { (key, label) ->
            val isSelected = fontTheme == key
            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { onChangeFontTheme(key) },
              color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
              else MaterialTheme.colorScheme.surfaceVariant,
              border = BorderStroke(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Google Sign-In / Account Auth Section
        if (currentUser != null) {
          OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Sign Out")
          }
        } else {
          Button(
            onClick = onSignInWithGoogle,
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Connecting…")
            } else {
              Text("Sign in with Google")
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Save Button
        Button(
          onClick = {
            onSaveProfile(name, nick, picUrl)
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Save Profile", fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}

@Composable
fun PluginsDialog(
  plugins: List<PluginItem>,
  onTogglePlugin: (String) -> Unit,
  onSavePlugin: (PluginItem) -> Unit,
  onDeletePlugin: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var showCreateDialog by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
      tonalElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 680.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Plugins & Tools",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
          )
          IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Text(
          text = "Extend Think Deeper with custom instructions, tools, and training data.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
          onClick = { showCreateDialog = true },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Upload / Create Plugin")
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
          modifier = Modifier
            .weight(1f, fill = false)
            .heightIn(max = 380.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(plugins) { plugin ->
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                  modifier = Modifier.size(40.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text(text = plugin.icon, fontSize = 18.sp)
                  }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = plugin.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = plugin.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                  )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Switch(
                  checked = plugin.enabled,
                  onCheckedChange = { onTogglePlugin(plugin.id) },
                  colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                  )
                )

                IconButton(
                  onClick = { onDeletePlugin(plugin.id) },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  if (showCreateDialog) {
    var pName by remember { mutableStateOf("") }
    var pDesc by remember { mutableStateOf("") }
    var pInst by remember { mutableStateOf("") }
    var pKnow by remember { mutableStateOf("") }
    var pIcon by remember { mutableStateOf("🧩") }

    AlertDialog(
      onDismissRequest = { showCreateDialog = false },
      title = { Text("New Plugin / Tool Spec", style = MaterialTheme.typography.titleLarge) },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
          OutlinedTextField(
            value = pName,
            onValueChange = { pName = it },
            label = { Text("Plugin Name") },
            placeholder = { Text("e.g. KaTeX Formatter, Code Auditor") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = pDesc,
            onValueChange = { pDesc = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = pInst,
            onValueChange = { pInst = it },
            label = { Text("System Instructions") },
            placeholder = { Text("Rules and behavior logic...") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = pKnow,
            onValueChange = { pKnow = it },
            label = { Text("Knowledge & Training Base (Spec)") },
            placeholder = { Text("Documentation or contextual knowledge...") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (pName.isNotBlank()) {
              onSavePlugin(
                PluginItem(
                  id = java.util.UUID.randomUUID().toString(),
                  name = pName,
                  description = pDesc,
                  instructions = pInst,
                  knowledge = pKnow,
                  icon = pIcon,
                  enabled = true
                )
              )
              showCreateDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
          Text("Add Plugin")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCreateDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun ExternalWorkspaceDialog(
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text("External Workspace", style = MaterialTheme.typography.titleLarge)
      }
    },
    text = {
      Column {
        Text(
          text = "Local files & attached project directories are synced to the active agent environment.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "Active Environment Path",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "/workspace/project",
              style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Status: Realtime Room DB Sync Active",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF22C55E)
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Text("Done")
      }
    }
  )
}

@Composable
fun ModelSelectorDialog(
  currentModel: String,
  modelLibrary: List<String>,
  onSelectModel: (String) -> Unit,
  onAddModel: (String) -> Unit,
  onRemoveModel: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var newModelInput by remember { mutableStateOf("") }

  val defaultPopular = listOf(
    "gemini-1.5-flash",
    "gemini-2.0-flash-exp",
    "gemini-1.5-pro",
    "gpt-4o-mini",
    "gpt-4o",
    "claude-3-5-sonnet",
    "deepseek-r1",
    "llama-3.3-70b"
  )

  val allModels = (modelLibrary + defaultPopular).distinct()

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        "Choose AI Model",
        style = MaterialTheme.typography.titleLarge
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Select a model or enter your custom model identifier below.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Model List
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp)
        ) {
          items(allModels) { modelName ->
            val isSelected = currentModel == modelName
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                  onSelectModel(modelName)
                  onDismiss()
                },
              color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
              else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              border = BorderStroke(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = modelName,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  ),
                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.weight(1f)
                )

                if (isSelected) {
                  Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Add custom model ID input
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = newModelInput,
            onValueChange = { newModelInput = it },
            placeholder = { Text("Custom Model ID…", fontSize = 12.sp) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              if (newModelInput.isNotBlank()) {
                val clean = newModelInput.trim()
                onAddModel(clean)
                onSelectModel(clean)
                newModelInput = ""
                onDismiss()
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Text("Done")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun NewProjectDialog(
  onCreate: (name: String, type: String, plan: String) -> Unit,
  onDismiss: () -> Unit
) {
  var name by remember { mutableStateOf("") }
  var type by remember { mutableStateOf("Web app") }
  var plan by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text("Create Project", style = MaterialTheme.typography.titleLarge)
    },
    text = {
      Column {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Project Name") },
          placeholder = { Text("e.g. Portfolio app") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          shape = RoundedCornerShape(10.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text("Project Type", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("Web app", "Library", "Service").forEach { t ->
            val sel = type == t
            Surface(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .clickable { type = t },
              color = if (sel) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
              else MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = t,
                style = MaterialTheme.typography.labelSmall,
                color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
          value = plan,
          onValueChange = { plan = it },
          label = { Text("Spec / Plan (Optional)") },
          placeholder = { Text("Key milestones or architectural constraints…") },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 3,
          shape = RoundedCornerShape(10.dp)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            onCreate(name, type, plan)
            onDismiss()
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Text("Create")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun NewAutomationDialog(
  onCreate: (name: String, triggerType: String, actionType: String) -> Unit,
  onDismiss: () -> Unit
) {
  var name by remember { mutableStateOf("") }
  var triggerType by remember { mutableStateOf("onAiReply") }
  var actionType by remember { mutableStateOf("notify") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text("Create Automation", style = MaterialTheme.typography.titleLarge)
    },
    text = {
      Column {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Rule Name") },
          placeholder = { Text("e.g. When AI replies → push notify") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          shape = RoundedCornerShape(10.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text("Trigger Event", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("onAiReply" to "AI Reply", "onUserSend" to "User Send", "clock" to "Clock").forEach { (type, label) ->
            Surface(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .clickable { triggerType = type },
              color = if (triggerType == type) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
              else MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (triggerType == type) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Action", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("notify" to "Notify", "playAudio" to "Sound", "store" to "Store").forEach { (act, label) ->
            Surface(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .clickable { actionType = act },
              color = if (actionType == act) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
              else MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (actionType == act) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            onCreate(name, triggerType, actionType)
            onDismiss()
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Text("Create Rule")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
