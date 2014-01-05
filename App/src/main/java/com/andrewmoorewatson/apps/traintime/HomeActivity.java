package com.andrewmoorewatson.apps.traintime;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.TextView;
import android.location.Criteria;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;

public class HomeActivity extends Activity {

    private static Landmarks mLandmarks;
    private static List<Place> nearbyLandmarks;

    private static LocationManager mLocationManager;

    private Place currentLocation;

    private TextView nearestStationView;
    private TextView locationView;
    private TextView distanceAwayView;
    private TextView lastUpdateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        mLandmarks = new Landmarks(this);
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

    }

    public void updateLocation() {
        DetermineLocationTask determiner = new DetermineLocationTask();
        determiner.execute();

        Toast.makeText(this, "Updating Location", Toast.LENGTH_LONG);
    }

    public void onResume() {
        super.onResume();
        updateLocation();

        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateLocation();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class DetermineLocationTask extends AsyncTask<String, Void, Hashtable<String, Place>> {

        @Override
        protected Hashtable<String, Place> doInBackground(String... params) {

            Logger.debug("Starting ASYNC Task");
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);

            String bestProvider = mLocationManager.getBestProvider(criteria, true);

            Hashtable<String, Place> results = new Hashtable<String, Place>(2);

            /**
             * Arts Center Marta Station
             */
            double myLatitude = 33.789715;
            double myLongitude = -84.387769;

            Place closestStation = new Place(myLatitude, myLongitude, "Default");

            if (bestProvider != null) {
                Location mLocation = mLocationManager.getLastKnownLocation(bestProvider);

                Place currentPlace = new Place(mLocation);

                if (mLocation != null) {
                    Logger.debug("LATx " + mLocation.getLatitude());
                    closestStation = mLandmarks.getClosestPlace(
                            mLocation.getLatitude(),
                            mLocation.getLongitude()
                    );

                    results.put("location", currentPlace);

                } else {
                    Logger.debug("last known location unavailable");
                }
            } else {
                Logger.debug("No Provider Available");
            }

            try {
                Thread.sleep(2000);
                Logger.debug("Ok I'm awake");

            } catch (InterruptedException e) {

            }

            results.put("closest", closestStation);
            return results;
        }

        protected void onPostExecute(Hashtable<String, Place> results) {

            Place mLocation = results.get("location");
            Place closestStation = results.get("closest");

            Logger.debug("LOCATION FOUND LAT " + mLocation.getLatitude() + " LONG " + mLocation.getLongitude());
            Logger.debug("STATION FOUND LAT " + closestStation.getLatitude() + " LONG " + closestStation.getLongitude());

            locationView = (TextView) findViewById(R.id.location);
            nearestStationView = (TextView) findViewById(R.id.nearestStation);
            distanceAwayView = (TextView) findViewById(R.id.distanceAway);
            lastUpdateView = (TextView) findViewById(R.id.lastUpdatedView);

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            String lastUpdated = sdf.format(cal.getTime());

            locationView.setText(mLocation.getLatitude() + ", " + mLocation.getLongitude());
            lastUpdateView.setText(lastUpdated);

            if (closestStation != null) {
                Logger.debug("Closest Station is " + closestStation.getName());
                Logger.debug("Station is " + closestStation.getDistanceAway() + " km away");

                if (nearestStationView == null) {
                    Logger.debug("nearestStationView is null!");
                } else {
                    nearestStationView.setText(closestStation.getName());
                    distanceAwayView.setText(closestStation.getDistanceAway() + " km away");
                }
            }

        }
    }
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_home, container, false);
            return rootView;
        }

    }

}
