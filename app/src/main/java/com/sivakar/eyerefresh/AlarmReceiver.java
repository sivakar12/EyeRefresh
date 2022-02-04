package com.sivakar.eyerefresh;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //TODO: Can I move this notification channel creation somewhere else, and do it only once
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "EyeRefresh";
            String description = "EyeRefresh";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("EyeRefresh", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            Intent appOpenIntent = new Intent(context.getApplicationContext(), MainActivity.class);
            appOpenIntent.putExtra("NOTIFICATION_ACTION", NotificationAction.OPEN_APP);
            PendingIntent appOpenPendingIntent = PendingIntent.getActivity(
                    context.getApplicationContext(), 0,  appOpenIntent, 0);

            Notification notification = new NotificationCompat.Builder(context, "EyeRefresh")
                    .setContentTitle("Eye Refresh")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentText("Look at an object 20m away for 20 seconds")
                    .setContentIntent(appOpenPendingIntent)
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .build();
            notificationManager.notify(0, notification);
        }
    }
}