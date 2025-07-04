package com.sivakar.eyerefresh

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromAppState(appState: AppState): String {
        val json = when (appState) {
            is AppState.RemindersPaused -> """{"type":"RemindersPaused"}"""
            is AppState.ReminderScheduled -> """{"type":"ReminderScheduled","timeInMillis":${appState.timeInMillis}}"""
            is AppState.ReminderSent -> """{"type":"ReminderSent"}"""
            is AppState.RefreshHappening -> """{"type":"RefreshHappening","startTimeInMillis":${appState.startTimeInMillis}}"""
            is AppState.RefreshComplete -> """{"type":"RefreshComplete"}"""
        }
        return json
    }

    @TypeConverter
    fun toAppState(appStateString: String): AppState {
        return try {
            val result = when {
                appStateString.contains("\"type\":\"RemindersPaused\"") -> AppState.RemindersPaused
                appStateString.contains("\"type\":\"ReminderScheduled\"") -> {
                    val timeInMillisMatch = Regex("\"timeInMillis\":(\\d+)").find(appStateString)
                    val timeInMillis = timeInMillisMatch?.groupValues?.get(1)?.toLongOrNull() ?: System.currentTimeMillis()
                    AppState.ReminderScheduled(timeInMillis)
                }
                appStateString.contains("\"type\":\"ReminderSent\"") -> AppState.ReminderSent
                appStateString.contains("\"type\":\"RefreshHappening\"") -> {
                    val startTimeInMillisMatch = Regex("\"startTimeInMillis\":(\\d+)").find(appStateString)
                    val startTimeInMillis = startTimeInMillisMatch?.groupValues?.get(1)?.toLongOrNull() ?: System.currentTimeMillis()
                    AppState.RefreshHappening(startTimeInMillis)
                }
                appStateString.contains("\"type\":\"RefreshComplete\"") -> AppState.RefreshComplete
                else -> AppState.RemindersPaused
            }
            result
        } catch (e: Exception) {
            AppState.RemindersPaused
        }
    }

}