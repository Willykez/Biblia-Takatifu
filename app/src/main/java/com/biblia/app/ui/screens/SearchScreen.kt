package com.biblia.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.app.data.BibleVerse
import com.biblia.app.ui.AuroraBackground
import com.biblia.app.ui.BibleViewModel
import com.biblia.app.ui.GroupPosition
import com.biblia.app.ui.GroupedListColumn
import com.biblia.app.ui.GroupedListItem
import com.biblia.app.ui.InPageHeader
import com.biblia.app.ui.SleekBottomNav
import com.biblia.app.ui.bouncyClickable
import com.biblia.app.ui.groupPositionFor
import com.biblia.app.ui.theme.SleekOnSurface
import com.biblia.app.ui.theme.SleekOnSurfaceVariant
import com.biblia.app.ui.theme.SleekPrimary
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: BibleViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    onOpenVerse: (BibleVerse) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<BibleVerse>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    val oldTestament by viewModel.oldTestament.collectAsState()
    val newTestament by viewModel.newTestament.collectAsState()
    val bookTitleById = remember(oldTestament, newTestament) {
        (oldTestament + newTestament).associate { it.id to it.title }
    }

    LaunchedEffect(query) {
        if (query.length < 2) {
            results = emptyList()
            return@LaunchedEffect
        }
        searching = true
        delay(300) // debounce
        results = viewModel.searchVerses(query)
        searching = false
    }

    Scaffold(
        bottomBar = { SleekBottomNav(currentRoute = "search", onNavigate = onNavigate) },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        AuroraBackground(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
                InPageHeader(title = "Tafuta", showBack = true, onBack = onBack)
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Tafuta neno au mstari...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                )
                Box(modifier = Modifier.fillMaxSize().padding(top = 12.dp)) {
                    when {
                        searching -> CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp),
                            color = SleekPrimary,
                        )
                        query.length >= 2 && results.isEmpty() -> Text(
                            "Hakuna matokeo kwa \"$query\"",
                            color = SleekOnSurfaceVariant,
                            fontSize = 14.sp,
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp),
                        )
                        else -> LazyColumn(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = innerPadding.calculateBottomPadding()),
                        ) {
                            item {
                                GroupedListColumn {
                                    results.forEachIndexed { index, verse ->
                                        GroupedListItem(position = groupPositionFor(index, results.size)) {
                                            SearchResultRow(
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
}

@Composable
private fun SearchResultRow(verse: BibleVerse, bookTitle: String, onClick: () -> Unit) {
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
    }
}
