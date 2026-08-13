package com.biblia.app.data.liturgical

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Offline-first wrapper around [LitCalApiClient]: caches each year's raw JSON response to
 * disk so the calendar keeps working without a connection after the first successful fetch.
 * This is the one part of Biblia that needs internet - see README "Liturgical Calendar:
 * online by necessity" for why the readings data can't be meaningfully bundled offline.
 */
class LitCalRepository(context: Context) {
    private val appContext = context.applicationContext
    private val cacheDir: File by lazy { File(appContext.filesDir, "litcal_cache").apply { mkdirs() } }
    private val inMemory = mutableMapOf<Int, List<LitCalEvent>>()

    private fun cacheFile(year: Int, locale: String) = File(cacheDir, "calendar_${year}_$locale.json")

    sealed class YearResult {
        data class Success(val events: List<LitCalEvent>, val fromCache: Boolean) : YearResult()
        data class Failure(val message: String, val staleCache: List<LitCalEvent>?) : YearResult()
    }

    suspend fun getYear(year: Int, locale: String = "en", forceRefresh: Boolean = false): YearResult =
        withContext(Dispatchers.IO) {
            if (!forceRefresh) {
                inMemory[year]?.let { return@withContext YearResult.Success(it, fromCache = true) }
            }
            val file = cacheFile(year, locale)
            if (!forceRefresh && file.exists()) {
                runCatching { LitCalApiClient.parse(file.readText()) }.getOrNull()?.let {
                    inMemory[year] = it
                    return@withContext YearResult.Success(it, fromCache = true)
                }
            }
            runCatching { fetchAndCache(year, locale, file) }
                .fold(
                    onSuccess = { YearResult.Success(it, fromCache = false) },
                    onFailure = { error ->
                        val stale = if (file.exists()) {
                            runCatching { LitCalApiClient.parse(file.readText()) }.getOrNull()
                        } else null
                        YearResult.Failure(error.message ?: "Imeshindwa kupata data", stale)
                    },
                )
        }

    private fun fetchAndCache(year: Int, locale: String, file: File): List<LitCalEvent> {
        val url = URL("https://litcal.johnromanodorazio.com/api/v5/calendar?year=$year&locale=$locale")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 20_000
            setRequestProperty("Accept", "application/json")
        }
        val body = try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("HTTP ${connection.responseCode}")
            }
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
        file.writeText(body)
        val events = LitCalApiClient.parse(body)
        inMemory[year] = events
        return events
    }

    /** Non-vigil events for [date] (there can be more than one - e.g. an optional memorial). */
    suspend fun eventsForDate(date: LocalDate, locale: String = "en"): List<LitCalEvent> {
        val result = getYear(date.year, locale)
        val events = when (result) {
            is YearResult.Success -> result.events
            is YearResult.Failure -> result.staleCache ?: emptyList()
        }
        return events.filter { it.date == date && !it.isVigilMass }
    }

    fun lastSyncedLabel(year: Int, locale: String = "en"): String? {
        val file = cacheFile(year, locale)
        if (!file.exists()) return null
        val instant = Instant.ofEpochMilli(file.lastModified())
        return DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    }
}
