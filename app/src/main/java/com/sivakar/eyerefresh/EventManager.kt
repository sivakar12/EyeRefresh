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
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.sivakar.eyerefresh.database.AppDatabase
import com.sivakar.eyerefresh.workers.HealthCheckWorker
import com.sivakar.eyerefresh.database.EventDao
import com.sivakar.eyerefresh.database.EventEntity
import com.sivakar.eyerefresh.core.AppEvent
import com.sivakar.eyerefresh.core.AppState
import com.sivakar.eyerefresh.core.Config
import com.sivakar.eyerefresh.core.NotificationKind
import com.sivakar.eyerefresh.core.SideEffect
import com.sivakar.eyerefresh.core.transition

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration

class EventManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "EventManager"
        private const val EYE_REFRESH_CHANNEL_ID = "eye_refresh_channel"
        private const val EYE_REFRESH_NOTIFICATION_ID = 1
        
        // Alarm request codes for different events
        private const val ALARM_REQUEST_REFRESH_DUE = 1001
        private const val ALARM_REQUEST_REFRESH_TIME_UP = 1002
        
        // Work names for health checks only
        private const val WORK_NAME_HEALTH_CHECK = "periodic_health_check"
        private const val WORK_NAME_BOOT_HEALTH_CHECK = "boot_health_check"
        
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
    private var eventDao: EventDao? = null
    private lateinit var alarmManager: AlarmManager
    
    init {
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        createNotificationChannel()
        schedulePeriodicHealthChecks()
    }
    
    private fun getDatabase(): AppDatabase {
        if (db == null) {
            db = AppDatabase.getInstance(context)
        }
        return db!!
    }
    
    private fun getEventDao(): EventDao {
        if (eventDao == null) {
            eventDao = getDatabase().eventDao()
        }
        return eventDao!!
    }
    
    fun processEvent(event: AppEvent) {
        scope.launch {
            Log.d(TAG, "Processing event: $event")
            
            try {
                // 1. Get current state from database
                val currentState = getCurrentStateFromDatabase()
                Log.d(TAG, "Current state from database: $currentState")
                
                // 2. Store the event in the database
                val eventEntity = EventEntity(event = event.toString(), timestamp = System.currentTimeMillis())
                getEventDao().insertEvent(eventEntity)
                Log.d(TAG, "Stored event in database: $event")
                
                // 3. Process event using pure transition function
                val config = Config.loadFromPreferences(context)
                val (newState, sideEffects) = transition(currentState, event, config)
                
                Log.d(TAG, "State transition: $currentState -> $newState, sideEffects: $sideEffects")
                
                // 4. Execute side effects immediately
                handleSideEffects(sideEffects)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error processing event: ${e.message}", e)
            }
        }
    }
    
    private suspend fun getCurrentStateFromDatabase(): AppState {
        return try {
            val latestEvent = getEventDao().getLatestEvent().first()
            val config = Config.loadFromPreferences(context)
            val appEvent: AppEvent? = latestEvent?.event?.let { AppEvent.fromString(it) }
            AppState.fromLastEvent(appEvent, config)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current state from database: ${e.message}")
            AppState.Paused
        }
    }
    
    private fun handleSideEffects(sideEffects: List<SideEffect>) {
        Log.d(TAG, "Handling ${sideEffects.size} side effects: $sideEffects")
        sideEffects.forEach { sideEffect ->
            when (sideEffect) {
                is SideEffect.ScheduleEvent -> scheduleEvent(sideEffect)
                is SideEffect.ShowNotification -> showNotification(sideEffect)
                is SideEffect.StopTimer -> stopTimer()
                is SideEffect.ClearNotification -> clearNotification()
            }
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
    
    private fun clearNotification() {
        Log.d(TAG, "Clearing notification")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(EYE_REFRESH_NOTIFICATION_ID)
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
        eventDao = null
    }
    
    private fun schedulePeriodicHealthChecks() {
        Log.d(TAG, "Scheduling periodic health checks every 20 minutes")
        
        val healthCheckWork = PeriodicWorkRequestBuilder<HealthCheckWorker>(
            Duration.ofMinutes(20)
        ).build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME_HEALTH_CHECK,
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            healthCheckWork
        )
        
        Log.d(TAG, "Periodic health checks scheduled successfully every 20 minutes")
    }
    
    /**
     * Restores the app state after device boot.
     * This method schedules a health check work that will run after the system is ready.
     */
    fun restoreStateAfterBoot() {
        Log.d(TAG, "Boot restoration initiated")
        
        // Schedule a health check work that will run after the system is ready
        val healthCheckWork = OneTimeWorkRequestBuilder<HealthCheckWorker>()
            .setInitialDelay(Duration.ofMinutes(1)) // Delay to ensure system is ready
            .build()
        
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME_BOOT_HEALTH_CHECK,
            ExistingWorkPolicy.REPLACE,
            healthCheckWork
        )
        
        Log.d(TAG, "Boot health check scheduled successfully")
    }
    
    /**
     * Recovers the app state from various scenarios like crashes, app closes, or periodic health checks.
     * This method handles expired timers and reschedules alarms appropriately.
     * 
     * @param recoveryReason The reason for recovery (e.g., "boot", "crash", "health_check", "app_restart")
     */
    fun recoverState(recoveryReason: String = "unknown") {
        scope.launch {
            try {
                Log.d(TAG, "Recovering state after: $recoveryReason")
                
                // Get the current state from database
                val currentState = getCurrentStateFromDatabase()
                Log.d(TAG, "Current state from database: $currentState")
                
                // Handle state restoration based on current state
                when (currentState) {
                    is AppState.Paused -> {
                        Log.d(TAG, "App was paused, no recovery needed")
                    }
                    
                    is AppState.TimeLeftForNextRefresh -> {
                        val now = System.currentTimeMillis()
                        val timeUntilScheduled = currentState.scheduledTimeInMillis - now
                        Log.d(TAG, "TimeLeftForNextRefresh state: scheduled for ${currentState.scheduledTimeInMillis}, now: $now, time until: ${timeUntilScheduled}ms")
                        
                        if (currentState.scheduledTimeInMillis <= now) {
                            // Timer has expired, trigger refresh due
                            Log.d(TAG, "Timer expired, triggering refresh due")
                            processEvent(AppEvent.RefreshDue)
                        } else {
                            // Timer is still valid, reschedule it
                            Log.d(TAG, "Rescheduling timer for ${currentState.scheduledTimeInMillis} (in ${timeUntilScheduled}ms)")
                            scheduleEvent(SideEffect.ScheduleEvent(AppEvent.RefreshDue, currentState.scheduledTimeInMillis))
                        }
                    }
                    
                    is AppState.RefreshCanStart -> {
                        // Show notification for refresh that can start
                        Log.d(TAG, "Refresh can start, showing notification")
                        showNotification(SideEffect.ShowNotification(NotificationKind.RefreshReminder))
                    }
                    
                    is AppState.RefreshHappening -> {
                        val now = System.currentTimeMillis()
                        val config = Config.loadFromPreferences(context)
                        val elapsedTime = now - currentState.startTimeInMillis
                        val remainingTime = config.breakDurationMs - elapsedTime
                        
                        Log.d(TAG, "RefreshHappening state: started at ${currentState.startTimeInMillis}, now: $now, elapsed: ${elapsedTime}ms, remaining: ${remainingTime}ms")
                        
                        if (elapsedTime >= config.breakDurationMs) {
                            // Break time has expired, trigger refresh time up
                            Log.d(TAG, "Break time expired, triggering refresh time up")
                            processEvent(AppEvent.RefreshTimeUp)
                        } else {
                            // Break is still in progress, reschedule the timer
                            val scheduledTime = now + remainingTime
                            Log.d(TAG, "Rescheduling break timer for ${scheduledTime} (in ${remainingTime}ms)")
                            scheduleEvent(SideEffect.ScheduleEvent(AppEvent.RefreshTimeUp, scheduledTime))
                        }
                    }
                    
                    is AppState.WaitingForRefreshAcknowledgement -> {
                        // Show notification for refresh completion
                        Log.d(TAG, "Waiting for acknowledgement, showing notification")
                        showNotification(SideEffect.ShowNotification(NotificationKind.RefreshComplete))
                    }
                }
                
                Log.d(TAG, "State recovery completed successfully for: $recoveryReason")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error recovering state after $recoveryReason: ${e.message}", e)
            }
        }
    }
} 