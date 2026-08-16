package com.biblia.app.data.liturgical

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val SWAHILI_MONTHS = listOf(
    "Januari", "Februari", "Machi", "Aprili", "Mei", "Juni",
    "Julai", "Agosti", "Septemba", "Oktoba", "Novemba", "Desemba",
)

/** "Machi 19" style, matching `watakatifu.tarehe` / the fixed sikukuu_maalum dates. */
private fun LocalDate.toSwahiliTarehe(): String = "${SWAHILI_MONTHS[monthValue - 1]} $dayOfMonth"

/** "Mon".."Sat" - matches the `day` column for every weekday-cycle table in the dataset. */
private fun LocalDate.toEnglishDayAbbrev(): String = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)

private fun sundayOnOrBefore(date: LocalDate): LocalDate = date.minusDays((date.dayOfWeek.value % 7).toLong())

/** The 12 fixed-date solemnities/feasts in `sikukuu_maalum`, keyed by period_key -> Swahili date. */
private val FIXED_SOLEMNITY_DATES: Map<String, Pair<Int, Int>> = mapOf( // periodKey -> (month, day)
    "kutolewa_hekaluni_bwana" to (2 to 2),
    "yosefu_bwana_arusi_wa_bikira_maria" to (3 to 19),
    "tangazo_la_bwana" to (3 to 25),
    "kuzaliwa_yohana_mbatizaji" to (6 to 24),
    "petro_na_paulo_mitume" to (6 to 29),
    "kubadilika_sura_kwa_bwana" to (8 to 6),
    "kupalizwa_bikira_maria_mbinguni" to (8 to 15),
    "kuinuliwa_msalaba_mtakatifu" to (9 to 14),
    "watakatifu_wote" to (11 to 1),
    "wafu_wote" to (11 to 2),
    "kuwekwa_wakfu_kanisa_kuu_la_laterano" to (11 to 9),
    "mimba_takatifu_ya_bikira_maria" to (12 to 8),
)

/**
 * Resolves any date to its place in the liturgical year and looks up the matching reading
 * set from [LectionaryRepository] - your own bundled dataset, not a live API. Implements
 * APP_LOGIC.md as closely as practical; see the class-level notes below for the handful of
 * deliberate simplifications (documented, not silently skipped).
 *
 * Known simplifications versus the full spec:
 * - Vigil Masses (Christmas Eve night, Easter Vigil, Pentecost Vigil) are not auto-selected
 *   by date - the app shows the principal/day Mass. A vigil-mass toggle is a reasonable v2.
 * - `misa_ya_krisma` (Chrism Mass) is never auto-surfaced - it's a once-a-year diocesan Mass,
 *   not a normal day's Mass, and picking a date for it is ambiguous without diocese data.
 * - Watakatifu (saints) have no reading text of their own in this dataset (by its own
 *   documented design - see APP_LOGIC.md \u00a79): when a saint's day wins precedence, this
 *   resolver surfaces their name/rank/color but keeps the underlying weekday's readings,
 *   rather than fabricating Common-of-Saints text that was never provided.
 */
