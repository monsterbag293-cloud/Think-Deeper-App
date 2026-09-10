package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// Anthropic Serif for titles, headlines, and prominent display text
val AnthropicSerif = FontFamily(
  Font(R.font.anthropic_serif_regular, FontWeight.Normal),
  Font(R.font.anthropic_serif_bold, FontWeight.Bold),
  Font(R.font.anthropic_serif_bold, FontWeight.SemiBold)
)

// Anthropic Sans for body, chats, labels, and UI controls
val AnthropicSans = FontFamily(
  Font(R.font.anthropic_sans_regular, FontWeight.Normal),
  Font(R.font.anthropic_sans_medium, FontWeight.Medium),
  Font(R.font.anthropic_sans_bold, FontWeight.Bold),
  Font(R.font.anthropic_sans_bold, FontWeight.SemiBold)
)

fun createTypographyForTheme(fontTheme: String): Typography {
  val (titleFont, bodyFont) = when (fontTheme) {
    "serif" -> FontFamily.Serif to FontFamily.Serif
    "mono" -> FontFamily.Monospace to FontFamily.Monospace
    "round", "sans" -> FontFamily.SansSerif to FontFamily.SansSerif
    else -> AnthropicSerif to AnthropicSans
  }

  return Typography(
    displayLarge = TextStyle(
      fontFamily = titleFont,
      fontWeight = FontWeight.Bold,
      fontSize = 32.sp,
      lineHeight = 38.sp,
      letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
      fontFamily = titleFont,
      fontWeight = FontWeight.Bold,
      fontSize = 28.sp,
      lineHeight = 34.sp,
      letterSpacing = (-0.3).sp
    ),
    headlineLarge = TextStyle(
      fontFamily = titleFont,
      fontWeight = FontWeight.Bold,
      fontSize = 24.sp,
      lineHeight = 30.sp,
      letterSpacing = (-0.2).sp
    ),
    headlineMedium = TextStyle(
      fontFamily = titleFont,
      fontWeight = FontWeight.SemiBold,
      fontSize = 22.sp,
      lineHeight = 28.sp,
      letterSpacing = (-0.2).sp
    ),
    headlineSmall = TextStyle(
      fontFamily = titleFont,
      fontWeight = FontWeight.SemiBold,
      fontSize = 18.sp,
      lineHeight = 24.sp,
      letterSpacing = (-0.1).sp
    ),
    titleLarge = TextStyle(
      fontFamily = titleFont,
      fontWeight = FontWeight.Bold,
      fontSize = 20.sp,
      lineHeight = 26.sp,
      letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
      fontFamily = bodyFont,
      fontWeight = FontWeight.SemiBold,
      fontSize = 16.sp,
      lineHeight = 22.sp,
      letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
      fontFamily = bodyFont,
      fontWeight = FontWeight.Medium,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
      fontFamily = bodyFont,
      fontWeight = FontWeight.Normal,
      fontSize = 15.sp,
      lineHeight = 23.sp,
      letterSpacing = 0.25.sp
    ),
    bodyMedium = TextStyle(
      fontFamily = bodyFont,
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
      fontFamily = bodyFont,
      fontWeight = FontWeight.Normal,
      fontSize = 12.sp,
      lineHeight = 17.sp,
      letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
      fontFamily = bodyFont,
      fontWeight = FontWeight.Medium,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
      fontFamily = bodyFont,
      fontWeight = FontWeight.Medium,
      fontSize = 12.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
      fontFamily = bodyFont,
      fontWeight = FontWeight.Medium,
      fontSize = 11.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.5.sp
    )
  )
}

val ThinkDeeperTypography = createTypographyForTheme("default")
