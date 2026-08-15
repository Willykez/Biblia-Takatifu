package com.biblia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Today
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.biblia.app.data.liturgical.ResolvedDay
import com.biblia.app.ui.LiturgicalViewModel
import com.biblia.app.ui.components.AppTopBar
import com.biblia.app.ui.theme.AppColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private fun colorForRangi(rangi: String?): androidx.compose.ui.graphics.Color = when {
    rangi == null -> AppColors.LiturgicalGreen
    rangi.contains("zambarau") || rangi.contains("purple") -> AppColors.LiturgicalViolet
    rangi.contains("nyeupe") || rangi.contains("dhahabu") || rangi.contains("white") -> AppColors.LiturgicalWhite
    rangi.contains("nyekundu") || rangi.contains("red") -> AppColors.LiturgicalRed
    rangi.contains("waridi") || rangi.contains("rose") -> AppColors.LiturgicalRose
    else -> AppColors.LiturgicalGreen
}

private fun colorForSeason(season: String): androidx.compose.ui.graphics.Color = when (season) {
    "majilio", "kwaresima" -> AppColors.LiturgicalViolet
    "noeli", "pasaka" -> AppColors.LiturgicalWhite
    "sikukuu_maalum" -> AppColors.LiturgicalRed
    else -> AppColors.LiturgicalGreen
}

@Composable
fun CalendarScreen(
    viewModel: LiturgicalViewModel,
    onNavigate: (String) -> Unit,
    onOpenDate: (LocalDate) -> Unit,
    onBack: () -> Unit,
) {
    val today = remember { LocalDate.now() }
    var month by remember { mutableStateOf(YearMonth.from(today)) }
    var daysInMonth by remember(month) { mutableStateOf<Map<LocalDate, ResolvedDay>>(emptyMap()) }

    LaunchedEffect(month) { daysInMonth = viewModel.resolveMonth(month) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
            AppTopBar(
                title = "Kalenda ya Liturujia",
                subtitle = "${month.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${month.year}",
                showBack = true,
                onBack = onBack,
            )

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.Center) {
                IconButton(onClick = { month = month.minusMonths(1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Mwezi uliopita")
                }
                IconButton(onClick = { month = YearMonth.from(today) }) {
                    Icon(Icons.Default.Today, contentDescription = "Leo")
                }
                IconButton(onClick = { month = month.plusMonths(1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Mwezi ujao")
                }
            }

            WeekdayHeaderRow()

            val firstOfMonth = month.atDay(1)
            val leadingBlanks = firstOfMonth.dayOfWeek.value % 7
            val totalCells = leadingBlanks + month.lengthOfMonth()

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding()),
            ) {
                items(totalCells) { index ->
                    if (index < leadingBlanks) {
                        Box(modifier = Modifier.aspectRatio(1f))
                    } else {
                        val date = month.atDay(index - leadingBlanks + 1)
                        val resolved = daysInMonth[date]
                        DayCell(date = date, isToday = date == today, resolved = resolved, onClick = { onOpenDate(date) })
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekdayHeaderRow() {
    val labels = listOf("J2", "J3", "J4", "J5", "Alh", "Ij", "J1")
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        labels.forEach { label ->
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DayCell(date: LocalDate, isToday: Boolean, resolved: ResolvedDay?, onClick: () -> Unit) {
    val dotColor = when {
        resolved == null -> null
        resolved.usedFixedSolemnity || resolved.saintOfTheDay != null -> colorForRangi(resolved.rangi ?: resolved.saintOfTheDay?.rangi)
        else -> colorForSeason(resolved.season)
    }
    val showDot = resolved?.usedFixedSolemnity == true || (resolved?.saintOfTheDay != null && resolved.day != null)

    Box(modifier = Modifier.aspectRatio(1f).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
            )
            if (showDot && dotColor != null) {
                Box(modifier = Modifier.padding(top = 3.dp).size(4.dp).background(dotColor, CircleShape))
            } else {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 3.dp).size(4.dp))
            }
        }
    }
}
