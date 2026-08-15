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
 *   "Isaya 42:1-4, 6-7"                same-chapter, comma-separated ranges/singles
 *   "1 Wafalme 19:9a, 11-13a"          multi-word book name, trailing sub-verse letters (a/b/c)
 *   "Zaburi 67:2-3, 5, 6+8"            "+" is another gap-separated group, same as ","
 *   "Isaya 63:16b-17, 19b; 64:2b-7"    ";" starts a new chapter group entirely
 *   "Mathayo 9:35\u201410:1"            em dash = continuous range crossing a chapter boundary
 *   "Mathayo 9:35-10:1, 5a, 6-8"       plain-hyphen cross-chapter also happens; the chapter
 *                                       it crosses into carries forward to later bare tokens
 *   "Isaya 2:1-5 (au Mwaka A: Isaya 4:2-6)"   alternate reading in parens - we take the first
 * Sub-verse letters (9a, 13a) are stripped for lookup purposes - this DB has no sub-verse
 * granularity, so "13a" resolves to verse 13 in full.
 */
object CitationParser {

    fun parse(citation: String): ParsedCitation {
        val primary = citation.substringBefore('(').trim()
            .removePrefix("Cf. ").removePrefix("cf. ").removePrefix("kama katika ").trim()
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
        val firstChapter = rest.substring(0, colonIndex).trim().toIntOrNull() ?: return emptyList()
        val verseSpecs = rest.substring(colonIndex + 1)

        // Stateful: a token that crosses into a new chapter (e.g. "35-10:1") updates the
        // chapter every *subsequent* bare-verse token uses too, e.g. in
        // "9:35-10:1, 5a, 6-8" the "5a, 6-8" belong to chapter 10, not the outer chapter 9.
        var currentChapter = firstChapter
        val spans = mutableListOf<VerseSpan>()
        for (rawToken in verseSpecs.split(',', '+')) {
            val t = rawToken.trim()
            if (t.isEmpty()) continue
            if (t.contains(':')) {
                val dashIdx = t.indexOf('-')
                if (dashIdx < 0) continue
                val startVerse = stripLetterSuffix(t.substring(0, dashIdx)) ?: continue
                val rightParts = t.substring(dashIdx + 1).split(':', limit = 2)
                if (rightParts.size < 2) continue
                val endChapter = rightParts[0].trim().toIntOrNull() ?: currentChapter
                val endVerse = stripLetterSuffix(rightParts[1]) ?: continue
                spans += VerseSpan(currentChapter, startVerse, endChapter, endVerse)
                currentChapter = endChapter
            } else if (t.contains('-')) {
                val parts = t.split('-', limit = 2)
                val start = stripLetterSuffix(parts[0]) ?: continue
                val end = stripLetterSuffix(parts[1]) ?: continue
                spans += VerseSpan(currentChapter, start, currentChapter, end)
            } else {
                val v = stripLetterSuffix(t) ?: continue
                spans += VerseSpan(currentChapter, v, currentChapter, v)
            }
        }
        return spans
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
