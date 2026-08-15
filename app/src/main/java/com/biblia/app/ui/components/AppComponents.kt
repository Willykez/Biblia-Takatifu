package com.biblia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Flat top bar: title (+ optional subtitle), an optional back arrow, an optional single
 * trailing action. No elevation, no background tint - just content and a hairline rule
 * beneath it, consistent with every other surface boundary in this design.
 */
@Composable
fun AppTopBar(
    title: String,
    subtitle: String? = null,
    showBack: Boolean = false,
    onBack: (() -> Unit)? = null,
    trailingIcon: ImageVector? = null,
    trailingDescription: String? = null,
    onTrailingClick: (() -> Unit)? = null,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showBack) {
                IconButton(onClick = { onBack?.invoke() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Nyuma")
                }
            } else {
                Spacer(modifier = Modifier.size(12.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (trailingIcon != null) {
                IconButton(onClick = { onTrailingClick?.invoke() }) {
                    Icon(trailingIcon, contentDescription = trailingDescription)
                }
            } else {
                Spacer(modifier = Modifier.size(12.dp))
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
    }
}

private data class NavItem(val route: String, val label: String, val outlined: ImageVector, val filled: ImageVector)

private val navItems = listOf(
    NavItem("home", "Biblia", Icons.Outlined.MenuBook, Icons.Filled.MenuBook),
    NavItem("search", "Tafuta", Icons.Outlined.Search, Icons.Filled.Search),
    NavItem("saved", "Yaliyohifadhiwa", Icons.Outlined.Bookmark, Icons.Filled.Bookmark),
    NavItem("settings", "Mipangilio", Icons.Outlined.Settings, Icons.Filled.Settings),
)

/** Plain text-and-icon tabs with a top hairline rule - no pill, no elevation, no motion. */
@Composable
fun AppBottomNav(currentRoute: String, onNavigate: (String) -> Unit) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            navItems.forEach { item ->
                val selected = item.route == currentRoute
                val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onNavigate(item.route) }.padding(horizontal = 12.dp),
                ) {
                    Icon(if (selected) item.filled else item.outlined, contentDescription = item.label, tint = tint, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(item.label, style = MaterialTheme.typography.labelSmall, color = tint)
                }
            }
        }
    }
}

/** A plain row with a hairline divider beneath it - the one repeating list pattern in the app. */
@Composable
fun DividedRow(
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        content()
        if (showDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        }
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp),
    )
}
