package com.biblia.app.data.liturgical

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.FileOutputStream

/**
 * Opens the bundled Swahili lectionary database (assets/lectionary_swahili.sqlite) - your
 * own dataset (readings/watakatifu/rangi_za_liturujia/mizunguko_ya_miaka), not a live API.
 * Same copy-on-first-launch pattern as BibleDatabase.kt.
 */
object LectionaryDatabase {
    private const val DB_NAME = "lectionary_swahili.sqlite"
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
                dbFile.parentFile?.mkdirs()
                context.assets.open(DB_NAME).use { input ->
                    FileOutputStream(dbFile).use { output -> input.copyTo(output, bufferSize = 1 shl 16) }
                }
                versionFile.writeText(DB_VERSION.toString())
            }
            val db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            instance = db
            return db
        }
    }

    private fun File.readTextOrNull(): String? = if (exists()) readText() else null
}
