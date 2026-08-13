package com.biblia.app.data.liturgical

import com.biblia.app.data.BibleRepository
import com.biblia.app.data.BibleVerse

/** Resolves citations from a live LitCalEvent into actual verse text from the Bible database. */
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

        val verses = mutableListOf<BibleVerse>()
        for (span in parsed.spans) {
            if (span.startChapter == span.endChapter) {
                val chapterVerses = repository.getVerses(bookId, span.startChapter)
                verses += chapterVerses.filter {
                    !it.isHeading && it.position in span.startVerse..span.endVerse
                }
            } else {
                for (chapterNum in span.startChapter..span.endChapter) {
                    val chapterVerses = repository.getVerses(bookId, chapterNum)
                    val filtered = when (chapterNum) {
                        span.startChapter -> chapterVerses.filter { !it.isHeading && it.position >= span.startVerse }
                        span.endChapter -> chapterVerses.filter { !it.isHeading && it.position <= span.endVerse }
                        else -> chapterVerses.filter { !it.isHeading }
                    }
                    verses += filtered
                }
            }
        }

        return if (verses.isEmpty()) {
            RenderedReading.Unavailable(label, citation, "Mistari haikupatikana kwa rejea hii.")
        } else {
            RenderedReading.Available(label, citation, verses)
        }
    }

    /** Renders every non-blank citation on a live LitCal event, in liturgical order. */
    suspend fun renderEvent(event: LitCalEvent): List<RenderedReading> =
        event.readings?.asLabeledList()?.map { (label, citation) -> render(label, citation) } ?: emptyList()
}
