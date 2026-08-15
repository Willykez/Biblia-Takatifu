package com.biblia.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

/**
 * Read-only access to the theme that is actually active.
 *
 * This is useful for UI components that need to branch
 * between light and dark visuals.
 */
val LocalIsDarkTheme = staticCompositionLocalOf { false }

private val LightScheme = lightColorScheme(
    primary = AppColors.LightAccent,
    onPrimary = AppColors.LightSurface,

    primaryContainer = AppColors.LightAccentMuted,
    onPrimaryContainer = AppColors.LightAccent,

    background = AppColors.LightBackground,
    onBackground = AppColors.LightInk,

    surface = AppColors.LightSurface,
    onSurface = AppColors.LightInk,

    surfaceVariant = AppColors.LightBackground,
    onSurfaceVariant = AppColors.LightInkMuted,

    outline = AppColors.LightDivider,
    outlineVariant = AppColors.LightDivider,
)

private val DarkScheme = darkColorScheme(
    primary = AppColors.DarkAccent,
    onPrimary = AppColors.DarkBackground,

    primaryContainer = AppColors.DarkAccentMuted,
    onPrimaryContainer = AppColors.DarkAccent,

    background = AppColors.DarkBackground,
    onBackground = AppColors.DarkInk,

    surface = AppColors.DarkSurface,
    onSurface = AppColors.DarkInk,

    surfaceVariant = AppColors.DarkBackground,
    onSurfaceVariant = AppColors.DarkInkMuted,

    outline = AppColors.DarkDivider,
    outlineVariant = AppColors.DarkDivider,
)

@Composable
fun BibliaTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    CompositionLocalProvider(
        LocalIsDarkTheme provides isDark
    ) {
        MaterialTheme(
            colorScheme = if (isDark) {
                DarkScheme
            } else {
                LightScheme
            },
            typography = AppTypography,
            shapes = Shapes(),
            content = content,
        )
    }
}