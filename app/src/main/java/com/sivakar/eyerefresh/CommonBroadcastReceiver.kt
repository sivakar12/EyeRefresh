package com.sivakar.eyerefresh

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sivakar.eyerefresh.core.AppEvent

class CommonBroadcastReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "CommonBroadcastReceiver"
        const val ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED"
        const val EXTRA_EVENT = "event"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received broadcast: ${intent.action}")
        
        when (intent.action) {
            ACTION_BOOT_COMPLETED -> {
                restoreReminders(context)
            }
            else -> {
                // Handle AppEvent directly
                val event = intent.getSerializableExtra(EXTRA_EVENT) as? AppEvent
                Log.d(TAG, "Received event: $event")
                
                if (event != null) {
                    // Use EventManager instead of service
                    EventManager.getInstance(context).processEvent(event)
                    
                    // Dismiss notification for user-triggered events
                    if (event is AppEvent.RefreshStarted || 
                        event is AppEvent.SnoozeRequested || 
                        event is AppEvent.SchedulingPaused ||
                        event is AppEvent.MarkRefreshCompleted ||
                        event is AppEvent.RefreshCouldNotHappen) {
                        dismissNotification(context)
                    }
                }
            }
        }
    }
    
    private fun restoreReminders(context: Context) {
        // Restore scheduled reminders after device reboot
        // This could be implemented by loading the last state and rescheduling alarms
    }
    
    private fun dismissNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.cancel(1) // Use the same notification ID as EventManager
    }
} 