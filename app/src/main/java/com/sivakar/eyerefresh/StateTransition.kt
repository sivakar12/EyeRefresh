package com.sivakar.eyerefresh
import com.sivakar.eyerefresh.AppState
import com.sivakar.eyerefresh.SideEffect
import com.sivakar.eyerefresh.AppEvent

data class StateTransition(val newState: AppState, val sideEffect: SideEffect? = null)

fun transition(currentState: AppState, event: AppEvent, config: Config): StateTransition {
    return when (currentState) {
        is AppState.RemindersPaused -> when (event) {
            is AppEvent.NotificationsTurnedOn -> {
                val scheduledTime = System.currentTimeMillis() + config.reminderIntervalMs
                StateTransition(
                    AppState.ReminderScheduled(scheduledTime, config.reminderIntervalMs),
                    SideEffect.ScheduleEvent(AppEvent.NotificationDue, scheduledTime)
                )
            }
            // TODO; Also offer to do now? Add that as parameter to event?
            else -> StateTransition(currentState)
        }
        is AppState.ReminderScheduled ->  when (event) {
            is AppEvent.NotificationDue -> StateTransition(
                AppState.ReminderSent, 
                SideEffect.ShowNotification(
                    "Time for Eye Refresh!",
                    "Take a 20-second break to look at something 20 feet away",
                    listOf(
                        NotificationOption("Start Break", AppEvent.RefreshStarted),
                        NotificationOption("Snooze", AppEvent.SnoozeRequested),
                        NotificationOption("Pause", AppEvent.NotificationsPaused)
                    )
                )
            )
            is AppEvent.NotificationsPaused -> StateTransition(AppState.RemindersPaused)
            else -> StateTransition(currentState)
        }

        is AppState.ReminderSent -> when (event) {
            is AppEvent.NotificationsPaused -> StateTransition(AppState.RemindersPaused)
            is AppEvent.SnoozeRequested -> {
                val scheduledTime = System.currentTimeMillis() + config.snoozeDurationMs
                StateTransition(
                    AppState.ReminderScheduled(scheduledTime, config.snoozeDurationMs),
                    SideEffect.ScheduleEvent(AppEvent.NotificationDue, scheduledTime)
                )
            }
            is AppEvent.RefreshStarted -> StateTransition(
                AppState.RefreshHappening(System.currentTimeMillis()),
                SideEffect.ScheduleEvent(AppEvent.RefreshTimeDone, System.currentTimeMillis() + config.breakDurationMs)
            )
            else -> StateTransition(currentState)
        }

        is AppState.RefreshHappening -> when (event) {
            is AppEvent.RefreshTimeDone -> StateTransition(
                AppState.RefreshComplete,
                SideEffect.ShowNotification(
                    "Refresh Complete",
                    "Your 20-second refresh is done!",
                    listOf(
                        NotificationOption("Complete", AppEvent.RefreshMarkedComplete),
                        NotificationOption("Skip", AppEvent.RefreshAbandoned)
                    )
                )
            )
            is AppEvent.RefreshMarkedComplete -> {
                val scheduledTime = System.currentTimeMillis() + config.reminderIntervalMs
                StateTransition(
                    AppState.ReminderScheduled(scheduledTime, config.reminderIntervalMs),
                    SideEffect.ScheduleEvent(AppEvent.NotificationDue, scheduledTime)
                )
            }
            is AppEvent.RefreshAbandoned -> {
                val scheduledTime = System.currentTimeMillis() + config.snoozeDurationMs
                StateTransition(
                    AppState.ReminderScheduled(scheduledTime, config.snoozeDurationMs),
                    SideEffect.ScheduleEvent(AppEvent.NotificationDue, scheduledTime)
                )
            }
            is AppEvent.RefreshStarted -> StateTransition(currentState) // Already in refresh state, keep existing start time
            is AppEvent.NotificationsPaused -> StateTransition(AppState.RemindersPaused)
            else -> StateTransition(currentState)
        }
        
        is AppState.RefreshComplete -> when (event) {
            is AppEvent.RefreshMarkedComplete -> {
                val scheduledTime = System.currentTimeMillis() + config.reminderIntervalMs
                StateTransition(
                    AppState.ReminderScheduled(scheduledTime, config.reminderIntervalMs),
                    SideEffect.ScheduleEvent(AppEvent.NotificationDue, scheduledTime)
                )
            }
            is AppEvent.RefreshAbandoned -> {
                val scheduledTime = System.currentTimeMillis() + config.snoozeDurationMs
                StateTransition(
                    AppState.ReminderScheduled(scheduledTime, config.snoozeDurationMs),
                    SideEffect.ScheduleEvent(AppEvent.NotificationDue, scheduledTime)
                )
            }
            is AppEvent.NotificationsPaused -> StateTransition(AppState.RemindersPaused)
            else -> StateTransition(currentState)
        }
    }
}