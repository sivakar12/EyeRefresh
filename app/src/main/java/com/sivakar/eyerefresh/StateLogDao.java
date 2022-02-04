package com.sivakar.eyerefresh;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.sivakar.eyerefresh.models.StateLog;

@Dao
public interface StateLogDao {
    @Query("SELECT * FROM StateLog ORDER BY timestamp DESC LIMIT 1")
    public LiveData<StateLog> getLatestLogLiveData();

    @Query("SELECT * FROM StateLog ORDER BY timestamp DESC LIMIT 1")
    public StateLog getLatestLog();

    @Insert()
    public void insert(StateLog log);
}
