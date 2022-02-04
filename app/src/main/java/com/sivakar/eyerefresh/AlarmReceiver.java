package com.sivakar.eyerefresh;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.room.Room;

import com.sivakar.eyerefresh.models.StateLog;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AsyncTask.execute(() -> {
            AppDatabase db = Room.databaseBuilder(context,
                    AppDatabase.class, "eye-refresh-db").build();
            db.stateLogDao().insert(StateLog.reminderSent());
        });

        Intent appOpenIntent = new Intent(context.getApplicationContext(), MainActivity.class);
        appOpenIntent.putExtra("NOTIFICATION_ACTION", NotificationAction.OPEN_APP);
        PendingIntent appOpenPendingIntent = PendingIntent.getActivity(
                context.getApplicationContext(), 0,  appOpenIntent, 0);

        Notification notification = new NotificationCompat.Builder(context, context.getResources().getString(R.string.notification_channel_id))
                .setContentTitle("Eye Refresh")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText("Look at an object 20m away for 20 seconds")
                .setContentIntent(appOpenPendingIntent)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(0, notification);

    }
}