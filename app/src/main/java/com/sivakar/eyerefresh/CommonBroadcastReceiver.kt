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
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received broadcast: ${intent.action}")
        
        when (intent.action) {
            ACTION_NOTIFICATION_DUE -> {
                Log.d(TAG, "Notification due - triggering reminder")
                // This would typically trigger the MainViewModel to handle the event
                // For now, we'll just log it
            }
            ACTION_REFRESH_TIME_DONE -> {
                Log.d(TAG, "Refresh time done - triggering completion")
                // This would typically trigger the MainViewModel to handle the event
                // For now, we'll just log it
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
    
    private fun restoreReminders(context: Context) {
        // In a real implementation, this would:
        // 1. Check if reminders were enabled before reboot
        // 2. Restore the scheduled notifications
        // 3. Update the app state accordingly
        Log.d(TAG, "Restoring reminders after boot")
    }
} 