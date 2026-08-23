package com.biblia.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.recentSearchesDataStore by preferencesDataStore(name = "biblia_recent_searches")
private const val MAX_RECENT = 8

class RecentSearchesPrefs(private val context: Context) {
    // Stored as "index:query" so a Set (unordered by nature) can still be read back in
    // most-recent-first order.
    private val key = stringSetPreferencesKey("recent_searches")

    val recentSearches: Flow<List<String>> = context.recentSearchesDataStore.data.map { p ->
        (p[key] ?: emptySet())
            .mapNotNull { entry ->
                val idx = entry.substringBefore(':', "").toIntOrNull() ?: return@mapNotNull null
                idx to entry.substringAfter(':')
            }
            .sortedBy { it.first }
            .map { it.second }
    }

    suspend fun addSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        context.recentSearchesDataStore.edit { p ->
            val existing = (p[key] ?: emptySet())
                .mapNotNull { entry ->
                    val idx = entry.substringBefore(':', "").toIntOrNull() ?: return@mapNotNull null
                    idx to entry.substringAfter(':')
                }
                .sortedBy { it.first }
                .map { it.second }
                .filterNot { it.equals(trimmed, ignoreCase = true) }
            val updated = (listOf(trimmed) + existing).take(MAX_RECENT)
            p[key] = updated.mapIndexed { index, q -> "$index:$q" }.toSet()
        }
    }

    suspend fun clear() {
        context.recentSearchesDataStore.edit { it[key] = emptySet() }
    }
}
