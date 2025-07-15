package com.sivakar.eyerefresh

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sivakar.eyerefresh.core.AppEvent
import com.sivakar.eyerefresh.core.AppState
import com.sivakar.eyerefresh.core.Config
import com.sivakar.eyerefresh.database.AppDatabase
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import android.util.Log

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val eventManager = EventManager.getInstance(application)
    private val eventDao = AppDatabase.getInstance(application).eventDao()
    
    val appState: StateFlow<AppState> = eventDao.getLatestEvent()
        .map { latestEvent -> 
            val config = Config.loadFromPreferences(application)
            val appEvent = latestEvent?.event?.let { AppEvent.fromString(it) }
            AppState.fromLastEvent(appEvent, config)
        }
        .stateIn(
            viewModelScope, 
            SharingStarted.WhileSubscribed(5000), 
            AppState.Paused
        )
    
    fun onEvent(event: AppEvent) {
        Log.d("MainViewModel", "Processing event: $event")
        eventManager.processEvent(event)
    }
    
    override fun onCleared() {
        super.onCleared()
        // Note: We don't cleanup the EventManager here as it's a singleton
        // that should persist across ViewModel instances
    }
}