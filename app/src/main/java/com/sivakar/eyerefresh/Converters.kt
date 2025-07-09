package com.sivakar.eyerefresh

import androidx.room.TypeConverter
import com.sivakar.eyerefresh.core.AppEvent

class Converters {
    @TypeConverter
    fun fromAppEvent(appEvent: AppEvent): String {
        return when (appEvent) {
            is AppEvent.SchedulingTurnedOn -> "SchedulingTurnedOn"
            is AppEvent.SchedulingPaused -> "SchedulingPaused"
            is AppEvent.RefreshDue -> "RefreshDue"
            is AppEvent.SnoozeRequested -> "SnoozeRequested"
            is AppEvent.RefreshStarted -> "RefreshStarted"
            is AppEvent.RefreshTimeUp -> "RefreshTimeUp"
            is AppEvent.MarkRefreshCompleted -> "MarkRefreshCompleted"
            is AppEvent.RefreshCouldNotHappen -> "RefreshCouldNotHappen"
        }
    }

    @TypeConverter
    fun toAppEvent(appEventString: String): AppEvent {
        return when (appEventString) {
            "SchedulingTurnedOn" -> AppEvent.SchedulingTurnedOn
            "SchedulingPaused" -> AppEvent.SchedulingPaused
            "RefreshDue" -> AppEvent.RefreshDue
            "SnoozeRequested" -> AppEvent.SnoozeRequested
            "RefreshStarted" -> AppEvent.RefreshStarted
            "RefreshTimeUp" -> AppEvent.RefreshTimeUp
            "MarkRefreshCompleted" -> AppEvent.MarkRefreshCompleted
            "RefreshCouldNotHappen" -> AppEvent.RefreshCouldNotHappen
            else -> AppEvent.SchedulingPaused // Default fallback
        }
    }
}