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
                    SideEffect.ScheduleNotification(
                        AppConfig.DEFAULT_NOTIFICATION_TITLE,
                        AppConfig.DEFAULT_NOTIFICATION_TEXT,
                        scheduledTime
                    )
                )
            }
            // TODO; Also offer to do now? Add that as parameter to event?
            else -> StateTransition(currentState)
        }
        is AppState.ReminderScheduled ->  when (event) {
            is AppEvent.NotificationDue -> StateTransition(AppState.ReminderSent, SideEffect.ShowNotification("Reminder", "Time's up!"))
            is AppEvent.NotificationsPaused -> StateTransition(AppState.RemindersPaused, SideEffect.CancelNotification)
            else -> StateTransition(currentState)
        }

        is AppState.ReminderSent -> when (event) {
            is AppEvent.NotificationsPaused -> StateTransition(AppState.RemindersPaused, SideEffect.CancelNotification)
            is AppEvent.SnoozeRequested -> {
                val scheduledTime = System.currentTimeMillis() + AppConfig.DEFAULT_SNOOZE_DURATION_MS
                StateTransition(
                    AppState.ReminderScheduled(scheduledTime, AppConfig.DEFAULT_SNOOZE_DURATION_MS),
                    SideEffect.ScheduleNotification(
                        AppConfig.SNOOZE_NOTIFICATION_TITLE,
                        "We'll remind you again in 2 minutes.",
                        scheduledTime
                    )
                )
            }
            is AppEvent.RefreshStarted -> StateTransition(
                AppState.RefreshHappening,
                SideEffect.ScheduleNotification(
                    AppConfig.REFRESH_COMPLETE_TITLE,
                    AppConfig.REFRESH_COMPLETE_TEXT,
                    System.currentTimeMillis() + AppConfig.DEFAULT_BREAK_DURATION_MS
                )
            )
            else -> StateTransition(currentState)
        }

        is AppState.RefreshHappening -> when (event) {
            is AppEvent.RefreshTimeDone -> StateTransition(
                currentState,
                SideEffect.ShowNotification("Refresh Complete", "Your 20-second refresh is done!")
            )
            is AppEvent.RefreshMarkedComplete -> {
                val scheduledTime = System.currentTimeMillis() + AppConfig.DEFAULT_REMINDER_INTERVAL_MS
                StateTransition(
                    AppState.ReminderScheduled(scheduledTime, AppConfig.DEFAULT_REMINDER_INTERVAL_MS),
                    SideEffect.ScheduleNotification(
                        AppConfig.NEXT_REMINDER_TITLE,
                        AppConfig.NEXT_REMINDER_TEXT,
                        scheduledTime
                    )
                )
            }
            is AppEvent.RefreshAbandoned -> {
                val scheduledTime = System.currentTimeMillis() + AppConfig.DEFAULT_SNOOZE_DURATION_MS
                StateTransition(
                    AppState.ReminderScheduled(scheduledTime, AppConfig.DEFAULT_SNOOZE_DURATION_MS),
                    SideEffect.ScheduleNotification(
                        "Reminder Rescheduled",
                        "We'll remind you again in 2 minutes.",
                        scheduledTime
                    )
                )
            }
            else -> StateTransition(currentState)
        }
    }
}