# EyeRefresh


20-20-20 rule is a solution to reducing eye strain when one looks at screens for long. Every twenty minutes, you look at someting twenty feet away for twenty seconds.

This is an Android app that sends reminders to do that. The reminders can be paused, snoozed, and acted upon. There is a timer for the twenty-second-refersh as well.

## Components

- MainActivity: Activity that shows different Fragments based on the state of the app
- AppDatabase: Data layer using Room library to store the state transitions. This is the central source of truth for the state transitions
- BroadcastListener: To handle the scheduled reminders and the actions coming from the notifications
- Notifications: Using AlarmManager and Notification API to schedule notifications. From the notifications actions can be taken
- UI: Styled using the brand new Material You desgins from Google and Jetpack Compose
- Settings: Settings are stored in Shared Preferences and durations can be set (mostly for debugging purposes)



## State Transitions

### PAUSED -> SCHEDULING_TURNED_ON -> REMINDER_SCHEDULED

- Only from Activity. State changed by updating database. Alarm is created. Alarm later sends REMINDER_DUE event.

### REMINDER_SCHEDULED -> SCHEDULING_PAUSED -> PAUSED

- Called from Activity. StateLog entry is created in the database. Fragment changes. Alarm is cancelled.

### REMINDER_SCHEDULED -> REMINDER_DUE -> REMINDER_SENT

- When alarm fires, it goes to the broadcast receiver. It sends a notification (TIME_FOR_REFRESH). It creates a StateLog entry in the database causing the Activity to change Fragment. Both from the activity and the fragment, snoozing, pausing and timer start can be done

### REMINDER_SENT -> SCHEDULING_PAUSED -> PAUSED

- Database gets a new StateLog entry. Causes fragment to change. Notification cancelled.

### REMINDER_SENT -> SNOOZE_REQUESTED -> REMINDER_SCHEDULED

- Database gets a REMINNDER_SCHEDULED StateLog entry. Alarm is created with a 2 minute interval. Alarm later sends a REMINDER_DUE event to the broadcast receiver.

### REMINDER_SENT -> REFRESH_STARTED -> REFRESH_HAPPENING

- Database gets a refresh started entry. Countdown timer started. An alarm is scheduled wihich will trigger a notification after 20 seconds.
- After 20 seconds is up, from the notification or the activity, the refresh can be marked as completed or missed. Completion is not marked automatically.

### REFRESH_HAPPENING -> REFRESH_COMPLETED -> REMINDER_SCHEDULED

- Database gets scheduled entry. Alarm is created. Alarm later sends a REMINDER_DUE event. Same is what happens when going from PAUSE to SCHEDULD

### REFRESH_HAPPENING -> REFRESH_CANCELLED -> REMINDER_SCHEDULED

- TODO: What should happen here? Now it just snoozes, reminding after two minutes.

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

- Start Timer (event, time)
- Send Notification (Notification, Notification Options-Event pairs)
- Stop Timer

# Notification Types

- TIME_FOR_REFRESH_SESSION
  - Start Refresh -> REFRESH_STARTED
  - Snooze -> SNOOZE_REQUESTED
  - Pause -> SCHEDULING_PAUSED

 - REFRESH_TIME_FINISHED
  - I did it! -> MARK_REFRESH_COMPLETED
  - I couldn't do it -> REFRESH_COULD_NOT_HAPPEN

# State Transitions

| State | Event | New State | Notification, Notification Options and Events | Other Side Effect |
| ------------| ---------------- | ----------------- | --------------- | ---------------------- |
| PAUSED | SCHEDULING_TURNED_ON | TIME_LEFT_FOR_NEXT_REFRESH | | Start Timer (REFRESH_DUE, 20 minutess from now) |
| TIME_LEFT_FOR_NEXT_REFRESH | SCHEDULING_PAUSED | PAUSED | | Stop Timer |
| TIME_LEFT_FOR_NEXT_REFRESH | REFRESH_DUE | REFRESH_CAN_START | TIME_FOR_REFRESH_SESSION | Start Timer (REFRESH_DUE, 1 minnute from now) |
| REFRESH_CAN_START | SNOOZE_REQUESTED | TIME_LEFT_FOR_NEXT_REFRESH | | Start Timer (REFRESH_DUE, Snooze Time) |
| REFRESH_CAN_START | SCHEDULING_PAUSED | PAUSED | | | Stop Timer |
| REFRESH_CAN_START | REFRESH_STARTED | REFRESH_HAPPENING | | Start Timer (REFRESH_TIME_UP, 20 seconds) |
| REFRESH_HAPPENING| REFRESH_TIME_UP | WAITING_FOR_REFRESH_ACKNOWLEDGEMENT | ERFRESH_TIME_FINISHED | | |
| WAITING_FOR_REFRESH_ACKNOWLEDGEMENT | MARK_REFRESH_COMPLETED | TIME_LEFT_FOR_NEXT_REFRESH | | Start Timer (REFRESH DUE, 20 minutes from now) | 
| WAITING_FOR_REFRESH_ACKNOWLEDGEMENT | REFRESH_COULD_NOT_HAPPEN | REFRESH_CAN_START | TIME_FOR_REFRESH_SESSION | Start Timer (REFRESH_DUE, 1 minnute from now) |

