package com.sivakar.eyerefresh;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.room.Room;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.sivakar.eyerefresh.fragments.PausedStateFragment;
import com.sivakar.eyerefresh.fragments.RefreshHappeningStateFragment;
import com.sivakar.eyerefresh.fragments.ReminderSentStateFragment;
import com.sivakar.eyerefresh.fragments.ScheduledStateFragment;
import com.sivakar.eyerefresh.models.State;
import com.sivakar.eyerefresh.models.StateLog;

public class MainActivity extends AppCompatActivity {
    private State state = State.NOT_SCHEDULED;
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
            NotificationChannel channel = new NotificationChannel(getString(R.string.notification_channel_id), name, importance);
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
            setState(State.NOT_SCHEDULED);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "eye-refresh-db").build();

        // Set state on app start as that form the database
        db.stateLogDao().getLatestLogLiveData().observe(this, new Observer<StateLog>() {
            @Override
            public void onChanged(StateLog stateLog) {
                if (stateLog != null) {
                    setState(stateLog.state);
                } else {
                    setState(State.NOT_SCHEDULED);
                }
            }
        });

        setFragmentBasedOnState();

    }

    private void setFragmentBasedOnState() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (state) {
            case NOT_SCHEDULED:
                fragmentTransaction.replace(R.id.fragmentContainerView, new PausedStateFragment(), null);
                break;
            case REMINDER_SCHEDULED:
                fragmentTransaction.replace(R.id.fragmentContainerView, new ScheduledStateFragment(), null);
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
        setReminder();
    }

    private void setReminder() {
        AlarmManager alarmManager =
                (AlarmManager) getApplicationContext()
                        .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        // It is 20 seconds instead of twenty minutes for testing purposes
        long alarmTime = System.currentTimeMillis() + 5 * 1000;
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);

        AsyncTask.execute(()-> {
            try {
                db.stateLogDao().insert(StateLog.reminderScheduled(alarmTime));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void onClickCancel(View view) {
        // TODO: Cancel the notification using the notification manager
        AsyncTask.execute(() -> {
            try {
                db.stateLogDao().insert(StateLog.notScheduled());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    public void onStartRefresh(View view) {
        AsyncTask.execute(() -> {
            try {
                db.stateLogDao().insert(StateLog.refreshHappening());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void onRefreshDone(View view) {
        setReminder();
    }
}