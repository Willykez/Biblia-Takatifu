package com.biblia.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * The type scale this app's screens actually use in practice, formalized here as a shared
 * reference. Most Text() calls across the app still set fontSize/fontWeight directly rather
 * than reading from MaterialTheme.typography (that predates this pass, and a blanket refactor
 * of every call site wasn't done without a real build to verify against - see below) but the
 * values below match what's already on screen, so use these names as the source of truth
 * when adding anything new instead of picking another one-off size.
 *
 *  - labelSmall   11sp bold, 1sp tracking  -> section eyebrows ("QUICK ACCESS", "ABOUT")
 *  - bodySmall    12sp regular             -> captions, row subtitles, meta text
 *  - bodyMedium   13sp regular/medium      -> secondary body copy, list descriptions
 *  - titleSmall   14sp bold                -> row titles (Settings rows, list items)
 *  - titleMedium  16sp bold                -> card/dialog titles
 *  - titleLarge   20sp bold                -> page/section headings ("All transfers")
 *  - headlineSmall 28sp extra-bold         -> hero text (onboarding)
 */
val Typography = Typography(
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.25.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp,
    ),
)
