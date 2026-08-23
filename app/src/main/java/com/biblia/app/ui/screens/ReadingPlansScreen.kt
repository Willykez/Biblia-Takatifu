package com.biblia.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.app.data.PlanPacing
import com.biblia.app.data.PlanProgress
import com.biblia.app.data.ReadingPlan
import com.biblia.app.ui.ReadingPlanViewModel
import com.biblia.app.ui.components.AppTopBar
import com.biblia.app.ui.theme.ReadingFont

@Composable
fun ReadingPlansScreen(
    viewModel: ReadingPlanViewModel,
    onBack: () -> Unit,
    onOpenPlan: (Int) -> Unit,
) {
    val plans by viewModel.plans.collectAsState()
    val progressByPlan by viewModel.progressByPlan.collectAsState()
    val pacingByPlan by viewModel.pacingByPlan.collectAsState()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            AppTopBar(title = "Mipango ya Kusoma", subtitle = "Soma Biblia yote kwa mpangilio wako", showBack = true, onBack = onBack)
            LazyColumn(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                items(plans, key = { it.id }) { plan ->
                    PlanCard(
                        plan = plan,
                        pacing = pacingByPlan[plan.id] ?: PlanPacing.ONE_YEAR,
                        progress = progressByPlan[plan.id],
                        onSelectPacing = { viewModel.setPacing(plan.id, it) },
                        onClick = { onOpenPlan(plan.id) },
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun PlanCard(
    plan: ReadingPlan,
    pacing: PlanPacing,
    progress: PlanProgress?,
    onSelectPacing: (PlanPacing) -> Unit,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress?.fraction ?: 0f },
                        modifier = Modifier.size(44.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline,
                        strokeWidth = 3.dp,
                    )
                    Text(
                        "${((progress?.fraction ?: 0f) * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 10.sp,
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(plan.title, fontFamily = ReadingFont, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        "${progress?.daysRead ?: 0}/${pacing.totalDays} siku",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(plan.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlanPacing.entries.forEach { candidate ->
                    val selected = candidate == pacing
                    Box(
                        modifier = Modifier
                            .clickable { onSelectPacing(candidate) }
                            .let {
                                if (selected) it.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                else it.border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(6.dp))
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    ) {
                        Text(
                            candidate.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }
    }
}
