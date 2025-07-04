package com.sivakar.eyerefresh

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_state")
data class AppStateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val state: AppState,
    val timestamp: Long = System.currentTimeMillis()
) 