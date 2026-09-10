package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ConversationEntity
import com.example.data.local.ProjectEntity
import com.example.ui.AppMode

@Composable
fun DrawerContent(
  currentMode: AppMode,
  conversations: List<ConversationEntity>,
  currentConversationId: String?,
  projects: List<ProjectEntity>,
  onSelectMode: (AppMode) -> Unit,
  onSelectConversation: (String) -> Unit,
  onNewConversation: () -> Unit,
  onDeleteConversation: (String) -> Unit,
  onRenameConversation: (String, String) -> Unit,
  onTogglePinConversation: (String) -> Unit,
  onNewProject: () -> Unit,
  onOpenPlugins: () -> Unit,
  onOpenExternalWorkspace: () -> Unit,
  onOpenAutomations: () -> Unit,
  onCloseDrawer: () -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var renameTargetId by remember { mutableStateOf<String?>(null) }
  var renameText by remember { mutableStateOf("") }

  Surface(
    modifier = Modifier
      .fillMaxHeight()
      .width(320.dp)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("drawer_content"),
    shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.98f),
    tonalElevation = 6.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxHeight()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      // Header: Brand & New Chat button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = "⚡",
                fontSize = 18.sp
              )
            }
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Think Deeper",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
              ),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Agentic Intelligence",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        IconButton(
          onClick = {
            onNewConversation()
            onCloseDrawer()
          },
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            .testTag("new_chat_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Start New Chat",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Navigation Modes (Chat, Spec & Plan, Automations)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        NavModeTab(
          title = "Chat",
          icon = Icons.Default.ChatBubbleOutline,
          isSelected = currentMode == AppMode.CHAT,
          onClick = {
            onSelectMode(AppMode.CHAT)
            onCloseDrawer()
          },
          modifier = Modifier.weight(1f)
        )
        NavModeTab(
          title = "Spec",
          icon = Icons.Default.Code,
          isSelected = currentMode == AppMode.SPEC_AND_PLAN,
          onClick = {
            onSelectMode(AppMode.SPEC_AND_PLAN)
            onCloseDrawer()
          },
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Search Bar
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search conversations…", fontSize = 13.sp) },
        leadingIcon = {
          Icon(
            Icons.Default.Search,
            contentDescription = "Search",
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
              Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
            }
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
          focusedBorderColor = MaterialTheme.colorScheme.primary
        ),
        singleLine = true
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Recents & Conversations List
      val filteredConversations = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) conversations
        else conversations.filter { it.title.contains(searchQuery, ignoreCase = true) }
      }

      Text(
        text = "RECENT CHATS",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
      )

      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
      ) {
        if (filteredConversations.isEmpty()) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (searchQuery.isNotBlank()) "No matching conversations" else "No recent conversations",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
              )
            }
          }
        } else {
          items(filteredConversations, key = { it.id }) { conv ->
            val isSelected = conv.id == currentConversationId
            ConversationRow(
              conversation = conv,
              isSelected = isSelected,
              onClick = {
                onSelectConversation(conv.id)
                onCloseDrawer()
              },
              onDelete = { onDeleteConversation(conv.id) },
              onRename = { newName -> onRenameConversation(conv.id, newName) },
              onTogglePin = { onTogglePinConversation(conv.id) }
            )
          }
        }
      }

      HorizontalDivider(
        modifier = Modifier.padding(vertical = 10.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
      )

      // SIDEBAR ORIGINAL FEATURES SECTION (Plugins, External Workspace, Build Automations)
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // Feature 1: Plugins
        SidebarFeatureRow(
          title = "Plugins & Tools",
          subtitle = "Upload custom specs & KaTeX",
          icon = Icons.Default.Extension,
          onClick = {
            onCloseDrawer()
            onOpenPlugins()
          }
        )

        // Feature 2: External Workspace
        SidebarFeatureRow(
          title = "External Workspace",
          subtitle = "Sync local files & directories",
          icon = Icons.Default.FolderOpen,
          onClick = {
            onCloseDrawer()
            onOpenExternalWorkspace()
          }
        )

        // Feature 3: Build Automations
        SidebarFeatureRow(
          title = "Build Automations",
          subtitle = "Triggers & agent workflows",
          icon = Icons.Default.Widgets,
          onClick = {
            onCloseDrawer()
            onOpenAutomations()
          }
        )
      }
    }
  }
}

@Composable
private fun SidebarFeatureRow(
  title: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() },
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
    shape = RoundedCornerShape(12.dp)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
        modifier = Modifier.size(34.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
private fun NavModeTab(
  title: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
  val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

  Surface(
    modifier = modifier
      .height(44.dp)
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() },
    color = bgColor,
    border = BorderStroke(
      1.dp,
      if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    ),
    shape = RoundedCornerShape(12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        modifier = Modifier.size(16.dp),
        tint = contentColor
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        ),
        color = contentColor
      )
    }
  }
}

@Composable
private fun ConversationRow(
  conversation: ConversationEntity,
  isSelected: Boolean,
  onClick: () -> Unit,
  onDelete: () -> Unit,
  onRename: (String) -> Unit,
  onTogglePin: () -> Unit
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp)
      .clip(RoundedCornerShape(10.dp))
      .background(
        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        else Color.Transparent
      )
      .clickable { onClick() }
      .padding(horizontal = 10.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    if (conversation.isPinned) {
      Icon(
        Icons.Default.PushPin,
        contentDescription = "Pinned",
        modifier = Modifier.size(14.dp),
        tint = MaterialTheme.colorScheme.primary
      )
      Spacer(modifier = Modifier.width(6.dp))
    }

    Text(
      text = conversation.title,
      style = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
      ),
      color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.weight(1f)
    )

    Box {
      IconButton(
        onClick = { menuExpanded = true },
        modifier = Modifier.size(28.dp)
      ) {
        Icon(
          Icons.Default.MoreVert,
          contentDescription = "Conversation options",
          modifier = Modifier.size(18.dp),
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false }
      ) {
        DropdownMenuItem(
          text = { Text(if (conversation.isPinned) "Unpin" else "Pin") },
          leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) },
          onClick = {
            menuExpanded = false
            onTogglePin()
          }
        )
        DropdownMenuItem(
          text = { Text("Delete") },
          leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
          onClick = {
            menuExpanded = false
            onDelete()
          }
        )
      }
    }
  }
}
