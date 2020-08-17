package com.example.esperscripttesterapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class TestBroadcastReceiver extends android.content.BroadcastReceiver {
    private static final String TAG = "TestBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String log = TestUtils.getSuccessMessage(intent);
        Log.i(TAG, log);
        Toast.makeText(context, log, Toast.LENGTH_LONG).show();
    }
}
