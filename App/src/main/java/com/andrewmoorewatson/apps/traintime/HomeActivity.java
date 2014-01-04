package com.andrewmoorewatson.apps.traintime;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.TextView;
import android.location.Criteria;

import java.util.List;

public class HomeActivity extends Activity {

    private static Landmarks mLandmarks;
    private static List<Place> nearbyLandmarks;

    private static LocationManager mLocationManager;

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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        private Place getCurrentLocation() {
            Place place = new Place(0.0,0.0, "");

            return place;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_home, container, false);
            return rootView;
        }

        public void updateLocation() {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);

            String bestProvider = mLocationManager.getBestProvider(criteria, true);

            /**
             * Arts Center Marta Station
             */
            double myLatitude = 33.789715;
            double myLongitude = -84.387769;

            if (bestProvider != null) {
                Location mLocation = mLocationManager.getLastKnownLocation(bestProvider);

                if (mLocation != null) {
                    Logger.debug("LAT " + mLocation.getLatitude());
                    myLatitude = mLocation.getLatitude();
                    myLongitude = mLocation.getLongitude();

                } else {
                    Logger.debug("last known location unavailable");
                }
            } else {
                Logger.debug("No Provider Available");
            }

            View rootView = this.getView();

            if (rootView == null) {
                Logger.debug("No View Attached");
                return;
            }

            TextView location = (TextView) rootView.findViewById(R.id.location);
            location.setText(myLatitude + ", " + myLongitude);

            Place closestStation = mLandmarks.getClosestPlace(myLatitude, myLongitude);
            TextView nearestStationView = (TextView) rootView.findViewById(R.id.nearestStation);
            TextView distanceAwayView = (TextView) rootView.findViewById(R.id.distanceAway);

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
        public void onResume() {
            super.onResume();
            Logger.debug("RESUME");
            updateLocation();
        }

        public void onStart() {
            super.onStart();
            Logger.debug("START");
//            updateLocation();
        }

    }

}
