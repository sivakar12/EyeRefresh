package com.sivakar.eyerefresh.models;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class StateLog {
    public StateLog(State state) {
        this.state = state;
        this.timestamp = System.currentTimeMillis();
    }

    @ColumnInfo(name = "state")
    public State state;

    @ColumnInfo(name = "causing_action")
    public Action causingAction;

    @PrimaryKey
    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "reminder_timestamp")
    @Nullable
    public long reminderTimestamp;
}
