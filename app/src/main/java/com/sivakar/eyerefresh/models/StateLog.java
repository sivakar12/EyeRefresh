package com.sivakar.eyerefresh.models;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class StateLog {

    public static StateLog reminderScheduled(long reminderTimeStamp) {
        StateLog log = new StateLog();
        log.reminderTimestamp = reminderTimeStamp;
        log.timestamp = System.currentTimeMillis();
        log.state = State.REMINDER_SCHEDULED;

        return log;
    }

    public static StateLog notScheduled() {
        StateLog log = new StateLog();
        log.timestamp = System.currentTimeMillis();
        log.state = State.NOT_SCHEDULED;

        return log;
    }

    public static StateLog reminderSent() {
        StateLog log = new StateLog();
        log.timestamp = System.currentTimeMillis();
        log.state = State.REMINDER_SENT;

        return log;
    }

    public static StateLog refreshHappening() {
        StateLog log = new StateLog();
        log.timestamp = System.currentTimeMillis();
        log.state = State.REFRESH_HAPPENING;

        return log;
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
