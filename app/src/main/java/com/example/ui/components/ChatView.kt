package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ActivityStep
import com.example.data.local.JsonHelper
import com.example.data.local.MessageEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatView(
  messages: List<MessageEntity>,
  isGenerating: Boolean,
  liveSteps: List<ActivityStep>,
  onSelectSuggestion: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val listState = rememberLazyListState()

  LaunchedEffect(messages.size, isGenerating, liveSteps.size) {
    if (messages.isNotEmpty() || liveSteps.isNotEmpty()) {
      val targetIndex = (messages.size + if (isGenerating) 1 else 0) - 1
      if (targetIndex >= 0) {
        listState.animateScrollToItem(targetIndex)
      }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .testTag("chat_view_container")
  ) {
    if (messages.isEmpty() && !isGenerating) {
      EmptyStateHero(onSelectSuggestion = onSelectSuggestion)
    } else {
      LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        items(messages, key = { it.id }) { msg ->
          MessageItem(message = msg)
        }

        if (isGenerating) {
          item(key = "generating_card") {
            LiveThinkingCard(liveSteps = liveSteps)
          }
        }
      }
    }
  }
}

@Composable
fun EmptyStateHero(onSelectSuggestion: (String) -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    // Brand Orb
    Surface(
      shape = CircleShape,
      color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
      border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
      modifier = Modifier.size(72.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text(
          text = "🧠",
          fontSize = 34.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    Text(
      text = "Think Deeper",
      style = MaterialTheme.typography.displaySmall.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
      ),
      color = MaterialTheme.colorScheme.onBackground
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "Agentic reasoning, verified step chains, and tool execution.",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(horizontal = 16.dp)
    )

    Spacer(modifier = Modifier.height(28.dp))

    // Mobile prompt suggestions
    val suggestions = listOf(
      "Correlation vs causation" to "Explain the difference between correlation and causation with a concrete example.",
      "Write Python function" to "Write a Python function for longest increasing subsequence with unit tests, then verify it.",
      "Artifact spec design" to "Create a clean architecture spec for an event-driven notification service.",
      "Reasoning puzzle" to "A bat and a ball cost $1.10. The bat costs $1 more than the ball. How much is the ball?"
    )

    suggestions.chunked(2).forEach { pair ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        pair.forEach { (title, prompt) ->
          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(14.dp))
              .clickable { onSelectSuggestion(prompt) },
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(14.dp)
          ) {
            Column(
              modifier = Modifier.padding(12.dp)
            ) {
              Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = prompt,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
              )
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(10.dp))
    }
  }
}

@Composable
fun MessageItem(message: MessageEntity) {
  val isUser = message.role == "user"
  val timeFormatted = remember(message.timestamp) {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    sdf.format(Date(message.timestamp))
  }

  val activities = remember(message.activitiesJson) {
    JsonHelper.deserializeActivities(message.activitiesJson)
  }

  val clipboardManager = LocalClipboardManager.current
  val context = LocalContext.current

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
  ) {
    // Role Label & Time
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)
    ) {
      Text(
        text = if (isUser) "You" else "Think Deeper",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = timeFormatted,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
      )

      if (!isUser && message.thinkMs > 0) {
        Spacer(modifier = Modifier.width(6.dp))
        val seconds = String.format(Locale.US, "%.1fs", message.thinkMs / 1000.0)
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ) {
          Text(
            text = "⚡ $seconds",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
          )
        }
      }
    }

    // Message Bubble
    Surface(
      shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isUser) 16.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 16.dp
      ),
      color = if (isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
      else MaterialTheme.colorScheme.surface,
      border = BorderStroke(
        1.dp,
        if (isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
      ),
      modifier = Modifier.fillMaxWidth(if (isUser) 0.85f else 1.0f)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        if (!isUser && activities.isNotEmpty()) {
          ActivitiesAccordion(activities = activities)
          Spacer(modifier = Modifier.height(10.dp))
        }

        MarkdownText(
          content = message.displayContent ?: message.content,
          color = MaterialTheme.colorScheme.onSurface
        )

        // Bottom action bar for assistant message (Copy content)
        if (!isUser) {
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable {
                  clipboardManager.setText(AnnotatedString(message.content))
                  Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                },
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              shape = RoundedCornerShape(6.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.ContentCopy,
                  contentDescription = "Copy message",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Copy",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun ActivitiesAccordion(activities: List<ActivityStep>) {
  var expanded by remember { mutableStateOf(false) }

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .clickable { expanded = !expanded },
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    shape = RoundedCornerShape(8.dp)
  ) {
    Column(modifier = Modifier.padding(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Thought process (${activities.size} steps)",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
          )
        }

        Icon(
          imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
          contentDescription = if (expanded) "Collapse" else "Expand",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(18.dp)
        )
      }

      AnimatedVisibility(visible = expanded) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          activities.forEach { step ->
            ActivityStepRow(step = step)
          }
        }
      }
    }
  }
}

