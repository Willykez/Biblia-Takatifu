package com.biblia.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Minimal/editorial palette:
 * warm neutral paper tones, near-black ink for text,
 * and one muted liturgical red accent.
 *
 * No gradients, glass effects, or dynamic color.
 * Surfaces and hairline dividers provide the visual structure.
 */
object AppColors {

    // ─────────────────────────────────────────────
    // Light theme
    // ─────────────────────────────────────────────

    val LightBackground = Color(0xFFFBF9F5)
    val LightSurface = Color(0xFFFFFFFF)

    val LightInk = Color(0xFF1C1B19)
    val LightInkMuted = Color(0xFF6B6560)

    val LightDivider = Color(0xFFE6E2DA)

    val LightAccent = Color(0xFF7A2E2E)
    val LightAccentMuted = Color(0xFFF3E4E1)


    // ─────────────────────────────────────────────
    // Dark theme
    // ─────────────────────────────────────────────

    val DarkBackground = Color(0xFF171614)
    val DarkSurface = Color(0xFF1F1E1B)

    val DarkInk = Color(0xFFEDEAE3)
    val DarkInkMuted = Color(0xFFA39C93)

    val DarkDivider = Color(0xFF34322D)

    val DarkAccent = Color(0xFFE0A0A0)
    val DarkAccentMuted = Color(0xFF3A2626)


    // ─────────────────────────────────────────────
    // Liturgical colors
    // Used only for small dots, labels and badges.
    // These do NOT control the application theme.
    // ─────────────────────────────────────────────

    val LiturgicalViolet = Color(0xFF6A4C93)
    val LiturgicalWhite = Color(0xFFC9B896)
    val LiturgicalGreen = Color(0xFF3F6B3F)
    val LiturgicalRed = Color(0xFFA23B3B)
    val LiturgicalRose = Color(0xFFC98B9C)
}