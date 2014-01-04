package com.andrewmoorewatson.apps.traintime;

import android.content.Context;
import android.location.LocationManager;

public class Locator implements Runnable {
    Context context;

    public Locator(Context context) {
        this.context = context;

        LocationManager mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void run() {

    }
}
