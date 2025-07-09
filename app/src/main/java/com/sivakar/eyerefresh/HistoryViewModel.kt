package com.sivakar.eyerefresh

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val eventDao = database.eventDao()
    
    val completedSessions: Flow<List<CompletedSession>> = combine(
        eventDao.getRefreshStartedEvents(),
        eventDao.getRefreshCompletedEvents(),
        eventDao.getAllEventsForDebug()
    ) { startedEvents, completedEvents, allEvents ->
        Log.d("HistoryViewModel", "All events in DB: ${allEvents.size}")
        allEvents.forEach { event ->
            Log.d("HistoryViewModel", "Event: ${event.event} at ${event.timestamp}")
        }
        
        Log.d("HistoryViewModel", "Started events: ${startedEvents.size}, Completed events: ${completedEvents.size}")
        
        val sessions = mutableListOf<CompletedSession>()
        
        // Create a map of start events by timestamp for quick lookup
        val startEventsMap = startedEvents.associateBy { it.timestamp }
        
        // For each completed event, find the most recent start event before it
        completedEvents.forEach { completedEvent ->
            Log.d("HistoryViewModel", "Processing completed event at ${completedEvent.timestamp}")
            
            val matchingStartEvent = startEventsMap.entries
                .filter { it.key < completedEvent.timestamp }
                .maxByOrNull { it.key }
            
            matchingStartEvent?.let { (startTime, startEvent) ->
                Log.d("HistoryViewModel", "Found matching start event at $startTime")
                
                // Check if there's no other start event between this start and complete
                val hasIntermediateStart = startEventsMap.any { (timestamp, _) ->
                    timestamp > startTime && timestamp < completedEvent.timestamp
                }
                
                if (!hasIntermediateStart) {
                    val session = CompletedSession(
                        sessionId = startEvent.id,
                        startTime = startTime,
                        completeTime = completedEvent.timestamp,
                        durationMs = completedEvent.timestamp - startTime
                    )
                    sessions.add(session)
                    Log.d("HistoryViewModel", "Added session: $session")
                } else {
                    Log.d("HistoryViewModel", "Skipped session due to intermediate start event")
                }
            } ?: Log.d("HistoryViewModel", "No matching start event found for completed event")
        }
        
        val sortedSessions = sessions.sortedByDescending { it.startTime }
        Log.d("HistoryViewModel", "Final sessions count: ${sortedSessions.size}")
        sortedSessions
    }
} 