package com.biblia.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.biblia.app.data.PlanChapterRef
import com.biblia.app.data.PlanPacing
import com.biblia.app.data.ReadingPlan
import com.biblia.app.ui.ReadingPlanViewModel
import com.biblia.app.ui.components.AppTopBar
import com.biblia.app.ui.theme.ReadingFont

@Composable
fun PlanDayScreen(
    viewModel: ReadingPlanViewModel,
    plan: ReadingPlan,
    pacing: PlanPacing,
    onBack: () -> Unit,
    onOpenChapter: (bookId: Int, chapterNum: Int) -> Unit,
) {
    var day by remember { mutableIntStateOf(1) }
    var chapters by remember { mutableStateOf<List<PlanChapterRef>>(emptyList()) }
    var isRead by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(plan.id, pacing) {
        if (!initialized) {
            day = viewModel.getNextUnreadDay(plan.id, pacing)
            initialized = true
        }
    }
    LaunchedEffect(day, plan.id, pacing) {
        if (initialized) {
            chapters = viewModel.getDayChapters(plan.id, pacing, day)
            isRead = viewModel.isDayRead(plan.id, day)
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            AppTopBar(title = plan.title, subtitle = "Siku $day ya ${pacing.totalDays}", showBack = true, onBack = onBack)

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(onClick = { if (day > 1) day-- }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Siku iliyopita")
                }
                IconButton(onClick = { if (day < pacing.totalDays) day++ }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Siku ijayo")
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                items(chapters, key = { "${it.bookId}_${it.chapterNum}" }) { chapter ->
                    ChapterRow(chapter = chapter, onClick = { onOpenChapter(chapter.bookId, chapter.chapterNum) })
                    Spacer(modifier = Modifier.height(10.dp))
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val next = !isRead
                        isRead = next
                        viewModel.setDayRead(plan.id, pacing, day, next)
                    }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    if (isRead) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    if (isRead) "Umesoma siku hii" else "Weka alama: nimesoma",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@Composable
private fun ChapterRow(chapter: PlanChapterRef, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${chapter.bookTitle} ${chapter.chapterNum}",
                fontFamily = ReadingFont,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
