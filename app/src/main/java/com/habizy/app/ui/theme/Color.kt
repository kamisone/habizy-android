package com.habizy.app.ui.theme

import androidx.compose.ui.graphics.Color

// Greens
val GreenPrimary = Color(0xFF17A877)
val GreenDark = Color(0xFF0E7A52)
val GreenLight = Color(0xFF1BB082)
val GreenBadgeBg = Color(0xFFE4F4EC)

// Text
val DarkText = Color(0xFF20312A)
val BodyText = Color(0xFF2C3A32)
val SubtitleText = Color(0xFF8A8275)
val LightText = Color(0xFFA89F90)
val MutedText = Color(0xFFB4ABA0)
val InactiveTab = Color(0xFFC2B9AA)

// Backgrounds
val ScreenBackground = Color(0xFFFBF7F0)
val CardBackground = Color.White
val LightCardBg = Color(0xFFF1ECE2)
val DividerColor = Color(0xFFF4EFE6)
val BorderColor = Color(0xFFECE5D8)
val NavBorder = Color(0xFFEFE9DF)

// Accents
val CoralRed = Color(0xFFFF6B5E)
val DarkRed = Color(0xFFE84F42)
val Blue = Color(0xFF3B82F6)
val DarkBlue = Color(0xFF2563EB)
val Orange = Color(0xFFFFB020)
val Yellow = Color(0xFFFFC24B)
val Purple = Color(0xFF7C6BFF)

// Badge / Warning
val WarningBg = Color(0xFFFFF3E0)
val WarningText = Color(0xFFE65100)
val SuccessBg = Color(0xFFE8F5E9)
val SuccessText = Color(0xFF2E7D32)
val ErrorBg = Color(0xFFFFEBEE)
val ErrorText = Color(0xFFC62828)
val InfoBg = Color(0xFFE3F2FD)
val InfoText = Color(0xFF1565C0)

// Preset member colors
val PresetColors = listOf(
    Color(0xFFFF6B5E),
    Color(0xFF3B82F6),
    Color(0xFFFFB020),
    Color(0xFF17A877),
    Color(0xFF7C6BFF),
)

val PresetHexColors = listOf(
    "#FF6B5E", "#3B82F6", "#FFB020", "#17A877", "#7C6BFF"
)

/**
 * Parse a hex color string (e.g. "#FF6B5E") into a [Color].
 */
fun String.toComposeColor(): Color {
    val hex = this.trimStart('#')
    return Color(android.graphics.Color.parseColor("#$hex"))
}

/**
 * Map a report/signalement tag name to its accent color.
 */
fun tagColor(tag: String): Color = when (tag) {
    "Urgent" -> CoralRed
    "Important" -> Orange
    "Information" -> Blue
    "Rappel" -> Purple
    "Suggestion" -> GreenPrimary
    "Besoin d'aide" -> Color(0xFFEC4899)
    else -> SubtitleText
}
