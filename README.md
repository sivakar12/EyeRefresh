# EyeRefresh


20-20-20 rule is a solution to reducing eye strain when one looks at screens for long. Every twenty minutes, you look at someting twenty feet away for twenty seconds.

This is an Android app that sends reminders to do that. The reminders can be paused, snoozed, and acted upon. There is a history view of sessions done to help form the habit.

## Components

- MainActivity
- AppDatabase
- BroadcastListener
- Notifications
- Service
- Settings


# States
- PAUSED
- TIME_LEFT_FOR_NEXT_REFRESH
- REFRESH_CAN_START
- REFRESH_HAPPENING
- WAITING_FOR_REFRESH_ACKNOWLEDGEMENT

# Events

- SCHEDULING_TURNED_ON
- SCHEDULING_PAUSED
- REFRESH_DUE
- SNOOZE_REQUESTED
- REFRESH_STARTED
- REFRESH_TIME_UP
- MARK_REFRESH_COMPLETED
- REFRESH_COULD_NOT_HAPPEN

# Side Effects

- ScheduleEvent (event, time)
- ShowNotification (NotificationKind)
- StopTimer
- ClearNotification

# Notification Types

- RefreshReminder
  - Start Refresh -> REFRESH_STARTED
  - Snooze -> SNOOZE_REQUESTED
  - Pause -> SCHEDULING_PAUSED

- RefreshComplete
  - I did it! -> MARK_REFRESH_COMPLETED
  - I couldn't do it -> REFRESH_COULD_NOT_HAPPEN

# State Transitions

| State | Event | New State | Side Effects |
| ------------| ---------------- | ----------------- | --------------- |
| PAUSED | SCHEDULING_TURNED_ON | TIME_LEFT_FOR_NEXT_REFRESH | ScheduleEvent (REFRESH_DUE, 20 minutes from now) |
| TIME_LEFT_FOR_NEXT_REFRESH | SCHEDULING_PAUSED | PAUSED | StopTimer, ClearNotification |
| TIME_LEFT_FOR_NEXT_REFRESH | REFRESH_DUE | REFRESH_CAN_START | ShowNotification (RefreshReminder) |
| REFRESH_CAN_START | SNOOZE_REQUESTED | TIME_LEFT_FOR_NEXT_REFRESH | ScheduleEvent (REFRESH_DUE, Snooze Time), ClearNotification |
| REFRESH_CAN_START | SCHEDULING_PAUSED | PAUSED | StopTimer, ClearNotification |
| REFRESH_CAN_START | REFRESH_STARTED | REFRESH_HAPPENING | ScheduleEvent (REFRESH_TIME_UP, 20 seconds), ClearNotification |
| REFRESH_HAPPENING | REFRESH_TIME_UP | WAITING_FOR_REFRESH_ACKNOWLEDGEMENT | ShowNotification (RefreshComplete) |
| WAITING_FOR_REFRESH_ACKNOWLEDGEMENT | MARK_REFRESH_COMPLETED | TIME_LEFT_FOR_NEXT_REFRESH | ScheduleEvent (REFRESH_DUE, 20 minutes from now), ClearNotification |
| WAITING_FOR_REFRESH_ACKNOWLEDGEMENT | REFRESH_COULD_NOT_HAPPEN | REFRESH_CAN_START | ClearNotification, ShowNotification (RefreshReminder) |

