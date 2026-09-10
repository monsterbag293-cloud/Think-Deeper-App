package com.example.ui.components

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AttachedFile(
  val name: String,
  val uri: Uri
)

@Composable
fun MessageInputArea(
  inputText: String,
  onInputTextChange: (String) -> Unit,
  onSend: () -> Unit,
  onStop: () -> Unit,
  isGenerating: Boolean,
  isDeepThink: Boolean,
  onToggleDeepThink: (Boolean) -> Unit,
  intensity: String,
  onSelectIntensity: (String) -> Unit,
  statusText: String,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var intensityMenuExpanded by remember { mutableStateOf(false) }
  var attachedFiles by remember { mutableStateOf<List<AttachedFile>>(emptyList()) }

  // File Picker for all file types
  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenMultipleDocuments()
  ) { uris: List<Uri> ->
    if (uris.isNotEmpty()) {
      val newFiles = uris.map { uri ->
        var displayName = "file"
        try {
          context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
              displayName = cursor.getString(nameIndex)
            }
          }
        } catch (e: Exception) {
          displayName = uri.lastPathSegment ?: "document"
        }
        AttachedFile(name = displayName, uri = uri)
      }
      attachedFiles = attachedFiles + newFiles
    }
  }

  fun triggerSend() {
    if ((inputText.isNotBlank() || attachedFiles.isNotEmpty()) && !isGenerating) {
      if (attachedFiles.isNotEmpty()) {
        val attachmentsHeader = attachedFiles.joinToString(", ") { it.name }
        val prompt = if (inputText.isNotBlank()) {
          "$inputText\n\n[Attached files: $attachmentsHeader]"
        } else {
          "[Attached files: $attachmentsHeader]"
        }
        onInputTextChange(prompt)
      }
      attachedFiles = emptyList()
      onSend()
    }
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .imePadding()
      .navigationBarsPadding()
      .testTag("message_input_container"),
    color = MaterialTheme.colorScheme.background,
    tonalElevation = 2.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
      // Top row: Deep Think chip & Intensity Mode chip & Status label
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Deep Think toggle pill with spring animated background
        val deepThinkBg by animateColorAsState(
          targetValue = if (isDeepThink) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
          else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
          animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
          label = "deepThinkBg"
        )
        Surface(
          modifier = Modifier
            .testTag("deep_think_toggle")
            .clip(RoundedCornerShape(99.dp))
            .clickable { onToggleDeepThink(!isDeepThink) },
          color = deepThinkBg,
          border = if (isDeepThink) BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
          else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
          shape = RoundedCornerShape(99.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Psychology,
              contentDescription = "Deep Think mode",
              modifier = Modifier.size(14.dp),
              tint = if (isDeepThink) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "Deep Think",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isDeepThink) FontWeight.Bold else FontWeight.Medium
              ),
              color = if (isDeepThink) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Intensity dropdown pill (Fast, Medium, Hard, Ultra, Extreme Ultra)
        Box {
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(99.dp))
              .clickable { intensityMenuExpanded = true },
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(99.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${intensity.replaceFirstChar { it.uppercase() }} ▾",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          DropdownMenu(
            expanded = intensityMenuExpanded,
            onDismissRequest = { intensityMenuExpanded = false }
          ) {
            listOf(
              "fast" to "Fast (~1.5k budget)",
              "medium" to "Medium (~4k budget)",
              "hard" to "Hard (~8k budget)",
              "ultra" to "Ultra (Team of 3 agents ~16k)",
              "xultra" to "Extreme Ultra (~32k max)"
            ).forEach { (key, label) ->
              DropdownMenuItem(
                text = { Text(label, fontSize = 13.sp) },
                onClick = {
                  onSelectIntensity(key)
                  intensityMenuExpanded = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Status text
        Text(
          text = statusText,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
      }

      // Attached files list
      AnimatedVisibility(visible = attachedFiles.isNotEmpty()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(bottom = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          attachedFiles.forEach { file ->
            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Description,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = file.name,
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.widthIn(max = 140.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Remove attachment",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier
                    .size(14.dp)
                    .clickable { attachedFiles = attachedFiles - file }
                )
              }
            }
          }
        }
      }

      // Input box surface
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
          1.dp,
          if (isGenerating) MaterialTheme.colorScheme.primary
          else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        shadowElevation = 2.dp
      ) {
        // Centered horizontally and vertically
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Centered Attach files button supporting multiple file types
          IconButton(
            onClick = {
              filePickerLauncher.launch(arrayOf("*/*"))
            },
            modifier = Modifier
              .size(44.dp)
              .testTag("attach_file_btn")
          ) {
            Icon(
              imageVector = Icons.Default.AttachFile,
              contentDescription = "Attach files",
              modifier = Modifier.size(20.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Main text field: Placeholder strictly says "Message Think Deeper"
          TextField(
            value = inputText,
            onValueChange = onInputTextChange,
            placeholder = {
              Text(
                "Message Think Deeper",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
              )
            },
            keyboardOptions = KeyboardOptions(
              imeAction = ImeAction.Send,
              capitalization = KeyboardCapitalization.Sentences
            ),
            keyboardActions = KeyboardActions(
              onSend = { triggerSend() }
            ),
            modifier = Modifier
              .weight(1f)
              .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyDown && !keyEvent.isShiftPressed) {
                  triggerSend()
                  true
                } else false
              }
              .testTag("chat_input_field"),
            colors = TextFieldDefaults.colors(
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent,
              disabledContainerColor = Color.Transparent,
              focusedIndicatorColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent
            ),
            maxLines = 6
          )

          Spacer(modifier = Modifier.width(4.dp))

          // Send / Stop button with spring scale animation, centered vertically
          if (isGenerating) {
            IconButton(
              onClick = onStop,
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                .testTag("stop_generation_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop generation",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
              )
            }
          } else {
            val canSend = inputText.isNotBlank() || attachedFiles.isNotEmpty()
            val sendScale by animateFloatAsState(
              targetValue = if (canSend) 1f else 0.85f,
              animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
              label = "sendScale"
            )

            IconButton(
              onClick = { triggerSend() },
              enabled = canSend,
              modifier = Modifier
                .size(44.dp)
                .scale(sendScale)
                .clip(CircleShape)
                .background(
                  if (canSend) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
                .testTag("send_message_btn")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send message",
                tint = if (canSend) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }
  }
}
