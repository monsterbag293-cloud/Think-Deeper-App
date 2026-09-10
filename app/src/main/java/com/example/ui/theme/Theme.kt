package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun getPalettePrimary(palette: String, isDark: Boolean): Color {
  return when (palette.lowercase()) {
    "red" -> if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
    "pink" -> if (isDark) Color(0xFFF472B6) else Color(0xFFDB2777)
    "green" -> if (isDark) Color(0xFF4ADE80) else Color(0xFF16A34A)
    "orange" -> if (isDark) Color(0xFFFB923C) else Color(0xFFEA580C)
    "yellow" -> if (isDark) Color(0xFFFACC15) else Color(0xFFCA8A04)
    "blue" -> if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB)
    "violet" -> if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED)
    else -> if (isDark) TerracottaPrimaryDark else TerracottaPrimary
  }
}

fun createCustomColorScheme(palette: String, isDark: Boolean): ColorScheme {
  val prim = getPalettePrimary(palette, isDark)
  return if (isDark) {
    darkColorScheme(
      primary = prim,
      onPrimary = WarmBgDark,
      primaryContainer = prim.copy(alpha = 0.2f),
      onPrimaryContainer = Color(0xFFFFF0EC),
      secondary = prim,
      onSecondary = WarmBgDark,
      secondaryContainer = prim.copy(alpha = 0.15f),
      onSecondaryContainer = TextPrimaryDark,
      tertiary = prim,
      onTertiary = WarmBgDark,
      background = WarmBgDark,
      onBackground = TextPrimaryDark,
      surface = WarmSurfaceDark,
      onSurface = TextPrimaryDark,
      surfaceVariant = WarmSurfaceVariantDark,
      onSurfaceVariant = TextSecondaryDark,
      outline = WarmBorderDark,
      outlineVariant = WarmBorderStrongDark,
      error = ErrorRed,
      onError = WarmBgDark
    )
  } else {
    lightColorScheme(
      primary = prim,
      onPrimary = WarmSurfaceLight,
      primaryContainer = prim.copy(alpha = 0.12f),
      onPrimaryContainer = Color(0xFF1A1915),
      secondary = prim,
      onSecondary = WarmSurfaceLight,
      secondaryContainer = prim.copy(alpha = 0.08f),
      onSecondaryContainer = TextPrimaryLight,
      tertiary = prim,
      onTertiary = WarmSurfaceLight,
      background = WarmBgLight,
      onBackground = TextPrimaryLight,
      surface = WarmSurfaceLight,
      onSurface = TextPrimaryLight,
      surfaceVariant = WarmSurfaceVariantLight,
      onSurfaceVariant = TextSecondaryLight,
      outline = WarmBorderLight,
      outlineVariant = WarmBorderStrongLight,
      error = ErrorRed,
      onError = WarmSurfaceLight
    )
  }
}

@Composable
fun ThinkDeeperTheme(
  themeMode: String = "dark",
  colorPalette: String = "default",
  fontTheme: String = "default",
  content: @Composable () -> Unit
) {
  val isDark = when (themeMode.lowercase()) {
    "light" -> false
    "dark" -> true
    else -> isSystemInDarkTheme()
  }

  val colorScheme = createCustomColorScheme(colorPalette, isDark)
  val typography = createTypographyForTheme(fontTheme)

  MaterialTheme(
    colorScheme = colorScheme,
    typography = typography,
    content = content
  )
}

// Backward compatibility
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  ThinkDeeperTheme(
    themeMode = if (darkTheme) "dark" else "light",
    content = content
  )
}
