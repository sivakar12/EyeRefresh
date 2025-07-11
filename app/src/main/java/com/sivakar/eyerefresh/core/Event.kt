package com.sivakar.eyerefresh.core

import java.io.Serializable

sealed class AppEvent : Serializable {
    data object SchedulingTurnedOn : AppEvent()
    data object SchedulingPaused : AppEvent()
    data object RefreshDue : AppEvent()
    data object SnoozeRequested : AppEvent()
    data object RefreshStarted : AppEvent()
    data object RefreshTimeUp : AppEvent()
    data object MarkRefreshCompleted : AppEvent()
    data object RefreshCouldNotHappen : AppEvent()
    
    companion object {
        fun fromString(eventString: String): AppEvent? {
            return when (eventString) {
                "SchedulingTurnedOn" -> SchedulingTurnedOn
                "SchedulingPaused" -> SchedulingPaused
                "RefreshDue" -> RefreshDue
                "SnoozeRequested" -> SnoozeRequested
                "RefreshStarted" -> RefreshStarted
                "RefreshTimeUp" -> RefreshTimeUp
                "MarkRefreshCompleted" -> MarkRefreshCompleted
                "RefreshCouldNotHappen" -> RefreshCouldNotHappen
                else -> null
            }
        }
    }
}