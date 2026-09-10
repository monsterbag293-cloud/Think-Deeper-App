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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AnthropicSans
import com.example.ui.theme.AnthropicSerif
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed interface RichMarkdownBlock {
  data class Paragraph(val text: String) : RichMarkdownBlock
  data class Heading(val level: Int, val text: String) : RichMarkdownBlock
  data class BulletItem(val text: String) : RichMarkdownBlock
  data class NumberedItem(val number: String, val text: String) : RichMarkdownBlock
  data class BlockQuote(val text: String) : RichMarkdownBlock
  data class MathBlock(val formula: String) : RichMarkdownBlock
  data class CodeBlock(val language: String, val code: String) : RichMarkdownBlock
  data class FileChip(val filePath: String) : RichMarkdownBlock
}

@Composable
fun RichMarkdownViewer(
  content: String,
  onCopyCode: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val blocks = remember(content) { parseRichMarkdown(content) }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    blocks.forEach { block ->
      when (block) {
        is RichMarkdownBlock.Heading -> {
          val style = when (block.level) {
            1 -> MaterialTheme.typography.headlineMedium.copy(
              fontFamily = AnthropicSerif,
              fontWeight = FontWeight.Bold,
              fontSize = 22.sp
            )
            2 -> MaterialTheme.typography.titleLarge.copy(
              fontFamily = AnthropicSerif,
              fontWeight = FontWeight.Bold,
              fontSize = 19.sp
            )
            else -> MaterialTheme.typography.titleMedium.copy(
              fontFamily = AnthropicSerif,
              fontWeight = FontWeight.SemiBold,
              fontSize = 17.sp
            )
          }
          Text(
            text = renderAnnotatedRichText(block.text),
            style = style,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
          )
        }

        is RichMarkdownBlock.Paragraph -> {
          Text(
            text = renderAnnotatedRichText(block.text),
            style = MaterialTheme.typography.bodyLarge.copy(
              fontFamily = AnthropicSans,
              fontSize = 15.sp,
              lineHeight = 23.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        is RichMarkdownBlock.BulletItem -> {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.Top
          ) {
            Box(
              modifier = Modifier
                .padding(top = 8.dp, end = 10.dp)
                .size(6.dp)
                .background(Color(0xFFD97757), CircleShape)
            )
            Text(
              text = renderAnnotatedRichText(block.text),
              style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = AnthropicSans,
                fontSize = 15.sp,
                lineHeight = 22.sp
              ),
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.weight(1f)
            )
          }
        }

        is RichMarkdownBlock.NumberedItem -> {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.Top
          ) {
            Text(
              text = "${block.number}.",
              style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = AnthropicSans,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD97757),
                fontSize = 14.sp
              ),
              modifier = Modifier
                .width(24.dp)
                .padding(top = 1.dp)
            )
            Text(
              text = renderAnnotatedRichText(block.text),
              style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = AnthropicSans,
                fontSize = 15.sp,
                lineHeight = 22.sp
              ),
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.weight(1f)
            )
          }
        }

        is RichMarkdownBlock.BlockQuote -> {
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            color = Color(0xFF1E1C18),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF3E3A33))
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.Top
            ) {
              Box(
                modifier = Modifier
                  .width(3.dp)
                  .height(36.dp)
                  .background(Color(0xFFD97757), RoundedCornerShape(2.dp))
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = renderAnnotatedRichText(block.text),
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontFamily = AnthropicSans,
                  fontStyle = FontStyle.Italic,
                  lineHeight = 21.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        is RichMarkdownBlock.MathBlock -> {
          MathematicalEquationCard(rawFormula = block.formula)
        }

        is RichMarkdownBlock.CodeBlock -> {
          RichCodeBlockCard(
            language = block.language,
            code = block.code,
            onCopy = { onCopyCode(block.code) }
          )
        }

        is RichMarkdownBlock.FileChip -> {
          FileChipView(filePath = block.filePath)
        }
      }
    }
  }
}

