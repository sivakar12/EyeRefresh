package com.sivakar.eyerefresh

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sivakar.eyerefresh.core.AppEvent
import com.sivakar.eyerefresh.core.AppState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import android.util.Log

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val eventManager = EventManager.getInstance(application)
    
    val appState: StateFlow<AppState> = eventManager.appState.stateIn(
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