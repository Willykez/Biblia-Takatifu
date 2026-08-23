package com.biblia.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * `chapter_order` in `reading_plans` is the book's 1-66 canonical position (1=Mwanzo,
 * 66=Ufunuo wa Yohana) - the exact same ordering the `chapters` table's own ids already use
 * (Mwanzo=8022 .. Ufunuo wa Yohana=8087), so bookId = 8021 + chapter_order. Confirmed against
 * the actual data (plan 1's Chronological reading jumps to chapter_order=18 on day 4, which
 * resolves to Ayubu/Job - the classic chronological-plan placement of Job alongside the
 * patriarchal narratives).
 */
private const val BOOK_ID_OFFSET = 8021

class ReadingPlanRepository(context: Context) {
    private val appContext = context.applicationContext
    private val db: SQLiteDatabase get() = BibleDatabase.getInstance(appContext)

    suspend fun getPlans(): List<ReadingPlan> = withContext(Dispatchers.IO) {
        db.rawQuery("SELECT _id, title, description FROM plans ORDER BY order_val ASC", null).use { c ->
            buildList { while (c.moveToNext()) add(ReadingPlan(c.getInt(0), c.getString(1) ?: "", c.getString(2) ?: "")) }
        }
    }

    /** The chapters assigned to one day of a plan, with book titles already joined in. */
    suspend fun getDayChapters(planId: Int, pacing: PlanPacing, day: Int): List<PlanChapterRef> =
        withContext(Dispatchers.IO) {
            db.rawQuery(
                "SELECT rp.chapter_order, rp.chapter_num, c.title " +
                    "FROM reading_plans rp JOIN chapters c ON c._id = ? + rp.chapter_order " +
                    "WHERE rp.plan_id = ? AND rp.mode_id = ? AND rp.day = ? " +
                    "ORDER BY rp.chapter_order ASC, rp.chapter_num ASC",
                arrayOf(BOOK_ID_OFFSET.toString(), planId.toString(), pacing.modeId.toString(), day.toString()),
            ).use { c ->
                buildList {
                    while (c.moveToNext()) {
                        add(PlanChapterRef(bookId = BOOK_ID_OFFSET + c.getInt(0), chapterNum = c.getInt(1), bookTitle = c.getString(2)))
                    }
                }
            }
        }

    suspend fun getProgress(planId: Int, pacing: PlanPacing): PlanProgress = withContext(Dispatchers.IO) {
        val daysRead = db.rawQuery(
            "SELECT COUNT(*) FROM reading_days WHERE plan_id = ? AND readed = 1",
            arrayOf(planId.toString()),
        ).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }
        PlanProgress(daysRead, pacing.totalDays)
    }

    /** First unread day (1-indexed) up to [PlanPacing.totalDays], or that total if every day is read. */
    suspend fun getNextUnreadDay(planId: Int, pacing: PlanPacing): Int = withContext(Dispatchers.IO) {
        val readDays = db.rawQuery(
            "SELECT day FROM reading_days WHERE plan_id = ? AND readed = 1",
            arrayOf(planId.toString()),
        ).use { c -> buildList { while (c.moveToNext()) add(c.getInt(0)) } }.toSet()
        (1..pacing.totalDays).firstOrNull { it !in readDays } ?: pacing.totalDays
    }

    suspend fun isDayRead(planId: Int, day: Int): Boolean = withContext(Dispatchers.IO) {
        db.rawQuery(
            "SELECT readed FROM reading_days WHERE plan_id = ? AND day = ? LIMIT 1",
            arrayOf(planId.toString(), day.toString()),
        ).use { c -> c.moveToFirst() && c.getInt(0) == 1 }
    }

    suspend fun setDayRead(planId: Int, day: Int, read: Boolean) = withContext(Dispatchers.IO) {
        val exists = db.rawQuery(
            "SELECT _id FROM reading_days WHERE plan_id = ? AND day = ? LIMIT 1",
            arrayOf(planId.toString(), day.toString()),
        ).use { it.moveToFirst() }
        if (exists) {
            db.execSQL("UPDATE reading_days SET readed = ? WHERE plan_id = ? AND day = ?", arrayOf<Any>(if (read) 1 else 0, planId, day))
        } else {
            db.execSQL("INSERT INTO reading_days (plan_id, day, readed) VALUES (?, ?, ?)", arrayOf<Any>(planId, day, if (read) 1 else 0))
        }
    }
}
