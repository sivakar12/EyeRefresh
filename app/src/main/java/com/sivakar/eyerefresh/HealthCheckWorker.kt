package com.sivakar.eyerefresh

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sivakar.eyerefresh.EventManager

class HealthCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "HealthCheckWorker"
    }
    
    override suspend fun doWork(): Result {
        try {
            Log.d(TAG, "Running health check")
            
            // Use the existing recoverState method from EventManager
            EventManager.getInstance(applicationContext).recoverState("health_check")
            
            Log.d(TAG, "Health check completed successfully")
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during health check: ${e.message}", e)
            return Result.failure()
        }
    }
} 