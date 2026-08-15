package com.biblia.app.data.liturgical

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val SWAHILI_MONTHS = listOf(
    "Januari",
    "Februari",
    "Machi",
    "Aprili",
    "Mei",
    "Juni",
    "Julai",
    "Agosti",
    "Septemba",
    "Oktoba",
    "Novemba",
    "Desemba",
)

/** "Machi 19" style, matching `watakatifu.tarehe` / fixed `sikukuu_maalum` dates. */
private fun LocalDate.toSwahiliTarehe(): String =
    "${SWAHILI_MONTHS[monthValue - 1]} $dayOfMonth"

/** "Mon".."Sun" - matches the `day` column for weekday-cycle tables. */
private fun LocalDate.toEnglishDayAbbrev(): String =
    dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)

/**
 * Returns the Sunday on or before [date].
 *
 * Kotlin's DayOfWeek.value:
 * Monday = 1 ... Sunday = 7
 *
 * Therefore:
 * Sunday -> subtract 0
 * Monday -> subtract 1
 * ...
 * Saturday -> subtract 6
 */
private fun sundayOnOrBefore(date: LocalDate): LocalDate =
    date.minusDays((date.dayOfWeek.value % 7).toLong())

/**
 * The fixed-date solemnities/feasts represented in `sikukuu_maalum`.
 *
 * periodKey -> (month, day)
 */
