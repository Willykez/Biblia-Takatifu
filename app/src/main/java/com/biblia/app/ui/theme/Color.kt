package com.biblia.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Minimal/editorial palette: warm neutral paper tones, near-black ink for text, one accent
 * color used sparingly. No gradients, no glass, no dynamic color - flat surfaces and hairline
 * dividers do the work instead. Sepia is a third, reading-focused scheme (warm paper/brown
 * ink) alongside Light/Dark - see ThemeMode.
 */
object AppColors {
    // Light
    val LightBackground = Color(0xFFFBF9F5)
    val LightSurface = Color(0xFFFFFFFF)
    val LightInk = Color(0xFF1C1B19)
    val LightInkMuted = Color(0xFF6B6560)
    val LightDivider = Color(0xFFE6E2DA)
    val LightAccent = Color(0xFF7A2E2E)
    val LightAccentMuted = Color(0xFFF3E4E1)

    // Dark
    val DarkBackground = Color(0xFF171614)
    val DarkSurface = Color(0xFF1F1E1B)
    val DarkInk = Color(0xFFEDEAE3)
    val DarkInkMuted = Color(0xFFA39C93)
    val DarkDivider = Color(0xFF34322D)
    val DarkAccent = Color(0xFFE0A0A0)
    val DarkAccentMuted = Color(0xFF3A2626)

    // Sepia - warm "old paper" reading theme: creamier background, brown ink, brown accent.
    val SepiaBackground = Color(0xFFF2E8D5)
    val SepiaSurface = Color(0xFFF8F0E1)
    val SepiaInk = Color(0xFF3B2F20)
    val SepiaInkMuted = Color(0xFF7A6A54)
    val SepiaDivider = Color(0xFFDDCCA8)
    val SepiaAccent = Color(0xFF8B4B2B)
    val SepiaAccentMuted = Color(0xFFE8D3B0)
}
