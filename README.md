# EyeRefresh

There is this 20-20-20 rule to reduce eye strain for those spent a lot time in front of screen. Every twenty minutes, you take a break and look at something twenty feet away for twenty seconds. This is a app that sends timely reminders to do that.

## Components
- MainActivity: Activity that shows different Fragments based on the state of the app
- AppDatabase: Data layer using Room library to store the state transitions. This is the central source of truth for the state transitions
- BroadcastListener: To handle the scheduled reminders and the actions coming from the notifications
- Notifications: Using AlarmManager and Notification API to schedule notifications. From the notifications actions can be taken
- UI: Styled using the brand new Material You desgins from Google and Constraint Layout to make the views responsive
- Settings: Settings are stored in Shared Preferences and durations can be set (mostly for debugging purposes)
