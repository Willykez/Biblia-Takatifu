package com.biblia.app.ui

import com.biblia.app.data.BibleVerse
import com.biblia.app.data.CuratedVerses
import java.time.LocalDate

data class VerseOfDay(
    val bookId: Int,
    val bookTitle: String,
    val chapterNum: Int,
    val verse: BibleVerse,
)

/** Picked deterministically by day-of-year, so it's stable within a day and rotates through the year. */
suspend fun loadVerseOfDay(bibleViewModel: BibleViewModel): VerseOfDay? {
    val curated = CuratedVerses.forDayOfYear(LocalDate.now().dayOfYear)
    val bookId = bibleViewModel.resolveBookId(curated.bookTitle) ?: return null
    val book = bibleViewModel.getBook(bookId) ?: return null
    val verse = bibleViewModel.getVerses(bookId, curated.chapter)
        .firstOrNull { !it.isHeading && it.position == curated.verse } ?: return null
    return VerseOfDay(bookId, book.title, curated.chapter, verse)
}
