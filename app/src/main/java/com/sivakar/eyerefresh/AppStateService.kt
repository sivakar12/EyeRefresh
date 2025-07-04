package com.sivakar.eyerefresh

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class AppStateService : Service() {
    
    companion object {
        private const val TAG = "AppStateService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "app_state_service_channel"
        
        // Action to start the service
        const val ACTION_START_SERVICE = "com.sivakar.eyerefresh.START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.sivakar.eyerefresh.STOP_SERVICE"
        const val ACTION_PROCESS_EVENT = "com.sivakar.eyerefresh.PROCESS_EVENT"
        
        // Extra key for event
        const val EXTRA_EVENT = "event"
    }
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var db: AppDatabase? = null
    private var appStateDao: AppStateDao? = null
    private val workManager = WorkManager.getInstance(this)
    
    // Binder for bound service
    private val binder = AppStateBinder()
    
    // State flow for observing app state
    private val _appState = MutableStateFlow<AppState>(AppState.RemindersPaused)
    val appState: Flow<AppState> = _appState
    
    private fun getDatabase(): AppDatabase {
        if (db == null) {
            db = Room.databaseBuilder(this, AppDatabase::class.java, "app-db").build()
        }
        return db!!
    }
    
    private fun getAppStateDao(): AppStateDao {
        if (appStateDao == null) {
            appStateDao = getDatabase().appStateDao()
        }
        return appStateDao!!
    }
    
    inner class AppStateBinder : Binder() {
        fun getService(): AppStateService = this@AppStateService
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Initialize state from database
        serviceScope.launch {
            getAppStateDao().getAppState().collect { appStateEntity ->
                val state = appStateEntity?.state ?: AppState.RemindersPaused
                _appState.value = state
            }
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started with action: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_START_SERVICE -> {
                Log.d(TAG, "Starting foreground service")
            }
            ACTION_STOP_SERVICE -> {
                Log.d(TAG, "Stopping service")
                stopSelf()
            }
            ACTION_PROCESS_EVENT -> {
                val event = intent.getSerializableExtra(EXTRA_EVENT) as? AppEvent
                if (event != null) {
                    processEvent(event)
                }
            }
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        db?.close()
    }
    
    fun processEvent(event: AppEvent) {
        Log.d(TAG, "Processing event: $event")
        serviceScope.launch {
            val currentState = _appState.value
            val (newState, sideEffect) = transition(currentState, event)
            
            // Update state
            _appState.value = newState
            
            // Persist the new state
            getAppStateDao().updateAppState(AppStateEntity(state = newState))
            
            // Handle the side effect
            handleSideEffect(sideEffect)
        }
    }
    
    private fun handleSideEffect(sideEffect: SideEffect?) {
        when (sideEffect) {
            is SideEffect.ScheduleEvent -> scheduleEvent(sideEffect)
            is SideEffect.ShowNotification -> showNotification(sideEffect)
            null -> { /* No side effect */ }
        }
    }

    private fun scheduleEvent(effect: SideEffect.ScheduleEvent) {
        // Schedule a broadcast to trigger the event at the specified time
        val intent = Intent(this, CommonBroadcastReceiver::class.java).apply {
            action = when (effect.event) {
                is AppEvent.NotificationDue -> CommonBroadcastReceiver.ACTION_NOTIFICATION_DUE
                is AppEvent.RefreshTimeDone -> CommonBroadcastReceiver.ACTION_REFRESH_TIME_DONE
                else -> return // Don't schedule other events
            }
        }
        
        // Use WorkManager to schedule the broadcast
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(effect.timeInMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                "action" to intent.action,
                "title" to "Eye Refresh",
                "text" to "Time for your eye refresh!"
            ))
            .build()
        workManager.enqueue(workRequest)
    }

    private fun showNotification(effect: SideEffect.ShowNotification) {
        // Create notification with the provided options
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationWorker.CHANNEL_ID,
                "Eye Refresh Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for eye refresh reminders"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, NotificationWorker.CHANNEL_ID)
            .setContentTitle(effect.title)
            .setContentText(effect.text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Add actions for each notification option
        effect.notificationOptions.forEach { option ->
            val intent = Intent(this, CommonBroadcastReceiver::class.java).apply {
                action = when (option.eventToSend) {
                    is AppEvent.RefreshStarted -> CommonBroadcastReceiver.ACTION_START_REFRESH
                    is AppEvent.SnoozeRequested -> CommonBroadcastReceiver.ACTION_SNOOZE
                    is AppEvent.NotificationsPaused -> CommonBroadcastReceiver.ACTION_PAUSE
                    is AppEvent.RefreshMarkedComplete -> CommonBroadcastReceiver.ACTION_COMPLETE_REFRESH
                    is AppEvent.RefreshAbandoned -> CommonBroadcastReceiver.ACTION_ABANDON_REFRESH
                    else -> return@forEach
                }
            }
            
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                this,
                option.label.hashCode(),
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            
            builder.addAction(
                android.R.drawable.ic_dialog_info,
                option.label,
                pendingIntent
            )
        }

        // Show the notification
        notificationManager.notify(NotificationWorker.NOTIFICATION_ID, builder.build())
    }
    
    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Eye Refresh Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the eye refresh app running in the background"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Eye Refresh")
            .setContentText("Running in background")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
} 