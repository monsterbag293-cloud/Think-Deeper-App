package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ServerSyncStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopHeaderBar(
  selectedModel: String,
  syncStatus: ServerSyncStatus,
  onOpenDrawer: () -> Unit,
  onOpenModelSelector: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenAccount: () -> Unit
) {
  TopAppBar(
    modifier = Modifier.testTag("top_header_bar"),
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.background,
      titleContentColor = MaterialTheme.colorScheme.onBackground
    ),
    navigationIcon = {
      IconButton(
        onClick = onOpenDrawer,
        modifier = Modifier
          .testTag("open_drawer_btn")
          .size(48.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Menu,
          contentDescription = "Open navigation menu",
          tint = MaterialTheme.colorScheme.onBackground
        )
      }
    },
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Model Selector Button Pill
        Surface(
          modifier = Modifier
            .testTag("model_selector_btn")
            .clip(RoundedCornerShape(10.dp))
            .clickable { onOpenModelSelector() },
          color = MaterialTheme.colorScheme.surfaceVariant,
          shape = RoundedCornerShape(10.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = selectedModel,
              style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
              ),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.Default.ArrowDropDown,
              contentDescription = "Select model",
              modifier = Modifier.size(18.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Vercel Server Live Status indicator
        val (statusColor, statusText) = when (syncStatus) {
          is ServerSyncStatus.Connected -> Color(0xFF22C55E) to "Vercel Live"
          is ServerSyncStatus.Connecting -> Color(0xFFE08A4F) to "Syncing"
          is ServerSyncStatus.OfflineFallback -> Color(0xFFE08A4F) to "Local Agent"
        }

        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
          shape = RoundedCornerShape(99.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .background(statusColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = statusText,
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    },
    actions = {
      IconButton(
        onClick = onOpenAccount,
        modifier = Modifier
          .testTag("account_btn")
          .size(44.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Person,
          contentDescription = "Account Profile",
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      IconButton(
        onClick = onOpenSettings,
        modifier = Modifier
          .testTag("settings_btn")
          .size(44.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "Settings & Vercel Endpoints",
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  )
}
