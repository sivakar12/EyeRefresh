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
                val event = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent.getSerializableExtra(EXTRA_EVENT, AppEvent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getSerializableExtra(EXTRA_EVENT) as? AppEvent
                }
                Log.d(TAG, "Received event: $event")
                
                if (event != null) {
                    // Use EventManager instead of service
                    EventManager.getInstance(context).processEvent(event)
                }
            }
        }
    }
    
    private fun restoreReminders(context: Context) {
        // Restore scheduled reminders after device reboot
        // This could be implemented by loading the last state and rescheduling alarms
    }
} 