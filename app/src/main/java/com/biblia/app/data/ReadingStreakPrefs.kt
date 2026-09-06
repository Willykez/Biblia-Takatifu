package com.biblia.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val Context.streakDataStore by preferencesDataStore(name = "biblia_reading_streak")

data class ReadingStreak(val currentStreak: Int = 0, val longestStreak: Int = 0)

/**
 * A day "counts" the first time the Reader is opened that day - recordReadToday() is
 * idempotent for repeat opens on the same day. Missing a day resets the current streak to 1
 * (the day just read), not 0, since that day itself is a real read.
 */
class ReadingStreakPrefs(private val context: Context) {
    private object Keys {
        val LAST_READ_DATE = stringPreferencesKey("streak_last_read_date")
        val CURRENT_STREAK = intPreferencesKey("streak_current")
        val LONGEST_STREAK = intPreferencesKey("streak_longest")
    }

    val state: Flow<ReadingStreak> = context.streakDataStore.data.map { p ->
        ReadingStreak(
            currentStreak = p[Keys.CURRENT_STREAK] ?: 0,
            longestStreak = p[Keys.LONGEST_STREAK] ?: 0,
        )
    }

    suspend fun recordReadToday() {
        val today = LocalDate.now()
        context.streakDataStore.edit { p ->
            val lastReadStr = p[Keys.LAST_READ_DATE]
            val lastRead = lastReadStr?.let { runCatching { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull() }
            if (lastRead == today) return@edit // already recorded today, nothing to do

            val current = p[Keys.CURRENT_STREAK] ?: 0
            val newCurrent = when (lastRead) {
                today.minusDays(1) -> current + 1
                else -> 1
            }
            p[Keys.CURRENT_STREAK] = newCurrent
            p[Keys.LONGEST_STREAK] = maxOf(p[Keys.LONGEST_STREAK] ?: 0, newCurrent)
            p[Keys.LAST_READ_DATE] = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        }
    }
}
