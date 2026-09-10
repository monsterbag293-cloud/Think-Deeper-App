package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AnthropicSans
import com.example.ui.theme.AnthropicSerif
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Utility to convert raw LaTeX expressions to human-readable mathematical unicode notation.
 */
object MathNotationParser {

  private val greekAndSymbols = mapOf(
    "\\alpha" to "α",
    "\\beta" to "β",
    "\\gamma" to "γ",
    "\\Gamma" to "Γ",
    "\\delta" to "δ",
    "\\Delta" to "Δ",
    "\\epsilon" to "ε",
    "\\zeta" to "ζ",
    "\\eta" to "η",
    "\\theta" to "θ",
    "\\Theta" to "Θ",
    "\\lambda" to "λ",
    "\\Lambda" to "Λ",
    "\\mu" to "μ",
    "\\pi" to "π",
    "\\Pi" to "Π",
    "\\rho" to "ρ",
    "\\sigma" to "σ",
    "\\Sigma" to "Σ",
    "\\tau" to "τ",
    "\\phi" to "φ",
    "\\Phi" to "Φ",
    "\\omega" to "ω",
    "\\Omega" to "Ω",
    "\\infty" to "∞",
    "\\int" to "∫",
    "\\iint" to "∬",
    "\\iiint" to "∭",
    "\\sum" to "∑",
    "\\prod" to "∏",
    "\\partial" to "∂",
    "\\nabla" to "∇",
    "\\pm" to "±",
    "\\mp" to "∓",
    "\\times" to "×",
    "\\cdot" to "·",
    "\\div" to "÷",
    "\\neq" to "≠",
    "\\leq" to "≤",
    "\\geq" to "≥",
    "\\approx" to "≈",
    "\\equiv" to "≡",
    "\\in" to "∈",
    "\\notin" to "∉",
    "\\subset" to "⊂",
    "\\supset" to "⊃",
    "\\subseteq" to "⊆",
    "\\cap" to "∩",
    "\\cup" to "∪",
    "\\forall" to "∀",
    "\\exists" to "∃",
    "\\rightarrow" to "→",
    "\\leftarrow" to "←",
    "\\Rightarrow" to "⇒",
    "\\Leftarrow" to "⇐",
    "\\leftrightarrow" to "↔",
    "\\Leftrightarrow" to "⇔",
    "\\sqrt" to "√",
    "\\degree" to "°",
    "\\angle" to "∠"
  )

  private val superscriptMap = mapOf(
    '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
    '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
    '+' to '⁺', '-' to '⁻', '=' to '⁼', '(' to '⁽', ')' to '⁾',
    'n' to 'ⁿ', 'i' to 'ⁱ', 'x' to 'ˣ', 'y' to 'ʸ', 't' to 'ᵗ'
  )

  private val subscriptMap = mapOf(
    '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
    '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
    '+' to '₊', '-' to '₋', '=' to '₌', '(' to '₍', ')' to '₎',
    'a' to 'ₐ', 'e' to 'ₑ', 'i' to 'ᵢ', 'j' to 'ⱼ', 'o' to 'ₒ',
    'r' to 'ᵣ', 'u' to 'ᵤ', 'v' to 'ᵥ', 'x' to 'ₓ'
  )

  fun formatToPrettyMath(rawLatex: String): String {
    var text = rawLatex.trim()
    if (text.startsWith("$$") && text.endsWith("$$")) {
      text = text.removePrefix("$$").removeSuffix("$$").trim()
    } else if (text.startsWith("$") && text.endsWith("$")) {
      text = text.removePrefix("$").removeSuffix("$").trim()
    } else if (text.startsWith("\\[") && text.endsWith("\\]")) {
      text = text.removePrefix("\\[").removeSuffix("\\]").trim()
    }

    // Replace fractions: \frac{a}{b} -> (a)/(b)
    val fracRegex = Regex("""\\frac\{([^{}]+)\}\{([^{}]+)\}""")
    text = fracRegex.replace(text) { match ->
      val num = match.groupValues[1]
      val den = match.groupValues[2]
      "($num)/($den)"
    }

    // Replace square roots: \sqrt{x} -> √(x)
    val sqrtRegex = Regex("""\\sqrt\{([^{}]+)\}""")
    text = sqrtRegex.replace(text) { match ->
      "√(${match.groupValues[1]})"
    }

    // Replace superscripts: ^{abc} or ^x
    val superGroupRegex = Regex("""\^\{([^{}]+)\}""")
    text = superGroupRegex.replace(text) { match ->
      match.groupValues[1].map { superscriptMap[it] ?: it }.joinToString("")
    }
    val singleSuperRegex = Regex("""\^([0-9a-zA-Z+\-])""")
    text = singleSuperRegex.replace(text) { match ->
      val ch = match.groupValues[1].firstOrNull() ?: ' '
      superscriptMap[ch]?.toString() ?: "^$ch"
    }

    // Replace subscripts: _{abc} or _x
    val subGroupRegex = Regex("""_\{([^{}]+)\}""")
    text = subGroupRegex.replace(text) { match ->
      match.groupValues[1].map { subscriptMap[it] ?: it }.joinToString("")
    }
    val singleSubRegex = Regex("""_([0-9a-zA-Z+\-])""")
    text = singleSubRegex.replace(text) { match ->
      val ch = match.groupValues[1].firstOrNull() ?: ' '
      subscriptMap[ch]?.toString() ?: "_$ch"
    }

    // Replace common LaTeX symbols
    greekAndSymbols.forEach { (latex, sym) ->
      text = text.replace(latex, sym)
    }

    // Clean up unnecessary brackets or backslashes
    text = text.replace("\\,", " ")
      .replace("\\;", " ")
      .replace("\\quad", "   ")
      .replace("\\text{", "")
      .replace("\\mathrm{", "")
      .replace("\\mathbf{", "")
      .replace("{", "")
      .replace("}", "")

    return text
  }
}

/**
 * Visual mathematical equation card for display-style LaTeX formulas ($$...$$).
 */
@Composable
fun MathematicalEquationCard(
  rawFormula: String,
  modifier: Modifier = Modifier
) {
  val formattedFormula = remember(rawFormula) {
    MathNotationParser.formatToPrettyMath(rawFormula)
  }
  val clipboard = LocalClipboardManager.current
  val scope = rememberCoroutineScope()
  var isCopied by remember { mutableStateOf(false) }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    shape = RoundedCornerShape(16.dp),
    color = Color(0xFF1E1C18),
    border = BorderStroke(1.dp, Color(0xFF3D2A20))
  ) {
    Column(
      modifier = Modifier.padding(14.dp)
    ) {
      // Header with Equation Badge & Copy button
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = Color(0xFFD97757).copy(alpha = 0.18f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Functions,
                contentDescription = "Math Formula",
                tint = Color(0xFFD97757),
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "EQUATION",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.8.sp,
                  fontSize = 10.sp
                ),
                color = Color(0xFFD97757)
              )
            }
          }
        }

        IconButton(
          onClick = {
            clipboard.setText(AnnotatedString(rawFormula.trim()))
            isCopied = true
            scope.launch {
              delay(2000)
              isCopied = false
            }
          },
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
            contentDescription = if (isCopied) "Copied" else "Copy LaTeX formula",
            tint = if (isCopied) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Equation Expression Rendered with horizontal scrolling if wide
      val scrollState = rememberScrollState()
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(scrollState)
          .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        Text(
          text = formattedFormula,
          style = MaterialTheme.typography.headlineSmall.copy(
            fontFamily = AnthropicSerif,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.5.sp
          ),
          color = Color(0xFFFFDBCF)
        )
      }
    }
  }
}
