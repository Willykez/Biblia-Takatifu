package com.biblia.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.biblia.app.data.BibleBook
import com.biblia.app.ui.BibleViewModel
import com.biblia.app.ui.components.AppBottomNav
import com.biblia.app.ui.components.DividedRow
import com.biblia.app.ui.theme.ReadingFont

@Composable
fun HomeScreen(
    viewModel: BibleViewModel,
    onNavigate: (String) -> Unit,
    onOpenBook: (BibleBook) -> Unit,
    onOpenCalendar: () -> Unit,
) {
    val oldTestament by viewModel.oldTestament.collectAsState()
    val newTestament by viewModel.newTestament.collectAsState()
    val readingState by viewModel.readingState.collectAsState()
    var tab by remember { mutableIntStateOf(0) }

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
                Column {
                    Text("Biblia Takatifu", fontFamily = ReadingFont, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        "Ukiendelea: ${readingState.lastBookTitle} ${readingState.lastChapterNum}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row {
                    IconButton(onClick = onOpenCalendar) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Kalenda ya Liturujia", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { onNavigate("search") }) {
                        Icon(Icons.Default.Search, contentDescription = "Tafuta", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

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
                        BookRow(book = book, onClick = { onOpenBook(book) })
                    }
                }
            }
        }
    }
}

@Composable
private fun BookRow(book: BibleBook, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(book.title, fontFamily = ReadingFont, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
            Text("Sura ${book.numChapters}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 8.dp))
    }
}
