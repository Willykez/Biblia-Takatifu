package com.biblia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.app.data.liturgical.LitCalEvent
import com.biblia.app.ui.AuroraBackground
import com.biblia.app.ui.CalendarLoadState
import com.biblia.app.ui.LiturgicalViewModel
import com.biblia.app.ui.SleekBottomNav
import com.biblia.app.ui.bouncyClickable
import com.biblia.app.ui.liturgical.LiturgicalColors
import com.biblia.app.ui.theme.SleekOnSurface
import com.biblia.app.ui.theme.SleekOnSurfaceVariant
import com.biblia.app.ui.theme.SleekPrimary
import com.biblia.app.ui.theme.SleekPrimaryContainer
import com.biblia.app.ui.theme.SleekSurfaceContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: LiturgicalViewModel,
    onNavigate: (String) -> Unit,
    onOpenDate: (LocalDate) -> Unit,
) {
    val today = remember { LocalDate.now() }
    var month by remember { mutableStateOf(YearMonth.from(today)) }
    val loadState by viewModel.loadState.collectAsState()
    var eventsByDate by remember(month) { mutableStateOf<Map<LocalDate, List<LitCalEvent>>>(emptyMap()) }

    LaunchedEffect(month) { viewModel.ensureYearLoaded(month.year) }
    LaunchedEffect(month, loadState) {
        eventsByDate = viewModel.eventsInMonth(month)
    }

    Scaffold(
        bottomBar = { SleekBottomNav(currentRoute = "calendar", onNavigate = onNavigate) },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        AuroraBackground(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "${month.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${month.year}",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekOnSurface,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { month = month.minusMonths(1) }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Mwezi uliopita", tint = SleekOnSurface)
                        }
                        IconButton(onClick = { month = YearMonth.from(today) }) {
                            Icon(Icons.Default.Today, contentDescription = "Leo", tint = SleekOnSurface)
                        }
                        IconButton(onClick = { month = month.plusMonths(1) }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Mwezi ujao", tint = SleekOnSurface)
                        }
                        IconButton(onClick = { viewModel.ensureYearLoaded(month.year, forceRefresh = true) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Sasisha", tint = SleekOnSurface)
                        }
                    }
                }

                when (val state = loadState) {
                    is CalendarLoadState.Loading -> Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(color = SleekPrimary, modifier = Modifier.size(20.dp)) }

                    is CalendarLoadState.Offline -> Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .background(SleekSurfaceContainer, MaterialTheme.shapes.medium)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.CloudOff, contentDescription = null, tint = SleekOnSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (state.hasCachedData) "Nje ya mtandao \u2014 data iliyohifadhiwa" else "Nje ya mtandao",
                            fontSize = 11.sp,
                            color = SleekOnSurfaceVariant,
                        )
                    }

                    is CalendarLoadState.Loaded -> {}
                }

                WeekdayHeaderRow()

                val firstOfMonth = month.atDay(1)
                val leadingBlanks = firstOfMonth.dayOfWeek.value % 7 // Sunday-first grid
                val totalCells = leadingBlanks + month.lengthOfMonth()

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding()),
                ) {
                    items(totalCells) { index ->
                        if (index < leadingBlanks) {
                            Box(modifier = Modifier.aspectRatio(1f))
                        } else {
                            val date = month.atDay(index - leadingBlanks + 1)
                            DayCell(
                                date = date,
                                isToday = date == today,
                                events = eventsByDate[date].orEmpty(),
                                offlineDay = if (eventsByDate[date].isNullOrEmpty()) viewModel.offlineDayFor(date) else null,
                                onClick = { onOpenDate(date) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekdayHeaderRow() {
    val labels = listOf("J2", "J3", "J4", "J5", "Alh", "Ij", "J1") // Sun-Sat, Swahili short forms
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        labels.forEach { label ->
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SleekOnSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isToday: Boolean,
    events: List<LitCalEvent>,
    offlineDay: com.biblia.app.data.liturgical.LiturgicalDay?,
    onClick: () -> Unit,
) {
    val primaryEvent = events.maxByOrNull { it.grade }
    val color = when {
        primaryEvent != null -> primaryEvent.colorLcl.firstOrNull()?.let { LiturgicalColors.fromApiName(it) }
        offlineDay != null -> LiturgicalColors.fromOffline(offlineDay.color)
        else -> null
    } ?: LiturgicalColors.Green

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(3.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(if (isToday) SleekPrimaryContainer else color.copy(alpha = 0.12f))
            .bouncyClickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                date.dayOfMonth.toString(),
                fontSize = 14.sp,
                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isToday) SleekPrimary else SleekOnSurface,
            )
            if (primaryEvent != null && primaryEvent.grade >= 3) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(5.dp)
                        .background(color, CircleShape),
                )
            }
        }
    }
}
