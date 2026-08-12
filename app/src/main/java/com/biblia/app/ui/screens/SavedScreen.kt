package com.biblia.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.app.data.BibleVerse
import com.biblia.app.ui.AuroraBackground
import com.biblia.app.ui.BibleViewModel
import com.biblia.app.ui.GroupedListColumn
import com.biblia.app.ui.GroupedListItem
import com.biblia.app.ui.SleekBottomNav
import com.biblia.app.ui.bouncyClickable
import com.biblia.app.ui.groupPositionFor
import com.biblia.app.ui.theme.SleekOnSurface
import com.biblia.app.ui.theme.SleekOnSurfaceVariant
import com.biblia.app.ui.theme.SleekPrimary

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
        bottomBar = { SleekBottomNav(currentRoute = "saved", onNavigate = onNavigate) },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        AuroraBackground(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
                Text(
                    "Ilivyohifadhiwa",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekOnSurface,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                )
                TabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Alama") })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Iliyoangaziwa") })
                    Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Dokezo") })
                }

                if (items.isEmpty()) {
                    Text(
                        "Bado hakuna kitu hapa",
                        color = SleekOnSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding()),
                    ) {
                        item {
                            GroupedListColumn {
                                items.forEachIndexed { index, verse ->
                                    GroupedListItem(position = groupPositionFor(index, items.size)) {
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
        }
    }
}

@Composable
private fun SavedRow(verse: BibleVerse, bookTitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable { onClick() }
            .padding(16.dp),
    ) {
        if (bookTitle.isNotEmpty()) {
            Text(
                "$bookTitle ${verse.chapterNum}:${verse.position}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SleekPrimary,
            )
        }
        Text(
            verse.primaryText,
            fontSize = 14.sp,
            color = SleekOnSurface,
            maxLines = 3,
            modifier = Modifier.padding(top = 2.dp),
        )
        if (!verse.note.isNullOrBlank()) {
            Text(
                "\u201C${verse.note}\u201D",
                fontSize = 12.sp,
                color = SleekOnSurfaceVariant,
                maxLines = 2,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
