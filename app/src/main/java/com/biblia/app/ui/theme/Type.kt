package com.biblia.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Two families only: serif for anything the person is actually reading (verses, readings),
 * plain system sans for everything else (labels, nav, buttons). No custom weights beyond
 * Normal/Medium/Bold - editorial typography leans on size and spacing, not a type ramp.
 */
val ReadingFont = FontFamily.Serif
val UiFont = FontFamily.Default

val AppTypography = Typography(
    titleLarge = TextStyle(fontFamily = UiFont, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    titleMedium = TextStyle(fontFamily = UiFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = UiFont, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = ReadingFont, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 27.sp),
    bodyMedium = TextStyle(fontFamily = UiFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = UiFont, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontFamily = UiFont, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = UiFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 1.sp),
    labelSmall = TextStyle(fontFamily = UiFont, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 14.sp),
)
