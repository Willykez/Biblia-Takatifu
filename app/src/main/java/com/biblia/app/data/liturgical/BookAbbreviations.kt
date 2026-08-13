package com.biblia.app.data.liturgical

/**
 * Resolves the book names LiturgicalCalendarAPI's `readings` citations actually use - full
 * English names ("Isaiah", "Matthew", "1 Corinthians", "Psalm") per the live schema sample -
 * to a row in the `chapters` table, keyed by Swahili book id. Also keeps the short
 * abbreviation forms (Isa, Matt, 1 Cor) since some lectionary sources use those instead.
 * Ids are built from the DB's own book ordering (Mwanzo=8022 through Ufunuo wa Yohana=8087).
 *
 * IMPORTANT GAP: bible_swahili.sqlite is a 66-book Protestant-canon Bible. The Catholic
 * Lectionary regularly draws First Readings from the deuterocanonical books - Wisdom,
 * Sirach, Baruch, Tobit, Judith, 1-2 Maccabees - which this database does not contain at
 * all. There is no book-id to map those citations to. ReadingRenderer.kt handles this by
 * returning an "unavailable in this translation" result instead of failing silently - but
 * the real fix is sourcing a deuterocanonical text (a different Swahili Catholic Bible
 * edition, e.g. Neno: Bibilia Habari Njema/UBS Common Language, with the Vitabu vya
 * Deuterokanoni) to merge in. Flagging this rather than guessing - needs your decision.
 */
object BookAbbreviations {
    private val ids = mapOf(
        "Genesis" to 8022, "Exodus" to 8023, "Leviticus" to 8024, "Numbers" to 8025,
        "Deuteronomy" to 8026, "Joshua" to 8027, "Judges" to 8028, "Ruth" to 8029,
        "1 Samuel" to 8030, "2 Samuel" to 8031, "1 Kings" to 8032, "2 Kings" to 8033,
        "1 Chronicles" to 8034, "2 Chronicles" to 8035, "Ezra" to 8036, "Nehemiah" to 8037,
        "Esther" to 8038, "Job" to 8039, "Psalm" to 8040, "Psalms" to 8040, "Proverbs" to 8041,
        "Ecclesiastes" to 8042, "Song of Songs" to 8043, "Song of Solomon" to 8043,
        "Isaiah" to 8044, "Jeremiah" to 8045, "Lamentations" to 8046, "Ezekiel" to 8047,
        "Daniel" to 8048, "Hosea" to 8049, "Joel" to 8050, "Amos" to 8051, "Obadiah" to 8052,
        "Jonah" to 8053, "Micah" to 8054, "Nahum" to 8055, "Habakkuk" to 8056,
        "Zephaniah" to 8057, "Haggai" to 8058, "Zechariah" to 8059, "Malachi" to 8060,
        // Deuterocanonical (Wisdom, Sirach/Ecclesiasticus, Baruch, Tobit, Judith, 1-2
        // Maccabees) intentionally NOT mapped - not present in this Bible. See doc above.
        "Matthew" to 8061, "Mark" to 8062, "Luke" to 8063, "John" to 8064, "Acts" to 8065,
        "Romans" to 8066, "1 Corinthians" to 8067, "2 Corinthians" to 8068,
        "Galatians" to 8069, "Ephesians" to 8070, "Philippians" to 8071, "Colossians" to 8072,
        "1 Thessalonians" to 8073, "2 Thessalonians" to 8074, "1 Timothy" to 8075,
        "2 Timothy" to 8076, "Titus" to 8077, "Philemon" to 8078, "Hebrews" to 8079,
        "James" to 8080, "1 Peter" to 8081, "2 Peter" to 8082, "1 John" to 8083,
        "2 John" to 8084, "3 John" to 8085, "Jude" to 8086, "Revelation" to 8087,
        // Short abbreviation forms, kept for any non-API source that uses them.
        "Gen" to 8022, "Exod" to 8023, "Lev" to 8024, "Num" to 8025, "Deut" to 8026,
        "Josh" to 8027, "Judg" to 8028, "1 Sam" to 8030, "2 Sam" to 8031, "1 Kgs" to 8032,
        "2 Kgs" to 8033, "1 Chr" to 8034, "2 Chr" to 8035, "Neh" to 8037, "Esth" to 8038,
        "Ps" to 8040, "Pss" to 8040, "Prov" to 8041, "Eccl" to 8042, "Song" to 8043,
        "Isa" to 8044, "Jer" to 8045, "Lam" to 8046, "Ezek" to 8047, "Dan" to 8048,
        "Hos" to 8049, "Obad" to 8052, "Mic" to 8054, "Nah" to 8055, "Hab" to 8056,
        "Zeph" to 8057, "Hag" to 8058, "Zech" to 8059, "Mal" to 8060, "Matt" to 8061,
        "Rom" to 8066, "1 Cor" to 8067, "2 Cor" to 8068, "Gal" to 8069, "Eph" to 8070,
        "Phil" to 8071, "Col" to 8072, "1 Thess" to 8073, "2 Thess" to 8074, "1 Tim" to 8075,
        "2 Tim" to 8076, "Phlm" to 8078, "Heb" to 8079, "Jas" to 8080, "1 Pet" to 8081,
        "2 Pet" to 8082,
    )

    val abbreviationToBookId: Map<String, Int> = ids

    /** Longest-name-first so "1 Corinthians"/"Song of Songs" match before a shorter prefix would. */
    val sortedAbbreviations: List<String> = ids.keys.sortedByDescending { it.length }
}
