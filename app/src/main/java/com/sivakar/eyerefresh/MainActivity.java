package com.sivakar.eyerefresh;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.sivakar.eyerefresh.fragments.PausedStateFragment;
import com.sivakar.eyerefresh.fragments.ScheduledStateFragment;
import com.sivakar.eyerefresh.models.State;

public class MainActivity extends AppCompatActivity {
    private State state = State.NOT_SCHEDULED;

    public void setState(State state) {
        this.state = state;
        setFragmentBasedOnState();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            state = (State) savedInstanceState.getSerializable("state");
        }
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
        }
        fragmentTransaction.commit();
    }

    public void onClickSchedule(View view) {
        AlarmManager alarmManager =
                (AlarmManager) getApplicationContext()
                        .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        // It is 20 seconds instead of twenty minutes for testing purposes
        long alarmTime = System.currentTimeMillis() + 20 * 1000;
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);

        setState(State.REMINDER_SCHEDULED);
    }

    public void onClickCancel(View view) {
        setState(State.NOT_SCHEDULED);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("state", state);
    }
}