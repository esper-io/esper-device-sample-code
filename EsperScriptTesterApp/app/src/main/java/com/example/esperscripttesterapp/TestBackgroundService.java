package com.example.esperscripttesterapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class TestBackgroundService extends Service {

    private final static String TAG = "TestBackgroundService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String log = TestUtils.getSuccessMessage(intent);
        Log.i(TAG, log);
        Toast.makeText(this, log, Toast.LENGTH_LONG).show();
        stopSelf();
        return START_NOT_STICKY;
    }
}
