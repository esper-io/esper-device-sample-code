package com.example.esperscripttesterapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class TestForegroundService extends Service {

    private final static String TAG = "TestForegroundService";
    private final static int ID = 1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(ID, getNotification());
        }
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String log = TestUtils.getSuccessMessage(intent);
        Log.i(TAG, log);
        Toast.makeText(this, log, Toast.LENGTH_LONG).show();
        stopSelf();
        return START_NOT_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification getNotification() {
        final String channelID = "com.example.esperscripttesterapp";
        final String channelName = "TestForegroundService";
        final String channelDescription = "TestForegroundService Notification Channel";
        final int importance = NotificationManager.IMPORTANCE_HIGH;
        final String title = "TestForegroundService is running in foreground";
        final String content = "";

        NotificationChannel channel = new NotificationChannel(channelID, channelName, importance);
        channel.setDescription(channelDescription);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(importance)
                .setCategory(Notification.CATEGORY_SERVICE);

        return notificationBuilder.build();
    }
}
