package com.sivakar.eyerefresh

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sivakar.eyerefresh.core.AppEvent

class EventBroadcastReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "EventBroadcastReceiver"
        const val ACTION_PROCESS_EVENT = "com.sivakar.eyerefresh.PROCESS_EVENT"
        const val EXTRA_EVENT = "event"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received broadcast: ${intent.action}")
        
        when (intent.action) {
            ACTION_PROCESS_EVENT -> {
                processEvent(context, intent)
            }
        }
    }
    
    private fun processEvent(context: Context, intent: Intent) {
        // Get the event from intent
        val event = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_EVENT, AppEvent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_EVENT) as? AppEvent
        }
        
        Log.d(TAG, "Received event: $event")
        
        if (event != null) {
            // Process the event through EventManager
            EventManager.getInstance(context).processEvent(event)
        } else {
            Log.e(TAG, "Event is null")
        }
    }
} 