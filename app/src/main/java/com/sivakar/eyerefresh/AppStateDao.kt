package com.sivakar.eyerefresh

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppStateDao {
    @Query("SELECT * FROM app_state ORDER BY timestamp DESC LIMIT 1")
    fun getAppState(): Flow<AppStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAppState(appState: AppStateEntity)
    
    @Query("DELETE FROM app_state")
    suspend fun clearAllStates()
} 