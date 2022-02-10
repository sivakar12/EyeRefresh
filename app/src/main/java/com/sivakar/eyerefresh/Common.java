package com.sivakar.eyerefresh;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.AsyncTask;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.sivakar.eyerefresh.models.Action;
import com.sivakar.eyerefresh.models.StateLog;


public class Common {

    public static long getReminderIntervalInMillis(Context context) {
        return 1000 * Long.parseLong(
                PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .getString("reminder_interval", "1200")
        );
    }
    public static long getSnoozeDurationInMillis(Context context) {
        return 1000 * Long.parseLong(
                PreferenceManager
                        .getDefaultSharedPreferences(context)
                        .getString("snooze_duration", "60")
        );
    }
    public static long getRefreshDurationInMillis(Context context) {
        return 1000 * Long.parseLong(
                PreferenceManager
                        .getDefaultSharedPreferences(context)
                        .getString("refresh_duration", "20")
        );
    }

    public static void setReminder(Context context, AppDatabase db, boolean snooze) {
        long alarmDuration = snooze ? getSnoozeDurationInMillis(context) :
                getReminderIntervalInMillis(context);

        long alarmTimeStamp = alarmDuration + System.currentTimeMillis();

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = makePendingIntentForRefreshAlarm(context);
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeStamp, pendingIntent);

        AsyncTask.execute(()-> {
            try {
                db.stateLogDao().insert(StateLog.reminderScheduled(alarmTimeStamp));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static PendingIntent makePendingIntentForRefreshAlarm(Context context) {
        Intent intent = new Intent(context, CommonBroadcastReceiver.class);
        intent.putExtra(Constants.NOTIFICATION_INTENT_ACTION_KEY, Action.SEND_NOTIFICATION.name());
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    public static void sendNotification(Context context, AppDatabase db) {
        AsyncTask.execute(() -> {
            try {
                db.stateLogDao().insert(StateLog.reminderSent());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Intent appOpenIntent = new Intent(context.getApplicationContext(), MainActivity.class);
        appOpenIntent.putExtra(Constants.NOTIFICATION_INTENT_ACTION_KEY, Action.OPEN_APP.name());
        PendingIntent appOpenPendingIntent = PendingIntent.getActivity(
                context.getApplicationContext(), 0,  appOpenIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent snoozeIntent = new Intent(context.getApplicationContext(), CommonBroadcastReceiver.class);
        snoozeIntent.putExtra(Constants.NOTIFICATION_INTENT_ACTION_KEY, Action.SNOOZE.name());
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context.getApplicationContext(), 1, snoozeIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent pauseSchedulingIntent = new Intent(context.getApplicationContext(), CommonBroadcastReceiver.class);
        pauseSchedulingIntent.putExtra(Constants.NOTIFICATION_INTENT_ACTION_KEY, Action.PAUSE_SCHEDULING.name());
        PendingIntent pauseSchedulingPendingIntent = PendingIntent.getBroadcast(
                context.getApplicationContext(), 2, pauseSchedulingIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Eye Refresh")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText("Look at an object 20 feet away for 20 seconds")
                .setContentIntent(appOpenPendingIntent)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .addAction(R.drawable.ic_launcher_foreground, "Snooze", snoozePendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "Pause scheduling", pauseSchedulingPendingIntent)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(Constants.NOTIFICATION_REMINDER_ID, notification);
    }

    public static void makeRefreshDoneNotification(Context context) {
        Intent appOpenIntent = new Intent(context.getApplicationContext(), MainActivity.class);
        appOpenIntent.putExtra(Constants.NOTIFICATION_INTENT_ACTION_KEY, Action.OPEN_APP.name());
        PendingIntent appOpenPendingIntent = PendingIntent.getActivity(
                context.getApplicationContext(), 3,  appOpenIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent refreshMissedIntent = new Intent(context.getApplicationContext(), CommonBroadcastReceiver.class);
        refreshMissedIntent.putExtra(Constants.NOTIFICATION_INTENT_ACTION_KEY, Action.STOP_REFRESH_MIDWAY.name());
        PendingIntent refreshMissedPendingIntent = PendingIntent.getBroadcast(
                context.getApplicationContext(), 4,  refreshMissedIntent, PendingIntent.FLAG_IMMUTABLE);
        
        Intent refreshDoneIntent = new Intent(context.getApplicationContext(), CommonBroadcastReceiver.class);
        refreshDoneIntent.putExtra(Constants.NOTIFICATION_INTENT_ACTION_KEY, Action.STOP_REFRESH_MIDWAY.name());
        PendingIntent refreshDonePendingIntent = PendingIntent.getBroadcast(
                context.getApplicationContext(), 5,  refreshDoneIntent, PendingIntent.FLAG_IMMUTABLE);
        
        Notification notification = new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Eye Refresh")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText("20 seconds is up")
                .setContentIntent(appOpenPendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "Missed", refreshMissedPendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "Done", refreshDonePendingIntent)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, notification);
    }

    public static void handleSnooze(Context context, AppDatabase db) {
        setReminder(context, db, true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(Constants.NOTIFICATION_REMINDER_ID);
    }


    public static void pauseScheduling(Context context, AppDatabase db) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(makePendingIntentForRefreshAlarm(context));
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

        long alarmTime = System.currentTimeMillis() +
                getRefreshDurationInMillis(context);

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = makePendingIntentForRefreshTimeUp(context);

        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(Constants.NOTIFICATION_REMINDER_ID);
    }

    private static PendingIntent makePendingIntentForRefreshTimeUp(Context context) {
        Intent intent = new Intent(context, CommonBroadcastReceiver.class);
        intent.putExtra(Constants.NOTIFICATION_INTENT_ACTION_KEY, Action.SEND_REFRESH_TIME_UP_NOTIFICATION.name());
        return PendingIntent.getBroadcast(context, 7, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    public static void handleRefreshMiss(Context context, AppDatabase db) {
        // This works just like snooze for now
        // TODO: Should this be something other that snooze?
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(makePendingIntentForRefreshTimeUp(context));
        setReminder(context, db, true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(Constants.NOTIFICATION_TIME_UP_ID);
    }

    public static void handleRefreshDone(Context context, AppDatabase db) {
        setReminder(context, db, false);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(Constants.NOTIFICATION_TIME_UP_ID);
    }
}
