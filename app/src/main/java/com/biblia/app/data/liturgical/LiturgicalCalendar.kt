package com.biblia.app.data.liturgical

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Pure date-math liturgical calendar engine. No data lookups here - everything is derived
 * from a date via Easter's computus and fixed offsets from it (or from Christmas/Advent).
 *
 * v1 scope, deliberately: Sundays and the major seasons/solemnities of the Lord and Mary.
 * Does NOT include the sanctoral cycle (saints' memorials/feasts) - see the SettingsScreen/
 * README notes on why that's a separate, much larger dataset problem, not more date math.
 * Also does NOT implement the full transfer rules for a fixed solemnity landing on a Sunday
 * of Advent/Lent/Easter (which outranks it and would bump the solemnity to the next open
 * day) - fixed solemnities here always show on their calendar date.
 */
object LiturgicalCalendar {

    /** The reference point used to derive Sunday Cycle A/B/C: Advent 2025 begins Year A. */
    private const val REFERENCE_ADVENT_CIVIL_YEAR = 2025

    /** Anonymous Gregorian algorithm (Meeus/Jones/Butcher) for the date of Easter Sunday. */
    fun easterSunday(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = (h + l - 7 * m + 114) % 31 + 1
        return LocalDate.of(year, month, day)
    }

    /** Most recent Sunday on or before [date]. */
    private fun sundayOnOrBefore(date: LocalDate): LocalDate {
        val offset = date.dayOfWeek.value % 7 // Sunday(7)->0, Monday(1)->1, ... Saturday(6)->6
        return date.minusDays(offset.toLong())
    }

    /**
     * First Sunday of Advent for the liturgical year that *begins* in [civilYear] - i.e. the
     * Sunday nearest Nov 30. Equivalently: the Sunday on/before Dec 24 of [civilYear], minus
     * 3 weeks (Advent always has exactly 4 Sundays before Christmas).
     */
    fun adventStart(civilYear: Int): LocalDate {
        val fourthAdventSunday = sundayOnOrBefore(LocalDate.of(civilYear, 12, 24))
        return fourthAdventSunday.minusWeeks(3)
    }

    /** Epiphany, observed the Sunday falling between Jan 2-8 (common outside the USA). */
    private fun epiphany(civilYear: Int): LocalDate {
        val jan2 = LocalDate.of(civilYear, 1, 2)
        return if (jan2.dayOfWeek == DayOfWeek.SUNDAY) jan2 else jan2.plusDays(
            (7 - jan2.dayOfWeek.value.toLong()) % 7
        )
    }

    /** Baptism of the Lord: the Sunday after Epiphany (Monday after, if Epiphany falls Jan 7-8). */
    private fun baptismOfTheLord(civilYear: Int): LocalDate {
        val epi = epiphany(civilYear)
        return if (epi.dayOfMonth >= 7) epi.plusDays(1) else epi.plusWeeks(1)
    }

    /** Everything anchored to a given liturgical-year start (the Advent that opens it). */
    private class YearAnchors(adventCivilYear: Int) {
        val adventStart: LocalDate = adventStart(adventCivilYear)
        val christmas: LocalDate = LocalDate.of(adventCivilYear, 12, 25)
        val easterCivilYear = adventCivilYear + 1
        val epiphany: LocalDate = epiphany(easterCivilYear)
        val baptism: LocalDate = baptismOfTheLord(easterCivilYear)
        val easter: LocalDate = easterSunday(easterCivilYear)
        val ashWednesday: LocalDate = easter.minusDays(46)
        val palmSunday: LocalDate = easter.minusDays(7)
        val pentecost: LocalDate = easter.plusWeeks(7)
        val christKing: LocalDate = adventStart(easterCivilYear).minusWeeks(1)
        val nextAdventStart: LocalDate = adventStart(easterCivilYear)
        val sundayCycle: SundayCycle = SundayCycle.entries[
            (((adventCivilYear - REFERENCE_ADVENT_CIVIL_YEAR) % 3) + 3) % 3
        ]
        val weekdayCycle: Int = if (easterCivilYear % 2 == 0) 2 else 1
    }

    fun dayFor(date: LocalDate): LiturgicalDay {
        val adventCivilYear = if (!date.isBefore(adventStart(date.year))) date.year else date.year - 1
        val y = YearAnchors(adventCivilYear)
        val isSunday = date.dayOfWeek == DayOfWeek.SUNDAY

        val fixedSolemnity = fixedSolemnityTitle(date)

        val (season, rank, color, otWeek, title) = when {
            !date.isBefore(y.adventStart) && date.isBefore(y.christmas) ->
                adventInfo(date, y, isSunday)
            !date.isBefore(y.christmas) && !date.isAfter(y.baptism) ->
                christmasInfo(date, y, isSunday)
            date.isAfter(y.baptism) && date.isBefore(y.ashWednesday) ->
                ordinaryTimeInfo(date, y, isSunday, beforeLent = true)
            !date.isBefore(y.ashWednesday) && date.isBefore(y.easter) ->
                lentInfo(date, y, isSunday)
            !date.isBefore(y.easter) && !date.isAfter(y.pentecost) ->
                easterInfo(date, y, isSunday)
            else ->
                ordinaryTimeInfo(date, y, isSunday, beforeLent = false)
        }

        val finalTitle = fixedSolemnity?.takeIf { season == LiturgicalSeason.ORDINARY_TIME || !isSunday } ?: title
        val finalRank = if (fixedSolemnity != null && (season == LiturgicalSeason.ORDINARY_TIME || !isSunday)) {
            LiturgicalRank.SOLEMNITY
        } else rank
        val finalColor = if (fixedSolemnity != null && (season == LiturgicalSeason.ORDINARY_TIME || !isSunday)) {
            LiturgicalColor.WHITE
        } else color

        return LiturgicalDay(
            date = date,
            season = season,
            isSunday = isSunday,
            ordinaryTimeWeek = otWeek,
            sundayCycle = y.sundayCycle,
            weekdayCycle = y.weekdayCycle,
            rank = finalRank,
            color = finalColor,
            title = finalTitle,
            lectionaryKey = lectionaryKeyFor(finalTitle, otWeek, isSunday, season),
        )
    }

    private data class DayInfo(
        val season: LiturgicalSeason,
        val rank: LiturgicalRank,
        val color: LiturgicalColor,
        val otWeek: Int?,
        val title: String,
    )

    private fun adventInfo(date: LocalDate, y: YearAnchors, isSunday: Boolean): DayInfo {
        val week = (java.time.temporal.ChronoUnit.WEEKS.between(y.adventStart, sundayOnOrBefore(date)) + 1).toInt()
        val title = if (isSunday) "Jumapili ya $week ya Majilio" else "Wiki ya $week ya Majilio"
        return DayInfo(LiturgicalSeason.ADVENT, if (isSunday) LiturgicalRank.SUNDAY else LiturgicalRank.WEEKDAY, LiturgicalColor.VIOLET, null, title)
    }

    private fun christmasInfo(date: LocalDate, y: YearAnchors, isSunday: Boolean): DayInfo {
        val title = when {
            date == y.christmas -> "Kuzaliwa kwa Bwana (Noeli)"
            date == y.baptism -> "Ubatizo wa Bwana"
            date == LocalDate.of(y.christmas.year, 1, 1) -> "Maria Mama wa Mungu"
            date == y.epiphany -> "Epifania ya Bwana"
            isSunday -> "Jumapili ndani ya Oktava ya Noeli"
            else -> "Wiki ya Noeli"
        }
        val rank = when (date) {
            y.christmas, LocalDate.of(y.christmas.year, 1, 1), y.epiphany, y.baptism -> LiturgicalRank.SOLEMNITY
            else -> if (isSunday) LiturgicalRank.FEAST else LiturgicalRank.WEEKDAY
        }
        return DayInfo(LiturgicalSeason.CHRISTMAS, rank, LiturgicalColor.WHITE, null, title)
    }

    private fun lentInfo(date: LocalDate, y: YearAnchors, isSunday: Boolean): DayInfo {
        val firstSundayOfLent = sundayOnOrBefore(y.ashWednesday).plusWeeks(1)
        val week = (java.time.temporal.ChronoUnit.WEEKS.between(firstSundayOfLent, sundayOnOrBefore(date)) + 1).toInt()
        val title = when {
            date == y.ashWednesday -> "Jumatano ya Majivu"
            date == y.palmSunday -> "Jumapili ya Matawi (Mateso ya Bwana)"
            date == y.easter.minusDays(3) -> "Alhamisi Kuu"
            date == y.easter.minusDays(2) -> "Ijumaa Kuu"
            date == y.easter.minusDays(1) -> "Jumamosi Kuu"
            isSunday -> "Jumapili ya $week ya Kwaresima"
            else -> "Wiki ya $week ya Kwaresima"
        }
        val rank = when (date) {
            y.ashWednesday, y.palmSunday -> LiturgicalRank.FEAST
            else -> if (isSunday) LiturgicalRank.SUNDAY else LiturgicalRank.WEEKDAY
        }
        val weekNum = week.coerceIn(1, 6)
        return DayInfo(LiturgicalSeason.LENT, rank, LiturgicalColor.VIOLET, weekNum, title)
    }

    private fun easterInfo(date: LocalDate, y: YearAnchors, isSunday: Boolean): DayInfo {
        val week = (java.time.temporal.ChronoUnit.WEEKS.between(y.easter, sundayOnOrBefore(date)) + 1).toInt()
        val title = when {
            date == y.easter -> "Pasaka ya Ufufuo wa Bwana"
            date == y.easter.plusDays(7) -> "Jumapili ya Huruma ya Mungu (Pasaka 2)"
            date == y.easter.plusDays(39) -> "Kupaa kwa Bwana Mbinguni"
            date == y.pentecost -> "Pentekoste"
            isSunday -> "Jumapili ya $week ya Pasaka"
            else -> "Wiki ya $week ya Pasaka"
        }
        val rank = when (date) {
            y.easter, y.pentecost -> LiturgicalRank.SOLEMNITY
            y.easter.plusDays(39) -> LiturgicalRank.SOLEMNITY
            else -> if (isSunday) LiturgicalRank.SUNDAY else LiturgicalRank.WEEKDAY
        }
        return DayInfo(LiturgicalSeason.EASTER, rank, LiturgicalColor.WHITE, null, title)
    }

    private fun ordinaryTimeInfo(date: LocalDate, y: YearAnchors, isSunday: Boolean, beforeLent: Boolean): DayInfo {
        val s = sundayOnOrBefore(date)
        val weekNum = if (beforeLent) {
            (java.time.temporal.ChronoUnit.WEEKS.between(y.baptism, s) + 1).toInt()
        } else {
            34 - (java.time.temporal.ChronoUnit.WEEKS.between(s, y.christKing)).toInt()
        }
        // Special-named solemnities of the Lord that replace a plain Ordinary Time Sunday.
        val special = when {
            date == y.easter.plusWeeks(8) -> "Utatu Mtakatifu"
            date == y.easter.plusWeeks(9) -> "Fumbo la Ekaristi Takatifu (Corpus Christi)"
            date == y.christKing -> "Kristo Mfalme wa Ulimwengu"
            else -> null
        }
        val title = special ?: if (isSunday) "Jumapili ya $weekNum ya Kawaida" else "Wiki ya $weekNum ya Kawaida"
        val rank = if (special != null) LiturgicalRank.SOLEMNITY else if (isSunday) LiturgicalRank.SUNDAY else LiturgicalRank.WEEKDAY
        return DayInfo(LiturgicalSeason.ORDINARY_TIME, rank, LiturgicalColor.GREEN, weekNum, title)
    }

    /** Fixed-date solemnities/feasts that don't depend on Easter. Deliberately a short v1 list. */
    private fun fixedSolemnityTitle(date: LocalDate): String? = when (date.monthValue to date.dayOfMonth) {
        3 to 19 -> "Yosefu, Bwana-arusi wa Bikira Maria"
        3 to 25 -> "Kutangazwa kwa Malaika kwa Bwana"
        8 to 15 -> "Kupalizwa Mbinguni kwa Bikira Maria"
        11 to 1 -> "Watakatifu Wote"
        12 to 8 -> "Kuchukuliwa Mimba Bila Dhambi kwa Bikira Maria"
        else -> null
    }

    private fun lectionaryKeyFor(title: String, otWeek: Int?, isSunday: Boolean, season: LiturgicalSeason): String =
        when {
            season == LiturgicalSeason.ORDINARY_TIME && isSunday && otWeek != null -> "OT_SUNDAY_$otWeek"
            else -> title
        }
}
