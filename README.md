# EyeRefresh

There is this 20-20-20 rule to reduce eye strain for those spent a lot time in front of screen. Every twenty minutes, you take a break and look at something twenty feet away for twenty seconds. This is a app that sends timely reminders to do that.

## Components

- MainActivity: Activity that shows different Fragments based on the state of the app
- AppDatabase: Data layer using Room library to store the state transitions. This is the central source of truth for the state transitions
- BroadcastListener: To handle the scheduled reminders and the actions coming from the notifications
- Notifications: Using AlarmManager and Notification API to schedule notifications. From the notifications actions can be taken
- UI: Styled using the brand new Material You desgins from Google and Constraint Layout to make the views responsive
- Settings: Settings are stored in Shared Preferences and durations can be set (mostly for debugging purposes)

## States

- PAUSED
- REMINDER_SCHEDULED
- REMINDER_SENT
- REFRESH_HAPPENING

## Events

- SCHEDULING_TURNED_ON
- SCHEDULING_PAUSED
- REMINDER_DUE
- SNOOZE_REQUESTED
- REFRESH_STARTED
- REFRESH_COMPLETED
- REFRESH_CANCELLED
- OPEN_APP (for opening app from notification through broadcast receiver)

## Notification Types

- TIME_FOR_REFERSH
- REFRESH_TIME_UP

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
