package com.biblia.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.app.data.BibleBook
import com.biblia.app.ui.components.AppTopBar

@Composable
fun ChaptersScreen(
    book: BibleBook,
    currentChapter: Int?,
    onBack: () -> Unit,
    onSelectChapter: (Int) -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            AppTopBar(title = book.title, subtitle = "${book.numChapters} sura \u2014 chagua moja", showBack = true, onBack = onBack)
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items((1..book.numChapters).toList()) { chapterNum ->
                    ChapterCell(
                        chapterNum = chapterNum,
                        isCurrent = chapterNum == currentChapter,
                        onClick = { onSelectChapter(chapterNum) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ChapterCell(chapterNum: Int, isCurrent: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .then(
                if (isCurrent) {
                    Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                } else {
                    Modifier.border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(4.dp))
                },
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            chapterNum.toString(),
            fontSize = 16.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
        )
    }
}
