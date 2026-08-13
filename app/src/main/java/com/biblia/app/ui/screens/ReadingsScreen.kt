package com.biblia.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.app.data.BibleVerse
import com.biblia.app.data.liturgical.LitCalEvent
import com.biblia.app.data.liturgical.ReadingLabel
import com.biblia.app.data.liturgical.RenderedDay
import com.biblia.app.data.liturgical.RenderedReading
import com.biblia.app.ui.AuroraBackground
import com.biblia.app.ui.CalendarLoadState
import com.biblia.app.ui.InPageHeader
import com.biblia.app.ui.LiturgicalViewModel
import com.biblia.app.ui.liturgical.LiturgicalColors
import com.biblia.app.ui.theme.SleekOnSurface
import com.biblia.app.ui.theme.SleekOnSurfaceVariant
import com.biblia.app.ui.theme.SleekPrimary
import com.biblia.app.ui.theme.SleekSurfaceContainer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun ReadingLabel.swahiliName(): String = when (this) {
    ReadingLabel.FIRST_READING -> "Somo la Kwanza"
    ReadingLabel.PSALM -> "Zaburi ya Kuitikia"
    ReadingLabel.SECOND_READING -> "Somo la Pili"
    ReadingLabel.GOSPEL -> "Injili"
}

@Composable
fun ReadingsScreen(
    viewModel: LiturgicalViewModel,
    date: LocalDate,
    onBack: () -> Unit,
) {
    val loadState by viewModel.loadState.collectAsState()
    var renderedDay by remember(date) { mutableStateOf<RenderedDay?>(null) }

    LaunchedEffect(date) {
        viewModel.ensureYearLoaded(date.year)
    }
    LaunchedEffect(date, loadState) {
        if (loadState !is CalendarLoadState.Loading) {
            renderedDay = viewModel.renderDay(date)
        }
    }

    val dateLabel = remember(date) {
        date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH))
    }

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        AuroraBackground(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
                InPageHeader(
                    title = "Masomo ya Siku",
                    subtitle = dateLabel,
                    showBack = true,
                    onBack = onBack,
                    rightIcon = Icons.Default.Refresh,
                    onRightClick = { viewModel.ensureYearLoaded(date.year, forceRefresh = true) },
                )

                when (val state = loadState) {
                    is CalendarLoadState.Loading -> Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(color = SleekPrimary) }

                    is CalendarLoadState.Offline -> OfflineBanner(
                        hasCachedData = state.hasCachedData,
                        message = state.message,
                    )

                    is CalendarLoadState.Loaded -> {}
                }

                val day = renderedDay
                if (day != null) {
                    if (day.events.isEmpty()) {
                        Text(
                            "Hakuna taarifa ya siku hii bado. Jaribu kusawazisha upya.",
                            color = SleekOnSurfaceVariant,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(20.dp),
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = innerPadding.calculateBottomPadding())
                                .padding(horizontal = 20.dp),
                        ) {
                            for (event in day.events) {
                                item(key = event.eventKey) {
                                    EventHeaderCard(event = event)
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                                val readings = day.readingsByEvent[event.eventKey].orEmpty()
                                items(readings, key = { event.eventKey + it.label.name }) { reading ->
                                    ReadingCard(reading = reading)
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OfflineBanner(hasCachedData: Boolean, message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .background(SleekSurfaceContainer, MaterialTheme.shapes.medium)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.CloudOff, contentDescription = null, tint = SleekOnSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            if (hasCachedData) "Haiko mtandaoni - inaonyesha data iliyohifadhiwa" else "Haiko mtandaoni: $message",
            fontSize = 12.sp,
            color = SleekOnSurfaceVariant,
        )
    }
}

@Composable
private fun EventHeaderCard(event: LitCalEvent) {
    val color = event.colorLcl.firstOrNull()?.let { LiturgicalColors.fromApiName(it) } ?: LiturgicalColors.Green
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.14f), MaterialTheme.shapes.large)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(event.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SleekOnSurface)
            val subtitle = listOfNotNull(event.gradeLcl.takeIf { it.isNotBlank() }, event.liturgicalYear)
                .joinToString(" \u2022 ")
            if (subtitle.isNotBlank()) {
                Text(subtitle, fontSize = 12.sp, color = SleekOnSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ReadingCard(reading: RenderedReading) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SleekSurfaceContainer, MaterialTheme.shapes.large)
            .padding(16.dp),
    ) {
        Text(
            reading.label.swahiliName(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SleekPrimary,
            letterSpacing = 0.5.sp,
        )
        Text(reading.citation, fontSize = 12.sp, color = SleekOnSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
        Spacer(modifier = Modifier.height(10.dp))
        when (reading) {
            is RenderedReading.Available -> RenderedVerses(reading.verses)
            is RenderedReading.Unavailable -> Text(
                reading.reason,
                fontSize = 13.sp,
                color = SleekOnSurfaceVariant,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}

/** Flowing verse text with a visible gap marker wherever the citation skips verses. */
@Composable
private fun RenderedVerses(verses: List<BibleVerse>) {
    Column {
        var previousPosition: Int? = null
        for (verse in verses.sortedBy { it.position }) {
            val prev = previousPosition
            if (prev != null && verse.position > prev + 1) {
                Text(
                    "\u2026",
                    fontSize = 13.sp,
                    color = SleekOnSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
            Row(modifier = Modifier.padding(bottom = 6.dp)) {
                Text(
                    verse.position.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekPrimary,
                    modifier = Modifier.width(20.dp).padding(top = 2.dp),
                )
                Text(
                    verse.primaryText,
                    fontSize = 15.sp,
                    color = SleekOnSurface,
                    lineHeight = 22.sp,
                )
            }
            previousPosition = verse.position
        }
    }
}