class LiturgicalResolver(
    private val repository: LectionaryRepository,
    private val keepThursdaySolemnities: Boolean = false,
) {
    suspend fun resolve(date: LocalDate): ResolvedDay {
        val anchors = LiturgicalComputus.anchorsFor(date.year, keepThursdaySolemnities)
        val liturgicalYear = liturgicalYearFor(date)
        val cycles = repository.getCycleFor(liturgicalYear)
        val sundayCycle = cycles?.first
        // Weekday cycle keys off the date's plain civil year (per spec \u00a76), not the
        // liturgical year - they only coincide for most of the year, not in Jan when the
        // liturgical year is still the one that began the previous Advent.
        val weekdayCycle = (if (date.year == liturgicalYear) cycles else repository.getCycleFor(date.year))?.second
            ?: (if (date.year % 2 == 0) "II" else "I")

        // Proper-of-time exact-date overrides (Triduum/Christmas/Ascension/Pentecost/Trinity/
        // Corpus Christi/Sacred Heart) checked first, regardless of season-block boundaries -
        // several of these live under season="pasaka" in the dataset even though the *next*
        // civil season is technically Ordinary Time by date-range rules alone.
        properOfTimeOverride(date, anchors, sundayCycle)?.let { return it }

        val seasonal = when {
            isInAdvent(date, anchors) -> resolveAdvent(date, anchors, sundayCycle)
            isInChristmas(date, anchors) -> resolveChristmas(date, anchors, sundayCycle)
            isInLent(date, anchors) -> resolveLent(date, anchors, sundayCycle)
            isInEaster(date, anchors) -> resolveEaster(date, anchors, sundayCycle)
            else -> resolveOrdinaryTime(date, anchors, sundayCycle, weekdayCycle)
        }

        return applyPrecedence(seasonal, date)
    }

    // ── Liturgical year / cycle ─────────────────────────────────────────────

    private fun liturgicalYearFor(date: LocalDate): Int {
        val advent1ThisCivilYear = LiturgicalComputus.firstAdventSunday(date.year)
        return if (!date.isBefore(advent1ThisCivilYear)) date.year + 1 else date.year
    }

    // ── Season boundaries ────────────────────────────────────────────────────

    private fun isInAdvent(date: LocalDate, a: LiturgicalAnchors): Boolean =
        !date.isBefore(a.firstSundayAdvent) && !date.isAfter(LocalDate.of(a.firstSundayAdvent.year, 12, 24))

    private fun isInChristmas(date: LocalDate, a: LiturgicalAnchors): Boolean {
        val christmasDay = LocalDate.of(a.firstSundayAdvent.year, 12, 25)
        val baptism = baptismOfTheLord(a.firstSundayAdvent.year + 1)
        return !date.isBefore(christmasDay) || !date.isAfter(baptism)
    }

    private fun isInLent(date: LocalDate, a: LiturgicalAnchors): Boolean =
        !date.isBefore(a.ashWednesday) && date.isBefore(a.holyThursday)

    private fun isInEaster(date: LocalDate, a: LiturgicalAnchors): Boolean =
        !date.isBefore(a.holyThursday) && !date.isAfter(a.pentecost)

    // ── Proper-of-time overrides ─────────────────────────────────────────────

    private suspend fun properOfTimeOverride(date: LocalDate, a: LiturgicalAnchors, cycle: String?): ResolvedDay? {
        val (periodKey, entryKey, title) = when (date) {
            a.holyThursday -> Triple("juma_kuu_takatifu", "alhamisi_kuu", "Alhamisi Kuu")
            a.goodFriday -> Triple("juma_kuu_takatifu", "ijumaa_kuu", "Ijumaa Kuu \u2014 Mateso ya Bwana")
            a.easterVigil -> Triple("juma_kuu_takatifu", "vigilia_ya_pasaka", "Vigilia ya Pasaka")
            a.easterSunday -> Triple("dominika_ya_ufufuo", null, "Pasaka \u2014 Ufufuo wa Bwana")
            a.ascension -> Triple("kupaa_kwa_bwana", "mwaka_$cycle", "Kupaa kwa Bwana Mbinguni")
            a.pentecost -> Triple("pentekoste", "siku", "Pentekoste")
            a.trinitySunday -> Triple("utatu_mtakatifu", "mwaka_$cycle", "Utatu Mtakatifu")
            a.corpusChristi -> Triple("fungu_takatifu_la_mwili_na_damu_ya_kristo", "mwaka_$cycle", "Fungu Takatifu la Mwili na Damu ya Kristo")
            a.sacredHeart -> Triple("moyo_mtakatifu_wa_yesu", "mwaka_$cycle", "Moyo Mtakatifu wa Yesu")
            else -> null
        } ?: return null

        val readings = repository.getReading("pasaka", periodKey, entryKey, null)
        return ResolvedDay(
            date = date, season = "pasaka", periodKey = periodKey, entryKey = entryKey, day = null,
            sundayCycle = cycle, weekdayCycle = null, title = title, rangi = null,
            saintOfTheDay = null, usedFixedSolemnity = true, readings = readings,
        )
    }

    // ── Advent ───────────────────────────────────────────────────────────────

    private suspend fun resolveAdvent(date: LocalDate, a: LiturgicalAnchors, cycle: String?): ResolvedDay {
        if (date.monthValue == 12 && date.dayOfMonth in 17..24) {
            val dayText = if (date.dayOfMonth == 24) "Des 24 asubuhi" else "Des ${date.dayOfMonth}"
            val readings = repository.getReadingByFreeTextDay("majilio", "desemba_17_hadi_24", dayText)
            return ResolvedDay(
                date, "majilio", "desemba_17_hadi_24", null, dayText, cycle, null,
                "Majilio \u2014 Des ${date.dayOfMonth}", null, null, false, readings,
            )
        }
        val week = weeksBetween(a.firstSundayAdvent, sundayOnOrBefore(date)) + 1
        val periodKey = "wiki_%02d".format(week.coerceIn(1, 4))
        val isSunday = date.dayOfWeek == DayOfWeek.SUNDAY
        val entryKey = if (isSunday) "dominika_mwaka_$cycle" else "siku_za_wiki"
        val day = if (isSunday) null else date.toEnglishDayAbbrev()
        val readings = repository.getReading("majilio", periodKey, entryKey, day)
        val title = if (isSunday) "Jumapili ya $week ya Majilio" else "Wiki ya $week ya Majilio"
        return ResolvedDay(date, "majilio", periodKey, entryKey, day, cycle, null, title, null, null, false, readings)
    }

    // ── Christmas ────────────────────────────────────────────────────────────

    private fun epiphany(year: Int): LocalDate {
        val jan2 = LocalDate.of(year, 1, 2)
        return if (jan2.dayOfWeek == DayOfWeek.SUNDAY) jan2 else jan2.plusDays((7 - jan2.dayOfWeek.value.toLong()) % 7)
    }

    private fun baptismOfTheLord(year: Int): LocalDate {
        val epi = epiphany(year)
        return if (epi.dayOfMonth >= 7) epi.plusDays(1) else epi.plusWeeks(1)
    }

    private data class ChristmasSlot(val periodKey: String, val entryKey: String?, val dayText: String?, val title: String)

    private suspend fun resolveChristmas(date: LocalDate, a: LiturgicalAnchors, cycle: String?): ResolvedDay {
        // Which civil year is this Christmas season anchored in? Dec dates use date.year;
        // Jan dates belong to the Christmas season that started the *previous* December.
        val adventCivilYear = if (date.monthValue == 12) date.year else date.year - 1
        val christmasDay = LocalDate.of(adventCivilYear, 12, 25)
        val epi = epiphany(adventCivilYear + 1)
        val baptism = baptismOfTheLord(adventCivilYear + 1)
        val isSunday = date.dayOfWeek == DayOfWeek.SUNDAY

        // Sunday within the Dec 26-31 octave (or Dec 30 if Christmas Day itself is a Sunday).
        val holyFamilySunday = if (christmasDay.dayOfWeek == DayOfWeek.SUNDAY) {
            LocalDate.of(adventCivilYear, 12, 30)
        } else {
            (26..31).map { LocalDate.of(adventCivilYear, 12, it) }.firstOrNull { it.dayOfWeek == DayOfWeek.SUNDAY }
        }

        val slot = when {
            date == christmasDay -> ChristmasSlot("mchana", null, null, "Kuzaliwa kwa Bwana (Noeli)")
            date == LocalDate.of(adventCivilYear + 1, 1, 1) -> ChristmasSlot("maria_mama_wa_mungu", null, null, "Maria Mama wa Mungu")
            date == epi -> ChristmasSlot("epifania", null, null, "Epifania ya Bwana")
            date == baptism -> ChristmasSlot("ubatizo_wa_bwana", "mwaka_$cycle", null, "Ubatizo wa Bwana")
            date == holyFamilySunday -> ChristmasSlot("familia_takatifu", "mwaka_$cycle", null, "Familia Takatifu")
            date.monthValue == 12 && date.dayOfMonth in 26..31 -> {
                val names = mapOf(26 to "Stefano Shahidi", 27 to "Yohana Mtume", 28 to "Watoto Wasio na Hatia")
                val dt = "Des ${date.dayOfMonth}" + (names[date.dayOfMonth]?.let { " - $it" } ?: "")
                ChristmasSlot("oktava", null, dt, "Oktava ya Noeli \u2014 Des ${date.dayOfMonth}")
            }
            date == LocalDate.of(adventCivilYear + 1, 1, 2) -> ChristmasSlot("oktava", null, "Jan 2", "Wiki ya Noeli")
            date.isAfter(epi) && date.isBefore(baptism) && !isSunday ->
                ChristmasSlot("baada_ya_epifania", null, date.toEnglishDayAbbrev(), "Baada ya Epifania")
            else -> ChristmasSlot("mchana", null, null, "Noeli")
        }

        val readings = if (slot.dayText != null) {
            repository.getReadingByFreeTextDay("noeli", slot.periodKey, slot.dayText)
        } else {
            repository.getReading("noeli", slot.periodKey, slot.entryKey, null)
        }
        return ResolvedDay(date, "noeli", slot.periodKey, slot.entryKey, slot.dayText, cycle, null, slot.title, null, null, false, readings)
    }

    // ── Lent ─────────────────────────────────────────────────────────────────

    private suspend fun resolveLent(date: LocalDate, a: LiturgicalAnchors, cycle: String?): ResolvedDay {
        if (date == a.ashWednesday) {
            val readings = repository.getReading("kwaresima", "jumatano_ya_majivu", null, null)
            return ResolvedDay(date, "kwaresima", "jumatano_ya_majivu", null, null, cycle, null, "Jumatano ya Majivu", null, null, false, readings)
        }
        if (date.isAfter(a.ashWednesday) && date < a.ashWednesday.plusDays(4)) {
            val day = date.toEnglishDayAbbrev()
            val readings = repository.getReading("kwaresima", "baada_ya_majivu", null, day)
            return ResolvedDay(date, "kwaresima", "baada_ya_majivu", null, day, cycle, null, "Baada ya Majivu", null, null, false, readings)
        }
        if (date == a.palmSunday) {
            val readings = repository.getReading("kwaresima", "dominika_ya_matawi", "mwaka_$cycle", null)
            return ResolvedDay(date, "kwaresima", "dominika_ya_matawi", "mwaka_$cycle", null, cycle, null, "Jumapili ya Matawi", null, null, false, readings)
        }
        if (date.isAfter(a.palmSunday) && date.isBefore(a.holyThursday)) {
            val day = date.toEnglishDayAbbrev()
            val readings = repository.getReading("kwaresima", "wiki_takatifu", null, day)
            return ResolvedDay(date, "kwaresima", "wiki_takatifu", null, day, cycle, null, "Juma Kuu Takatifu", null, null, false, readings)
        }

        val firstSundayOfLent = sundayOnOrBefore(a.ashWednesday).plusWeeks(1)
        val week = (weeksBetween(firstSundayOfLent, sundayOnOrBefore(date)) + 1).coerceIn(1, 5)
        val periodKey = "wiki_%02d".format(week)
        val isSunday = date.dayOfWeek == DayOfWeek.SUNDAY
        val entryKey = if (isSunday) "dominika_mwaka_$cycle" else "siku_za_wiki"
        val day = if (isSunday) null else date.toEnglishDayAbbrev()
        val readings = repository.getReading("kwaresima", periodKey, entryKey, day)
        val title = if (isSunday) "Jumapili ya $week ya Kwaresima" else "Wiki ya $week ya Kwaresima"
        return ResolvedDay(date, "kwaresima", periodKey, entryKey, day, cycle, null, title, null, null, false, readings)
    }

    // ── Easter ───────────────────────────────────────────────────────────────

    private suspend fun resolveEaster(date: LocalDate, a: LiturgicalAnchors, cycle: String?): ResolvedDay {
        if (date.isAfter(a.easterSunday) && date < a.easterSunday.plusWeeks(1)) {
            val day = date.toEnglishDayAbbrev()
            val readings = repository.getReading("pasaka", "oktava", null, day)
            return ResolvedDay(date, "pasaka", "oktava", null, day, cycle, null, "Oktava ya Pasaka", null, null, false, readings)
        }

        val week = (weeksBetween(a.easterSunday, sundayOnOrBefore(date)) + 1).toInt().coerceIn(2, 7)
        val periodKey = when (week) {
            2 -> "wiki_02_huruma_ya_mungu"
            4 -> "wiki_04_mchungaji_mwema"
            else -> "wiki_%02d".format(week)
        }
        val isSunday = date.dayOfWeek == DayOfWeek.SUNDAY
        val entryKey = if (isSunday) "mwaka_$cycle" else "siku_za_wiki"
        val day = if (isSunday) null else date.toEnglishDayAbbrev()
        val readings = repository.getReading("pasaka", periodKey, entryKey, day)
        val title = if (isSunday) "Jumapili ya $week ya Pasaka" else "Wiki ya $week ya Pasaka"
        return ResolvedDay(date, "pasaka", periodKey, entryKey, day, cycle, null, title, null, null, false, readings)
    }

    // ── Ordinary Time ────────────────────────────────────────────────────────

    private suspend fun resolveOrdinaryTime(date: LocalDate, a: LiturgicalAnchors, sundayCycle: String?, weekdayCycle: String?): ResolvedDay {
        val baptism = baptismOfTheLord(date.year)
        val beforeLent = date.isAfter(baptism) && date.isBefore(a.ashWednesday)
        val s = sundayOnOrBefore(date)
        val weekNum = if (beforeLent) {
            (weeksBetween(baptism, s) + 1).toInt()
        } else {
            34 - weeksBetween(s, a.christTheKing).toInt()
        }
        val periodKey = "wiki_%02d".format(weekNum.coerceIn(1, 34))
        val isSunday = date.dayOfWeek == DayOfWeek.SUNDAY
        val entryKey = if (isSunday) "dominika_mwaka_$sundayCycle" else "siku_za_wiki_mwaka_$weekdayCycle"
        val day = if (isSunday) null else date.toEnglishDayAbbrev()
        val readings = repository.getReading("muda_wa_kawaida", periodKey, entryKey, day)
        val title = when {
            weekNum == 34 && isSunday -> "Kristo Mfalme wa Ulimwengu"
            isSunday -> "Jumapili ya $weekNum ya Kawaida"
            else -> "Wiki ya $weekNum ya Kawaida"
        }
        return ResolvedDay(date, "muda_wa_kawaida", periodKey, entryKey, day, sundayCycle, weekdayCycle, title, null, null, false, readings)
    }

    // ── Precedence: fixed solemnities + saints ──────────────────────────────

    private suspend fun applyPrecedence(seasonal: ResolvedDay, date: LocalDate): ResolvedDay {
        // Rule 5: Lent weekdays, and Advent Dec 17-24, always beat feasts/memorials - skip
        // the saints/solemnity lookup entirely for those.
        val skipsOverride = seasonal.season == "kwaresima" && seasonal.day != null && seasonal.entryKey != "dominika_mwaka_${seasonal.sundayCycle}" ||
            (seasonal.season == "majilio" && seasonal.periodKey == "desemba_17_hadi_24")
        if (skipsOverride) return withSaintInfo(seasonal, date)

        val isSeasonalSunday = seasonal.day == null && seasonal.entryKey?.contains("mwaka") == true &&
            seasonal.season in setOf("majilio", "kwaresima", "pasaka") && seasonal.season != "muda_wa_kawaida"

        if (!isSeasonalSunday) {
            val fixedMatch = FIXED_SOLEMNITY_DATES.entries.firstOrNull { (_, md) -> md.first == date.monthValue && md.second == date.dayOfMonth }
            if (fixedMatch != null) {
                val (periodKey, _) = fixedMatch
                val readings = repository.getReading("sikukuu_maalum", periodKey, null, null)
                    ?: repository.getReading("sikukuu_maalum", periodKey, "mwaka_${seasonal.sundayCycle}", null)
                return ResolvedDay(
                    date, "sikukuu_maalum", periodKey, null, null, seasonal.sundayCycle, seasonal.weekdayCycle,
                    solemnityTitle(periodKey), null, null, true, readings ?: seasonal.readings,
                )
            }
        }

        return withSaintInfo(seasonal, date)
    }

    private suspend fun withSaintInfo(seasonal: ResolvedDay, date: LocalDate): ResolvedDay {
        val saint = repository.getSaintFor(date.toSwahiliTarehe()) ?: return seasonal
        val isPlainWeekday = seasonal.day != null // any weekday slot, any season
        return if (isPlainWeekday) {
            seasonal.copy(
                title = "${seasonal.title} \u2014 ${saint.jina}",
                rangi = saint.rangi,
                saintOfTheDay = saint,
            )
        } else {
            seasonal.copy(saintOfTheDay = saint) // informational only, doesn't touch title/readings
        }
    }

    private fun solemnityTitle(periodKey: String): String = when (periodKey) {
        "kutolewa_hekaluni_bwana" -> "Kutolewa Hekaluni kwa Bwana"
        "yosefu_bwana_arusi_wa_bikira_maria" -> "Yosefu, Bwana-arusi wa Bikira Maria"
        "tangazo_la_bwana" -> "Kutangazwa kwa Malaika kwa Bwana"
        "kuzaliwa_yohana_mbatizaji" -> "Kuzaliwa kwa Yohane Mbatizaji"
        "petro_na_paulo_mitume" -> "Petro na Paulo, Mitume"
        "kubadilika_sura_kwa_bwana" -> "Kubadilika Sura kwa Bwana"
        "kupalizwa_bikira_maria_mbinguni" -> "Kupalizwa Mbinguni kwa Bikira Maria"
        "kuinuliwa_msalaba_mtakatifu" -> "Kuinuliwa kwa Msalaba Mtakatifu"
        "watakatifu_wote" -> "Watakatifu Wote"
        "wafu_wote" -> "Wafu Wote"
        "kuwekwa_wakfu_kanisa_kuu_la_laterano" -> "Kuwekwa Wakfu kwa Kanisa Kuu la Laterano"
        "mimba_takatifu_ya_bikira_maria" -> "Kuchukuliwa Mimba Bila Dhambi kwa Bikira Maria"
        else -> periodKey
    }

    private fun weeksBetween(start: LocalDate, end: LocalDate): Long =
        java.time.temporal.ChronoUnit.WEEKS.between(start, end)
}
