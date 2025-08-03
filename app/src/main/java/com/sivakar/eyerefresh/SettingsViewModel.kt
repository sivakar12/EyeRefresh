package com.sivakar.eyerefresh

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sivakar.eyerefresh.core.Config
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _config = MutableStateFlow(Config.loadFromPreferences(application))
    val config: StateFlow<Config> = _config.asStateFlow()
    
    fun updateReminderInterval(intervalMs: Long) {
        val newConfig = _config.value.copy(reminderIntervalMs = intervalMs)
        _config.value = newConfig
        Config.saveToPreferences(getApplication(), newConfig)
    }
    
    fun updateBreakDuration(durationMs: Long) {
        val newConfig = _config.value.copy(breakDurationMs = durationMs)
        _config.value = newConfig
        Config.saveToPreferences(getApplication(), newConfig)
    }
    
    fun updateSnoozeDuration(durationMs: Long) {
        val newConfig = _config.value.copy(snoozeDurationMs = durationMs)
        _config.value = newConfig
        Config.saveToPreferences(getApplication(), newConfig)
    }
    
    fun resetToDefaults() {
        val defaultConfig = Config.getDefault()
        _config.value = defaultConfig
        Config.saveToPreferences(getApplication(), defaultConfig)
    }
    
    fun refreshConfig() {
        _config.value = Config.loadFromPreferences(getApplication())
    }
} 