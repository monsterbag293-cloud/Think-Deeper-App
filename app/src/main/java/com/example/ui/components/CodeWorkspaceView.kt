package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ActivityStep
import com.example.data.local.MessageEntity
import com.example.ui.VirtualFile

@Composable
fun CodeWorkspaceView(
  virtualFiles: List<VirtualFile>,
  messages: List<MessageEntity>,
  isGenerating: Boolean,
  liveSteps: List<ActivityStep>,
  onSendMessage: (String) -> Unit,
  onStop: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Code Chat, 1: Live Preview
  val skills = listOf("TDD", "Code Review", "Refactor", "Debug", "Architecture", "Frontend", "Docs")
  var selectedSkills by remember { mutableStateOf(setOf("TDD", "Code Review", "Frontend")) }
  var codeInputText by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  LaunchedEffect(messages.size, isGenerating) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("code_workspace_view")
  ) {
    // Top Tabs: Code Assistant Chat vs Preview
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      contentColor = MaterialTheme.colorScheme.primary
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Code AI Chat")
          }
        }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("App Preview")
          }
        }
      )
    }

    // Skills Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "SKILLS:",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
      )
      Spacer(modifier = Modifier.width(6.dp))
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        items(skills) { skill ->
          val isSelected = selectedSkills.contains(skill)
          val chipBg by animateColorAsState(
            targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
            else MaterialTheme.colorScheme.surfaceVariant,
            label = "chipBg"
          )
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(99.dp))
              .clickable {
                selectedSkills = if (isSelected) selectedSkills - skill else selectedSkills + skill
              },
            color = chipBg,
            border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(99.dp)
          ) {
            Text(
              text = skill,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
              ),
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }
      }
    }

    if (selectedTab == 0) {
      // CODE CHAT PANEL (Replaced the code editor per user request)
      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
      ) {
        // Quick Action Prompt Starters
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          val codeQuickActions = listOf(
            "Write comprehensive unit tests",
            "Perform senior code review",
            "Refactor for performance & safety",
            "Generate REST API client",
            "Implement UI component"
          )
          items(codeQuickActions) { action ->
            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  onSendMessage(action)
                },
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
              shape = RoundedCornerShape(8.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  Icons.Default.AutoAwesome,
                  contentDescription = null,
                  modifier = Modifier.size(12.dp),
                  tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = action,
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Chat Message List in Code Menu
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
        ) {
          if (messages.isEmpty() && !isGenerating) {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(56.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    Icons.Default.DeveloperMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Talk to Code AI",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontFamily = FontFamily.Serif,
                  fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Ask for code generation, architecture planning, bug fixes, and unit testing.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          } else {
            LazyColumn(
              state = listState,
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(vertical = 8.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              items(messages, key = { it.id }) { msg ->
                MessageItem(message = msg)
              }

              if (isGenerating) {
                item(key = "code_generating_card") {
                  LiveThinkingCard(liveSteps = liveSteps)
                }
              }
            }
          }
        }

        // Code Chat Input Bar (Moves up with keyboard & sends on enter)
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .testTag("code_chat_input_container"),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
          tonalElevation = 3.dp
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = codeInputText,
              onValueChange = { codeInputText = it },
              placeholder = {
                Text(
                  "Ask Code AI (Enter to send)…",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
              },
              keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Send,
                capitalization = KeyboardCapitalization.Sentences
              ),
              keyboardActions = KeyboardActions(
                onSend = {
                  if (codeInputText.isNotBlank() && !isGenerating) {
                    val txt = codeInputText.trim()
                    codeInputText = ""
                    onSendMessage(txt)
                  }
                }
              ),
              modifier = Modifier
                .weight(1f)
                .onPreviewKeyEvent { keyEvent ->
                  if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyDown && !keyEvent.isShiftPressed) {
                    if (codeInputText.isNotBlank() && !isGenerating) {
                      val txt = codeInputText.trim()
                      codeInputText = ""
                      onSendMessage(txt)
                      true
                    } else false
                  } else false
                }
                .testTag("code_chat_input_field"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
              ),
              shape = RoundedCornerShape(20.dp),
              maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (isGenerating) {
              IconButton(
                onClick = onStop,
                modifier = Modifier
                  .size(42.dp)
                  .background(MaterialTheme.colorScheme.error, CircleShape)
              ) {
                Icon(
                  Icons.Default.Stop,
                  contentDescription = "Stop",
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }
            } else {
              val canSend = codeInputText.isNotBlank()
              val btnScale by animateFloatAsState(
                targetValue = if (canSend) 1.05f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "btnScale"
              )
              IconButton(
                onClick = {
                  if (canSend) {
                    val txt = codeInputText.trim()
                    codeInputText = ""
                    onSendMessage(txt)
                  }
                },
                enabled = canSend,
                modifier = Modifier
                  .scale(btnScale)
                  .size(42.dp)
                  .background(
                    if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape
                  )
                  .testTag("code_send_button")
              ) {
                Icon(
                  Icons.AutoMirrored.Filled.Send,
                  contentDescription = "Send",
                  tint = if (canSend) MaterialTheme.colorScheme.onPrimary
                  else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    } else {
      // Live Preview Tab
      Surface(
        modifier = Modifier
          .fillMaxSize()
          .padding(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
          ) {
            Column(
              modifier = Modifier.padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "Think Deeper Live Project",
                style = MaterialTheme.typography.headlineMedium.copy(
                  fontFamily = FontFamily.Serif,
                  fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Dynamic updates synced with your code & Vercel server.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(18.dp))
              Button(
                onClick = {
                  onSendMessage("Test the current project build and verify all components.")
                  selectedTab = 0
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("Chat with AI About This App")
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "Rendered from ${virtualFiles.find { it.name.endsWith(".html") }?.path ?: "code/index.html"}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
          )
        }
      }
    }
  }
}
