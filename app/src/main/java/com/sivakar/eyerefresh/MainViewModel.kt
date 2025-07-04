package com.sivakar.eyerefresh

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(application, AppDatabase::class.java, "app-db").build()
    private val appStateDao = db.appStateDao()

    val appState: StateFlow<AppState> = appStateDao.getAppState()
        .map { it?.state ?: AppState.RemindersPaused } // Provide a default state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppState.RemindersPaused)

    init {
        startCountdownTimer()
    }

    private fun startCountdownTimer() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000) // Update every second
                val currentState = appState.value
                if (currentState is AppState.ReminderScheduled) {
                    val remainingTime = currentState.timeInMillis - System.currentTimeMillis()
                    if (remainingTime > 0) {
                        // Update the countdown
                        val updatedState = currentState.copy(countdownMillis = remainingTime)
                        appStateDao.updateAppState(AppStateEntity(state = updatedState))
                    } else {
                        // Time is up, trigger notification
                        onEvent(AppEvent.NotificationDue)
                    }
                }
            }
        }
    }

    fun onEvent(event: AppEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = appState.value
            val (newState, sideEffect) = transition(currentState, event)

            // Persist the new state
            appStateDao.updateAppState(AppStateEntity(state = newState))

            // Handle the side effect
            handleSideEffect(sideEffect)
        }
    }

    private fun handleSideEffect(sideEffect: SideEffect?) {
        when (sideEffect) {
            is SideEffect.ScheduleNotification -> scheduleNotification(sideEffect)
            is SideEffect.ShowNotification -> showNotification(sideEffect)
            is SideEffect.CancelNotification -> cancelNotification()
            null -> { /* No side effect */ }
        }
    }

    private fun scheduleNotification(effect: SideEffect.ScheduleNotification) {
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(effect.time - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                "title" to effect.title,
                "text" to effect.text
            ))
            .build()
        WorkManager.getInstance(getApplication()).enqueue(workRequest)
    }

    private fun showNotification(effect: SideEffect.ShowNotification) {
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(workDataOf(
                "title" to effect.title,
                "text" to effect.text
            ))
            .build()
        WorkManager.getInstance(getApplication()).enqueue(workRequest)
    }

    private fun cancelNotification() {
        WorkManager.getInstance(getApplication()).cancelAllWork()
    }
}