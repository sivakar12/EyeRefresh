package com.sivakar.eyerefresh;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.sivakar.eyerefresh.models.StateLog;

import java.util.List;

@Dao
public interface StateLogDao {
    @Query("SELECT * FROM StateLog ORDER BY timestamp DESC LIMIT 1")
    public LiveData<StateLog> getLatestLogLiveData();

    @Query("SELECT * FROM StateLog ORDER BY timestamp DESC LIMIT 1")
    public StateLog getLatestLog();

    @Query("SELECT * FROM StateLog ORDER BY timestamp DESC LIMIT 100")
    public List<StateLog> getLastHundredLogs();
    @Insert()
    public void insert(StateLog log);
}
