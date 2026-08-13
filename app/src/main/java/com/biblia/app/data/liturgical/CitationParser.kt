package com.biblia.app.data.liturgical

/** A contiguous verse range, possibly spanning two chapters (e.g. Matt 9:36-10:8). */
data class VerseSpan(
    val startChapter: Int,
    val startVerse: Int,
    val endChapter: Int,
    val endVerse: Int,
)

data class ParsedCitation(
    val raw: String,
    val bookAbbreviation: String,
    val bookId: Int?, // null if the book isn't in this Bible (see BookAbbreviations doc)
    val spans: List<VerseSpan>,
)

/**
 * Parses citations in the shape the Lectionary tables actually use, e.g.:
 *   "Isa 42:1-4, 6-7"            same-chapter, comma-separated ranges/singles
 *   "1 Kgs 19:9a, 11-13a"        multi-word book name, trailing sub-verse letters (a/b/c)
 *   "Ps 67:2-3, 5, 6+8"          "+" is another gap-separated group, same as ","
 *   "Matt 9:36\u201410:8"         em dash = continuous range crossing a chapter boundary
 *   "Matt 22:1-14 or 22:1-10"    alternate/shorter form after " or " - we take the first
 * Sub-verse letters (9a, 13a) are stripped for lookup purposes - this DB has no sub-verse
 * granularity, so "13a" resolves to verse 13 in full.
 */
object CitationParser {

    fun parse(citation: String): ParsedCitation {
        val primary = citation.substringBefore(" or ").trim()
            .removePrefix("Cf. ").removePrefix("cf. ").removePrefix("Cf.").trim()
        val (abbrev, rest) = splitBook(primary)
        val bookId = abbrev?.let { BookAbbreviations.abbreviationToBookId[it] }
        val spans = if (rest == null) emptyList() else parseSpans(rest)
        return ParsedCitation(raw = citation, bookAbbreviation = abbrev ?: "", bookId = bookId, spans = spans)
    }

    private fun splitBook(text: String): Pair<String?, String?> {
        for (abbrev in BookAbbreviations.sortedAbbreviations) {
            if (text.startsWith(abbrev) && text.length > abbrev.length && text[abbrev.length] == ' ') {
                return abbrev to text.substring(abbrev.length).trim()
            }
        }
        return null to null
    }

    private fun parseSpans(rest: String): List<VerseSpan> {
        val emDash = rest.indexOf('\u2014')
        if (emDash >= 0) {
            val left = rest.substring(0, emDash).trim()
            val right = rest.substring(emDash + 1).trim()
            val (c1, v1) = parseChapterVerse(left) ?: return emptyList()
            val (c2, v2) = parseChapterVerse(right) ?: return emptyList()
            return listOf(VerseSpan(c1, v1, c2, v2))
        }

        if (rest.contains(';')) {
            return rest.split(';').flatMap { parseSpans(it.trim()) }
        }

        val colonIndex = rest.indexOf(':')
        if (colonIndex < 0) return emptyList()
        val chapter = rest.substring(0, colonIndex).trim().toIntOrNull() ?: return emptyList()
        val verseSpecs = rest.substring(colonIndex + 1)

        return verseSpecs.split(',', '+').mapNotNull { token ->
            val t = token.trim()
            if (t.isEmpty()) return@mapNotNull null
            if (t.contains(':')) {
                // A token can itself carry a new chapter (plain-hyphen cross-chapter span,
                // e.g. "36-10:8" meaning verse 36 of the outer chapter through 10:8).
                val dashIdx = t.indexOf('-')
                if (dashIdx < 0) return@mapNotNull null
                val startVerse = stripLetterSuffix(t.substring(0, dashIdx)) ?: return@mapNotNull null
                val rightParts = t.substring(dashIdx + 1).split(':', limit = 2)
                if (rightParts.size < 2) return@mapNotNull null
                val endChapter = rightParts[0].trim().toIntOrNull() ?: chapter
                val endVerse = stripLetterSuffix(rightParts[1]) ?: return@mapNotNull null
                VerseSpan(chapter, startVerse, endChapter, endVerse)
            } else if (t.contains('-')) {
                val parts = t.split('-', limit = 2)
                val start = stripLetterSuffix(parts[0]) ?: return@mapNotNull null
                val end = stripLetterSuffix(parts[1]) ?: return@mapNotNull null
                VerseSpan(chapter, start, chapter, end)
            } else {
                val v = stripLetterSuffix(t) ?: return@mapNotNull null
                VerseSpan(chapter, v, chapter, v)
            }
        }
    }

    private fun parseChapterVerse(text: String): Pair<Int, Int>? {
        val idx = text.indexOf(':')
        if (idx < 0) return null
        val chapter = text.substring(0, idx).trim().toIntOrNull() ?: return null
        val verse = stripLetterSuffix(text.substring(idx + 1).trim()) ?: return null
        return chapter to verse
    }

    private fun stripLetterSuffix(token: String): Int? =
        token.trim().takeWhile { it.isDigit() }.toIntOrNull()
}
