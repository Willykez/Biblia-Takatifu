package com.biblia.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.biblia.app.data.ReminderPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val state = ReminderPrefs(context).state.first()
                if (state.enabled) {
                    DailyReminderReceiver.ensureChannel(context)
                    ReminderScheduler.schedule(context, state.hour, state.minute)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
