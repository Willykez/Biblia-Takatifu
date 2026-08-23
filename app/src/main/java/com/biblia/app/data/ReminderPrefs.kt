package com.biblia.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.reminderPrefsDataStore by preferencesDataStore(name = "biblia_reminder_prefs")

data class ReminderState(val enabled: Boolean = false, val hour: Int = 7, val minute: Int = 0)

class ReminderPrefs(private val context: Context) {
    private object Keys {
        val ENABLED = booleanPreferencesKey("reminder_enabled")
        val HOUR = intPreferencesKey("reminder_hour")
        val MINUTE = intPreferencesKey("reminder_minute")
    }

    val state: Flow<ReminderState> = context.reminderPrefsDataStore.data.map { p ->
        ReminderState(
            enabled = p[Keys.ENABLED] ?: false,
            hour = p[Keys.HOUR] ?: 7,
            minute = p[Keys.MINUTE] ?: 0,
        )
    }

    suspend fun setEnabled(enabled: Boolean) {
        context.reminderPrefsDataStore.edit { it[Keys.ENABLED] = enabled }
    }

    suspend fun setTime(hour: Int, minute: Int) {
        context.reminderPrefsDataStore.edit {
            it[Keys.HOUR] = hour
            it[Keys.MINUTE] = minute
        }
    }
}
