package com.sivakar.eyerefresh.core

sealed class AppState {
    data object Paused : AppState()
    data class TimeLeftForNextRefresh(val scheduledTimeInMillis: Long) : AppState()
    data object RefreshCanStart : AppState()
    data class RefreshHappening(val startTimeInMillis: Long) : AppState()
    data object WaitingForRefreshAcknowledgement : AppState()
}