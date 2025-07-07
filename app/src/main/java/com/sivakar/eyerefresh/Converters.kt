package com.sivakar.eyerefresh

import androidx.room.TypeConverter
import com.sivakar.eyerefresh.core.AppState

class Converters {
    @TypeConverter
    fun fromAppState(appState: AppState): String {
        val json = when (appState) {
            is AppState.Paused -> """{"type":"Paused"}"""
            is AppState.TimeLeftForNextRefresh -> """{"type":"TimeLeftForNextRefresh","scheduledTimeInMillis":${appState.scheduledTimeInMillis}}"""
            is AppState.RefreshCanStart -> """{"type":"RefreshCanStart"}"""
            is AppState.RefreshHappening -> """{"type":"RefreshHappening","startTimeInMillis":${appState.startTimeInMillis}}"""
            is AppState.WaitingForRefreshAcknowledgement -> """{"type":"WaitingForRefreshAcknowledgement"}"""
        }
        return json
    }

    @TypeConverter
    fun toAppState(appStateString: String): AppState {
        return try {
            val result = when {
                appStateString.contains("\"type\":\"Paused\"") -> AppState.Paused
                appStateString.contains("\"type\":\"TimeLeftForNextRefresh\"") -> {
                    val scheduledTimeInMillisMatch = Regex("\"scheduledTimeInMillis\":(\\d+)").find(appStateString)
                    val scheduledTimeInMillis = scheduledTimeInMillisMatch?.groupValues?.get(1)?.toLongOrNull() ?: System.currentTimeMillis()
                    AppState.TimeLeftForNextRefresh(scheduledTimeInMillis)
                }
                appStateString.contains("\"type\":\"RefreshCanStart\"") -> AppState.RefreshCanStart
                appStateString.contains("\"type\":\"RefreshHappening\"") -> {
                    val startTimeInMillisMatch = Regex("\"startTimeInMillis\":(\\d+)").find(appStateString)
                    val startTimeInMillis = startTimeInMillisMatch?.groupValues?.get(1)?.toLongOrNull() ?: System.currentTimeMillis()
                    AppState.RefreshHappening(startTimeInMillis)
                }
                appStateString.contains("\"type\":\"WaitingForRefreshAcknowledgement\"") -> AppState.WaitingForRefreshAcknowledgement
                else -> AppState.Paused
            }
            result
        } catch (e: Exception) {
            AppState.Paused
        }
    }

}