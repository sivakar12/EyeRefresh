package com.sivakar.eyerefresh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.room.Room;

import com.sivakar.eyerefresh.models.Action;

public class CommonBroadcastReceiver extends BroadcastReceiver {
    private AppDatabase db;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (db == null) {
            db = Room.databaseBuilder(context, AppDatabase.class, "eye-refresh-db")
                            .enableMultiInstanceInvalidation()
                            .build();
        }
        Action intentAction = Action.valueOf(intent.getStringExtra(Constants.NOTIFICATION_INTENT_ACTION_KEY));


        if (!intent.getExtras().containsKey(Constants.NOTIFICATION_INTENT_ACTION_KEY)) {
            Toast.makeText(context, "DEBUG: Receiver intent empty", Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(context, (String) intent.getExtras().get(Constants.NOTIFICATION_INTENT_ACTION_KEY), Toast.LENGTH_SHORT).show();
        switch(intentAction) {
            case SEND_NOTIFICATION:
                Common.sendNotification(context, db);
                break;
            case SNOOZE:
                Common.handleSnooze(context, db);
                break;
            case PAUSE_SCHEDULING:
                Toast.makeText(context, "Pausing", Toast.LENGTH_LONG).show();
                Common.pauseScheduling(context, db);
                break;
            case SEND_REFRESH_TIME_UP_NOTIFICATION:
                Common.makeRefreshDoneNotification(context);
                break;
            case STOP_REFRESH_MIDWAY:
                Common.handleRefreshMiss(context, db);
                break;
            case REFRESH_DONE:
                Common.handleRefreshDone(context, db);
                break;
        }

    }


}