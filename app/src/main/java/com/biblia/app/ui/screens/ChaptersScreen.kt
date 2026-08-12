package com.biblia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.app.data.BibleBook
import com.biblia.app.ui.AuroraBackground
import com.biblia.app.ui.InPageHeader
import com.biblia.app.ui.bouncyClickable
import com.biblia.app.ui.theme.SleekOnSurface
import com.biblia.app.ui.theme.SleekPrimary
import com.biblia.app.ui.theme.SleekSurfaceContainer

@Composable
fun ChaptersScreen(
    book: BibleBook,
    onBack: () -> Unit,
    onSelectChapter: (Int) -> Unit,
) {
    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        AuroraBackground(modifier = Modifier.fillMaxSize()) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding()),
            ) {
                InPageHeader(title = book.title, subtitle = "Chagua sura", showBack = true, onBack = onBack)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items((1..book.numChapters).toList()) { chapterNum ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .fillMaxWidth()
                                .background(SleekSurfaceContainer, MaterialTheme.shapes.medium)
                                .bouncyClickable { onSelectChapter(chapterNum) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                chapterNum.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekOnSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}
