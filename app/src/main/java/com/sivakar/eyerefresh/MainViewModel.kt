package com.sivakar.eyerefresh

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import android.util.Log

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private var appStateService: AppStateService? = null
    private var isBound = false
    
    private val _appState = MutableStateFlow<AppState>(AppState.RemindersPaused)
    val appState: StateFlow<AppState> = _appState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppState.RemindersPaused)
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AppStateService.AppStateBinder
            appStateService = binder.getService()
            isBound = true
            
            // Start observing the service's app state
            viewModelScope.launch {
                appStateService?.appState?.collect { state ->
                    _appState.value = state
                }
            }
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            appStateService = null
            isBound = false
        }
    }
    
    init {
        startAndBindService()
    }
    
    private fun startAndBindService() {
        val intent = Intent(getApplication(), AppStateService::class.java).apply {
            action = AppStateService.ACTION_START_SERVICE
        }
        
        // Start the service
        getApplication<Application>().startService(intent)
        
        // Bind to the service
        getApplication<Application>().bindService(
            intent,
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }
    
    fun onEvent(event: AppEvent) {
        if (isBound) {
            appStateService?.processEvent(event)
        } else {
            val intent = Intent(getApplication(), AppStateService::class.java).apply {
                action = AppStateService.ACTION_PROCESS_EVENT
                putExtra(AppStateService.EXTRA_EVENT, event)
            }
            getApplication<Application>().startService(intent)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        if (isBound) {
            getApplication<Application>().unbindService(serviceConnection)
            isBound = false
        }
    }
}