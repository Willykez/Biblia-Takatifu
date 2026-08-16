package com.biblia.app.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.biblia.app.data.BibleVerse
import com.biblia.app.ui.BibleViewModel
import com.biblia.app.ui.components.AppTopBar
import com.biblia.app.ui.components.DividedRow
import com.biblia.app.ui.theme.ReadingFont
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: BibleViewModel,
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
        delay(300)
        results = viewModel.searchVerses(query)
        searching = false
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
            AppTopBar(title = "Tafuta", showBack = true, onBack = onBack)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Tafuta neno au mstari...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(20.dp),
            )
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    searching -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    query.length >= 2 && results.isEmpty() -> Text(
                        "Hakuna matokeo kwa \"$query\"",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp),
                    )
                    else -> LazyColumn(
                        contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding()),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(results, key = { it.id }) { verse ->
                            DividedRow {
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

@Composable
private fun SearchResultRow(verse: BibleVerse, bookTitle: String, onClick: () -> Unit) {
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
    }
}
