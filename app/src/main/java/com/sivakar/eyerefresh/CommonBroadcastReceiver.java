package com.sivakar.eyerefresh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.room.Room;

import com.sivakar.eyerefresh.models.Event;

public class CommonBroadcastReceiver extends BroadcastReceiver {
    private AppDatabase db;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (db == null) {
            db = Room.databaseBuilder(context, AppDatabase.class, "eye-refresh-db")
                            .enableMultiInstanceInvalidation()
                            .build();
        }
        Event intentEvent = Event.valueOf(intent.getStringExtra(Constants.INTENT_EVENT_KEY));


        if (!intent.getExtras().containsKey(Constants.INTENT_EVENT_KEY)) {
            Toast.makeText(context, "DEBUG: Receiver intent empty", Toast.LENGTH_LONG).show();
            return;
        }

        switch(intentEvent) {
            case REMINDER_DUE:
                EventHandlers.handleReminderDue(context, db);
                break;
            case SNOOZE_REQUESTED:
                EventHandlers.handleSnooze(context, db);
                break;
            case SCHEDULING_PAUSED:
                EventHandlers.handlePaused(context, db);
                break;
            case REFRESH_STARTED:
                EventHandlers.handleRefreshStarted(context, db);
                break;
            case REFRESH_TIME_UP:
                EventHandlers.handleRefreshTimeUp(context);
                break;
            case REFRESH_CANCELLED:
                EventHandlers.handleRefreshCancelled(context, db);
                break;
            case REFRESH_COMPLETED:
                EventHandlers.handleRefreshCompleted(context, db);
                break;
        }

    }


}