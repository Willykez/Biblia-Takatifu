package com.biblia.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Opens the bundled Swahili/English Bible database (assets/bible_swahili.sqlite).
 *
 * Plain android.database.sqlite — no Room — since the schema is fixed content shipped
 * with the app, not something the app itself needs to migrate. First launch copies the
 * ~18MB asset into the app's private database directory; every launch after that just
 * opens the copy in place.
 *
 * Tables (see BibleModels.kt for the Kotlin shape of each row):
 *   chapters (_id, title, num, mode, short_title, ntitle)  — the 66 books
 *   texts    (_id, chapter_id, chapter_num, position, rank, text, ntext,
 *             head, bookmark, highlight, note, bookmark_date, highlight_date, note_date)
 *   plans, reading_plans, reading_days                     — reading-plan tables (not wired up yet)
 */
object BibleDatabase {
    private const val DB_NAME = "bible_swahili.sqlite"
    private const val DB_VERSION = 1

    @Volatile private var instance: SQLiteDatabase? = null

    fun getInstance(context: Context): SQLiteDatabase {
        instance?.let { if (it.isOpen) return it }
        synchronized(this) {
            instance?.let { if (it.isOpen) return it }
            val dbFile = context.getDatabasePath(DB_NAME)
            val versionFile = File(dbFile.parentFile, "$DB_NAME.version")
            val needsCopy = !dbFile.exists() || versionFile.readTextOrNull() != DB_VERSION.toString()
            if (needsCopy) {
                copyFromAssets(context, dbFile)
                versionFile.parentFile?.mkdirs()
                versionFile.writeText(DB_VERSION.toString())
            }
            val db = SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READWRITE,
            )
            instance = db
            return db
        }
    }

    private fun copyFromAssets(context: Context, dest: File) {
        dest.parentFile?.mkdirs()
        context.assets.open(DB_NAME).use { input: InputStream ->
            FileOutputStream(dest).use { output: OutputStream ->
                input.copyTo(output, bufferSize = 1 shl 16)
            }
        }
    }

    private fun File.readTextOrNull(): String? = if (exists()) readText() else null
}
