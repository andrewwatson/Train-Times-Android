package com.andrewmoorewatson.apps.traintime;

import java.net.URL;
import java.util.Hashtable;

public class TrainSchedule {

    private Hashtable<String, Hashtable<String, String>> schedule;

    public TrainSchedule() {

    }

    public static TrainSchedule getSchedule(String venue) {
        TrainSchedule schedule = new TrainSchedule();

        try {
            URL url = new URL("http://andrewmoorewatson.com/marta/venue/" + venue);
            Logger.debug("HTTP Connect: " + url.toString());

        } catch (Exception e) {

        }

        return schedule;
    }

}
