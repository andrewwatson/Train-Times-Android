package com.andrewmoorewatson.apps.traintime;

import android.location.Location;

import java.io.Serializable;

/**
 * This class represents a point of interest that has geographical coordinates (latitude and
 * longitude) and a name that is displayed to the user.
 */
public class Place implements Serializable {

    private final double mLatitude;
    private final double mLongitude;
    private final String mName;
    private double mDistanceAway;

    public Place(Location location) {
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
        mName = "";
        mDistanceAway = 0.0;
    }

    /**
     * Initializes a new place with the specified coordinates and name.
     *
     * @param latitude the latitude of the place
     * @param longitude the longitude of the place
     * @param name the name of the place
     */
    public Place(double latitude, double longitude, String name) {
        mLatitude = latitude;
        mLongitude = longitude;
        mName = name;
        mDistanceAway = 0.0;
    }

    /**
     * Initializes a new place with the specified coordinates and name.
     *
     * @param latitude the latitude of the place
     * @param longitude the longitude of the place
     * @param name the name of the place
     */
    public Place(double latitude, double longitude, String name, double distanceAway) {
        mLatitude = latitude;
        mLongitude = longitude;
        mName = name;
        mDistanceAway = distanceAway;
    }

    public void setDistanceAway(double distance) {
        mDistanceAway = distance;
    }

    /**
     * Gets the distance this landmark is from your current position if it
     * was known when constructed
     * @return
     */
    public double getDistanceAway() { return mDistanceAway; }

    /**
     * Gets the latitude of the place.
     *
     * @return the latitude of the place
     */
    public double getLatitude() {
        return mLatitude;
    }

    /**
     * Gets the longitude of the place.
     *
     * @return the longitude of the place
     */
    public double getLongitude() {
        return mLongitude;
    }

    /**
     * Gets the name of the place.
     *
     * @return the name of the place
     */
    public String getName() {
        return mName;
    }
}
