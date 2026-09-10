package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AutomationEntity

@Composable
fun AutomationsView(
  automations: List<AutomationEntity>,
  onNewAutomation: () -> Unit,
  onDeleteAutomation: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  // Built-in presets if empty
  val displayList = remember(automations) {
    if (automations.isNotEmpty()) automations
    else listOf(
      AutomationEntity(
        id = "default_1",
        name = "When AI replies → Notify completion",
        enabled = true,
        triggerType = "onAiReply",
        actionType = "notify",
        created = System.currentTimeMillis()
      ),
      AutomationEntity(
        id = "default_2",
        name = "Sound on AI talked",
        enabled = true,
        triggerType = "onBroadcast",
        actionType = "playAudio",
        created = System.currentTimeMillis()
      ),
      AutomationEntity(
        id = "default_3",
        name = "Daily briefing at 09:00",
        enabled = false,
        triggerType = "clock",
        actionType = "sendSilent",
        created = System.currentTimeMillis()
      )
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("automations_view")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Automations",
          style = MaterialTheme.typography.headlineMedium.copy(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold
          ),
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "Event-driven background workflows and scheduled triggers",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Button(
        onClick = onNewAutomation,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(10.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("New")
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(bottom = 24.dp)
    ) {
      items(displayList, key = { it.id }) { rule ->
        AutomationCard(
          rule = rule,
          onDelete = { onDeleteAutomation(rule.id) }
        )
      }
    }
  }
}

@Composable
fun AutomationCard(
  rule: AutomationEntity,
  onDelete: () -> Unit
) {
  var isEnabled by remember { mutableStateOf(rule.enabled) }

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    tonalElevation = 1.dp
  ) {
    Column(
      modifier = Modifier.padding(16.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = rule.name,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.weight(1f)
        )
        Switch(
          checked = isEnabled,
          onCheckedChange = { isEnabled = it },
          colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            checkedTrackColor = MaterialTheme.colorScheme.primary
          )
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Visual pipeline blocks: Trigger -> Action
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        PipelineChip(
          label = when (rule.triggerType) {
            "onAiReply" -> "When AI Replies"
            "onUserSend" -> "When You Send"
            "clock" -> "Scheduled 09:00"
            else -> "Broadcast Event"
          },
          icon = when (rule.triggerType) {
            "clock" -> Icons.Default.Schedule
            else -> Icons.Default.AutoAwesome
          },
          isTrigger = true
        )

        Spacer(modifier = Modifier.width(8.dp))
        Icon(
          imageVector = Icons.Default.ArrowForward,
          contentDescription = null,
          modifier = Modifier.size(14.dp),
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))

        PipelineChip(
          label = when (rule.actionType) {
            "notify" -> "Push Notification"
            "playAudio" -> "Play Chime"
            "store" -> "Store Answer"
            else -> "Agent Reason"
          },
          icon = when (rule.actionType) {
            "notify" -> Icons.Default.Notifications
            "playAudio" -> Icons.Default.VolumeUp
            "store" -> Icons.Default.Storage
            else -> Icons.Default.AutoAwesome
          },
          isTrigger = false
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            Icons.Default.Delete,
            contentDescription = "Delete automation",
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun PipelineChip(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isTrigger: Boolean
) {
  val bg = if (isTrigger) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
  else MaterialTheme.colorScheme.surfaceVariant
  val textColor = if (isTrigger) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

  Surface(
    color = bg,
    shape = RoundedCornerShape(8.dp)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(12.dp),
        tint = textColor
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
        color = textColor
      )
    }
  }
}
