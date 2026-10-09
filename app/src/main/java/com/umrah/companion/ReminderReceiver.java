package com.umrah.companion;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "umrah_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {

        String title = intent.getStringExtra("title");
        int reminderId = intent.getIntExtra("reminderId", 1);

        if (title == null || title.trim().isEmpty()) {
            title = "Umrah Companion Reminder";
        }

        Intent openAppIntent =
                context.getPackageManager()
                        .getLaunchIntentForPackage(context.getPackageName());

        PendingIntent openAppPendingIntent = null;

        if (openAppIntent != null) {
            openAppIntent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            );

            openAppPendingIntent =
                    PendingIntent.getActivity(
                            context,
                            reminderId,
                            openAppIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT |
                            PendingIntent.FLAG_IMMUTABLE
                    );
        }

        Uri soundUri =
                RingtoneManager.getDefaultUri(
                        RingtoneManager.TYPE_NOTIFICATION
                );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Umrah Companion")
                        .setContentText(title)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setSound(soundUri)
                        .setVibrate(new long[]{0, 500, 250, 500});

        if (openAppPendingIntent != null) {
            builder.setContentIntent(openAppPendingIntent);
        }

        NotificationManager notificationManager =
                (NotificationManager)
                        context.getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        if (notificationManager != null) {
            notificationManager.notify(reminderId, builder.build());
        }
    }
}