@Composable
fun RichCodeBlockCard(
  language: String,
  code: String,
  onCopy: () -> Unit
) {
  var isCopied by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp)),
    color = Color(0xFF141310),
    shape = RoundedCornerShape(14.dp),
    border = BorderStroke(1.dp, Color(0xFF2E2A24))
  ) {
    Column {
      // Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFF1E1C18))
          .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          color = Color(0xFFD97757).copy(alpha = 0.15f),
          shape = RoundedCornerShape(6.dp)
        ) {
          Text(
            text = language.ifBlank { "code" },
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
            color = Color(0xFFD97757),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable {
              onCopy()
              isCopied = true
              scope.launch {
                delay(2000)
                isCopied = false
              }
            }
            .padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
            contentDescription = "Copy code",
            tint = if (isCopied) Color(0xFF22C55E) else Color(0xFFA8A49C),
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isCopied) "Copied" else "Copy",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = if (isCopied) Color(0xFF22C55E) else Color(0xFFA8A49C)
          )
        }
      }

      // Code text
      Text(
        text = code,
        style = MaterialTheme.typography.bodySmall.copy(
          fontFamily = FontFamily.Monospace,
          fontSize = 12.sp,
          lineHeight = 18.sp
        ),
        color = Color(0xFFECE5D8),
        modifier = Modifier.padding(14.dp)
      )
    }
  }
}

@Composable
fun FileChipView(filePath: String) {
  Surface(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp)),
    color = MaterialTheme.colorScheme.surfaceVariant,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    shape = RoundedCornerShape(8.dp)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Description,
        contentDescription = "File",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(14.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = filePath,
        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

/**
 * Parses inline formatting:
 * **bold**, *italic*, `inline_code`, and inline math $math$
 */
@Composable
fun renderAnnotatedRichText(text: String): AnnotatedString {
  val primaryColor = MaterialTheme.colorScheme.primary
  val onSurface = MaterialTheme.colorScheme.onSurface
  val codeBg = Color(0xFF282520)
  val mathColor = Color(0xFFFFB59D)

  return remember(text, primaryColor, onSurface) {
    buildAnnotatedString {
      var i = 0
      val len = text.length

      while (i < len) {
        // Check for inline math: $formula$
        if (text[i] == '$' && (i == 0 || text[i - 1] != '\\')) {
          val nextDollar = text.indexOf('$', i + 1)
          if (nextDollar != -1 && nextDollar > i + 1) {
            val rawMath = text.substring(i + 1, nextDollar)
            val prettyMath = MathNotationParser.formatToPrettyMath(rawMath)
            withStyle(
              SpanStyle(
                fontFamily = AnthropicSerif,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                color = mathColor
              )
            ) {
              append(prettyMath)
            }
            i = nextDollar + 1
            continue
          }
        }

        // Check for bold: **text** or __text__
        if (i + 1 < len && ((text[i] == '*' && text[i + 1] == '*') || (text[i] == '_' && text[i + 1] == '_'))) {
          val delim = text.substring(i, i + 2)
          val end = text.indexOf(delim, i + 2)
          if (end != -1) {
            val boldText = text.substring(i + 2, end)
            withStyle(
              SpanStyle(
                fontWeight = FontWeight.Bold,
                color = onSurface
              )
            ) {
              append(boldText)
            }
            i = end + 2
            continue
          }
        }

        // Check for inline code: `code`
        if (text[i] == '`') {
          val end = text.indexOf('`', i + 1)
          if (end != -1) {
            val codeContent = text.substring(i + 1, end)
            withStyle(
              SpanStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                background = codeBg,
                color = primaryColor
              )
            ) {
              append(" $codeContent ")
            }
            i = end + 1
            continue
          }
        }

        // Check for italic: *text* or _text_
        if ((text[i] == '*' || text[i] == '_') && (i == 0 || text[i - 1] != '\\')) {
          val delim = text[i]
          val end = text.indexOf(delim, i + 1)
          if (end != -1 && end > i + 1) {
            val italicText = text.substring(i + 1, end)
            withStyle(
              SpanStyle(
                fontStyle = FontStyle.Italic,
                color = onSurface
              )
            ) {
              append(italicText)
            }
            i = end + 1
            continue
          }
        }

        // Regular character
        append(text[i])
        i++
      }
    }
  }
}

