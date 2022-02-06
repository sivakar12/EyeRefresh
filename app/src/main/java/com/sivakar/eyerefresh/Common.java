package com.sivakar.eyerefresh;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.sivakar.eyerefresh.models.Action;
import com.sivakar.eyerefresh.models.StateLog;

public class Common {

    public static final long SNOOZE_DURATION_MILLIS = 2 * 60 * 1000;
    public static final long ALARM_INTERVAL_MILLIS = 20 * 60 * 1000;

    public static void setReminder(Context context, AppDatabase db, boolean snooze) {
        // Toast.makeText(context, "Setting Reminder", Toast.LENGTH_LONG).show();

        // long alarmTime = System.currentTimeMillis() + (snooze ? SNOOZE_DURATION_MILLIS: ALARM_INTERVAL_MILLIS);
        long alarmTime = System.currentTimeMillis() + 5 * 1000; // for debug

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, CommonBroadcastReceiver.class);
        intent.putExtra(Constant.NOTIFICATION_INTENT_ACTION_KEY, Action.SEND_NOTIFICATION.name());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);

        AsyncTask.execute(()-> {
            try {
                db.stateLogDao().insert(StateLog.reminderScheduled(alarmTime));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void sendNotification(Context context, AppDatabase db) {
        AsyncTask.execute(() -> {
            db.stateLogDao().insert(StateLog.reminderSent());
        });

        Intent appOpenIntent = new Intent(context.getApplicationContext(), MainActivity.class);
        appOpenIntent.putExtra(Constant.NOTIFICATION_INTENT_ACTION_KEY, Action.OPEN_APP.name());
        PendingIntent appOpenPendingIntent = PendingIntent.getActivity(
                context.getApplicationContext(), 0,  appOpenIntent, 0);

        Intent snoozeIntent = new Intent(context.getApplicationContext(), CommonBroadcastReceiver.class);
        snoozeIntent.putExtra(Constant.NOTIFICATION_INTENT_ACTION_KEY, "SNOOZE");
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context.getApplicationContext(), 1, snoozeIntent, 0);

        Intent pauseSchedulingIntent = new Intent(context.getApplicationContext(), CommonBroadcastReceiver.class);
        pauseSchedulingIntent.putExtra(Constant.NOTIFICATION_INTENT_ACTION_KEY, Action.PAUSE_SCHEDULING.name());
        PendingIntent pauseSchedulingPendingIntent = PendingIntent.getBroadcast(
                context.getApplicationContext(), 2, pauseSchedulingIntent, 0);

        Notification notification = new NotificationCompat.Builder(context, context.getResources().getString(R.string.notification_channel_id))
                .setContentTitle("Eye Refresh")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText("Look at an object 20m away for 20 seconds")
                .setContentIntent(appOpenPendingIntent)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .addAction(R.drawable.ic_launcher_foreground, "Snooze", snoozePendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "Pause scheduling", pauseSchedulingPendingIntent)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(0, notification);
    }

    public static void handleSnooze(Context context, AppDatabase db) {
        Toast.makeText(context, "Snoozing", Toast.LENGTH_LONG).show();
        setReminder(context, db, true);
    }


    public static void pauseScheduling(Context context, AppDatabase db) {
        Toast.makeText(context, "Pausing", Toast.LENGTH_LONG).show();
        AsyncTask.execute(() -> {
            try {
                db.stateLogDao().insert(StateLog.notScheduled());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void startRefresh(Context context, AppDatabase db) {
        AsyncTask.execute(() -> {
            try {
                db.stateLogDao().insert(StateLog.refreshHappening());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
