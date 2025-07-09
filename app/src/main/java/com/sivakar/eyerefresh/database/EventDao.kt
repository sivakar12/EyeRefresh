package com.sivakar.eyerefresh.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY timestamp ASC")
    fun getAllEvents(): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events ORDER BY timestamp DESC LIMIT 1")
    fun getLatestEvent(): Flow<EventEntity?>
    
    @Insert
    suspend fun insertEvent(event: EventEntity)
    
    @Query("DELETE FROM events")
    suspend fun clearAllEvents()
    
    @Query("SELECT * FROM events WHERE event = 'RefreshStarted' ORDER BY timestamp DESC")
    fun getRefreshStartedEvents(): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE event = 'MarkRefreshCompleted' ORDER BY timestamp DESC")
    fun getRefreshCompletedEvents(): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events ORDER BY timestamp DESC")
    fun getAllEventsForDebug(): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getEventsInRange(startTime: Long, endTime: Long): Flow<List<EventEntity>>
} 