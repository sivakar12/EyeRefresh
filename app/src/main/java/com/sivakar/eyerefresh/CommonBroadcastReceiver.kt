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
        const val ACTION_PROCESS_EVENT = "com.sivakar.eyerefresh.PROCESS_EVENT"
        const val EXTRA_EVENT = "event"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received broadcast: ${intent.action}")
        
        when (intent.action) {
            ACTION_BOOT_COMPLETED -> {
                restoreReminders(context)
            }
            ACTION_PROCESS_EVENT -> {
                // Handle AppEvent explicitly
                val event = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent.getSerializableExtra(EXTRA_EVENT, AppEvent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getSerializableExtra(EXTRA_EVENT) as? AppEvent
                }
                Log.d(TAG, "Received event: $event")
                
                if (event != null) {
                    EventManager.getInstance(context).processEvent(event)
                }
            }
            else -> {
                // Handle AppEvent from AlarmManager (no explicit action)
                val event = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent.getSerializableExtra(EXTRA_EVENT, AppEvent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getSerializableExtra(EXTRA_EVENT) as? AppEvent
                }
                Log.d(TAG, "Received event from alarm: $event")
                
                if (event != null) {
                    EventManager.getInstance(context).processEvent(event)
                }
            }
        }
    }
    
    private fun restoreReminders(context: Context) {
        // Restore scheduled reminders after device reboot
        Log.d(TAG, "Restoring reminders after boot")
        
        try {
            // Ensure EventManager is initialized (this will create the singleton if it doesn't exist)
            val eventManager = EventManager.getInstance(context)
            
            // Restore the app state after boot
            eventManager.restoreStateAfterBoot()
            
            Log.d(TAG, "Boot restoration initiated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring reminders after boot: ${e.message}", e)
        }
    }
} 