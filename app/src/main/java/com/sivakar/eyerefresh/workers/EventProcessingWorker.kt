package com.sivakar.eyerefresh.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sivakar.eyerefresh.EventManager
import com.sivakar.eyerefresh.core.AppEvent

class EventProcessingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "EventProcessingWorker"
        const val KEY_EVENT = "event"
    }
    
    override suspend fun doWork(): Result {
        try {
            val eventString = inputData.getString(KEY_EVENT)
            Log.d(TAG, "Processing event from work: $eventString")
            
            if (eventString != null) {
                val event = AppEvent.fromString(eventString)
                if (event != null) {
                    EventManager.getInstance(applicationContext).processEvent(event)
                    Log.d(TAG, "Event processed successfully: $event")
                    return Result.success()
                } else {
                    Log.e(TAG, "Failed to parse event from string: $eventString")
                    return Result.failure()
                }
            } else {
                Log.e(TAG, "Event string is null")
                return Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing event in worker: ${e.message}", e)
            return Result.failure()
        }
    }
} 