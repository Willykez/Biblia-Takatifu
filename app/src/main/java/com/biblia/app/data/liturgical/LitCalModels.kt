package com.biblia.app.data.liturgical

import java.time.LocalDate

/**
 * Mirrors the actual LiturgicalCalendarAPI v5 `/calendar` response schema, confirmed against
 * a live sample (event_key, grade, color, readings, liturgical_year, etc.) - field names
 * here are not guessed. https://litcal.johnromanodorazio.com (Apache-2.0, John R. D'Orazio).
 */
data class LitCalReadings(
    val firstReading: String?,
    val responsorialPsalm: String?,
    val secondReading: String?,
    val gospelAcclamation: String?,
    val gospel: String?,
) {
    /** In display order, skipping anything the API left blank (common on ferial weekdays). */
    fun asLabeledList(): List<Pair<ReadingLabel, String>> = listOfNotNull(
        firstReading?.takeIf { it.isNotBlank() }?.let { ReadingLabel.FIRST_READING to it },
        responsorialPsalm?.takeIf { it.isNotBlank() }?.let { ReadingLabel.PSALM to it },
        secondReading?.takeIf { it.isNotBlank() }?.let { ReadingLabel.SECOND_READING to it },
        gospel?.takeIf { it.isNotBlank() }?.let { ReadingLabel.GOSPEL to it },
    )
}

data class LitCalEvent(
    val eventKey: String,
    val name: String,
    val grade: Int,
    val gradeLcl: String,
    val colorLcl: List<String>,
    val date: LocalDate,
    val liturgicalYear: String?, // "YEAR A" - present on Sundays/major days, absent on weekdays
    val liturgicalSeasonLcl: String?,
    val isVigilMass: Boolean,
    val readings: LitCalReadings?,
)
