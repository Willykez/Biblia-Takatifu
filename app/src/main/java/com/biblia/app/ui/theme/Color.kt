package com.biblia.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Application color palette.
 *
 * AppColors contains the underlying light/dark palette.
 * Sleek* tokens are semantic aliases used by the UI components.
 */
object AppColors {

    // -------------------------------------------------------------------------
    // Light
    // -------------------------------------------------------------------------

    val LightBackground = Color(0xFFFBF9F5)
    val LightSurface = Color(0xFFFFFFFF)
    val LightInk = Color(0xFF1C1B19)
    val LightInkMuted = Color(0xFF6B6560)
    val LightDivider = Color(0xFFE6E2DA)

    val LightAccent = Color(0xFF7A2E2E)
    val LightAccentMuted = Color(0xFFF3E4E1)

    // -------------------------------------------------------------------------
    // Dark
    // -------------------------------------------------------------------------

    val DarkBackground = Color(0xFF171614)
    val DarkSurface = Color(0xFF1F1E1B)
    val DarkInk = Color(0xFFEDEAE3)
    val DarkInkMuted = Color(0xFFA39C93)
    val DarkDivider = Color(0xFF34322D)

    val DarkAccent = Color(0xFFE0A0A0)
    val DarkAccentMuted = Color(0xFF3A2626)

    // -------------------------------------------------------------------------
    // Liturgical colors
    // -------------------------------------------------------------------------

    val LiturgicalViolet = Color(0xFF6A4C93)
    val LiturgicalWhite = Color(0xFFC9B896)
    val LiturgicalGreen = Color(0xFF3F6B3F)
    val LiturgicalRed = Color(0xFFA23B3B)
    val LiturgicalRose = Color(0xFFC98B9C)
}

/*
 * ============================================================================
 * SLEEK SEMANTIC COLORS
 * ============================================================================
 *
 * These aliases are intentionally backed by MaterialTheme.colorScheme.
 *
 * This allows SleekComponents.kt and GroupedList.kt to use the same semantic
 * names while automatically following LIGHT/DARK theme selection.
 *
 * IMPORTANT:
 * Do not make these constants using AppColors.Light... directly.
 * The UI needs to change when BibliaTheme switches between light and dark.
 */

val SleekPrimary: Color
    @Composable
    get() = androidx.compose.material3.MaterialTheme.colorScheme.primary

val SleekOnPrimary: Color
    @Composable
    get() = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary

val SleekPrimaryContainer: Color
    @Composable
    get() = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer

val SleekOnPrimaryContainer: Color
    @Composable
    get() = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer

val SleekSecondary: Color
    @Composable
    get() = androidx.compose.material3.MaterialTheme.colorScheme.secondary

val SleekOnSecondary: Color
    @Composable
    get() = androidx.compose.material3.MaterialTheme.colorScheme.onSecondary

val SleekBg: Color
    @Composable
    get() = androidx.compose.material3.MaterialTheme.colorScheme.background

val SleekSurface: Color
    @Composable
    get() = androidx.compose.material3.MaterialTheme.colorScheme.surface

val SleekSurfaceContainer: Color
    @Composable
    get() = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant

val SleekOnSurface: Color
    @Composable
    get() = androidx.compose.material3.MaterialTheme.colorScheme.onSurface

val SleekOnSurfaceVariant: Color
    @Composable
    get() = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant

val SleekOutline: Color
    @Composable
    get() = androidx.compose.material3.MaterialTheme.colorScheme.outline

val SleekOutlineVariant: Color
    @Composable
    get() = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant