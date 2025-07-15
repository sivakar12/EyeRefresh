package com.sivakar.eyerefresh.core

sealed class NotificationKind {
    data object RefreshReminder : NotificationKind() {
        val options = listOf(
            NotificationOption("Start Refresh", AppEvent.RefreshStarted),
            NotificationOption("Snooze", AppEvent.SnoozeRequested),
            NotificationOption("Pause", AppEvent.SchedulingPaused)
        )
    }
    
    data object RefreshComplete : NotificationKind() {
        val options = listOf(
            NotificationOption("I did it!", AppEvent.MarkRefreshCompleted),
            NotificationOption("I couldn't do it", AppEvent.RefreshCouldNotHappen)
        )
    }
}

data class NotificationOption(
    val label: String,
    val eventToSend: AppEvent
)

sealed class SideEffect {
    data class ScheduleEvent(val event: AppEvent, val timeInMillis: Long) : SideEffect()
    data class ShowNotification(val notificationKind: NotificationKind) : SideEffect()
    data object StopTimer : SideEffect()
    data object ClearNotification : SideEffect()
}