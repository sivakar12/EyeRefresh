package com.sivakar.eyerefresh

import android.app.Application

class EyeRefreshApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize the EventManager singleton
        EventManager.getInstance(this)
    }
} 