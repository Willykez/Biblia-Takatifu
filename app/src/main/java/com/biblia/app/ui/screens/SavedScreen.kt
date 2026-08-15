package com.biblia.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.biblia.app.data.BibleVerse
import com.biblia.app.ui.BibleViewModel
import com.biblia.app.ui.components.AppBottomNav
import com.biblia.app.ui.components.DividedRow
import com.biblia.app.ui.theme.ReadingFont

private enum class SavedTab { BOOKMARKS, HIGHLIGHTS, NOTES }

@Composable
fun SavedScreen(
    viewModel: BibleViewModel,
    onNavigate: (String) -> Unit,
    onOpenVerse: (BibleVerse) -> Unit,
) {
    var tab by remember { mutableIntStateOf(0) }
    val oldTestament by viewModel.oldTestament.collectAsState()
    val newTestament by viewModel.newTestament.collectAsState()
    val bookTitleById = remember(oldTestament, newTestament) {
        (oldTestament + newTestament).associate { it.id to it.title }
    }
    var items by remember { mutableStateOf<List<BibleVerse>>(emptyList()) }

    LaunchedEffect(tab, viewModel) {
        items = when (SavedTab.entries[tab]) {
            SavedTab.BOOKMARKS -> viewModel.getBookmarks()
            SavedTab.HIGHLIGHTS -> viewModel.getHighlights()
            SavedTab.NOTES -> viewModel.getNotes()
        }
    }

    Scaffold(
        bottomBar = { AppBottomNav(currentRoute = "saved", onNavigate = onNavigate) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
            Text(
                "Yaliyohifadhiwa",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            )
            TabRow(
                selectedTabIndex = tab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Alama") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Iliyoangaziwa") })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Dokezo") })
            }

            if (items.isEmpty()) {
                Text(
                    "Bado hakuna kitu hapa",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                    textAlign = TextAlign.Center,
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding()),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(items, key = { it.id }) { verse ->
                        DividedRow {
                            SavedRow(
                                verse = verse,
                                bookTitle = bookTitleById[verse.bookId] ?: "",
                                onClick = { onOpenVerse(verse) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedRow(verse: BibleVerse, bookTitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        if (bookTitle.isNotEmpty()) {
            Text(
                "$bookTitle ${verse.chapterNum}:${verse.position}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            verse.primaryText,
            fontFamily = ReadingFont,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 3,
            modifier = Modifier.padding(top = 3.dp),
        )
        if (!verse.note.isNullOrBlank()) {
            Text(
                "\u201C${verse.note}\u201D",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