private val FIXED_SOLEMNITY_DATES: Map<String, Pair<Int, Int>> = mapOf(
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
 * Resolves any date to its place in the liturgical year and looks up
 * the matching reading set from [LectionaryRepository].
 *
 * The resolver uses the bundled/local lectionary dataset.
 *
 * Known simplifications:
 *
 * - Vigil Masses (Christmas Eve night, Easter Vigil, Pentecost Vigil)
 *   are not automatically selected by date. The principal/day Mass is used.
 *
 * - `misa_ya_krisma` (Chrism Mass) is not automatically surfaced because
 *   it is a diocesan celebration whose exact scheduling depends on the diocese.
 *
 * - Saints do not provide their own reading text in the current dataset.
 *   When a saint's day wins precedence, the resolver keeps the underlying
 *   weekday readings while attaching saint information.
 */
class LiturgicalResolver(
    private val repository: LectionaryRepository,
    private val keepThursdaySolemnities: Boolean = false,
) {

    suspend fun resolve(date: LocalDate): ResolvedDay {
        val anchors = LiturgicalComputus.anchorsFor(
            date.year,
            keepThursdaySolemnities
        )

        val liturgicalYear = liturgicalYearFor(date)

        val cycles = repository.getCycleFor(liturgicalYear)

        val sundayCycle = cycles?.first

        /*
         * Weekday cycle is based on the civil year, not the liturgical year.
         *
         * January dates belong to the liturgical year that began in the
         * previous Advent, but weekday readings still follow the civil year
         * cycle according to the application specification.
         */
        val weekdayCycle =
            (
                if (date.year == liturgicalYear) {
                    cycles
                } else {
                    repository.getCycleFor(date.year)
                }
                )?.second
                ?: if (date.year % 2 == 0) "II" else "I"

        /*
         * Proper-of-time exact-date overrides are checked first.
         *
         * This covers:
         * Triduum, Easter, Ascension, Pentecost, Trinity,
         * Corpus Christi and Sacred Heart.
         */
        properOfTimeOverride(
            date = date,
            a = anchors,
            cycle = sundayCycle
        )?.let {
            return it
        }

        val seasonal = when {
            isInAdvent(date, anchors) ->
                resolveAdvent(date, anchors, sundayCycle)

            isInChristmas(date, anchors) ->
                resolveChristmas(date, anchors, sundayCycle)

            isInLent(date, anchors) ->
                resolveLent(date, anchors, sundayCycle)

            isInEaster(date, anchors) ->
                resolveEaster(date, anchors, sundayCycle)

            else ->
                resolveOrdinaryTime(
                    date = date,
                    a = anchors,
                    sundayCycle = sundayCycle,
                    weekdayCycle = weekdayCycle
                )
        }

        return applyPrecedence(
            seasonal = seasonal,
            date = date
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Liturgical year / cycle
    // ─────────────────────────────────────────────────────────────────────────

    private fun liturgicalYearFor(date: LocalDate): Int {
        val advent1ThisCivilYear =
            LiturgicalComputus.firstAdventSunday(date.year)

        return if (!date.isBefore(advent1ThisCivilYear)) {
            date.year + 1
        } else {
            date.year
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Season boundaries
    // ─────────────────────────────────────────────────────────────────────────

    private fun isInAdvent(
        date: LocalDate,
        a: LiturgicalAnchors,
    ): Boolean =
        !date.isBefore(a.firstSundayAdvent) &&
            !date.isAfter(
                LocalDate.of(
                    a.firstSundayAdvent.year,
                    12,
                    24
                )
            )

    private fun isInChristmas(
        date: LocalDate,
        a: LiturgicalAnchors,
    ): Boolean {
        val christmasDay =
            LocalDate.of(
                a.firstSundayAdvent.year,
                12,
                25
            )

        val baptism =
            baptismOfTheLord(
                a.firstSundayAdvent.year + 1
            )

        /*
         * Christmas season continues from Christmas Day through
         * the Baptism of the Lord.
         */
        return !date.isBefore(christmasDay) &&
            !date.isAfter(baptism)
    }

    private fun isInLent(
        date: LocalDate,
        a: LiturgicalAnchors,
    ): Boolean =
        !date.isBefore(a.ashWednesday) &&
            date.isBefore(a.holyThursday)

    private fun isInEaster(
        date: LocalDate,
        a: LiturgicalAnchors,
    ): Boolean =
        !date.isBefore(a.holyThursday) &&
            !date.isAfter(a.pentecost)

    // ─────────────────────────────────────────────────────────────────────────
    // Proper-of-time overrides
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun properOfTimeOverride(
        date: LocalDate,
        a: LiturgicalAnchors,
        cycle: String?,
    ): ResolvedDay? {

        val (periodKey, entryKey, title) = when (date) {

            a.holyThursday ->
                Triple(
                    "juma_kuu_takatifu",
                    "alhamisi_kuu",
                    "Alhamisi Kuu"
                )

            a.goodFriday ->
                Triple(
                    "juma_kuu_takatifu",
                    "ijumaa_kuu",
                    "Ijumaa Kuu — Mateso ya Bwana"
                )

            a.easterVigil ->
                Triple(
                    "juma_kuu_takatifu",
                    "vigilia_ya_pasaka",
                    "Vigilia ya Pasaka"
                )

            a.easterSunday ->
                Triple(
                    "dominika_ya_ufufuo",
                    null,
                    "Pasaka — Ufufuo wa Bwana"
                )

            a.ascension ->
                Triple(
                    "kupaa_kwa_bwana",
                    "mwaka_$cycle",
                    "Kupaa kwa Bwana Mbinguni"
                )

            a.pentecost ->
                Triple(
                    "pentekoste",
                    "siku",
                    "Pentekoste"
                )

            a.trinitySunday ->
                Triple(
                    "utatu_mtakatifu",
                    "mwaka_$cycle",
                    "Utatu Mtakatifu"
                )

            a.corpusChristi ->
                Triple(
                    "fungu_takatifu_la_mwili_na_damu_ya_kristo",
                    "mwaka_$cycle",
                    "Fungu Takatifu la Mwili na Damu ya Kristo"
                )

            a.sacredHeart ->
                Triple(
                    "moyo_mtakatifu_wa_yesu",
                    "mwaka_$cycle",
                    "Moyo Mtakatifu wa Yesu"
                )

            else -> return null
        }

        val readings = repository.getReading(
            "pasaka",
            periodKey,
            entryKey,
            null
        )

        return ResolvedDay(
            date = date,
            season = "pasaka",
            periodKey = periodKey,
            entryKey = entryKey,
            day = null,
            sundayCycle = cycle,
            weekdayCycle = null,
            title = title,
            rangi = null,
            saintOfTheDay = null,
            usedFixedSolemnity = true,
            readings = readings,
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Advent
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun resolveAdvent(
        date: LocalDate,
        a: LiturgicalAnchors,
        cycle: String?,
    ): ResolvedDay {

        /*
         * December 17–24 have their own daily readings.
         */
        if (date.monthValue == 12 && date.dayOfMonth in 17..24) {

            val dayText =
                if (date.dayOfMonth == 24) {
                    "Des 24 asubuhi"
                } else {
                    "Des ${date.dayOfMonth}"
                }

            val readings =
                repository.getReadingByFreeTextDay(
                    "majilio",
                    "desemba_17_hadi_24",
                    dayText
                )

            return ResolvedDay(
                date = date,
                season = "majilio",
                periodKey = "desemba_17_hadi_24",
                entryKey = null,
                day = dayText,
                sundayCycle = cycle,
                weekdayCycle = null,
                title = "Majilio — Des ${date.dayOfMonth}",
                rangi = null,
                saintOfTheDay = null,
                usedFixedSolemnity = false,
                readings = readings,
            )
        }

        val week =
            weeksBetween(
                a.firstSundayAdvent,
                sundayOnOrBefore(date)
            ).toInt() + 1

        val periodKey =
            "wiki_%02d".format(
                week.coerceIn(1, 4)
            )

        val isSunday =
            date.dayOfWeek == DayOfWeek.SUNDAY

        val entryKey =
            if (isSunday) {
                "dominika_mwaka_$cycle"
            } else {
                "siku_za_wiki"
            }

        val day =
            if (isSunday) {
                null
            } else {
                date.toEnglishDayAbbrev()
            }

        val readings =
            repository.getReading(
                "majilio",
                periodKey,
                entryKey,
                day
            )

        val title =
            if (isSunday) {
                "Jumapili ya $week ya Majilio"
            } else {
                "Wiki ya $week ya Majilio"
            }

        return ResolvedDay(
            date,
            "majilio",
            periodKey,
            entryKey,
            day,
            cycle,
            null,
            title,
            null,
            null,
            false,
            readings
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Christmas
    // ─────────────────────────────────────────────────────────────────────────

    private fun epiphany(year: Int): LocalDate {
        val jan2 = LocalDate.of(year, 1, 2)

        return if (jan2.dayOfWeek == DayOfWeek.SUNDAY) {
            jan2
        } else {
            jan2.plusDays(
                (7 - jan2.dayOfWeek.value.toLong()) % 7
            )
        }
    }

    private fun baptismOfTheLord(year: Int): LocalDate {
        val epi = epiphany(year)

        return if (epi.dayOfMonth >= 7) {
            epi.plusDays(1)
        } else {
            epi.plusWeeks(1)
        }
    }

    private data class ChristmasSlot(
        val periodKey: String,
        val entryKey: String?,
        val dayText: String?,
        val title: String,
    )

    private suspend fun resolveChristmas(
        date: LocalDate,
        a: LiturgicalAnchors,
        cycle: String?,
    ): ResolvedDay {

        /*
         * December dates belong to the Christmas season beginning
         * in the same civil year.
         *
         * January dates belong to the Christmas season beginning
         * in the previous December.
         */
        val adventCivilYear =
            if (date.monthValue == 12) {
                date.year
            } else {
                date.year - 1
            }

        val christmasDay =
            LocalDate.of(
                adventCivilYear,
                12,
                25
            )

        val epi =
            epiphany(
                adventCivilYear + 1
            )

        val baptism =
            baptismOfTheLord(
                adventCivilYear + 1
            )

        val isSunday =
            date.dayOfWeek == DayOfWeek.SUNDAY

        /*
         * Sunday during the Christmas octave.
         */
        val holyFamilySunday =
            if (christmasDay.dayOfWeek == DayOfWeek.SUNDAY) {
                LocalDate.of(
                    adventCivilYear,
                    12,
                    30
                )
            } else {
                (26..31)
                    .map {
                        LocalDate.of(
                            adventCivilYear,
                            12,
                            it
                        )
                    }
                    .firstOrNull {
                        it.dayOfWeek == DayOfWeek.SUNDAY
                    }
            }

        val slot = when {

            date == christmasDay ->
                ChristmasSlot(
                    "mchana",
                    null,
                    null,
                    "Kuzaliwa kwa Bwana (Noeli)"
                )

            date == LocalDate.of(
                adventCivilYear + 1,
                1,
                1
            ) ->
                ChristmasSlot(
                    "maria_mama_wa_mungu",
                    null,
                    null,
                    "Maria Mama wa Mungu"
                )

            date == epi ->
                ChristmasSlot(
                    "epifania",
                    null,
                    null,
                    "Epifania ya Bwana"
                )

            date == baptism ->
                ChristmasSlot(
                    "ubatizo_wa_bwana",
                    "mwaka_$cycle",
                    null,
                    "Ubatizo wa Bwana"
                )

            date == holyFamilySunday ->
                ChristmasSlot(
                    "familia_takatifu",
                    "mwaka_$cycle",
                    null,
                    "Familia Takatifu"
                )

            date.monthValue == 12 &&
                date.dayOfMonth in 26..31 -> {

                val names =
                    mapOf(
                        26 to "Stefano Shahidi",
                        27 to "Yohana Mtume",
                        28 to "Watoto Wasio na Hatia",
                    )

                val dt =
                    "Des ${date.dayOfMonth}" +
                        (
                            names[date.dayOfMonth]
                                ?.let { " - $it" }
                                ?: ""
                            )

                ChristmasSlot(
                    "oktava",
                    null,
                    dt,
                    "Oktava ya Noeli — Des ${date.dayOfMonth}"
                )
            }

            date == LocalDate.of(
                adventCivilYear + 1,
                1,
                2
            ) ->
                ChristmasSlot(
                    "oktava",
                    null,
                    "Jan 2",
                    "Wiki ya Noeli"
                )

            date.isAfter(epi) &&
                date.isBefore(baptism) &&
                !isSunday ->
                ChristmasSlot(
                    "baada_ya_epifania",
                    null,
                    date.toEnglishDayAbbrev(),
                    "Baada ya Epifania"
                )

            else ->
                ChristmasSlot(
                    "mchana",
                    null,
                    null,
                    "Noeli"
                )
        }

        val readings =
            if (slot.dayText != null) {
                repository.getReadingByFreeTextDay(
                    "noeli",
                    slot.periodKey,
                    slot.dayText
                )
            } else {
                repository.getReading(
                    "noeli",
                    slot.periodKey,
                    slot.entryKey,
                    null
                )
            }

        return ResolvedDay(
            date,
            "noeli",
            slot.periodKey,
            slot.entryKey,
            slot.dayText,
            cycle,
            null,
            slot.title,
            null,
            null,
            false,
            readings
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lent
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun resolveLent(
        date: LocalDate,
        a: LiturgicalAnchors,
        cycle: String?,
    ): ResolvedDay {

        if (date == a.ashWednesday) {

            val readings =
                repository.getReading(
                    "kwaresima",
                    "jumatano_ya_majivu",
                    null,
                    null
                )

            return ResolvedDay(
                date,
                "kwaresima",
                "jumatano_ya_majivu",
                null,
                null,
                cycle,
                null,
                "Jumatano ya Majivu",
                null,
                null,
                false,
                readings
            )
        }

        if (
            date.isAfter(a.ashWednesday) &&
            date.isBefore(a.ashWednesday.plusDays(4))
        ) {

            val day =
                date.toEnglishDayAbbrev()

            val readings =
                repository.getReading(
                    "kwaresima",
                    "baada_ya_majivu",
                    null,
                    day
                )

            return ResolvedDay(
                date,
                "kwaresima",
                "baada_ya_majivu",
                null,
                day,
                cycle,
                null,
                "Baada ya Majivu",
                null,
                null,
                false,
                readings
            )
        }

        if (date == a.palmSunday) {

            val readings =
                repository.getReading(
                    "kwaresima",
                    "dominika_ya_matawi",
                    "mwaka_$cycle",
                    null
                )

            return ResolvedDay(
                date,
                "kwaresima",
                "dominika_ya_matawi",
                "mwaka_$cycle",
                null,
                cycle,
                null,
                "Jumapili ya Matawi",
                null,
                null,
                false,
                readings
            )
        }

        if (
            date.isAfter(a.palmSunday) &&
            date.isBefore(a.holyThursday)
        ) {

            val day =
                date.toEnglishDayAbbrev()

            val readings =
                repository.getReading(
                    "kwaresima",
                    "wiki_takatifu",
                    null,
                    day
                )

            return ResolvedDay(
                date,
                "kwaresima",
                "wiki_takatifu",
                null,
                day,
                cycle,
                null,
                "Juma Kuu Takatifu",
                null,
                null,
                false,
                readings
            )
        }

        val firstSundayOfLent =
            sundayOnOrBefore(a.ashWednesday)
                .plusWeeks(1)

        val week =
            (
                weeksBetween(
                    firstSundayOfLent,
                    sundayOnOrBefore(date)
                ).toInt() + 1
                ).coerceIn(1, 5)

        val periodKey =
            "wiki_%02d".format(week)

        val isSunday =
            date.dayOfWeek == DayOfWeek.SUNDAY

        val entryKey =
            if (isSunday) {
                "dominika_mwaka_$cycle"
            } else {
                "siku_za_wiki"
            }

        val day =
            if (isSunday) {
                null
            } else {
                date.toEnglishDayAbbrev()
            }

        val readings =
            repository.getReading(
                "kwaresima",
                periodKey,
                entryKey,
                day
            )

        val title =
            if (isSunday) {
                "Jumapili ya $week ya Kwaresima"
            } else {
                "Wiki ya $week ya Kwaresima"
            }

        return ResolvedDay(
            date,
            "kwaresima",
            periodKey,
            entryKey,
            day,
            cycle,
            null,
            title,
            null,
            null,
            false,
            readings
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Easter
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun resolveEaster(
        date: LocalDate,
        a: LiturgicalAnchors,
        cycle: String?,
    ): ResolvedDay {

        if (
            date.isAfter(a.easterSunday) &&
            date.isBefore(a.easterSunday.plusWeeks(1))
        ) {

            val day =
                date.toEnglishDayAbbrev()

            val readings =
                repository.getReading(
                    "pasaka",
                    "oktava",
                    null,
                    day
                )

            return ResolvedDay(
                date,
                "pasaka",
                "oktava",
                null,
                day,
                cycle,
                null,
                "Oktava ya Pasaka",
                null,
                null,
                false,
                readings
            )
        }

        val week =
            (
                weeksBetween(
                    a.easterSunday,
                    sundayOnOrBefore(date)
                ).toInt() + 1
                ).coerceIn(2, 7)

        val periodKey =
            when (week) {
                2 -> "wiki_02_huruma_ya_mungu"
                4 -> "wiki_04_mchungaji_mwema"
                else -> "wiki_%02d".format(week)
            }

        val isSunday =
            date.dayOfWeek == DayOfWeek.SUNDAY

        val entryKey =
            if (isSunday) {
                "mwaka_$cycle"
            } else {
                "siku_za_wiki"
            }

        val day =
            if (isSunday) {
                null
            } else {
                date.toEnglishDayAbbrev()
            }

        val readings =
            repository.getReading(
                "pasaka",
                periodKey,
                entryKey,
                day
            )

        val title =
            if (isSunday) {
                "Jumapili ya $week ya Pasaka"
            } else {
                "Wiki ya $week ya Pasaka"
            }

        return ResolvedDay(
            date,
            "pasaka",
            periodKey,
            entryKey,
            day,
            cycle,
            null,
            title,
            null,
            null,
            false,
            readings
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Ordinary Time
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun resolveOrdinaryTime(
        date: LocalDate,
        a: LiturgicalAnchors,
        sundayCycle: String?,
        weekdayCycle: String?,
    ): ResolvedDay {

        val baptism =
            baptismOfTheLord(date.year)

        val beforeLent =
            date.isAfter(baptism) &&
                date.isBefore(a.ashWednesday)

        val sunday =
            sundayOnOrBefore(date)

        /*
         * Explicitly make weekNum an Int.
         *
         * weeksBetween() returns Long, which was the source of
         * the compiler's Long/Int mismatch in the previous build.
         */
        val weekNum: Int =
            if (beforeLent) {
                weeksBetween(
                    baptism,
                    sunday
                ).toInt() + 1
            } else {
                34 - weeksBetween(
                    sunday,
                    a.christTheKing
                ).toInt()
            }

        val periodKey =
            "wiki_%02d".format(
                weekNum.coerceIn(1, 34)
            )

        val isSunday =
            date.dayOfWeek == DayOfWeek.SUNDAY

        val entryKey =
            if (isSunday) {
                "dominika_mwaka_$sundayCycle"
            } else {
                "siku_za_wiki_mwaka_$weekdayCycle"
            }

        val day =
            if (isSunday) {
                null
            } else {
                date.toEnglishDayAbbrev()
            }

        val readings =
            repository.getReading(
                "muda_wa_kawaida",
                periodKey,
                entryKey,
                day
            )

        val title =
            when {
                weekNum == 34 && isSunday ->
                    "Kristo Mfalme wa Ulimwengu"

                isSunday ->
                    "Jumapili ya $weekNum ya Kawaida"

                else ->
                    "Wiki ya $weekNum ya Kawaida"
            }

        return ResolvedDay(
            date,
            "muda_wa_kawaida",
            periodKey,
            entryKey,
            day,
            sundayCycle,
            weekdayCycle,
            title,
            null,
            null,
            false,
            readings
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Precedence: fixed solemnities + saints
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun applyPrecedence(
        seasonal: ResolvedDay,
        date: LocalDate,
    ): ResolvedDay {

        /*
         * Lent weekdays and December 17–24 Advent weekdays
         * take precedence over saints/feasts in this resolver.
         */
        val skipsOverride =
            (
                seasonal.season == "kwaresima" &&
                    seasonal.day != null &&
                    seasonal.entryKey !=
                    "dominika_mwaka_${seasonal.sundayCycle}"
                ) ||
                (
                    seasonal.season == "majilio" &&
                        seasonal.periodKey ==
                        "desemba_17_hadi_24"
                    )

        if (skipsOverride) {
            return withSaintInfo(
                seasonal,
                date
            )
        }

        val isSeasonalSunday =
            seasonal.day == null &&
                seasonal.entryKey?.contains("mwaka") == true &&
                seasonal.season in setOf(
                    "majilio",
                    "kwaresima",
                    "pasaka"
                ) &&
                seasonal.season != "muda_wa_kawaida"

        if (!isSeasonalSunday) {

            val fixedMatch =
                FIXED_SOLEMNITY_DATES.entries.firstOrNull {
                    (_, md) ->
                    md.first == date.monthValue &&
                        md.second == date.dayOfMonth
                }

            if (fixedMatch != null) {

                val periodKey =
                    fixedMatch.key

                val readings =
                    repository.getReading(
                        "sikukuu_maalum",
                        periodKey,
                        null,
                        null
                    ) ?: repository.getReading(
                        "sikukuu_maalum",
                        periodKey,
                        "mwaka_${seasonal.sundayCycle}",
                        null
                    )

                return ResolvedDay(
                    date,
                    "sikukuu_maalum",
                    periodKey,
                    null,
                    null,
                    seasonal.sundayCycle,
                    seasonal.weekdayCycle,
                    solemnityTitle(periodKey),
                    null,
                    null,
                    true,
                    readings ?: seasonal.readings,
                )
            }
        }

        return withSaintInfo(
            seasonal,
            date
        )
    }

    private suspend fun withSaintInfo(
        seasonal: ResolvedDay,
        date: LocalDate,
    ): ResolvedDay {

        val saint =
            repository.getSaintFor(
                date.toSwahiliTarehe()
            ) ?: return seasonal

        /*
         * A weekday slot can display the saint alongside
         * the normal weekday readings.
         */
        val isPlainWeekday =
            seasonal.day != null

        return if (isPlainWeekday) {

            seasonal.copy(
                title = "${seasonal.title} — ${saint.jina}",
                rangi = saint.rangi,
                saintOfTheDay = saint,
            )

        } else {

            /*
             * On Sundays and major celebrations, retain
             * the liturgical title/readings and expose
             * the saint only as additional information.
             */
            seasonal.copy(
                saintOfTheDay = saint
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fixed solemnity titles
    // ─────────────────────────────────────────────────────────────────────────

    private fun solemnityTitle(
        periodKey: String,
    ): String =
        when (periodKey) {

            "kutolewa_hekaluni_bwana" ->
                "Kutolewa Hekaluni kwa Bwana"

            "yosefu_bwana_arusi_wa_bikira_maria" ->
                "Yosefu, Bwana-arusi wa Bikira Maria"

            "tangazo_la_bwana" ->
                "Kutangazwa kwa Malaika kwa Bwana"

            "kuzaliwa_yohana_mbatizaji" ->
                "Kuzaliwa kwa Yohane Mbatizaji"

            "petro_na_paulo_mitume" ->
                "Petro na Paulo, Mitume"

            "kubadilika_sura_kwa_bwana" ->
                "Kubadilika Sura kwa Bwana"

            "kupalizwa_bikira_maria_mbinguni" ->
                "Kupalizwa Mbinguni kwa Bikira Maria"

            "kuinuliwa_msalaba_mtakatifu" ->
                "Kuinuliwa kwa Msalaba Mtakatifu"

            "watakatifu_wote" ->
                "Watakatifu Wote"

            "wafu_wote" ->
                "Wafu Wote"

            "kuwekwa_wakfu_kanisa_kuu_la_laterano" ->
                "Kuwekwa Wakfu kwa Kanisa Kuu la Laterano"

            "mimba_takatifu_ya_bikira_maria" ->
                "Kuchukuliwa Mimba Bila Dhambi kwa Bikira Maria"

            else ->
                periodKey
        }

    // ─────────────────────────────────────────────────────────────────────────
    // Date helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun weeksBetween(
        start: LocalDate,
        end: LocalDate,
    ): Long =
        java.time.temporal.ChronoUnit.WEEKS.between(
            start,
            end
        )
}