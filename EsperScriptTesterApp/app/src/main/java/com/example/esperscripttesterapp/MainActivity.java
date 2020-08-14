package com.example.esperscripttesterapp;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private final static String BROADCAST_ACTIVITY = "com.example.esperscripttesterapp.IMPLICIT_BROADCAST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BroadcastReceiver receiver = new TestBroadcastReceiver();
        IntentFilter filter = new IntentFilter(BROADCAST_ACTIVITY);
        this.registerReceiver(receiver, filter);
    }
}
