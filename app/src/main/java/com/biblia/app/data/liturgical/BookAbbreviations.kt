package com.biblia.app.data.liturgical

/**
 * Resolves the Swahili book names your lectionary dataset actually uses (confirmed by
 * scanning every citation string in lectionary_swahili.json - not guessed) to a row in the
 * `chapters` table. These names match bible_swahili.sqlite's own `title` column exactly,
 * since both datasets use the same Union Version Swahili book names - plus a couple of short
 * forms ("Ufunuo" for the DB's "Ufunuo wa Yohana") the lectionary uses that the DB doesn't.
 * Ids are the DB's own book ordering (Mwanzo=8022 through Ufunuo wa Yohana=8087).
 *
 * IMPORTANT GAP: bible_swahili.sqlite is a 66-book Protestant-canon Bible. Your lectionary
 * cites the deuterocanonical books for several First Readings - Hekima (Wisdom), Sira
 * (Sirach), Baruku (Baruch), Tobiti (Tobit), and 1-2 Wamakabayo (Maccabees) all appear in
 * lectionary_swahili.json - and none of them exist in this database. There's no book id to
 * map those to. ReadingRenderer.kt surfaces this as an "unavailable in this translation"
 * result rather than failing silently or guessing - the real fix is sourcing a Swahili
 * Catholic Bible edition with the deuterocanonical books and merging it in.
 */
object BookAbbreviations {
    private val ids = mapOf(
        "Mwanzo" to 8022, "Kutoka" to 8023, "Mambo ya Walawi" to 8024, "Hesabu" to 8025,
        "Kumbukumbu la Torati" to 8026, "Yoshua" to 8027, "Waamuzi" to 8028, "Ruthu" to 8029,
        "1 Samweli" to 8030, "2 Samweli" to 8031, "1 Wafalme" to 8032, "2 Wafalme" to 8033,
        "1 Mambo ya Nyakati" to 8034, "2 Mambo ya Nyakati" to 8035, "Ezra" to 8036, "Nehemia" to 8037,
        "Esta" to 8038, "Ayubu" to 8039, "Zaburi" to 8040, "Mithali" to 8041, "Mhubiri" to 8042,
        "Wimbo Ulio Bora" to 8043, "Isaya" to 8044, "Yeremia" to 8045, "Maombolezo" to 8046,
        "Ezekieli" to 8047, "Danieli" to 8048, "Hosea" to 8049, "Yoeli" to 8050, "Amosi" to 8051,
        "Obadia" to 8052, "Yona" to 8053, "Mika" to 8054, "Nahumu" to 8055, "Habakuki" to 8056,
        "Sefania" to 8057, "Hagai" to 8058, "Zekaria" to 8059, "Malaki" to 8060,
        // Deuterocanonical (Hekima, Sira, Baruku, Tobiti, 1-2 Wamakabayo) intentionally NOT
        // mapped - not present in this Bible. See doc above.
        "Mathayo" to 8061, "Marko" to 8062, "Luka" to 8063, "Yohana" to 8064,
        "Matendo ya Mitume" to 8065, "Warumi" to 8066, "1 Wakorintho" to 8067, "2 Wakorintho" to 8068,
        "Wagalatia" to 8069, "Waefeso" to 8070, "Wafilipi" to 8071, "Wakolosai" to 8072,
        "1 Wathesalonike" to 8073, "2 Wathesalonike" to 8074, "1 Timotheo" to 8075, "2 Timotheo" to 8076,
        "Tito" to 8077, "Filemoni" to 8078, "Waebrania" to 8079, "Yakobo" to 8080,
        "1 Petro" to 8081, "2 Petro" to 8082, "1 Yohana" to 8083, "2 Yohana" to 8084,
        "3 Yohana" to 8085, "Yuda" to 8086, "Ufunuo wa Yohana" to 8087,
        "Ufunuo" to 8087, // short form the lectionary uses; DB's own title is the full form above
    )

    val abbreviationToBookId: Map<String, Int> = ids

    /** Longest-name-first so e.g. "1 Wakorintho"/"Kumbukumbu la Torati" match before a shorter prefix would. */
    val sortedAbbreviations: List<String> = ids.keys.sortedByDescending { it.length }
}
