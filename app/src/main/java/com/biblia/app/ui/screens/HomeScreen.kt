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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.biblia.app.data.BibleBook
import com.biblia.app.ui.BibleViewModel
import com.biblia.app.ui.VerseOfDay
import com.biblia.app.ui.components.AppBottomNav
import com.biblia.app.ui.components.DividedRow
import com.biblia.app.ui.loadVerseOfDay
import com.biblia.app.ui.theme.ReadingFont

@Composable
fun HomeScreen(
    viewModel: BibleViewModel,
    onNavigate: (String) -> Unit,
    onOpenBook: (BibleBook) -> Unit,
    onContinueReading: () -> Unit,
    onOpenVerseOfDay: (bookId: Int, chapterNum: Int) -> Unit,
    onOpenReadingPlans: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val oldTestament by viewModel.oldTestament.collectAsState()
    val newTestament by viewModel.newTestament.collectAsState()
    val readingState by viewModel.readingState.collectAsState()
    val streak by viewModel.readingStreak.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    var verseOfDay by remember { mutableStateOf<VerseOfDay?>(null) }

    LaunchedEffect(Unit) { verseOfDay = loadVerseOfDay(viewModel) }

    Scaffold(
        bottomBar = { AppBottomNav(currentRoute = "home", onNavigate = onNavigate) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Biblia Takatifu", fontFamily = ReadingFont, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (streak.currentStreak > 0) {
                        Text(
                            "\uD83D\uDD25 ${streak.currentStreak}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Tafuta", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Mipangilio", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(horizontal = 20.dp)) {
                ContinueReadingCard(
                    bookTitle = readingState.lastBookTitle,
                    chapterNum = readingState.lastChapterNum,
                    onClick = onContinueReading,
                )
                verseOfDay?.let { vod ->
                    VerseOfDayCard(vod, onClick = { onOpenVerseOfDay(vod.bookId, vod.chapterNum) })
                }
                ReadingPlansEntryCard(onClick = onOpenReadingPlans)
            }

            Spacer(modifier = Modifier.height(4.dp))
            TabRow(
                selectedTabIndex = tab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Agano la Kale") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Agano Jipya") })
            }

            val books = if (tab == 0) oldTestament else newTestament
            LazyColumn(modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())) {
                items(books, key = { it.id }) { book ->
                    DividedRow {
                        BookRow(
                            book = book,
                            isCurrent = book.id == readingState.lastBookId,
                            onClick = { onOpenBook(book) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    eyebrow: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(eyebrow, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(2.dp))
                content()
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ContinueReadingCard(bookTitle: String, chapterNum: Int, onClick: () -> Unit) {
    HomeCard(icon = Icons.Default.MenuBook, eyebrow = "UKIENDELEA", onClick = onClick) {
        Text(
            "$bookTitle $chapterNum",
            fontFamily = ReadingFont,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun VerseOfDayCard(verseOfDay: VerseOfDay, onClick: () -> Unit) {
    HomeCard(icon = Icons.Default.AutoStories, eyebrow = "MSTARI WA LEO", onClick = onClick) {
        Text(
            "${verseOfDay.bookTitle} ${verseOfDay.chapterNum}:${verseOfDay.verse.position}",
            fontFamily = ReadingFont,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            verseOfDay.verse.primaryText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun ReadingPlansEntryCard(onClick: () -> Unit) {
    HomeCard(icon = Icons.Default.AutoStories, eyebrow = "MIPANGO YA KUSOMA", onClick = onClick) {
        Text(
            "Soma Biblia yote kwa mwaka, miezi sita, au miezi mitatu",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun BookRow(book: BibleBook, isCurrent: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (isCurrent) {
                Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                Spacer(modifier = Modifier.width(10.dp))
            }
            Column {
                Text(book.title, fontFamily = ReadingFont, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                Text(
                    "${book.numChapters} sura",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 8.dp))
    }
}
