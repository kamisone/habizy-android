package com.habizy.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CagnotteColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = CardBackground,
    primaryContainer = GreenBadgeBg,
    onPrimaryContainer = GreenDark,
    secondary = GreenLight,
    onSecondary = CardBackground,
    secondaryContainer = GreenBadgeBg,
    onSecondaryContainer = GreenDark,
    tertiary = Blue,
    onTertiary = CardBackground,
    background = ScreenBackground,
    onBackground = DarkText,
    surface = CardBackground,
    onSurface = DarkText,
    surfaceVariant = LightCardBg,
    onSurfaceVariant = BodyText,
    outline = BorderColor,
    outlineVariant = DividerColor,
    error = CoralRed,
    onError = CardBackground,
    errorContainer = ErrorBg,
    onErrorContainer = DarkRed,
)

@Composable
fun CagnotteTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CagnotteColorScheme,
        typography = CagnotteTypography,
        content = content,
    )
}