/**
 * Parses markdown into structured blocks (Headers, Lists, Quotes, Math, Code, Paragraphs)
 */
fun parseRichMarkdown(text: String): List<RichMarkdownBlock> {
  val blocks = mutableListOf<RichMarkdownBlock>()
  val lines = text.lines()

  var inCodeBlock = false
  var codeLang = ""
  val codeBuilder = StringBuilder()

  var inMathBlock = false
  val mathBuilder = StringBuilder()

  var i = 0
  while (i < lines.size) {
    val line = lines[i]
    val trimmed = line.trim()

    // Code block check: ```
    if (trimmed.startsWith("```")) {
      if (inCodeBlock) {
        blocks.add(RichMarkdownBlock.CodeBlock(codeLang, codeBuilder.toString().trimEnd()))
        codeBuilder.clear()
        inCodeBlock = false
      } else {
        codeLang = trimmed.removePrefix("```").trim()
        inCodeBlock = true
      }
      i++
      continue
    }

    if (inCodeBlock) {
      codeBuilder.append(line).append("\n")
      i++
      continue
    }

    // Math block check: $$
    if (trimmed.startsWith("$$") && trimmed.endsWith("$$") && trimmed.length > 4) {
      val formula = trimmed.removePrefix("$$").removeSuffix("$$").trim()
      blocks.add(RichMarkdownBlock.MathBlock(formula))
      i++
      continue
    }

    if (trimmed == "$$") {
      if (inMathBlock) {
        blocks.add(RichMarkdownBlock.MathBlock(mathBuilder.toString().trim()))
        mathBuilder.clear()
        inMathBlock = false
      } else {
        inMathBlock = true
      }
      i++
      continue
    }

    if (inMathBlock) {
      mathBuilder.append(line).append("\n")
      i++
      continue
    }

    // File chips: [[file:path]]
    if (trimmed.startsWith("[[file:") && trimmed.endsWith("]]")) {
      val path = trimmed.removePrefix("[[file:").removeSuffix("]]")
      blocks.add(RichMarkdownBlock.FileChip(path))
      i++
      continue
    }

    // Headings
    if (trimmed.startsWith("### ")) {
      blocks.add(RichMarkdownBlock.Heading(3, trimmed.removePrefix("### ")))
      i++
      continue
    }
    if (trimmed.startsWith("## ")) {
      blocks.add(RichMarkdownBlock.Heading(2, trimmed.removePrefix("## ")))
      i++
      continue
    }
    if (trimmed.startsWith("# ")) {
      blocks.add(RichMarkdownBlock.Heading(1, trimmed.removePrefix("# ")))
      i++
      continue
    }

    // Blockquote
    if (trimmed.startsWith("> ")) {
      blocks.add(RichMarkdownBlock.BlockQuote(trimmed.removePrefix("> ")))
      i++
      continue
    }

    // Bullet list: *, -, •
    if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ")) {
      val itemText = trimmed.substring(2)
      blocks.add(RichMarkdownBlock.BulletItem(itemText))
      i++
      continue
    }

    // Numbered list: "1. ", "2. "
    val numMatch = Regex("""^(\d+)\.\s+(.*)""").find(trimmed)
    if (numMatch != null) {
      val num = numMatch.groupValues[1]
      val itemText = numMatch.groupValues[2]
      blocks.add(RichMarkdownBlock.NumberedItem(num, itemText))
      i++
      continue
    }

    // Paragraph (skip empty lines or combine)
    if (trimmed.isNotEmpty()) {
      blocks.add(RichMarkdownBlock.Paragraph(trimmed))
    }

    i++
  }

  if (inCodeBlock) {
    blocks.add(RichMarkdownBlock.CodeBlock(codeLang, codeBuilder.toString().trimEnd()))
  }
  if (inMathBlock) {
    blocks.add(RichMarkdownBlock.MathBlock(mathBuilder.toString().trim()))
  }

  return blocks
}
