package com.sivakar.eyerefresh

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sivakar.eyerefresh.core.AppEvent

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val event: AppEvent,
    val timestamp: Long = System.currentTimeMillis()
) 