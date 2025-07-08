package com.sivakar.eyerefresh

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.sivakar.eyerefresh.core.AppEvent
import com.sivakar.eyerefresh.core.AppState
import com.sivakar.eyerefresh.core.Config
import com.sivakar.eyerefresh.core.NotificationKind
import com.sivakar.eyerefresh.core.SideEffect
import com.sivakar.eyerefresh.core.transition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "EventManager"
        private const val EYE_REFRESH_CHANNEL_ID = "eye_refresh_channel"
        private const val EYE_REFRESH_NOTIFICATION_ID = 1
        
        // Alarm request codes for different events
        private const val ALARM_REQUEST_REFRESH_DUE = 1001
        private const val ALARM_REQUEST_REFRESH_TIME_UP = 1002
        
        @Volatile
        private var INSTANCE: EventManager? = null
        
        fun getInstance(context: Context): EventManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EventManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var db: AppDatabase? = null
    private var appStateDao: AppStateDao? = null
    private lateinit var alarmManager: AlarmManager
    
    // State flow for observing app state
    private val _appState = MutableStateFlow<AppState>(AppState.Paused)
    val appState: Flow<AppState> = _appState.stateIn(
        scope, 
        SharingStarted.WhileSubscribed(5000), 
        AppState.Paused
    )
    
    init {
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        createNotificationChannel()
        loadStateFromDatabase()
    }
    
    private fun getDatabase(): AppDatabase {
        if (db == null) {
            db = Room.databaseBuilder(context, AppDatabase::class.java, "app-db").build()
        }
        return db!!
    }
    
    private fun getAppStateDao(): AppStateDao {
        if (appStateDao == null) {
            appStateDao = getDatabase().appStateDao()
        }
        return appStateDao!!
    }
    
    private fun loadStateFromDatabase() {
        scope.launch {
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
    
    fun processEvent(event: AppEvent) {
        scope.launch {
            Log.d(TAG, "Processing event: $event, current state: ${_appState.value}")
            
            if (db == null) {
                getDatabase()
            }
            
            val currentState = _appState.value
            val config = Config.loadFromPreferences(context)
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
        
        val intent = Intent(context, CommonBroadcastReceiver::class.java).apply {
            putExtra(CommonBroadcastReceiver.EXTRA_EVENT, effect.event)
        }
        
        val requestCode = when (effect.event) {
            is AppEvent.RefreshDue -> ALARM_REQUEST_REFRESH_DUE
            is AppEvent.RefreshTimeUp -> ALARM_REQUEST_REFRESH_TIME_UP
            else -> ALARM_REQUEST_REFRESH_DUE
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
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
        val refreshDueIntent = Intent(context, CommonBroadcastReceiver::class.java)
        val refreshDuePendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_REFRESH_DUE,
            refreshDueIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        refreshDuePendingIntent?.let { alarmManager.cancel(it) }
        
        val refreshTimeUpIntent = Intent(context, CommonBroadcastReceiver::class.java)
        val refreshTimeUpPendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_REFRESH_TIME_UP,
            refreshTimeUpIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        refreshTimeUpPendingIntent?.let { alarmManager.cancel(it) }
    }

    private fun showNotification(effect: SideEffect.ShowNotification) {
        Log.d(TAG, "Showing notification: ${effect.notificationKind}")
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Cancel any existing notifications before showing new ones
        notificationManager.cancel(EYE_REFRESH_NOTIFICATION_ID)

        // Create content intent to open the app when notification is clicked
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, text) = when (effect.notificationKind) {
            is NotificationKind.RefreshReminder -> "Eye Refresh" to "Time for your eye refresh!"
            is NotificationKind.RefreshComplete -> "Eye Refresh Complete" to "Great job! Your eye refresh is complete."
        }
        
        val builder = NotificationCompat.Builder(context, EYE_REFRESH_CHANNEL_ID)
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
            val intent = Intent(context, CommonBroadcastReceiver::class.java).apply {
                putExtra(CommonBroadcastReceiver.EXTRA_EVENT, option.eventToSend)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                index,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            builder.addAction(
                android.R.drawable.ic_dialog_info,
                option.label,
                pendingIntent
            )
        }

        notificationManager.notify(EYE_REFRESH_NOTIFICATION_ID, builder.build())
    }
    
    private fun createNotificationChannel() {
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
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun cleanup() {
        db?.close()
        db = null
        appStateDao = null
    }
} 