package com.biblia.app.data.liturgical

import com.biblia.app.data.BibleVerse
import java.time.LocalDate

/** One row from `readings`: the five (mostly-nullable) citation strings for a Mass. */
data class ReadingSet(
    val somoLaKwanza: String?,
    val wimboLaKatikati: String?,
    val somoLaPili: String?,
    val shangilio: String?,
    val injili: String?,
    val mwakaLiturujia: String?,
)

/** One row from `watakatifu`. */
data class SaintEntry(
    val tarehe: String, // "Januari 2" style, matched against the resolved date
    val jina: String,
    val daraja: String, // "Sikukuu" | "Kumbukumbu" | "Kumbukumbu ya Hiari" | "Sikukuu Kuu"
    val rangi: String,
)

/**
 * What LiturgicalResolver.resolve(date) produces: which readings key applies, plus enough
 * display context (title, color, any saint of the day) for the UI - see APP_LOGIC.md \u00a72.
 */
data class ResolvedDay(
    val date: LocalDate,
    val season: String,
    val periodKey: String,
    val entryKey: String?,
    val day: String?,
    val sundayCycle: String?,
    val weekdayCycle: String?,
    val title: String,
    val rangi: String?,
    val saintOfTheDay: SaintEntry?, // shown informationally even when it didn't win precedence
    val usedFixedSolemnity: Boolean,
    val readings: ReadingSet?,
)

enum class ReadingLabel { FIRST_READING, PSALM, SECOND_READING, GOSPEL }

/** A single passage, rendered from a citation against the actual Bible database. */
sealed class RenderedReading {
    abstract val label: ReadingLabel
    abstract val citation: String

    data class Available(
        override val label: ReadingLabel,
        override val citation: String,
        val verses: List<BibleVerse>,
    ) : RenderedReading()

    /** Book not in this Bible (deuterocanonical) or citation didn't resolve to any rows. */
    data class Unavailable(
        override val label: ReadingLabel,
        override val citation: String,
        val reason: String,
    ) : RenderedReading()
}

/** A resolved day plus its readings rendered into actual verse text. */
data class RenderedDay(
    val resolvedDay: ResolvedDay,
    val renderedReadings: List<RenderedReading>,
)
