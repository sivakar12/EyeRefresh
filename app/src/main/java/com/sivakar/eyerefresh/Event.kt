package com.sivakar.eyerefresh

import java.io.Serializable

sealed class AppEvent : Serializable {
    data object NotificationsTurnedOn : AppEvent()
    data object NotificationsPaused : AppEvent()
    data object NotificationDue : AppEvent()
    data object RefreshStarted : AppEvent()
    data object RefreshTimeDone : AppEvent()
    data object RefreshMarkedComplete : AppEvent()
    data object RefreshAbandoned : AppEvent()
    data object SnoozeRequested : AppEvent()
}