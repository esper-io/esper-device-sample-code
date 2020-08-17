package com.example.esperscripttesterapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class TestUtils {

    private final static String ON_START_MESSAGE = "Sucessfully started with action: [%s] and extras: {%s}";

    public static String getSuccessMessage(Intent intent) {
        return String.format(ON_START_MESSAGE, intent.getAction(), getFormattedExtras(intent.getExtras()));
    }

    private static String getFormattedExtras(@Nullable Bundle extras) {
        if (extras == null)
            return null;

        StringBuilder extrasBuilder = new StringBuilder();
        for (String key : extras.keySet()) {
            extrasBuilder.append(key).append(": ").append(extras.get(key)).append(',');
        }
        extrasBuilder.deleteCharAt(extrasBuilder.length() - 1);
        return extrasBuilder.toString();
    }

}
