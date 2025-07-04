package com.sivakar.eyerefresh

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class CommonBroadcastReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "CommonBroadcastReceiver"
        const val ACTION_NOTIFICATION_DUE = "com.sivakar.eyerefresh.NOTIFICATION_DUE"
        const val ACTION_REFRESH_TIME_DONE = "com.sivakar.eyerefresh.REFRESH_TIME_DONE"
        const val ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED"
        
        // Notification action constants (matching NotificationWorker)
        const val ACTION_START_REFRESH = "com.sivakar.eyerefresh.START_REFRESH"
        const val ACTION_SNOOZE = "com.sivakar.eyerefresh.SNOOZE"
        const val ACTION_PAUSE = "com.sivakar.eyerefresh.PAUSE"
        const val ACTION_COMPLETE_REFRESH = "com.sivakar.eyerefresh.COMPLETE_REFRESH"
        const val ACTION_ABANDON_REFRESH = "com.sivakar.eyerefresh.ABANDON_REFRESH"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_NOTIFICATION_DUE -> {
                sendEventToService(context, AppEvent.NotificationDue)
            }
            ACTION_REFRESH_TIME_DONE -> {
                sendEventToService(context, AppEvent.RefreshTimeDone)
            }
            ACTION_START_REFRESH -> {
                sendEventToService(context, AppEvent.RefreshStarted)
                dismissNotification(context)
            }
            ACTION_SNOOZE -> {
                sendEventToService(context, AppEvent.SnoozeRequested)
                dismissNotification(context)
            }
            ACTION_PAUSE -> {
                sendEventToService(context, AppEvent.NotificationsPaused)
                dismissNotification(context)
            }
            ACTION_COMPLETE_REFRESH -> {
                sendEventToService(context, AppEvent.RefreshMarkedComplete)
                dismissNotification(context)
            }
            ACTION_ABANDON_REFRESH -> {
                sendEventToService(context, AppEvent.RefreshAbandoned)
                dismissNotification(context)
            }
            ACTION_BOOT_COMPLETED -> {
                restoreReminders(context)
            }
            else -> {
                // Unknown action
            }
        }
    }
    
    private fun sendEventToService(context: Context, event: AppEvent) {
        val serviceIntent = Intent(context, AppStateService::class.java).apply {
            action = AppStateService.ACTION_PROCESS_EVENT
            putExtra(AppStateService.EXTRA_EVENT, event)
        }
        context.startService(serviceIntent)
    }
    
    private fun restoreReminders(context: Context) {
        // Restore scheduled reminders after device reboot
    }
    
    private fun dismissNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.cancel(NotificationWorker.NOTIFICATION_ID)
    }
} 