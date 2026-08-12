package com.biblia.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.app.ui.BibleViewModel
import com.biblia.app.ui.GroupPosition
import com.biblia.app.ui.GroupedListColumn
import com.biblia.app.ui.GroupedListItem
import com.biblia.app.ui.SleekBottomNav
import com.biblia.app.ui.bouncyClickable
import com.biblia.app.ui.AuroraBackground
import com.biblia.app.ui.InPageHeader
import com.biblia.app.ui.groupPositionFor
import com.biblia.app.ui.settings.AppearanceSection
import com.biblia.app.ui.theme.LocalSnackbarHostState
import com.biblia.app.ui.theme.LocalThemePrefs
import com.biblia.app.ui.theme.LocalThemeState
import com.biblia.app.ui.theme.SleekOnSurface
import com.biblia.app.ui.theme.SleekOnSurfaceVariant
import com.biblia.app.ui.theme.SleekPrimary
import com.biblia.app.ui.theme.SleekPrimaryContainer

/**
 * Settings, restyled around a grouped-list layout: sections read as one seamless
 * rounded card (GroupedListColumn/GroupedListItem) instead of separate floating
 * cards per row, with section headers as small caps labels above each group -
 * matches the reference app's Settings screen structure.
 *
 * The MWONEKANO (Appearance) section below reuses AppearanceSection verbatim from the
 * design framework, per the "has to be exactly the same" requirement. USOMAJI (Reading)
 * and DATA YAKO (Your Data) replace the framework's file-transfer CONNECTIVITY/STORAGE
 * sections with Bible-relevant equivalents.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: BibleViewModel,
    onNavigate: (String) -> Unit,
) {
    val readingState by viewModel.readingState.collectAsState()
    val dataCounts by viewModel.dataCounts.collectAsState()
    var clearBookmarksExpanded by remember { mutableStateOf(false) }
    var clearHighlightsExpanded by remember { mutableStateOf(false) }
    var clearNotesExpanded by remember { mutableStateOf(false) }
    val themePrefs = LocalThemePrefs.current
    val themeState = LocalThemeState.current
    var showAppearanceSheet by remember { mutableStateOf(false) }
    val snackbarHostState = LocalSnackbarHostState.current

    Scaffold(
        bottomBar = {
            SleekBottomNav(currentRoute = "settings", onNavigate = onNavigate)
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        AuroraBackground(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())) {
            InPageHeader(title = "Mipangilio", showBack = true, onBack = { onNavigate("home") })
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = SleekPrimaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(SleekPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("BT", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Biblia Takatifu", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SleekOnSurface)
                            Text("Kiswahili \u2022 English \u2022 Nje ya mtandao", fontSize = 13.sp, color = SleekOnSurfaceVariant)
                        }
                    }
                }
            }

            item {
                SettingsSection(title = "USOMAJI") {
                    val items = listOf<@Composable (GroupPosition) -> Unit>(
                        { position ->
                            GroupedListItem(position = position) {
                                SettingsRow(
                                    icon = Icons.Default.Language,
                                    title = "Maandishi mawili",
                                    subtitle = "Onyesha Kiswahili na Kiingereza pamoja",
                                    trailing = {
                                        Switch(
                                            checked = readingState.bilingual,
                                            onCheckedChange = { viewModel.setBilingual(it) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = SleekPrimary,
                                            ),
                                        )
                                    }
                                )
                            }
                        },
                        { position ->
                            GroupedListItem(position = position) {
                                SettingsRow(
                                    icon = Icons.Default.Numbers,
                                    title = "Namba za mstari",
                                    subtitle = "Onyesha namba ya kila mstari",
                                    trailing = {
                                        Switch(
                                            checked = readingState.showVerseNumbers,
                                            onCheckedChange = { viewModel.setShowVerseNumbers(it) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = SleekPrimary,
                                            ),
                                        )
                                    }
                                )
                            }
                        },
                        { position ->
                            GroupedListItem(position = position) {
                                Column {
                                    SettingsRow(
                                        icon = Icons.Default.FormatSize,
                                        title = "Ukubwa wa maandishi",
                                        subtitle = "${readingState.fontSizeSp}sp",
                                    )
                                    Slider(
                                        value = readingState.fontSizeSp.toFloat(),
                                        onValueChange = { viewModel.setFontSize(it.toInt()) },
                                        valueRange = 12f..28f,
                                        steps = 15,
                                        colors = SliderDefaults.colors(
                                            thumbColor = SleekPrimary,
                                            activeTrackColor = SleekPrimary,
                                        ),
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                                    )
                                }
                            }
                        },
                    )
                    items.forEachIndexed { index, row -> row(groupPositionFor(index, items.size)) }
                }
            }

            item {
                SettingsSection(title = "MWONEKANO") {
                    GroupedListItem(position = GroupPosition.ONLY) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAppearanceSheet = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Mwonekano", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SleekOnSurface)
                                Text("Mandhari, rangi na mtindo", fontSize = 12.sp, color = SleekOnSurfaceVariant)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SleekOnSurfaceVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            item {
                SettingsSection(title = "DATA YAKO") {
                    ClearableDataRow(
                        title = "Alama",
                        subtitle = "${dataCounts.bookmarks} mstari umewekwa alama",
                        position = GroupPosition.FIRST,
                        expanded = clearBookmarksExpanded,
                        onToggleExpanded = { clearBookmarksExpanded = !clearBookmarksExpanded },
                        onConfirmClear = { viewModel.clearAllBookmarks(); clearBookmarksExpanded = false },
                    )
                    ClearableDataRow(
                        title = "Iliyoangaziwa",
                        subtitle = "${dataCounts.highlights} mstari umeangaziwa",
                        position = GroupPosition.MIDDLE,
                        expanded = clearHighlightsExpanded,
                        onToggleExpanded = { clearHighlightsExpanded = !clearHighlightsExpanded },
                        onConfirmClear = { viewModel.clearAllHighlights(); clearHighlightsExpanded = false },
                    )
                    ClearableDataRow(
                        title = "Dokezo",
                        subtitle = "${dataCounts.notes} dokezo limehifadhiwa",
                        position = GroupPosition.LAST,
                        expanded = clearNotesExpanded,
                        onToggleExpanded = { clearNotesExpanded = !clearNotesExpanded },
                        onConfirmClear = { viewModel.clearAllNotes(); clearNotesExpanded = false },
                    )
                }
            }

            item {
                SettingsSection(title = "KUHUSU") {
                    GroupedListItem(position = GroupPosition.ONLY) {
                        SettingsRow(
                            icon = Icons.Default.Info,
                            title = "Biblia Takatifu",
                            subtitle = "Tafsiri ya Kiswahili na Kiingereza \u2014 vitabu 66, chapisho la nje ya mtandao"
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        }
        }
    }

    if (showAppearanceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAppearanceSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            AppearanceSection(
                prefs = themePrefs,
                state = themeState,
                snackbarHostState = snackbarHostState,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ClearableDataRow(
    title: String,
    subtitle: String,
    position: GroupPosition,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onConfirmClear: () -> Unit,
) {
    GroupedListItem(position = position) {
        Column {
            SettingsRow(
                icon = Icons.Default.DeleteForever,
                title = "Futa $title",
                subtitle = subtitle,
                iconTint = Color(0xFFD32F2F),
                onClick = onToggleExpanded,
            )
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onConfirmClear,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text("Thibitisha", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SleekOnSurfaceVariant,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        GroupedListColumn {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color = SleekPrimary,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.bouncyClickable { onClick() } else it }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(iconTint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SleekOnSurface)
                Text(subtitle, fontSize = 12.sp, color = SleekOnSurfaceVariant, modifier = Modifier.alpha(0.85f))
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(12.dp))
            trailing()
        }
    }
}
