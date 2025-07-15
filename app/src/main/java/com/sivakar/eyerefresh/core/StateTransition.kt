package com.sivakar.eyerefresh.core

import com.sivakar.eyerefresh.core.Config

data class StateTransition(val newState: AppState, val sideEffects: List<SideEffect> = emptyList())

fun transition(currentState: AppState, event: AppEvent, config: Config): StateTransition {
    return when (currentState) {
        is AppState.Paused -> when (event) {
            is AppEvent.SchedulingTurnedOn -> {
                val scheduledTime = System.currentTimeMillis() + config.reminderIntervalMs
                StateTransition(
                    AppState.TimeLeftForNextRefresh(scheduledTime),
                    listOf(SideEffect.ScheduleEvent(AppEvent.RefreshDue, scheduledTime))
                )
            }
            // TODO; Also offer to do now? Add that as parameter to event?
            else -> StateTransition(currentState)
        }
        
        is AppState.TimeLeftForNextRefresh -> when (event) {
            is AppEvent.SchedulingPaused -> StateTransition(
                AppState.Paused,
                listOf(SideEffect.StopTimer, SideEffect.ClearNotification)
            )
            is AppEvent.RefreshDue -> {
                val scheduledTime = System.currentTimeMillis() + config.snoozeDurationMs
                StateTransition(
                    AppState.RefreshCanStart,
                    listOf(SideEffect.ShowNotification(NotificationKind.RefreshReminder))
                )
            }
            else -> StateTransition(currentState)
        }
        
        is AppState.RefreshCanStart -> when (event) {
            is AppEvent.SnoozeRequested -> {
                val scheduledTime = System.currentTimeMillis() + config.snoozeDurationMs
                StateTransition(
                    AppState.TimeLeftForNextRefresh(scheduledTime),
                    listOf(
                        SideEffect.ScheduleEvent(AppEvent.RefreshDue, scheduledTime),
                        SideEffect.ClearNotification
                    )
                )
            }
            is AppEvent.SchedulingPaused -> StateTransition(
                AppState.Paused,
                listOf(SideEffect.StopTimer, SideEffect.ClearNotification)
            )
            is AppEvent.RefreshStarted -> {
                val scheduledTime = System.currentTimeMillis() + config.breakDurationMs
                StateTransition(
                    AppState.RefreshHappening(System.currentTimeMillis()),
                    listOf(
                        SideEffect.ScheduleEvent(AppEvent.RefreshTimeUp, scheduledTime),
                        SideEffect.ClearNotification
                    )
                )
            }
            else -> StateTransition(currentState)
        }
        
        is AppState.RefreshHappening -> when (event) {
            is AppEvent.RefreshTimeUp -> StateTransition(
                AppState.WaitingForRefreshAcknowledgement,
                listOf(SideEffect.ShowNotification(NotificationKind.RefreshComplete))
            )
            else -> StateTransition(currentState)
        }
        
        is AppState.WaitingForRefreshAcknowledgement -> when (event) {
            is AppEvent.MarkRefreshCompleted -> {
                val scheduledTime = System.currentTimeMillis() + config.reminderIntervalMs
                StateTransition(
                    AppState.TimeLeftForNextRefresh(scheduledTime),
                    listOf(
                        SideEffect.ScheduleEvent(AppEvent.RefreshDue, scheduledTime),
                        SideEffect.ClearNotification
                    )
                )
            }
            is AppEvent.RefreshCouldNotHappen -> {
                val scheduledTime = System.currentTimeMillis() + config.snoozeDurationMs
                StateTransition(
                    AppState.RefreshCanStart,
                    listOf(
                        SideEffect.ClearNotification,
                        SideEffect.ShowNotification(NotificationKind.RefreshReminder)
                    )
                )
            }
            else -> StateTransition(currentState)
        }
    }
}