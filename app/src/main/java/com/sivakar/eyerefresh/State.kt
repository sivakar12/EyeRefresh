package com.sivakar.eyerefresh

sealed class AppState {
    data object RemindersPaused : AppState()
    data class ReminderScheduled(val timeInMillis: Long, val countdownMillis: Long = 0L) : AppState()
    data object ReminderSent : AppState()
    data class RefreshHappening(val startTimeInMillis: Long) : AppState()
    data object RefreshComplete : AppState()
}