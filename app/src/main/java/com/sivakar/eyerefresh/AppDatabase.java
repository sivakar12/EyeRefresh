package com.sivakar.eyerefresh;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.sivakar.eyerefresh.models.StateLog;

@Database(entities = { StateLog.class}, version = 1, exportSchema = false )
public abstract class AppDatabase extends RoomDatabase {
    public abstract StateLogDao stateLogDao();
}
