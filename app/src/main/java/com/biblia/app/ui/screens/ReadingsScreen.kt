package com.biblia.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import com.biblia.app.data.BibleVerse
import com.biblia.app.data.liturgical.ReadingLabel
import com.biblia.app.data.liturgical.RenderedDay
import com.biblia.app.data.liturgical.RenderedReading
import com.biblia.app.ui.LiturgicalViewModel
import com.biblia.app.ui.components.AppTopBar
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
) {
    var renderedDay by remember(date) { mutableStateOf<RenderedDay?>(null) }

    LaunchedEffect(date) { renderedDay = viewModel.renderDay(date) }

    val dateLabel = remember(date) { date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
            AppTopBar(title = "Masomo ya Siku", subtitle = dateLabel, showBack = true, onBack = onBack)

            val day = renderedDay
            if (day == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Inapakia\u2026", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())) {
                    item {
                        DayHeader(day)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                    }
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
                            item(key = reading.label.name) {
                                ReadingBlock(reading)
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                            }
                        }
                    }
                    day.resolvedDay.readings?.shangilio?.takeIf { it.isNotBlank() }?.let { shangilio ->
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
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

@Composable
private fun DayHeader(day: RenderedDay) {
    val resolved = day.resolvedDay
    val color = colorForRangi(resolved.rangi)
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(resolved.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            val cycleLabel = listOfNotNull(
                resolved.sundayCycle?.let { "Mzunguko $it" },
                resolved.saintOfTheDay?.jina?.takeIf { resolved.usedFixedSolemnity.not() && !resolved.title.contains(it) }?.let { "Ukumbusho: $it" },
            ).joinToString(" \u2022 ")
            if (cycleLabel.isNotBlank()) {
                Text(cycleLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ReadingBlock(reading: RenderedReading) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text(reading.label.swahiliName(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(reading.citation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
        Spacer(modifier = Modifier.height(10.dp))
        when (reading) {
            is RenderedReading.Available -> RenderedVerses(reading.verses)
            is RenderedReading.Unavailable -> Text(reading.reason, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RenderedVerses(verses: List<BibleVerse>) {
    Column {
        var previousPosition: Int? = null
        for (verse in verses.sortedBy { it.position }) {
            val prev = previousPosition
            if (prev != null && verse.position > prev + 1) {
                Text("\u2026", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
            }
            Row(modifier = Modifier.padding(bottom = 6.dp)) {
                Text(
                    verse.position.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(22.dp).padding(top = 2.dp),
                )
                Text(verse.primaryText, fontFamily = ReadingFont, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
            }
            previousPosition = verse.position
        }
    }
}
