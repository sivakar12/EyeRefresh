package com.sivakar.eyerefresh

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class CommonBroadcastReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "CommonBroadcastReceiver"
        const val ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received broadcast: ${intent.action}")
        
        when (intent.action) {
            ACTION_BOOT_COMPLETED -> {
                restoreReminders(context)
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