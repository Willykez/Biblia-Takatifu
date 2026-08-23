package com.biblia.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.planPrefsDataStore by preferencesDataStore(name = "biblia_plan_prefs")

class ReadingPlanPrefs(private val context: Context) {
    private fun key(planId: Int) = intPreferencesKey("plan_${planId}_mode_id")

    fun pacingFor(planId: Int): Flow<PlanPacing> = context.planPrefsDataStore.data.map { p ->
        val modeId = p[key(planId)] ?: PlanPacing.ONE_YEAR.modeId
        PlanPacing.entries.firstOrNull { it.modeId == modeId } ?: PlanPacing.ONE_YEAR
    }

    suspend fun setPacing(planId: Int, pacing: PlanPacing) {
        context.planPrefsDataStore.edit { it[key(planId)] = pacing.modeId }
    }
}
