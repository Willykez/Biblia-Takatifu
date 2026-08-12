package com.biblia.app.data

/** A book of the Bible (row from the `chapters` table — the table's naming, not this class's). */
data class BibleBook(
    val id: Int,
    val title: String,
    val shortTitle: String,
    val numChapters: Int,
    /** 1 = Old Testament (Agano la Kale), 2 = New Testament (Agano Jipya). */
    val mode: Int,
)

/**
 * One row from `texts`: either a normal verse (head == 0) or a section heading
 * (head == 1, position == 0, no verse number shown).
 *
 * [text] is raw bilingual HTML: Swahili followed by `<br/><i>English</i>`. [primaryText]
 * and [secondaryText] split that pair apart for display.
 */
data class BibleVerse(
    val id: Int,
    val bookId: Int,
    val chapterNum: Int,
    val position: Int,
    val rank: Int,
    val text: String,
    val head: Boolean,
    val bookmark: Boolean,
    val highlight: Int,
    val note: String?,
    val bookmarkDate: Long,
    val highlightDate: Long,
    val noteDate: Long,
) {
    val isHeading: Boolean get() = head

    val primaryText: String
        get() = text.substringBefore("<br/>").stripHtml()

    val secondaryText: String?
        get() = if (text.contains("<br/>")) text.substringAfter("<br/>").stripHtml() else null
}

private fun String.stripHtml(): String =
    replace(Regex("<[^>]*>"), "").trim()

/** Highlight color slots. Index 0 = none. */
enum class HighlightColor(val index: Int, val label: String) {
    NONE(0, "Hakuna"),
    YELLOW(1, "Njano"),
    GREEN(2, "Kijani"),
    BLUE(3, "Bluu"),
    PINK(4, "Waridi"),
    ORANGE(5, "Chungwa");

    companion object {
        fun fromIndex(index: Int): HighlightColor = entries.firstOrNull { it.index == index } ?: NONE
    }
}

/** Snapshot of the user's saved-content counts, shown in Settings. */
data class BibleDataCounts(
    val bookmarks: Int = 0,
    val highlights: Int = 0,
    val notes: Int = 0,
)
