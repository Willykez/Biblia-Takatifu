package com.biblia.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.biblia.app.data.liturgical.ResolvedDay
import com.biblia.app.ui.LiturgicalViewModel
import com.biblia.app.ui.components.AppBottomNav
import com.biblia.app.ui.components.AppTopBar
import com.biblia.app.ui.theme.AppColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private fun colorForRangi(rangi: String?): Color = when {
    rangi == null -> AppColors.LiturgicalGreen
    rangi.contains("zambarau") || rangi.contains("purple") -> AppColors.LiturgicalViolet
    rangi.contains("nyeupe") || rangi.contains("dhahabu") || rangi.contains("white") -> AppColors.LiturgicalWhite
    rangi.contains("nyekundu") || rangi.contains("red") -> AppColors.LiturgicalRed
    rangi.contains("waridi") || rangi.contains("rose") -> AppColors.LiturgicalRose
    else -> AppColors.LiturgicalGreen
}

private fun colorForSeason(season: String): Color = when (season) {
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
) {
    val today = remember { LocalDate.now() }
    var month by remember { mutableStateOf(YearMonth.from(today)) }
    var daysInMonth by remember(month) { mutableStateOf<Map<LocalDate, ResolvedDay>>(emptyMap()) }

    LaunchedEffect(month) { daysInMonth = viewModel.resolveMonth(month) }

    Scaffold(
        bottomBar = { AppBottomNav(currentRoute = "calendar", onNavigate = onNavigate) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
            AppTopBar(
                title = "Kalenda ya Liturujia",
                subtitle = "${month.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${month.year}",
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
                modifier = Modifier.weight(1f),
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

            ColorLegend()
            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))
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
    val seasonColor = resolved?.let { colorForSeason(it.season) }
    val markColor = when {
        resolved == null -> null
        resolved.usedFixedSolemnity || resolved.saintOfTheDay != null -> colorForRangi(resolved.rangi ?: resolved.saintOfTheDay?.rangi)
        else -> seasonColor
    }
    val showMark = resolved?.usedFixedSolemnity == true || (resolved?.saintOfTheDay != null && resolved.day != null)

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(4.dp))
            .then(if (seasonColor != null) Modifier.background(seasonColor.copy(alpha = 0.08f)) else Modifier)
            .then(if (isToday) Modifier.border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(4.dp)) else Modifier)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
            )
            if (showMark && markColor != null) {
                Box(modifier = Modifier.padding(top = 3.dp).size(4.dp).background(markColor, CircleShape))
            } else {
                Spacer(modifier = Modifier.padding(top = 3.dp).size(4.dp))
            }
        }
    }
}

@Composable
private fun ColorLegend() {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            LegendDot(AppColors.LiturgicalGreen, "Kawaida")
            LegendDot(AppColors.LiturgicalViolet, "Majilio/Kwaresima")
            LegendDot(AppColors.LiturgicalWhite, "Sikukuu")
            LegendDot(AppColors.LiturgicalRed, "Mashahidi")
            LegendDot(AppColors.LiturgicalRose, "Furaha")
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.padding(start = 4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
