package com.andrewmoorewatson.apps.traintime;

import android.util.Log;

public class Logger {

    public Logger () {

    }

    public static void debug(String message) {
        if (BuildConfig.DEBUG) {
            Log.d(Constants.LOG, message);
        }
    }

    public static void error(String message) {
        if (BuildConfig.DEBUG) {
            Log.e(Constants.LOG, message);
        }
    }
}
