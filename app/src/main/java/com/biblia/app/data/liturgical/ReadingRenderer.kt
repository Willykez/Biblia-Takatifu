package com.biblia.app.data.liturgical

import com.biblia.app.data.BibleRepository
import com.biblia.app.data.BibleVerse

/** Resolves Swahili lectionary citations into actual verse text from the Bible database. */
class ReadingRenderer(private val repository: BibleRepository) {

    suspend fun render(label: ReadingLabel, citation: String): RenderedReading {
        val parsed = CitationParser.parse(citation)
        val bookId = parsed.bookId
            ?: return RenderedReading.Unavailable(
                label, citation,
                "Kitabu hiki hakipo katika tafsiri hii ya Biblia (deuterokanoni).",
            )
        if (parsed.spans.isEmpty()) {
            return RenderedReading.Unavailable(label, citation, "Imeshindwa kusoma rejea hii.")
        }

        val verses = mutableListOf<DisplayVerse>()
        for (span in parsed.spans) {
            if (span.startChapter == span.endChapter) {
                val chapterVerses = repository.getVerses(bookId, span.startChapter)
                verses += chapterVerses
                    .filter { !it.isHeading && it.position in span.startVerse..span.endVerse }
                    .map { it.withPartialLabel(span, it.position) }
            } else {
                for (chapterNum in span.startChapter..span.endChapter) {
                    val chapterVerses = repository.getVerses(bookId, chapterNum)
                    val filtered = when (chapterNum) {
                        span.startChapter -> chapterVerses.filter { !it.isHeading && it.position >= span.startVerse }
                        span.endChapter -> chapterVerses.filter { !it.isHeading && it.position <= span.endVerse }
                        else -> chapterVerses.filter { !it.isHeading }
                    }
                    verses += filtered.map { it.withPartialLabel(span, it.position, chapterNum) }
                }
            }
        }

        return if (verses.isEmpty()) {
            RenderedReading.Unavailable(label, citation, "Mistari haikupatikana kwa rejea hii.")
        } else {
            RenderedReading.Available(label, citation, verses)
        }
    }

    /**
     * "6a"/"10ab" style label for a verse at the edge of a partial-verse span, otherwise just
     * the plain verse number. Interior verses of a range are never lettered - see VerseSpan.
     */
    private fun BibleVerse.withPartialLabel(span: VerseSpan, verse: Int, chapter: Int = span.startChapter): DisplayVerse {
        val letter = when {
            chapter == span.startChapter && verse == span.startVerse -> span.startLetter
            chapter == span.endChapter && verse == span.endVerse -> span.endLetter
            else -> null
        }
        return DisplayVerse(this, "$verse${letter ?: ""}")
    }

    /** Renders every non-blank citation in a resolved day's reading set, in Mass order. */
    suspend fun renderReadingSet(readings: ReadingSet?): List<RenderedReading> {
        if (readings == null) return emptyList()
        val labeled = listOfNotNull(
            readings.somoLaKwanza?.takeIf { it.isNotBlank() }?.let { ReadingLabel.FIRST_READING to it },
            readings.wimboLaKatikati?.takeIf { it.isNotBlank() }?.let { ReadingLabel.PSALM to it },
            readings.somoLaPili?.takeIf { it.isNotBlank() }?.let { ReadingLabel.SECOND_READING to it },
            readings.injili?.takeIf { it.isNotBlank() }?.let { ReadingLabel.GOSPEL to it },
        )
        return labeled.map { (label, citation) -> render(label, citation) }
    }
}
