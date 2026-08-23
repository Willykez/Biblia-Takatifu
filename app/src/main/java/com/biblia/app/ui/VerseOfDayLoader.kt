package com.biblia.app.ui

import com.biblia.app.data.BibleVerse
import com.biblia.app.data.CuratedVerses
import com.biblia.app.data.liturgical.BookAbbreviations
import com.biblia.app.data.liturgical.CitationParser
import java.time.LocalDate

data class VerseOfDay(
    val bookId: Int,
    val bookTitle: String,
    val chapterNum: Int,
    val verse: BibleVerse,
    /** true if this came from today's actual liturgical Gospel; false if it's the curated fallback. */
    val isFromTodaysGospel: Boolean,
)

/**
 * Tries today's liturgical Gospel citation first (ties the Home screen back to the calendar
 * work), falling back to a curated well-known verse - picked deterministically by day-of-year
 * - whenever the Gospel citation is missing, unresolvable (deuterocanonical gap doesn't apply
 * to Gospels, but a resolver miss or blank citation can still happen), or simply not yet
 * covered by the bundled lectionary data for that date.
 */
suspend fun loadVerseOfDay(bibleViewModel: BibleViewModel, liturgicalViewModel: LiturgicalViewModel): VerseOfDay? {
    val today = LocalDate.now()

    val fromGospel = runCatching {
        val resolved = liturgicalViewModel.resolveDay(today)
        val citation = resolved.readings?.injili?.takeIf { it.isNotBlank() } ?: return@runCatching null
        val parsed = CitationParser.parse(citation)
        val bookId = parsed.bookId ?: return@runCatching null
        val span = parsed.spans.firstOrNull() ?: return@runCatching null
        val book = bibleViewModel.getBook(bookId) ?: return@runCatching null
        val verse = bibleViewModel.getVerses(bookId, span.startChapter)
            .firstOrNull { !it.isHeading && it.position == span.startVerse } ?: return@runCatching null
        VerseOfDay(bookId, book.title, span.startChapter, verse, isFromTodaysGospel = true)
    }.getOrNull()
    if (fromGospel != null) return fromGospel

    val curated = CuratedVerses.forDayOfYear(today.dayOfYear)
    val bookId = BookAbbreviations.resolveId(curated.bookTitle) ?: return null
    val book = bibleViewModel.getBook(bookId) ?: return null
    val verse = bibleViewModel.getVerses(bookId, curated.chapter)
        .firstOrNull { !it.isHeading && it.position == curated.verse } ?: return null
    return VerseOfDay(bookId, book.title, curated.chapter, verse, isFromTodaysGospel = false)
}
