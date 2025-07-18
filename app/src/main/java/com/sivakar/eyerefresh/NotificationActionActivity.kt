package com.sivakar.eyerefresh

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.sivakar.eyerefresh.core.AppEvent

class NotificationActionActivity : Activity() {
    
    companion object {
        private const val TAG = "NotificationActionActivity"
        const val EXTRA_EVENT = "event"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "NotificationActionActivity created")
        
        // Get the event from intent
        val event = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_EVENT, AppEvent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_EVENT) as? AppEvent
        }
        
        Log.d(TAG, "Received event: $event")
        
        if (event != null) {
            // Process the event
            EventManager.getInstance(this).processEvent(event)
        }
        
        // Close the activity immediately
        finish()
    }
} 