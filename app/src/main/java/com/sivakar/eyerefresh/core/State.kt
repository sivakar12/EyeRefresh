package com.sivakar.eyerefresh.core

sealed class AppState {
    data object Paused : AppState()
    data class TimeLeftForNextRefresh(val scheduledTimeInMillis: Long) : AppState()
    data object RefreshCanStart : AppState()
    data class RefreshHappening(val startTimeInMillis: Long) : AppState()
    data object WaitingForRefreshAcknowledgement : AppState()
    
    companion object {
        /**
         * Derives the current state from the last event that occurred.
         * This is a simple mapping that doesn't require replaying the entire event history.
         */
        fun fromLastEvent(lastEvent: AppEvent?, config: Config): AppState {
            return when (lastEvent) {
                null -> Paused
                is AppEvent.SchedulingTurnedOn -> {
                    val scheduledTime = System.currentTimeMillis() + config.reminderIntervalMs
                    TimeLeftForNextRefresh(scheduledTime)
                }
                is AppEvent.SchedulingPaused -> Paused
                is AppEvent.RefreshDue -> RefreshCanStart
                is AppEvent.SnoozeRequested -> {
                    val scheduledTime = System.currentTimeMillis() + config.snoozeDurationMs
                    TimeLeftForNextRefresh(scheduledTime)
                }
                is AppEvent.RefreshStarted -> RefreshHappening(System.currentTimeMillis())
                is AppEvent.RefreshTimeUp -> WaitingForRefreshAcknowledgement
                is AppEvent.MarkRefreshCompleted -> {
                    val scheduledTime = System.currentTimeMillis() + config.reminderIntervalMs
                    TimeLeftForNextRefresh(scheduledTime)
                }
                is AppEvent.RefreshCouldNotHappen -> RefreshCanStart
            }
        }
    }
}