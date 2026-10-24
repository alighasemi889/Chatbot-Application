package com.example.aichat;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/** Shows the notification when a task's alarm fires, and provides scheduling helpers. */
public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "tasks";
    static final String EXTRA_TITLE = "title";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        String title = intent.getStringExtra(EXTRA_TITLE);
        if (title == null) title = "";

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, ctx.getString(R.string.tasks), NotificationManager.IMPORTANCE_HIGH);
            ctx.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        if (Build.VERSION.SDK_INT >= 33
                && ActivityCompat.checkSelfPermission(ctx, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Notification n = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(ctx.getString(R.string.notification_title))
                .setContentText(title)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
        NotificationManagerCompat.from(ctx).notify(title.hashCode(), n);
    }

    static PendingIntent pending(Context ctx, TaskStore.Task t) {
        Intent i = new Intent(ctx, ReminderReceiver.class).putExtra(EXTRA_TITLE, t.title);
        return PendingIntent.getBroadcast(ctx, t.id, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static void schedule(Context ctx, TaskStore.Task t) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = pending(ctx, t);
        if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t.time, pi);
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t.time, pi);
        }
    }

    static void cancel(Context ctx, TaskStore.Task t) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pending(ctx, t));
    }
}
