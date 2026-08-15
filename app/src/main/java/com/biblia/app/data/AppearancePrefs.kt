package com.biblia.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.biblia.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appearancePrefsDataStore by preferencesDataStore(name = "biblia_appearance_prefs")

/** The whole of "Appearance" in this editorial design: which of the two flat schemes to use. */
class AppearancePrefs(private val context: Context) {
    private val key = stringPreferencesKey("theme_mode")

    val themeMode: Flow<ThemeMode> = context.appearancePrefsDataStore.data.map { p ->
        runCatching { ThemeMode.valueOf(p[key] ?: "") }.getOrDefault(ThemeMode.SYSTEM)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.appearancePrefsDataStore.edit { it[key] = mode.name }
    }
}
