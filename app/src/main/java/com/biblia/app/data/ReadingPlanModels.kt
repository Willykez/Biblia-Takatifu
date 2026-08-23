package com.biblia.app.data

/** A row from `plans`. */
data class ReadingPlan(
    val id: Int,
    val title: String,
    val description: String,
)

enum class PlanPacing(val modeId: Int, val totalDays: Int, val label: String) {
    ONE_YEAR(1, 365, "Mwaka Mmoja"),
    SIX_MONTHS(2, 180, "Miezi Sita"),
    THREE_MONTHS(3, 90, "Miezi Mitatu"),
}

/** One chapter assigned to a plan day - [bookId] already resolved from `chapter_order`. */
data class PlanChapterRef(val bookId: Int, val bookTitle: String, val chapterNum: Int)

data class PlanProgress(val daysRead: Int, val totalDays: Int) {
    val fraction: Float get() = if (totalDays == 0) 0f else daysRead.toFloat() / totalDays
    val isComplete: Boolean get() = totalDays > 0 && daysRead >= totalDays
}
