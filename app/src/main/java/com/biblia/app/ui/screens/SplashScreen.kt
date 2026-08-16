package com.biblia.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.app.ui.theme.ReadingFont
import kotlinx.coroutines.delay

/** Plain, still - a title fades in, holds, and hands off. No motion beyond that one fade. */
@Composable
fun SplashScreen(onFinish: () -> Unit) {
    val alpha = remember { mutableStateOf(0f) }
    val animatedAlpha by animateFloatAsState(
        targetValue = alpha.value,
        animationSpec = tween(600, easing = LinearEasing),
        label = "splash_fade",
    )

    LaunchedEffect(Unit) {
        alpha.value = 1f
        delay(1100)
        onFinish()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Text(
            "Biblia Takatifu",
            fontFamily = ReadingFont,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = animatedAlpha),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Kiswahili \u2022 English",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = animatedAlpha),
        )
    }
}
