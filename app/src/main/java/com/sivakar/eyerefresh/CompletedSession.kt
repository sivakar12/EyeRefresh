package com.sivakar.eyerefresh

import androidx.room.Embedded
import androidx.room.Relation

data class CompletedSession(
    val sessionId: Long,
    val startTime: Long,
    val completeTime: Long,
    val durationMs: Long
) {
    val durationSeconds: Int
        get() = (durationMs / 1000).toInt()
    
    val durationMinutes: Int
        get() = (durationMs / (1000 * 60)).toInt()
} 