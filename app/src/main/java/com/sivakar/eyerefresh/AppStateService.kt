package com.sivakar.eyerefresh

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.sivakar.eyerefresh.core.AppEvent
import com.sivakar.eyerefresh.core.AppState
import com.sivakar.eyerefresh.core.Config
import com.sivakar.eyerefresh.core.NotificationKind
import com.sivakar.eyerefresh.core.SideEffect
import com.sivakar.eyerefresh.core.StateTransition
import com.sivakar.eyerefresh.core.transition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class AppStateService : Service() {
    
    companion object {
        private const val TAG = "AppStateService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "app_state_service_channel"
        
        // Notification constants
        const val EYE_REFRESH_CHANNEL_ID = "eye_refresh_channel"
        const val EYE_REFRESH_NOTIFICATION_ID = 1
        
        const val ACTION_START_SERVICE = "com.sivakar.eyerefresh.START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.sivakar.eyerefresh.STOP_SERVICE"
        const val ACTION_PROCESS_EVENT = "com.sivakar.eyerefresh.PROCESS_EVENT"
        const val EXTRA_EVENT = "event"
        
        // Alarm request codes for different events
        private const val ALARM_REQUEST_REFRESH_DUE = 1001
        private const val ALARM_REQUEST_REFRESH_TIME_UP = 1002
    }
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var db: AppDatabase? = null
    private var appStateDao: AppStateDao? = null
    private lateinit var alarmManager: AlarmManager
    
    // Binder for bound service
    private val binder = AppStateBinder()
    
    // State flow for observing app state
    private val _appState = MutableStateFlow<AppState>(AppState.Paused)
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
        Log.d(TAG, "AppStateService onCreate")
        
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        serviceScope.launch {
            getAppStateDao().getAppState().collect { appStateEntity ->
                try {
                    val state = appStateEntity?.state ?: AppState.Paused
                    Log.d(TAG, "Loaded state from database: $state")
                    if (_appState.value != state) {
                        _appState.value = state
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading state from database: ${e.message}")
                    getAppStateDao().clearAllStates()
                    getAppStateDao().updateAppState(AppStateEntity(state = AppState.Paused))
                    _appState.value = AppState.Paused
                }
            }
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_START_SERVICE -> {
                // Starting foreground service
            }
            ACTION_STOP_SERVICE -> {
                stopSelf()
            }
            ACTION_PROCESS_EVENT -> {
                val event = intent.getSerializableExtra(EXTRA_EVENT) as? AppEvent
                Log.d(TAG, "Processing event from intent: $event")
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
        db?.close()
    }
    
    fun processEvent(event: AppEvent) {
        serviceScope.launch {
            Log.d(TAG, "Processing event: $event, current state: ${_appState.value}")
            
            if (db == null) {
                getDatabase()
            }
            
            val currentState = _appState.value
            val config = Config.loadFromPreferences(this@AppStateService)
            val (newState, sideEffect) = transition(currentState, event, config)
            
            Log.d(TAG, "State transition: $currentState -> $newState, sideEffect: $sideEffect")
            
            if (_appState.value != newState) {
                _appState.value = newState
            }
            
            try {
                val entity = AppStateEntity(state = newState)
                getAppStateDao().updateAppState(entity)
            } catch (e: Exception) {
                Log.e(TAG, "Database error: ${e.message}")
            }
            
            handleSideEffect(sideEffect)
        }
    }
    
    private fun handleSideEffect(sideEffect: SideEffect?) {
        Log.d(TAG, "Handling side effect: $sideEffect")
        when (sideEffect) {
            is SideEffect.ScheduleEvent -> scheduleEvent(sideEffect)
            is SideEffect.ShowNotification -> showNotification(sideEffect)
            is SideEffect.StopTimer -> stopTimer()
            null -> { /* No side effect */ }
        }
    }

    private fun scheduleEvent(effect: SideEffect.ScheduleEvent) {
        Log.d(TAG, "Scheduling event: ${effect.event} at ${effect.timeInMillis}")
        
        val intent = Intent(this, CommonBroadcastReceiver::class.java).apply {
            putExtra(CommonBroadcastReceiver.EXTRA_EVENT, effect.event)
        }
        
        val requestCode = when (effect.event) {
            is AppEvent.RefreshDue -> ALARM_REQUEST_REFRESH_DUE
            is AppEvent.RefreshTimeUp -> ALARM_REQUEST_REFRESH_TIME_UP
            else -> ALARM_REQUEST_REFRESH_DUE
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Cancel any existing alarm for this event type
        alarmManager.cancel(pendingIntent)
        
        // Use inexact alarms - they don't require special permissions and work reliably
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                effect.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                effect.timeInMillis,
                pendingIntent
            )
        }
        
        Log.d(TAG, "Inexact alarm scheduled successfully")
    }

    private fun stopTimer() {
        Log.d(TAG, "Stopping all timers")
        
        // Cancel all scheduled alarms
        val refreshDueIntent = Intent(this, CommonBroadcastReceiver::class.java)
        val refreshDuePendingIntent = PendingIntent.getBroadcast(
            this,
            ALARM_REQUEST_REFRESH_DUE,
            refreshDueIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        refreshDuePendingIntent?.let { alarmManager.cancel(it) }
        
        val refreshTimeUpIntent = Intent(this, CommonBroadcastReceiver::class.java)
        val refreshTimeUpPendingIntent = PendingIntent.getBroadcast(
            this,
            ALARM_REQUEST_REFRESH_TIME_UP,
            refreshTimeUpIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        refreshTimeUpPendingIntent?.let { alarmManager.cancel(it) }
    }

    private fun showNotification(effect: SideEffect.ShowNotification) {
        Log.d(TAG, "Showing notification: ${effect.notificationKind}")
        
        // Create notification with the provided options
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Cancel any existing notifications before showing new ones
        notificationManager.cancel(EYE_REFRESH_NOTIFICATION_ID)
        
        // Create notification channel for Android O and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                EYE_REFRESH_CHANNEL_ID,
                "Eye Refresh Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for eye refresh reminders"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create content intent to open the app when notification is clicked
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, text) = when (effect.notificationKind) {
            is NotificationKind.RefreshReminder -> "Eye Refresh" to "Time for your eye refresh!"
            is NotificationKind.RefreshComplete -> "Eye Refresh Complete" to "Great job! Your eye refresh is complete."
        }
        
        val builder = NotificationCompat.Builder(this, EYE_REFRESH_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        // Add actions for each notification option
        val options = when (effect.notificationKind) {
            is NotificationKind.RefreshReminder -> effect.notificationKind.options
            is NotificationKind.RefreshComplete -> effect.notificationKind.options
        }
        
        options.forEachIndexed { index, option ->
            val intent = Intent(this, CommonBroadcastReceiver::class.java).apply {
                putExtra(CommonBroadcastReceiver.EXTRA_EVENT, option.eventToSend)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                index, // Use index instead of hashCode to ensure unique request codes
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            builder.addAction(
                android.R.drawable.ic_dialog_info,
                option.label,
                pendingIntent
            )
        }

        // Show the notification
        notificationManager.notify(EYE_REFRESH_NOTIFICATION_ID, builder.build())
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