package com.sivakar.eyerefresh
import com.sivakar.eyerefresh.AppState
import com.sivakar.eyerefresh.SideEffect
import com.sivakar.eyerefresh.AppEvent

data class StateTransition(val newState: AppState, val sideEffect: SideEffect? = null)

fun transition(currentState: AppState, event: AppEvent): StateTransition {
    return when (currentState) {
        is AppState.RemindersPaused -> when (event) {
            is AppEvent.NotificationsTurnedOn -> {
                val scheduledTime = System.currentTimeMillis() + AppConfig.DEFAULT_REMINDER_INTERVAL_MS
                StateTransition(
                    AppState.ReminderScheduled(scheduledTime, AppConfig.DEFAULT_REMINDER_INTERVAL_MS),
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
                val scheduledTime = System.currentTimeMillis() + AppConfig.DEFAULT_SNOOZE_DURATION_MS
                StateTransition(
                    AppState.ReminderScheduled(scheduledTime, AppConfig.DEFAULT_SNOOZE_DURATION_MS),
                    SideEffect.ScheduleEvent(AppEvent.NotificationDue, scheduledTime)
                )
            }
            is AppEvent.RefreshStarted -> StateTransition(
                AppState.RefreshHappening,
                SideEffect.ScheduleEvent(AppEvent.RefreshTimeDone, System.currentTimeMillis() + AppConfig.DEFAULT_BREAK_DURATION_MS)
            )
            else -> StateTransition(currentState)
        }

        is AppState.RefreshHappening -> when (event) {
            is AppEvent.RefreshTimeDone -> StateTransition(
                currentState,
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
                val scheduledTime = System.currentTimeMillis() + AppConfig.DEFAULT_REMINDER_INTERVAL_MS
                StateTransition(
                    AppState.ReminderScheduled(scheduledTime, AppConfig.DEFAULT_REMINDER_INTERVAL_MS),
                    SideEffect.ScheduleEvent(AppEvent.NotificationDue, scheduledTime)
                )
            }
            is AppEvent.RefreshAbandoned -> {
                val scheduledTime = System.currentTimeMillis() + AppConfig.DEFAULT_SNOOZE_DURATION_MS
                StateTransition(
                    AppState.ReminderScheduled(scheduledTime, AppConfig.DEFAULT_SNOOZE_DURATION_MS),
                    SideEffect.ScheduleEvent(AppEvent.NotificationDue, scheduledTime)
                )
            }
            else -> StateTransition(currentState)
        }
    }
}