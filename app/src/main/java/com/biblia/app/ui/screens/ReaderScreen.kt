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
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.app.data.BibleVerse
import com.biblia.app.data.HighlightColor
import com.biblia.app.data.ReaderFontStyle
import com.biblia.app.ui.AuroraBackground
import com.biblia.app.ui.BibleViewModel
import com.biblia.app.ui.bouncyClickable
import com.biblia.app.ui.theme.SleekOnSurface
import com.biblia.app.ui.theme.SleekOnSurfaceVariant
import com.biblia.app.ui.theme.SleekPrimary
import com.biblia.app.ui.theme.SleekPrimaryContainer
import com.biblia.app.ui.theme.SleekSurfaceContainer

private val highlightSwatch = mapOf(
    HighlightColor.YELLOW to Color(0xFFFFF176),
    HighlightColor.GREEN to Color(0xFFC8E6C9),
    HighlightColor.BLUE to Color(0xFFBBDEFB),
    HighlightColor.PINK to Color(0xFFF8BBD0),
    HighlightColor.ORANGE to Color(0xFFFFE0B2),
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
        ReaderFontStyle.SERIF -> FontFamily.Serif
        ReaderFontStyle.MONO -> FontFamily.Monospace
    }

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        AuroraBackground(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Nyuma", tint = SleekOnSurface)
                        }
                        Column {
                            Text("$bookTitle $chapterNum", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = SleekOnSurface)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (chapterNum > 1) onChapterChange(chapterNum - 1) },
                        ) { Icon(Icons.Default.ChevronLeft, contentDescription = "Sura iliyopita", tint = SleekOnSurface) }
                        IconButton(
                            onClick = { if (chapterNum < numChapters) onChapterChange(chapterNum + 1) },
                        ) { Icon(Icons.Default.ChevronRight, contentDescription = "Sura ijayo", tint = SleekOnSurface) }
                        IconButton(onClick = onSearch) {
                            Icon(Icons.Default.Search, contentDescription = "Tafuta", tint = SleekOnSurface)
                        }
                        IconButton(onClick = onSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Mipangilio", tint = SleekOnSurface)
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                        .padding(horizontal = 20.dp),
                ) {
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
            fontSize = (fontSizeSp - 1).sp,
            fontWeight = FontWeight.Bold,
            color = SleekPrimary,
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 6.dp),
        )
        return
    }

    val bg = highlightSwatch[HighlightColor.fromIndex(verse.highlight)]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .let { if (bg != null) it.background(bg.copy(alpha = 0.35f), MaterialTheme.shapes.small) else it }
            .padding(vertical = 4.dp, horizontal = if (bg != null) 6.dp else 0.dp),
    ) {
        if (showVerseNumbers) {
            Text(
                verse.position.toString(),
                fontSize = (fontSizeSp - 5).sp.coerceAtLeastSp(10.sp),
                fontWeight = FontWeight.Bold,
                color = SleekPrimary,
                modifier = Modifier.width(24.dp).padding(top = 3.dp),
            )
        }
        Column {
            Text(
                verse.primaryText,
                fontSize = fontSizeSp.sp,
                fontFamily = fontFamily,
                color = SleekOnSurface,
                lineHeight = (fontSizeSp * 1.5f).sp,
                textAlign = if (justify) TextAlign.Justify else TextAlign.Start,
            )
            if (bilingual && verse.secondaryText != null) {
                Text(
                    verse.secondaryText!!,
                    fontSize = (fontSizeSp - 2).sp,
                    fontFamily = fontFamily,
                    color = SleekOnSurfaceVariant,
                    lineHeight = ((fontSizeSp - 2) * 1.5f).sp,
                    textAlign = if (justify) TextAlign.Justify else TextAlign.Start,
                    modifier = Modifier.padding(top = 2.dp, bottom = 6.dp),
                )
            } else {
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

private fun androidx.compose.ui.unit.TextUnit.coerceAtLeastSp(min: androidx.compose.ui.unit.TextUnit): androidx.compose.ui.unit.TextUnit =
    if (this.value < min.value) min else this

@Composable
private fun VerseActionSheet(
    verse: BibleVerse,
    onToggleBookmark: () -> Unit,
    onSetHighlight: (HighlightColor) -> Unit,
    onSetNote: (String?) -> Unit,
) {
    var noteText by remember(verse.id) { mutableStateOf(verse.note ?: "") }

    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        Text(
            "Mstari ${verse.position}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SleekOnSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(verse.primaryText, fontSize = 14.sp, color = SleekOnSurfaceVariant)
        Spacer(modifier = Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SleekPrimaryContainer, CircleShape)
                    .bouncyClickable { onToggleBookmark() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (verse.bookmark) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Weka alama",
                    tint = SleekPrimary,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                if (verse.bookmark) "Ina alama" else "Weka alama",
                fontSize = 13.sp,
                color = SleekOnSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Rangi ya kuangazia", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SleekOnSurfaceVariant)
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(SleekSurfaceContainer, CircleShape)
                    .bouncyClickable { onSetHighlight(HighlightColor.NONE) },
            )
            HighlightColor.entries.filter { it != HighlightColor.NONE }.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(highlightSwatch.getValue(color), CircleShape)
                        .bouncyClickable { onSetHighlight(color) },
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Dokezo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SleekOnSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = noteText,
            onValueChange = {
                noteText = it
                onSetNote(it.ifBlank { null })
            },
            placeholder = { Text("Andika dokezo lako...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}
