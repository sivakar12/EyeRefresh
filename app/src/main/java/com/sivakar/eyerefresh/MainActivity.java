package com.sivakar.eyerefresh;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.sivakar.eyerefresh.fragments.PausedStateFragment;
import com.sivakar.eyerefresh.fragments.RefreshHappeningStateFragment;
import com.sivakar.eyerefresh.fragments.ReminderSentStateFragment;
import com.sivakar.eyerefresh.fragments.ReminderScheduledStateFragment;
import com.sivakar.eyerefresh.models.ReminderType;
import com.sivakar.eyerefresh.models.State;
import com.sivakar.eyerefresh.models.StateLog;

public class MainActivity extends AppCompatActivity {
    private State state = State.PAUSED;
    private AppDatabase db;

    public void setState(State state) {
        this.state = state;
        setFragmentBasedOnState();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "EyeRefresh";
            String description = "EyeRefresh";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setStateFromDatabase() {
        StateLog lastStateLog = db.stateLogDao().getLatestLog();
        if (lastStateLog != null) {
            setState(lastStateLog.state);
        } else {
            setState(State.PAUSED);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "eye-refresh-db")
                .enableMultiInstanceInvalidation()
                .build();

        // Set state on app start as that form the database
        db.stateLogDao().getLatestLogLiveData().observe(this, stateLog -> {
            if (stateLog == null) {
                setState(State.PAUSED);
                return;
            }

            // If for some reason the last alarm didn't happen, reset the thing
//            if (stateLog.reminderTimestamp != 0
//                    && stateLog.reminderTimestamp < System.currentTimeMillis()) {
//                Common.pauseScheduling(getApplicationContext(), db);
//            }
            setState(stateLog.state);
        });

        setFragmentBasedOnState();

    }

    private void setFragmentBasedOnState() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (state) {
            case PAUSED:
                fragmentTransaction.replace(R.id.fragmentContainerView, new PausedStateFragment(), null);
                break;
            case REMINDER_SCHEDULED:
                fragmentTransaction.replace(R.id.fragmentContainerView, new ReminderScheduledStateFragment(), null);
                break;
            case REMINDER_SENT:
                fragmentTransaction.replace(R.id.fragmentContainerView, new ReminderSentStateFragment(), null);
                break;
            case REFRESH_HAPPENING:
                fragmentTransaction.replace(R.id.fragmentContainerView, new RefreshHappeningStateFragment(), null);
                break;
        }
        fragmentTransaction.commit();
    }

    public void onClickSchedule(View view) {
        Common.scheduleReminder(getApplicationContext(), db, ReminderType.NORMAL);
    }
    public void onClickCancel(View view) {
        Common.pauseScheduling(getApplicationContext(), db);
    }
    public void onStartRefresh(View view) {
        Common.startRefresh(getApplicationContext(), db);
    }
    public void onRefreshDone(View view) {
        Common.handleRefreshDone(getApplicationContext(), db);
    }
    public void onRefreshMiss(View view) {
        Common.handleRefreshMiss(getApplicationContext(), db);
    }
    public void onSnooze(View view) {
        Common.handleSnooze(getApplicationContext(), db);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent openSettings = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(openSettings);
                return true;
            case R.id.view_logs:
                Intent openLogs = new Intent(getApplicationContext(), LogsActivity.class);
                startActivity(openLogs);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}