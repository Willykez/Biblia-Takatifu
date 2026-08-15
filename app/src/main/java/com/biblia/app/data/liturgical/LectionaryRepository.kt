package com.biblia.app.data.liturgical

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LectionaryRepository(context: Context) {
    private val appContext = context.applicationContext
    private val db: SQLiteDatabase get() = LectionaryDatabase.getInstance(appContext)

    suspend fun getReading(
        season: String,
        periodKey: String,
        entryKey: String?,
        day: String?,
        mwakaLiturujia: String? = null,
    ): ReadingSet? = withContext(Dispatchers.IO) {
        val where = StringBuilder("season = ? AND period_key = ?")
        val args = mutableListOf(season, periodKey)
        if (entryKey != null) { where.append(" AND entry_key = ?"); args += entryKey } else where.append(" AND entry_key IS NULL")
        if (day != null) { where.append(" AND day = ?"); args += day } else where.append(" AND day IS NULL")
        if (mwakaLiturujia != null) { where.append(" AND mwaka_liturujia = ?"); args += mwakaLiturujia }
        db.rawQuery(
            "SELECT somo_la_kwanza, wimbo_la_katikati, somo_la_pili, shangilio, injili, mwaka_liturujia " +
                "FROM readings WHERE $where LIMIT 1",
            args.toTypedArray(),
        ).use { c -> if (c.moveToFirst()) c.toReadingSet() else null }
    }

    /** For desemba_17_hadi_24 / oktava / baada_ya_epifania - day holds a free-text key like "Des 17". */
    suspend fun getReadingByFreeTextDay(season: String, periodKey: String, dayText: String): ReadingSet? =
        withContext(Dispatchers.IO) {
            db.rawQuery(
                "SELECT somo_la_kwanza, wimbo_la_katikati, somo_la_pili, shangilio, injili, mwaka_liturujia " +
                    "FROM readings WHERE season = ? AND period_key = ? AND day LIKE ? LIMIT 1",
                arrayOf(season, periodKey, "$dayText%"),
            ).use { c -> if (c.moveToFirst()) c.toReadingSet() else null }
        }

    suspend fun getSaintFor(tarehe: String): SaintEntry? = withContext(Dispatchers.IO) {
        db.rawQuery(
            "SELECT tarehe, jina, daraja, rangi FROM watakatifu WHERE tarehe = ? LIMIT 1",
            arrayOf(tarehe),
        ).use { c ->
            if (c.moveToFirst()) SaintEntry(c.getString(0), c.getString(1), c.getString(2), c.getString(3)) else null
        }
    }

    /** Returns "A"/"B"/"C" and "I"/"II" for a liturgical year; null if outside the bundled 2024-2035 table. */
    suspend fun getCycleFor(liturgicalYear: Int): Pair<String, String>? = withContext(Dispatchers.IO) {
        db.rawQuery(
            "SELECT mzunguko_wa_dominika, mzunguko_wa_wiki FROM mizunguko_ya_miaka WHERE mwaka = ?",
            arrayOf(liturgicalYear.toString()),
        ).use { c -> if (c.moveToFirst()) c.getString(0) to c.getString(1) else null }
    }

    private fun Cursor.toReadingSet() = ReadingSet(
        somoLaKwanza = getStringOrNull(0),
        wimboLaKatikati = getStringOrNull(1),
        somoLaPili = getStringOrNull(2),
        shangilio = getStringOrNull(3),
        injili = getStringOrNull(4),
        mwakaLiturujia = getStringOrNull(5),
    )

    private fun Cursor.getStringOrNull(index: Int): String? = if (isNull(index)) null else getString(index)
}
