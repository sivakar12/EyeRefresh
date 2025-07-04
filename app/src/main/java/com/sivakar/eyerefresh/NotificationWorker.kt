package com.sivakar.eyerefresh

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        const val CHANNEL_ID = "eye_refresh_channel"
        const val NOTIFICATION_ID = 1
    }

    override fun doWork(): Result {
        val action = inputData.getString("action")
        
        if (action != null) {
            // Send broadcast to trigger the event
            val intent = Intent(context, CommonBroadcastReceiver::class.java).apply {
                this.action = action
            }
            context.sendBroadcast(intent)
        }
        
        return Result.success()
    }
} 