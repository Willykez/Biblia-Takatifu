package com.biblia.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.app.data.liturgical.DisplayVerse
import com.biblia.app.data.liturgical.ReadingLabel
import com.biblia.app.data.liturgical.RenderedDay
import com.biblia.app.data.liturgical.RenderedReading
import com.biblia.app.ui.LiturgicalViewModel
import com.biblia.app.ui.theme.AppColors
import com.biblia.app.ui.theme.ReadingFont
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun ReadingLabel.swahiliName(): String = when (this) {
    ReadingLabel.FIRST_READING -> "Somo la Kwanza"
    ReadingLabel.PSALM -> "Zaburi ya Kuitikia"
    ReadingLabel.SECOND_READING -> "Somo la Pili"
    ReadingLabel.GOSPEL -> "Injili"
}

private fun colorForRangi(rangi: String?): androidx.compose.ui.graphics.Color = when {
    rangi == null -> AppColors.LiturgicalGreen
    rangi.contains("zambarau") || rangi.contains("purple") -> AppColors.LiturgicalViolet
    rangi.contains("nyeupe") || rangi.contains("dhahabu") || rangi.contains("white") -> AppColors.LiturgicalWhite
    rangi.contains("nyekundu") || rangi.contains("red") -> AppColors.LiturgicalRed
    rangi.contains("waridi") || rangi.contains("rose") -> AppColors.LiturgicalRose
    else -> AppColors.LiturgicalGreen
}

@Composable
fun ReadingsScreen(
    viewModel: LiturgicalViewModel,
    date: LocalDate,
    onBack: () -> Unit,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onOpenCalendar: () -> Unit,
) {
    var renderedDay by remember(date) { mutableStateOf<RenderedDay?>(null) }

    LaunchedEffect(date) { renderedDay = viewModel.renderDay(date) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
            val day = renderedDay
            LiturgicalMasthead(
                date = date,
                day = day,
                onBack = onBack,
                onPreviousDate = onPreviousDate,
                onNextDate = onNextDate,
                onOpenCalendar = onOpenCalendar,
            )

            if (day == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Inapakia\u2026", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())) {
                    if (day.renderedReadings.isEmpty()) {
                        item {
                            Text(
                                "Hakuna masomo yaliyopatikana kwa siku hii bado.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(20.dp),
                            )
                        }
                    } else {
                        day.renderedReadings.forEach { reading ->
                            item(key = reading.label.name) { ReadingCard(reading) }
                        }
                    }
                    day.resolvedDay.readings?.shangilio?.takeIf { it.isNotBlank() }?.let { shangilio ->
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
                                Text("Shangilio", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Text(shangilio, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

/**
 * Editorial masthead: back arrow + tap-the-date-to-open-calendar row, a big serif title, a
 * season/color-dot/rank line, week and cycle badges, and any saint of the day - then a
 * hairline rule. Adapted from a reference LiturgX header design.
 */
@Composable
private fun LiturgicalMasthead(
    date: LocalDate,
    day: RenderedDay?,
    onBack: () -> Unit,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onOpenCalendar: () -> Unit,
) {
    val resolved = day?.resolvedDay
    val color = colorForRangi(resolved?.rangi)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Nyuma", modifier = Modifier.size(16.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onOpenCalendar() },
            ) {
                Text(
                    date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)).uppercase(Locale.ROOT),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Filled.CalendarMonth, contentDescription = "Fungua kalenda", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row {
                IconButton(onClick = onPreviousDate, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Siku iliyopita", modifier = Modifier.size(14.dp))
                }
                IconButton(onClick = onNextDate, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Siku ijayo", modifier = Modifier.size(14.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            resolved?.title ?: "Inapakia\u2026",
            fontFamily = ReadingFont,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.onBackground,
        )

        if (resolved != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(7.dp).background(color, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                val subtitle = listOfNotNull(
                    resolved.season.replaceFirstChar { it.uppercase() },
                ).joinToString("  \u00b7  ")
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (resolved.sundayCycle != null || resolved.weekdayCycle != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    resolved.sundayCycle?.let { HeaderBadge("Mzunguko $it", color) }
                    if (resolved.day != null) resolved.weekdayCycle?.let { HeaderBadge("Wiki $it", MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }

            if (resolved.saintOfTheDay != null && !resolved.title.contains(resolved.saintOfTheDay.jina)) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(resolved.saintOfTheDay.jina, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
    }
}

@Composable
private fun HeaderBadge(text: String, color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = color, fontSize = 12.sp)
    }
}

/**
 * A bounded content block, not a floating card: a hairline border, background matching the
 * page (no elevation), generous internal padding - content is clearly scoped per reading
 * without competing visually with the text. Adapted from a reference LiturgX component.
 */
@Composable
private fun ReadingCard(reading: RenderedReading) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    reading.label.swahiliName().uppercase(Locale.ROOT),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (reading.label == ReadingLabel.GOSPEL) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(reading.citation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            when (reading) {
                is RenderedReading.Available -> RenderedVerses(reading.verses)
                is RenderedReading.Unavailable -> Text(
                    reading.reason,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Flowing verse text with a visible gap marker wherever the citation skips verses, and any
 *  partial-verse letter (e.g. "6a") shown next to the verse number instead of a bare "6". */
@Composable
private fun RenderedVerses(verses: List<DisplayVerse>) {
    Column {
        var previousPosition: Int? = null
        for (dv in verses.sortedBy { it.verse.position }) {
            val prev = previousPosition
            if (prev != null && dv.verse.position > prev + 1) {
                Text("\u2026", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
            }
            Row(modifier = Modifier.padding(bottom = 6.dp)) {
                Text(
                    dv.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(26.dp).padding(top = 2.dp),
                )
                Text(dv.verse.primaryText, fontFamily = ReadingFont, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
            }
            previousPosition = dv.verse.position
        }
    }
}
