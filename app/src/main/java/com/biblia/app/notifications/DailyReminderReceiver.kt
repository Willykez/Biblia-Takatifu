package com.biblia.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.biblia.app.MainActivity
import com.biblia.app.data.BibleRepository
import com.biblia.app.data.CuratedVerses
import com.biblia.app.data.ReminderPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

const val REMINDER_CHANNEL_ID = "biblia_daily_reminder"
private const val NOTIFICATION_ID = 4202

/** What actually goes in the notification: reference + text. */
private data class ReminderVerse(val bookTitle: String, val chapterNum: Int, val verseNum: Int, val text: String)

/**
 * Runs outside any Activity/ViewModel scope, so it builds its own small BibleRepository
 * instance directly from the applicationContext rather than reusing BibleViewModel (which
 * needs a ViewModelStoreOwner this receiver doesn't have). Picks from CuratedVerses - the
 * app is Bible-only now, no liturgical calendar to source a "Gospel of the day" from.
 */
class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ensureChannel(context)
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                postNotification(context)
                val prefs = ReminderPrefs(context).state.first()
                if (prefs.enabled) ReminderScheduler.scheduleNext(context, prefs.hour, prefs.minute)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun postNotification(context: Context) {
        val bibleRepository = BibleRepository(context)
        val verse = curatedVerse(bibleRepository) ?: return

        val contentIntent = PendingIntent.getActivity(
            context, 0, MainActivity.newIntent(context),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentTitle("${verse.bookTitle} ${verse.chapterNum}:${verse.verseNum}")
            .setContentText(verse.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(verse.text))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val canPost = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (canPost) {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    private suspend fun curatedVerse(bibleRepository: BibleRepository): ReminderVerse? = runCatching {
        val curated = CuratedVerses.forDayOfYear(LocalDate.now().dayOfYear)
        val bookId = bibleRepository.getBookByTitle(curated.bookTitle)?.id ?: return@runCatching null
        val verse = bibleRepository.getVerses(bookId, curated.chapter)
            .firstOrNull { !it.isHeading && it.position == curated.verse } ?: return@runCatching null
        ReminderVerse(curated.bookTitle, curated.chapter, verse.position, verse.primaryText)
    }.getOrNull()

    companion object {
        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(NotificationManager::class.java)
                if (manager.getNotificationChannel(REMINDER_CHANNEL_ID) == null) {
                    manager.createNotificationChannel(
                        NotificationChannel(REMINDER_CHANNEL_ID, "Ukumbusho wa Kusoma", NotificationManager.IMPORTANCE_DEFAULT).apply {
                            description = "Ukumbusho wa kila siku wa mstari wa Biblia"
                        },
                    )
                }
            }
        }
    }
}
