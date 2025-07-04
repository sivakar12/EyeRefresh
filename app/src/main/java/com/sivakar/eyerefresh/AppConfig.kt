package com.sivakar.eyerefresh

object AppConfig {
    // Default reminder interval (20 minutes)
    const val DEFAULT_REMINDER_INTERVAL_MS = 20 * 1000L //20 * 60 * 1000L
    
    // Default break duration (20 seconds)
    const val DEFAULT_BREAK_DURATION_MS = 5 * 1000L //20 * 1000L
    
    // Default snooze duration (2 minutes)
    const val DEFAULT_SNOOZE_DURATION_MS = 2 * 1000L //2 * 60 * 1000L
    
    // Extended snooze duration (5 minutes) - used when snoozing from scheduled state
    const val EXTENDED_SNOOZE_DURATION_MS = 5 * 1000L //5 * 60 * 1000L
    
    // Notification titles and messages
    const val DEFAULT_NOTIFICATION_TITLE = "Eye Refresh Reminder"
    const val DEFAULT_NOTIFICATION_TEXT = "Time to take a break and refresh your eyes!"
    const val SNOOZE_NOTIFICATION_TITLE = "Snoozed"
    const val REFRESH_COMPLETE_TITLE = "Refresh Complete"
    const val REFRESH_COMPLETE_TEXT = "Your 20-second refresh is done!"
    const val NEXT_REMINDER_TITLE = "Next Reminder"
    const val NEXT_REMINDER_TEXT = "Next reminder scheduled in 20 minutes."
} 