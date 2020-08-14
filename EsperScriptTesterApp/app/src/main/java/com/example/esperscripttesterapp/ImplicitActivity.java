package com.example.esperscripttesterapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ImplicitActivity extends Activity {

    private final static String TAG = "ImplicitActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String log = TestUtils.getSuccessMessage(getIntent());
        Log.i(TAG, log);
        Toast.makeText(this, log, Toast.LENGTH_LONG).show();
        finish();
    }
}
