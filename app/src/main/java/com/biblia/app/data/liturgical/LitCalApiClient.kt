package com.biblia.app.data.liturgical

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.OffsetDateTime

private const val TAG = "LitCalApiClient"

/**
 * Talks to the real, public LiturgicalCalendarAPI (https://litcal.johnromanodorazio.com).
 *
 * Endpoint/param names here follow the sample response you provided and the project's
 * public docs at https://litcal.johnromanodorazio.com/dist/ (Swagger UI) - I could not
 * execute a live request against it from this build environment (no network egress here,
 * and web_fetch is restricted to URLs that surfaced in a prior search/fetch result), so
 * this integration is built directly from your real sample JSON rather than a guess, but
 * it hasn't been round-tripped against the live server yet. If `year` turns out to need to
 * be a path segment instead of a query param, or the base path differs, that's a one-line
 * fix in [buildUrl] - flagging this openly rather than pretending certainty.
 */
object LitCalApiClient {
    private const val BASE_URL = "https://litcal.johnromanodorazio.com/api/v5/calendar"

    private fun buildUrl(year: Int, locale: String): String =
        "$BASE_URL?year=$year&locale=$locale"

    /** Throws on any network/parse failure - caller (LitCalRepository) decides the fallback. */
    fun fetchYear(year: Int, locale: String = "en"): List<LitCalEvent> {
        val url = URL(buildUrl(year, locale))
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 20_000
            setRequestProperty("Accept", "application/json")
        }
        try {
            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("LitCal API returned HTTP $code")
            }
            val body = BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8)).use { it.readText() }
            return parse(body)
        } finally {
            connection.disconnect()
        }
    }

    fun parse(body: String): List<LitCalEvent> {
        val root = JSONObject(body)
        val array: JSONArray = root.getJSONArray("litcal")
        val events = mutableListOf<LitCalEvent>()
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            runCatching { parseEvent(obj) }
                .onSuccess { events += it }
                .onFailure { Log.w(TAG, "Skipping malformed event at index $i: ${it.message}") }
        }
        return events
    }

    private fun parseEvent(obj: JSONObject): LitCalEvent {
        val readingsObj = obj.optJSONObject("readings")
        val readings = readingsObj?.let {
            LitCalReadings(
                firstReading = it.optStringOrNull("first_reading"),
                responsorialPsalm = it.optStringOrNull("responsorial_psalm"),
                secondReading = it.optStringOrNull("second_reading"),
                gospelAcclamation = it.optStringOrNull("gospel_acclamation"),
                gospel = it.optStringOrNull("gospel"),
            )
        }
        val colorArray = obj.optJSONArray("color_lcl") ?: obj.optJSONArray("color")
        val colors = buildList {
            if (colorArray != null) for (i in 0 until colorArray.length()) add(colorArray.optString(i))
        }
        return LitCalEvent(
            eventKey = obj.getString("event_key"),
            name = obj.optString("name", obj.getString("event_key")),
            grade = obj.optInt("grade", 0),
            gradeLcl = obj.optString("grade_lcl", ""),
            colorLcl = colors,
            date = parseDate(obj.getString("date")),
            liturgicalYear = obj.optStringOrNull("liturgical_year"),
            liturgicalSeasonLcl = obj.optStringOrNull("liturgical_season_lcl"),
            isVigilMass = obj.optBoolean("is_vigil_mass", false),
            readings = readings,
        )
    }

    private fun parseDate(iso: String): LocalDate = OffsetDateTime.parse(iso).toLocalDate()

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (has(key) && !isNull(key)) getString(key) else null
}
