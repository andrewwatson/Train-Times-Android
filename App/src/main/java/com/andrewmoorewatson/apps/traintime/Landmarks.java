package com.andrewmoorewatson.apps.traintime;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * This class provides access to a list of hard-coded landmarks (located in
 * {@code res/raw/landmarks.json}) that will appear on the compass when the user is near them.
 */
public class Landmarks {

    private static final String TAG = Landmarks.class.getSimpleName();

    /**
     * The threshold used to display a landmark on the compass.
     */
    private static final double MAX_DISTANCE_KM = 0.5;

    /**
     * The list of landmarks loaded from resources.
     */
    private final ArrayList<Place> mPlaces;

    /**
     * Initializes a new {@code Landmarks} object by loading the landmarks from the resource
     * bundle.
     */
    public Landmarks(Context context) {
        mPlaces = new ArrayList<Place>();

        // This class will be instantiated on the service's main thread, and doing I/O on the
        // main thread can be dangerous if it will block for a noticeable amount of time. In
        // this case, we assume that the landmark data will be small enough that there is not
        // a significant penalty to the application. If the landmark data were much larger,
        // we may want to load it in the background instead.
        String jsonString = readLandmarksResource(context);
//        Logger.debug(jsonString);
        populatePlaceList(jsonString);
    }

    public List<Place> getNearbyLandmarks(double latitude, double longitude) {
        Logger.debug("no radius passed in");
        return getNearbyLandmarks(latitude, longitude, MAX_DISTANCE_KM);
    }

    /**
     * Gets a list of landmarks that are within ten kilometers of the specified coordinates. This
     * function will never return null; if there are no locations within that threshold, then an
     * empty list will be returned.
     */
    public List<Place> getNearbyLandmarks(double latitude, double longitude, double radius) {
        ArrayList<Place> nearbyPlaces = new ArrayList<Place>();
        Logger.debug("searching within radius " + radius);
        for (Place knownPlace : mPlaces) {
            if (MathUtils.getDistance(latitude, longitude,
                    knownPlace.getLatitude(), knownPlace.getLongitude()) <= radius) {
                nearbyPlaces.add(knownPlace);
            }
        }

        return nearbyPlaces;
    }

    public Place getClosestPlace(double latitude, double longitude) {
        return getClosestPlace(latitude, longitude, 100.0);
    }

    public Place getClosestPlace(double latitude, double longitude, double maximum) {
        Place closestPlace = null;
        double shortestDistance = 0.0;

        for (Place knownPlace : mPlaces) {

            double placeDistance = MathUtils.getDistance(latitude, longitude,
                    knownPlace.getLatitude(), knownPlace.getLongitude());

            if ((shortestDistance == 0.0 || placeDistance < shortestDistance) && placeDistance < maximum) {
                shortestDistance = placeDistance;
                closestPlace = knownPlace;
                closestPlace.setDistanceAway(shortestDistance);
            }

        }

        return closestPlace;
    }

    /**
     * Populates the internal places list from places found in a JSON string. This string should
     * contain a root object with a "landmarks" property that is an array of objects that represent
     * places. A place has three properties: name, latitude, and longitude.
     */
    private void populatePlaceList(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            JSONArray array = json.optJSONArray("landmarks");

            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.optJSONObject(i);

                    Place place = jsonObjectToPlace(object);
                    if (place != null) {
                        mPlaces.add(place);
                    }
                }
            }
        } catch (JSONException e) {
            Logger.error("Could not parse landmarks JSON string " + e.getMessage());
        }
    }

    /**
     * Converts a JSON object that represents a place into a {@link Place} object.
     */
    private Place jsonObjectToPlace(JSONObject object) {
        String name = object.optString("name");
        double latitude = object.optDouble("latitude", Double.NaN);
        double longitude = object.optDouble("longitude", Double.NaN);
        String venue = object.optString("venue", "");

//        Logger.debug("MAKE PLACE " + name);
        if (!name.isEmpty() && !Double.isNaN(latitude) && !Double.isNaN(longitude)) {

            return new Place(latitude, longitude, name, venue);
        } else {
            return null;
        }
    }

    /**
     * Reads the text from {@code res/raw/landmarks.json} and returns it as a string.
     */
    private static String readLandmarksResource(Context context) {
        InputStream is = context.getResources().openRawResource(R.raw.landmarks);
        StringBuffer buffer = new StringBuffer();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append('\n');
            }
        } catch (IOException e) {
            Log.e(TAG, "Could not read landmarks resource", e);
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "Could not close landmarks resource stream", e);
                }
            }
        }

        return buffer.toString();
    }
}
