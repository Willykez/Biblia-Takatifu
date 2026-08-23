package com.biblia.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biblia.app.data.PlanChapterRef
import com.biblia.app.data.PlanPacing
import com.biblia.app.data.PlanProgress
import com.biblia.app.data.ReadingPlan
import com.biblia.app.data.ReadingPlanPrefs
import com.biblia.app.data.ReadingPlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReadingPlanViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReadingPlanRepository(application)
    private val prefs = ReadingPlanPrefs(application)

    private val _plans = MutableStateFlow<List<ReadingPlan>>(emptyList())
    val plans: StateFlow<List<ReadingPlan>> = _plans.asStateFlow()

    private val _progressByPlan = MutableStateFlow<Map<Int, PlanProgress>>(emptyMap())
    val progressByPlan: StateFlow<Map<Int, PlanProgress>> = _progressByPlan.asStateFlow()

    private val _pacingByPlan = MutableStateFlow<Map<Int, PlanPacing>>(emptyMap())
    val pacingByPlan: StateFlow<Map<Int, PlanPacing>> = _pacingByPlan.asStateFlow()

    init {
        viewModelScope.launch {
            val loaded = repository.getPlans()
            _plans.value = loaded
            loaded.forEach { plan ->
                viewModelScope.launch {
                    prefs.pacingFor(plan.id).collect { pacing ->
                        _pacingByPlan.value = _pacingByPlan.value + (plan.id to pacing)
                        refreshProgress(plan.id, pacing)
                    }
                }
            }
        }
    }

    private suspend fun refreshProgress(planId: Int, pacing: PlanPacing) {
        _progressByPlan.value = _progressByPlan.value + (planId to repository.getProgress(planId, pacing))
    }

    fun setPacing(planId: Int, pacing: PlanPacing) {
        viewModelScope.launch { prefs.setPacing(planId, pacing) }
    }

    suspend fun getDayChapters(planId: Int, pacing: PlanPacing, day: Int): List<PlanChapterRef> =
        repository.getDayChapters(planId, pacing, day)

    suspend fun getNextUnreadDay(planId: Int, pacing: PlanPacing): Int =
        repository.getNextUnreadDay(planId, pacing)

    suspend fun isDayRead(planId: Int, day: Int): Boolean = repository.isDayRead(planId, day)

    fun setDayRead(planId: Int, pacing: PlanPacing, day: Int, read: Boolean) {
        viewModelScope.launch {
            repository.setDayRead(planId, day, read)
            refreshProgress(planId, pacing)
        }
    }
}
