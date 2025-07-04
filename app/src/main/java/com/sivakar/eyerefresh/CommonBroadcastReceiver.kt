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
        Log.d(TAG, "Received broadcast: ${intent.action}")
        
        when (intent.action) {
            ACTION_NOTIFICATION_DUE -> {
                Log.d(TAG, "Notification due - triggering reminder")
                sendEventToService(context, AppEvent.NotificationDue)
            }
            ACTION_REFRESH_TIME_DONE -> {
                Log.d(TAG, "Refresh time done - triggering completion")
                sendEventToService(context, AppEvent.RefreshTimeDone)
            }
            ACTION_START_REFRESH -> {
                Log.d(TAG, "User clicked Start Break from notification")
                sendEventToService(context, AppEvent.RefreshStarted)
            }
            ACTION_SNOOZE -> {
                Log.d(TAG, "User clicked Snooze from notification")
                sendEventToService(context, AppEvent.SnoozeRequested)
            }
            ACTION_PAUSE -> {
                Log.d(TAG, "User clicked Pause from notification")
                sendEventToService(context, AppEvent.NotificationsPaused)
            }
            ACTION_COMPLETE_REFRESH -> {
                Log.d(TAG, "User clicked Complete from notification")
                sendEventToService(context, AppEvent.RefreshMarkedComplete)
            }
            ACTION_ABANDON_REFRESH -> {
                Log.d(TAG, "User clicked Skip from notification")
                sendEventToService(context, AppEvent.RefreshAbandoned)
            }
            ACTION_BOOT_COMPLETED -> {
                Log.d(TAG, "Boot completed - restoring reminders if needed")
                // Restore scheduled reminders after device reboot
                restoreReminders(context)
            }
            else -> {
                Log.d(TAG, "Unknown action: ${intent.action}")
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
        // In a real implementation, this would:
        // 1. Check if reminders were enabled before reboot
        // 2. Restore the scheduled notifications
        // 3. Update the app state accordingly
        Log.d(TAG, "Restoring reminders after boot")
    }
} 