@Composable
fun ActivityStepRow(step: ActivityStep) {
  Row(
    verticalAlignment = Alignment.Top,
    modifier = Modifier.fillMaxWidth()
  ) {
    val (color, icon) = when (step.status) {
      "done" -> MaterialTheme.colorScheme.primary to Icons.Default.Check
      "running" -> MaterialTheme.colorScheme.primary to null
      "error" -> MaterialTheme.colorScheme.error to Icons.Default.Warning
      else -> MaterialTheme.colorScheme.onSurfaceVariant to Icons.Default.Check
    }

    Box(
      modifier = Modifier
        .size(18.dp)
        .padding(top = 2.dp),
      contentAlignment = Alignment.Center
    ) {
      if (step.status == "running") {
        CircularProgressIndicator(
          strokeWidth = 2.dp,
          color = color,
          modifier = Modifier.size(14.dp)
        )
      } else if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = step.status,
          tint = color,
          modifier = Modifier.size(14.dp)
        )
      }
    }

    Spacer(modifier = Modifier.width(8.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = step.name,
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface
      )
      if (step.detail.isNotBlank()) {
        Text(
          text = step.detail,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
fun LiveThinkingCard(liveSteps: List<ActivityStep>) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("live_thinking_card")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        CircularProgressIndicator(
          modifier = Modifier.size(16.dp),
          strokeWidth = 2.dp,
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Thinking deeply…",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold
          ),
          color = MaterialTheme.colorScheme.primary
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        liveSteps.forEach { step ->
          ActivityStepRow(step = step)
        }
      }
    }
  }
}

@Composable
fun MarkdownText(content: String, color: Color) {
  // Parses markdown headers, code blocks, bold, lists
  val blocks = remember(content) { parseMarkdown(content) }

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    blocks.forEach { block ->
      when (block) {
        is MdBlock.CodeBlock -> {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              if (block.language.isNotBlank()) {
                Text(
                  text = block.language,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  ),
                  color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
              }
              Text(
                text = block.code,
                style = MaterialTheme.typography.bodySmall.copy(
                  fontFamily = FontFamily.Monospace,
                  fontSize = 12.sp,
                  lineHeight = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
        is MdBlock.Heading -> {
          Text(
            text = block.text,
            style = when (block.level) {
              1 -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
              2 -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              else -> MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            },
            color = MaterialTheme.colorScheme.primary
          )
        }
        is MdBlock.Paragraph -> {
          Text(
            text = block.text,
            style = MaterialTheme.typography.bodyMedium.copy(
              lineHeight = 21.sp,
              fontSize = 14.sp
            ),
            color = color
          )
        }
      }
    }
  }
}

sealed class MdBlock {
  data class Paragraph(val text: String) : MdBlock()
  data class Heading(val level: Int, val text: String) : MdBlock()
  data class CodeBlock(val language: String, val code: String) : MdBlock()
}

fun parseMarkdown(text: String): List<MdBlock> {
  val lines = text.lines()
  val blocks = mutableListOf<MdBlock>()
  var inCode = false
  var codeLang = ""
  val codeBuilder = StringBuilder()
  val paragraphBuilder = StringBuilder()

  fun flushParagraph() {
    val p = paragraphBuilder.toString().trim()
    if (p.isNotEmpty()) {
      blocks.add(MdBlock.Paragraph(p))
      paragraphBuilder.clear()
    }
  }

  for (line in lines) {
    if (line.startsWith("```")) {
      if (inCode) {
        blocks.add(MdBlock.CodeBlock(codeLang, codeBuilder.toString().trimEnd()))
        codeBuilder.clear()
        inCode = false
      } else {
        flushParagraph()
        inCode = true
        codeLang = line.removePrefix("```").trim()
      }
    } else if (inCode) {
      codeBuilder.append(line).append("\n")
    } else if (line.startsWith("# ")) {
      flushParagraph()
      blocks.add(MdBlock.Heading(1, line.removePrefix("# ").trim()))
    } else if (line.startsWith("## ")) {
      flushParagraph()
      blocks.add(MdBlock.Heading(2, line.removePrefix("## ").trim()))
    } else if (line.startsWith("### ")) {
      flushParagraph()
      blocks.add(MdBlock.Heading(3, line.removePrefix("### ").trim()))
    } else if (line.isBlank()) {
      flushParagraph()
    } else {
      if (paragraphBuilder.isNotEmpty()) paragraphBuilder.append("\n")
      paragraphBuilder.append(line)
    }
  }

  if (inCode) {
    blocks.add(MdBlock.CodeBlock(codeLang, codeBuilder.toString().trimEnd()))
  } else {
    flushParagraph()
  }

  return if (blocks.isEmpty()) listOf(MdBlock.Paragraph(text)) else blocks
}
