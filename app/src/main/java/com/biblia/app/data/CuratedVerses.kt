package com.biblia.app.data

/**
 * A small curated set of well-known, widely quotable verses, used as the Verse of the Day
 * fallback whenever today's liturgical Gospel citation can't be resolved (deuterocanonical
 * gap, a resolver miss, or simply no reading for that date yet). Picked deterministically by
 * day-of-year so it's stable within a day and rotates through the year.
 *
 * Book names match bible_swahili.sqlite's own `title` column exactly (see BookAbbreviations
 * for the same convention on the lectionary side) so the same CitationParser/BibleRepository
 * lookup path works for both sources.
 */
object CuratedVerses {
    data class Ref(val bookTitle: String, val chapter: Int, val verse: Int)

    val all: List<Ref> = listOf(
        Ref("Yohana", 3, 16),
        Ref("Zaburi", 23, 1),
        Ref("Mithali", 3, 5),
        Ref("Isaya", 41, 10),
        Ref("Yeremia", 29, 11),
        Ref("Warumi", 8, 28),
        Ref("Wafilipi", 4, 13),
        Ref("Wafilipi", 4, 6),
        Ref("2 Timotheo", 1, 7),
        Ref("Waebrania", 11, 1),
        Ref("Yakobo", 1, 5),
        Ref("1 Petro", 5, 7),
        Ref("1 Yohana", 4, 19),
        Ref("Mathayo", 6, 33),
        Ref("Mathayo", 11, 28),
        Ref("Zaburi", 46, 1),
        Ref("Zaburi", 118, 24),
        Ref("Waefeso", 2, 8),
        Ref("Yoshua", 1, 9),
        Ref("Wagalatia", 5, 22),
    )

    fun forDayOfYear(dayOfYear: Int): Ref = all[dayOfYear % all.size]
}
