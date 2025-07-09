package com.sivakar.eyerefresh

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
} 