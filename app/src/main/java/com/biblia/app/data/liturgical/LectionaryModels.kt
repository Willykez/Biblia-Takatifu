package com.biblia.app.data.liturgical

import com.biblia.app.data.BibleVerse

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

/** One calendar day's full liturgical picture: which celebration(s), and their readings. */
data class RenderedDay(
    val date: java.time.LocalDate,
    val events: List<LitCalEvent>,
    val readingsByEvent: Map<String, List<RenderedReading>>, // keyed by LitCalEvent.eventKey
)
