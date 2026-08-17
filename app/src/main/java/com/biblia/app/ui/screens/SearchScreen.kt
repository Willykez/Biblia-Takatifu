package com.biblia.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.biblia.app.data.BibleVerse
import com.biblia.app.ui.BibleViewModel
import com.biblia.app.ui.components.AppTopBar
import com.biblia.app.ui.theme.ReadingFont
import kotlinx.coroutines.delay

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
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(query) {
        if (query.trim().length < 2) {
            results = emptyList()
            searching = false
            return@LaunchedEffect
        }
        searching = true
        delay(300)
        results = viewModel.searchVerses(query.trim())
        searching = false
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(title = "Tafuta", showBack = true, onBack = onBack)

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text("Tafuta neno au mstari...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                )
            }
            if (query.isNotEmpty()) {
                IconButton(onClick = { query = "" }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Futa", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                searching -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 32.dp).size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                )

                query.trim().length in 1..1 -> EmptyState(
                    icon = Icons.Outlined.MenuBook,
                    message = "Andika angalau herufi mbili",
                )

                query.isEmpty() -> EmptyState(
                    icon = Icons.Outlined.MenuBook,
                    message = "Tafuta neno lolote katika Biblia yote",
                )

                results.isEmpty() -> EmptyState(
                    icon = Icons.Outlined.MenuBook,
                    message = "Hakuna matokeo kwa \u201c${query.trim()}\u201d",
                )

                else -> Column {
                    Text(
                        "MATOKEO (${results.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    )
                    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), modifier = Modifier.fillMaxSize()) {
                        items(results, key = { it.id }) { verse ->
                            SearchResultRow(
                                verse = verse,
                                bookTitle = bookTitleById[verse.bookId] ?: "",
                                query = query.trim(),
                                onClick = { onOpenVerse(verse) },
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(36.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SearchResultRow(verse: BibleVerse, bookTitle: String, query: String, onClick: () -> Unit) {
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
            highlightMatch(verse.primaryText, query),
            fontFamily = ReadingFont,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 3,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}

/** Bolds the first case-insensitive occurrence of [query] within [text]. */
private fun highlightMatch(text: String, query: String) = buildAnnotatedString {
    if (query.isEmpty()) {
        append(text)
        return@buildAnnotatedString
    }
    val index = text.indexOf(query, ignoreCase = true)
    if (index < 0) {
        append(text)
        return@buildAnnotatedString
    }
    append(text.substring(0, index))
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
        append(text.substring(index, index + query.length))
    }
    append(text.substring(index + query.length))
}
