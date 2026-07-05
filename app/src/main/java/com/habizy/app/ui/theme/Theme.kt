package com.habizy.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val HabizyColorScheme = lightColorScheme(
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
fun HabizyTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = HabizyColorScheme,
        typography = HabizyTypography,
        content = content,
    )
}
