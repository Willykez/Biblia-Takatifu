package com.biblia.app.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.app.data.BibleVerse
import com.biblia.app.data.HighlightColor
import com.biblia.app.data.ReaderFontStyle
import com.biblia.app.ui.BibleViewModel
import com.biblia.app.ui.theme.ReadingFont

private val highlightSwatch = mapOf(
    HighlightColor.YELLOW to androidx.compose.ui.graphics.Color(0xFFE9D98A),
    HighlightColor.GREEN to androidx.compose.ui.graphics.Color(0xFFA9C4A5),
    HighlightColor.BLUE to androidx.compose.ui.graphics.Color(0xFFA6C0D6),
    HighlightColor.PINK to androidx.compose.ui.graphics.Color(0xFFD9A9BB),
    HighlightColor.ORANGE to androidx.compose.ui.graphics.Color(0xFFE0B589),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: BibleViewModel,
    bookId: Int,
    bookTitle: String,
    chapterNum: Int,
    numChapters: Int,
    onChapterChange: (Int) -> Unit,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
) {
    val readingState by viewModel.readingState.collectAsState()
    var verses by remember { mutableStateOf<List<BibleVerse>>(emptyList()) }
    var selectedVerse by remember { mutableStateOf<BibleVerse?>(null) }

    LaunchedEffect(bookId, chapterNum) {
        verses = viewModel.getVerses(bookId, chapterNum)
        viewModel.setLastRead(bookId, chapterNum, bookTitle)
    }

    val fontFamily = when (readingState.fontStyle) {
        ReaderFontStyle.SANS -> FontFamily.SansSerif
        ReaderFontStyle.SERIF -> ReadingFont
        ReaderFontStyle.MONO -> FontFamily.Monospace
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Nyuma")
                    }
                    Text("$bookTitle $chapterNum", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (chapterNum > 1) onChapterChange(chapterNum - 1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Sura iliyopita")
                    }
                    IconButton(onClick = { if (chapterNum < numChapters) onChapterChange(chapterNum + 1) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Sura ijayo")
                    }
                    IconButton(onClick = onSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Tafuta")
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Mipangilio")
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .padding(horizontal = 24.dp),
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                items(verses, key = { it.id }) { verse ->
                    VerseRow(
                        verse = verse,
                        fontFamily = fontFamily,
                        fontSizeSp = readingState.fontSizeSp,
                        bilingual = readingState.bilingual,
                        showVerseNumbers = readingState.showVerseNumbers,
                        justify = readingState.justifyText,
                        onClick = { if (!verse.isHeading) selectedVerse = verse },
                    )
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }

    val verse = selectedVerse
    if (verse != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedVerse = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            VerseActionSheet(
                verse = verse,
                onToggleBookmark = { viewModel.toggleBookmark(verse); selectedVerse = verse.copy(bookmark = !verse.bookmark) },
                onSetHighlight = { color -> viewModel.setHighlight(verse, color.index); selectedVerse = verse.copy(highlight = color.index) },
                onSetNote = { note -> viewModel.setNote(verse, note) },
            )
        }
    }
}

@Composable
private fun VerseRow(
    verse: BibleVerse,
    fontFamily: FontFamily,
    fontSizeSp: Int,
    bilingual: Boolean,
    showVerseNumbers: Boolean,
    justify: Boolean,
    onClick: () -> Unit,
) {
    if (verse.isHeading) {
        Text(
            verse.primaryText,
            fontFamily = fontFamily,
            fontSize = (fontSizeSp - 1).sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 8.dp),
        )
        return
    }

    val bg = highlightSwatch[HighlightColor.fromIndex(verse.highlight)]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .let { if (bg != null) it.background(bg.copy(alpha = 0.32f)) else it }
            .padding(vertical = 5.dp, horizontal = if (bg != null) 6.dp else 0.dp),
    ) {
        if (showVerseNumbers) {
            Text(
                verse.position.toString(),
                fontSize = (fontSizeSp - 5).coerceAtLeast(10).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(26.dp).padding(top = 3.dp),
            )
        }
        Column {
            Text(
                verse.primaryText,
                fontSize = fontSizeSp.sp,
                fontFamily = fontFamily,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = (fontSizeSp * 1.55f).sp,
                textAlign = if (justify) TextAlign.Justify else TextAlign.Start,
            )
            if (bilingual && verse.secondaryText != null) {
                Text(
                    verse.secondaryText!!,
                    fontSize = (fontSizeSp - 2).sp,
                    fontFamily = fontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = ((fontSizeSp - 2) * 1.55f).sp,
                    textAlign = if (justify) TextAlign.Justify else TextAlign.Start,
                    modifier = Modifier.padding(top = 3.dp, bottom = 8.dp),
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun VerseActionSheet(
    verse: BibleVerse,
    onToggleBookmark: () -> Unit,
    onSetHighlight: (HighlightColor) -> Unit,
    onSetNote: (String?) -> Unit,
) {
    var noteText by remember(verse.id) { mutableStateOf(verse.note ?: "") }

    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Text("Mstari ${verse.position}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(4.dp))
        Text(verse.primaryText, fontFamily = ReadingFont, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onToggleBookmark() },
        ) {
            Icon(
                if (verse.bookmark) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = "Weka alama",
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(if (verse.bookmark) "Ina alama" else "Weka alama", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("RANGI YA KUANGAZIA", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable { onSetHighlight(HighlightColor.NONE) },
            )
            HighlightColor.entries.filter { it != HighlightColor.NONE }.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(highlightSwatch.getValue(color), CircleShape)
                        .clickable { onSetHighlight(color) },
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("DOKEZO", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it; onSetNote(it.ifBlank { null }) },
            placeholder = { Text("Andika dokezo lako...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}
