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

import com.sivakar.eyerefresh.models.Event;
import com.sivakar.eyerefresh.models.NotificationType;
import com.sivakar.eyerefresh.models.ReminderType;
import com.sivakar.eyerefresh.models.State;
import com.sivakar.eyerefresh.models.StateLog;


public class EventHandlers {

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

    private static PendingIntent makePendingIntentForRefreshAlarm(Context context) {
        Intent intent = new Intent(context, CommonBroadcastReceiver.class);
        intent.putExtra(Constants.INTENT_EVENT_KEY, Event.REMINDER_DUE.name());
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    private static PendingIntent makePendingIntentForRefreshTimeUp(Context context) {
        Intent intent = new Intent(context, CommonBroadcastReceiver.class);
        intent.putExtra(Constants.INTENT_EVENT_KEY, Event.REFRESH_TIME_UP.name());
        return PendingIntent.getBroadcast(context, 7, intent, PendingIntent.FLAG_IMMUTABLE);
    }
    
    public static void sendRefreshDueNotification(Context context, AppDatabase db) {
        // Take this out to calling method
        AsyncTask.execute(() -> {
            StateLog stateLog = new StateLog(State.REMINDER_SENT);
            try {
                db.stateLogDao().insert(stateLog);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Intent appOpenIntent = new Intent(context.getApplicationContext(), MainActivity.class);
        appOpenIntent.putExtra(Constants.INTENT_EVENT_KEY, Event.OPEN_APP.name());
        PendingIntent appOpenPendingIntent = PendingIntent.getActivity(
                context.getApplicationContext(), 0,  appOpenIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent snoozeIntent = new Intent(context.getApplicationContext(), CommonBroadcastReceiver.class);
        snoozeIntent.putExtra(Constants.INTENT_EVENT_KEY, Event.SNOOZE_REQUESTED.name());
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context.getApplicationContext(), 1, snoozeIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent pauseSchedulingIntent = new Intent(context.getApplicationContext(), CommonBroadcastReceiver.class);
        pauseSchedulingIntent.putExtra(Constants.INTENT_EVENT_KEY, Event.SCHEDULING_PAUSED.name());
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
        notificationManager.notify(NotificationType.TIME_FOR_REFRESH.ordinal(), notification);
    }

    public static void sendRefreshTimeUpNotification(Context context) {
        Intent appOpenIntent = new Intent(context.getApplicationContext(), MainActivity.class);
        appOpenIntent.putExtra(Constants.INTENT_EVENT_KEY, Event.OPEN_APP.name());
        PendingIntent appOpenPendingIntent = PendingIntent.getActivity(
                context.getApplicationContext(), 3,  appOpenIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent refreshCancelledIntent = new Intent(context.getApplicationContext(), CommonBroadcastReceiver.class);
        refreshCancelledIntent.putExtra(Constants.INTENT_EVENT_KEY, Event.REFRESH_CANCELLED.name());
        PendingIntent refreshMissedPendingIntent = PendingIntent.getBroadcast(
                context.getApplicationContext(), 4,  refreshCancelledIntent, PendingIntent.FLAG_IMMUTABLE);
        
        Intent refreshDoneIntent = new Intent(context.getApplicationContext(), CommonBroadcastReceiver.class);
        refreshDoneIntent.putExtra(Constants.INTENT_EVENT_KEY, Event.REFRESH_COMPLETED.name());
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

    // sends and alarm and changes the database state
    public static void scheduleReminder(Context context, AppDatabase db, ReminderType reminderType) {
        long alarmDuration;
        switch (reminderType) {
            case SNOOZE:
                alarmDuration = getSnoozeDurationInMillis(context);
                break;
            default:
                alarmDuration = getReminderIntervalInMillis(context);
                break;
        }

        long alarmTimeStamp = alarmDuration + System.currentTimeMillis();

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = makePendingIntentForRefreshAlarm(context);
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeStamp, pendingIntent);

        AsyncTask.execute(()-> {
            StateLog stateLog = new StateLog(State.REMINDER_SCHEDULED);
            stateLog.reminderTimestamp = alarmTimeStamp;
            try {
                db.stateLogDao().insert(stateLog);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void handleSnooze(Context context, AppDatabase db) {
        scheduleReminder(context, db, ReminderType.SNOOZE);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(NotificationType.TIME_FOR_REFRESH.ordinal());
    }


    public static void pauseScheduling(Context context, AppDatabase db) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(NotificationType.TIME_FOR_REFRESH.ordinal());

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(makePendingIntentForRefreshAlarm(context));

        AsyncTask.execute(() -> {
            StateLog stateLog = new StateLog(State.PAUSED);
            try {
                db.stateLogDao().insert(stateLog);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void startRefresh(Context context, AppDatabase db) {
        AsyncTask.execute(() -> {
            StateLog stateLog = new StateLog(State.REFRESH_HAPPENING);
            try {
                db.stateLogDao().insert(stateLog);
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
        notificationManager.cancel(NotificationType.TIME_FOR_REFRESH.ordinal());
    }

    

    public static void handleRefreshMiss(Context context, AppDatabase db) {
        // This works just like snooze for now
        // TODO: Should this be something other that snooze?
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(makePendingIntentForRefreshTimeUp(context));
        scheduleReminder(context, db, ReminderType.SNOOZE);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(NotificationType.REFRESH_TIME_UP.ordinal());
    }

    public static void handleRefreshDone(Context context, AppDatabase db) {
        scheduleReminder(context, db, ReminderType.NORMAL);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(NotificationType.REFRESH_TIME_UP.ordinal());
    }
}
