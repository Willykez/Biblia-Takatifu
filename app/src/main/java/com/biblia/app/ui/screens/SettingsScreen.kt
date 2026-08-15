package com.biblia.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.biblia.app.ui.BibleViewModel
import com.biblia.app.ui.components.AppBottomNav
import com.biblia.app.ui.components.AppTopBar
import com.biblia.app.ui.components.DividedRow
import com.biblia.app.ui.components.SectionLabel
import com.biblia.app.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    viewModel: BibleViewModel,
    onNavigate: (String) -> Unit,
) {
    val readingState by viewModel.readingState.collectAsState()
    val dataCounts by viewModel.dataCounts.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    var clearBookmarksExpanded by remember { mutableStateOf(false) }
    var clearHighlightsExpanded by remember { mutableStateOf(false) }
    var clearNotesExpanded by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { AppBottomNav(currentRoute = "settings", onNavigate = onNavigate) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
            AppTopBar(title = "Mipangilio", showBack = true, onBack = { onNavigate("home") })
            LazyColumn(modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())) {
                item {
                    SectionLabel("USOMAJI")
                    DividedRow {
                        SettingsRow(title = "Maandishi mawili", subtitle = "Onyesha Kiswahili na Kiingereza pamoja") {
                            Switch(
                                checked = readingState.bilingual,
                                onCheckedChange = { viewModel.setBilingual(it) },
                                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                            )
                        }
                    }
                    DividedRow {
                        SettingsRow(title = "Namba za mstari", subtitle = "Onyesha namba ya kila mstari") {
                            Switch(
                                checked = readingState.showVerseNumbers,
                                onCheckedChange = { viewModel.setShowVerseNumbers(it) },
                                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                            )
                        }
                    }
                    DividedRow {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                            Text("Ukubwa wa maandishi (${readingState.fontSizeSp}sp)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                            Slider(
                                value = readingState.fontSizeSp.toFloat(),
                                onValueChange = { viewModel.setFontSize(it.toInt()) },
                                valueRange = 12f..28f,
                                steps = 15,
                                colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary),
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    SectionLabel("MWONEKANO")
                    DividedRow {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            listOf(ThemeMode.SYSTEM to "Mfumo", ThemeMode.LIGHT to "Mwanga", ThemeMode.DARK to "Giza").forEach { (mode, label) ->
                                val selected = mode == themeMode
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.clickable { viewModel.setThemeMode(mode) },
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    SectionLabel("DATA YAKO")
                    ClearableDataRow(
                        title = "Alama",
                        subtitle = "${dataCounts.bookmarks} mstari umewekwa alama",
                        expanded = clearBookmarksExpanded,
                        onToggleExpanded = { clearBookmarksExpanded = !clearBookmarksExpanded },
                        onConfirmClear = { viewModel.clearAllBookmarks(); clearBookmarksExpanded = false },
                    )
                    ClearableDataRow(
                        title = "Iliyoangaziwa",
                        subtitle = "${dataCounts.highlights} mstari umeangaziwa",
                        expanded = clearHighlightsExpanded,
                        onToggleExpanded = { clearHighlightsExpanded = !clearHighlightsExpanded },
                        onConfirmClear = { viewModel.clearAllHighlights(); clearHighlightsExpanded = false },
                    )
                    ClearableDataRow(
                        title = "Dokezo",
                        subtitle = "${dataCounts.notes} dokezo limehifadhiwa",
                        expanded = clearNotesExpanded,
                        onToggleExpanded = { clearNotesExpanded = !clearNotesExpanded },
                        onConfirmClear = { viewModel.clearAllNotes(); clearNotesExpanded = false },
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    SectionLabel("KUHUSU")
                    DividedRow(showDivider = false) {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                            Text("Biblia Takatifu", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                            Text(
                                "Tafsiri ya Kiswahili na Kiingereza \u2014 vitabu 66, chapisho la nje ya mtandao",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ClearableDataRow(
    title: String,
    subtitle: String,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onConfirmClear: () -> Unit,
) {
    DividedRow {
        Column {
            SettingsRow(title = "Futa $title", subtitle = subtitle, titleColor = MaterialTheme.colorScheme.error, onClick = onToggleExpanded)
            AnimatedVisibility(visible = expanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    OutlinedButton(onClick = onConfirmClear) {
                        Text("Thibitisha")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onBackground,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = titleColor)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(12.dp))
            trailing()
        }
    }
}
