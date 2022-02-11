package com.sivakar.eyerefresh;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.sivakar.eyerefresh.models.StateLog;
import com.sivakar.eyerefresh.models.State;
import java.util.List;

@Dao
public interface StateLogDao {
    @Query("SELECT * FROM StateLog ORDER BY timestamp DESC LIMIT 1")
    public LiveData<StateLog> getLatestLogLiveData();

    @Query("SELECT * FROM StateLog ORDER BY timestamp DESC LIMIT 1")
    public StateLog getLatestLog();

    @Query("SELECT * FROM StateLog WHERE reminder_timestamp != 0 ORDER BY timestamp DESC LIMIT 1")
    public StateLog getLastReminderScheduled();

    @Query("SELECT * FROM StateLog ORDER BY timestamp DESC LIMIT 100")
    public List<StateLog> getLastHundredLogs();

    @Query("SELECT * FROM StateLog WHERE state = 'REFRESH_HAPPENING' ORDER BY timestamp DESC LIMIT 1")
    public StateLog getLastRefreshHappeningEntry();

    @Insert()
    public void insert(StateLog log);
}
