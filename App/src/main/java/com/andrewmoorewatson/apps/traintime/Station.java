package com.andrewmoorewatson.apps.traintime;


import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;

public class Station {

    public Station() {

    }

    /**
     *
     * @return
     */
    public static int getDefaultStation(Fragment fragment)
    {
        Context context = fragment.getActivity();
        SharedPreferences sharedPref = fragment.getActivity().getPreferences(Context.MODE_PRIVATE);

        int defaultStation = 0;
        int station = sharedPref.getInt(fragment.getString(R.string.shared_preference_key), defaultStation);

        return station;
    }
    /**
     *
     * @param station
     * @return
     */
    public static boolean setDefaultStation(int station, Fragment fragment) {

        Context context = fragment.getActivity();
        SharedPreferences sharedPref = fragment.getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(fragment.getString(R.string.shared_preference_key), station);
        editor.commit();

        boolean success = false;
        if (station == 0) {
            success = true;
        }

        Logger.error("default station updated");
        return success;
    }
}
