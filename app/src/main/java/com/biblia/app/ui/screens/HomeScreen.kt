package com.biblia.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biblia.app.data.BibleBook
import com.biblia.app.ui.AuroraBackground
import com.biblia.app.ui.BibleViewModel
import com.biblia.app.ui.GroupPosition
import com.biblia.app.ui.GroupedListColumn
import com.biblia.app.ui.GroupedListItem
import com.biblia.app.ui.SleekBottomNav
import com.biblia.app.ui.bouncyClickable
import com.biblia.app.ui.groupPositionFor
import com.biblia.app.ui.theme.SleekOnSurface
import com.biblia.app.ui.theme.SleekOnSurfaceVariant
import com.biblia.app.ui.theme.SleekPrimary
import com.biblia.app.ui.theme.SleekPrimaryContainer

@Composable
fun HomeScreen(
    viewModel: BibleViewModel,
    onNavigate: (String) -> Unit,
    onOpenBook: (BibleBook) -> Unit,
) {
    val oldTestament by viewModel.oldTestament.collectAsState()
    val newTestament by viewModel.newTestament.collectAsState()
    val readingState by viewModel.readingState.collectAsState()
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = { SleekBottomNav(currentRoute = "home", onNavigate = onNavigate) },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        AuroraBackground(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Biblia Takatifu", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SleekOnSurface)
                        Text(
                            "Ukiendelea: ${readingState.lastBookTitle} ${readingState.lastChapterNum}",
                            fontSize = 12.sp,
                            color = SleekOnSurfaceVariant,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(SleekPrimaryContainer, CircleShape)
                            .bouncyClickable { onNavigate("search") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = SleekPrimary, modifier = Modifier.size(20.dp))
                    }
                }

                TabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Agano la Kale") })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Agano Jipya") })
                }

                val books = if (tab == 0) oldTestament else newTestament
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    item {
                        GroupedListColumn {
                            books.forEachIndexed { index, book ->
                                GroupedListItem(position = groupPositionFor(index, books.size)) {
                                    BookRow(book = book, onClick = { onOpenBook(book) })
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
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
            .bouncyClickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(SleekPrimary.copy(alpha = 0.14f), MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(book.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SleekOnSurface)
                Text("Sura ${book.numChapters}", fontSize = 12.sp, color = SleekOnSurfaceVariant)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SleekOnSurfaceVariant.copy(alpha = 0.5f))
    }
}
