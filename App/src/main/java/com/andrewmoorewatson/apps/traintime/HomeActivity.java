package com.andrewmoorewatson.apps.traintime;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.location.Criteria;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;

import fi.foyt.foursquare.api.FoursquareApi;

public class HomeActivity extends Activity {


    private static Landmarks mLandmarks;
    private static List<Place> nearbyLandmarks;

    private static LocationManager mLocationManager;

    private Place currentLocationPlace;
    private Location currentLocation;

    private TextView nearestStationView;
    private TextView locationView;
    private TextView distanceAwayView;
    private TextView lastUpdateView;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        context = getApplicationContext();

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        mLandmarks = new Landmarks(this);
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

    }

    public void locationToast(String message) {
        Toast updateToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        updateToast.setGravity(Gravity.CENTER_VERTICAL, 0,0);
        updateToast.show();
    }

    public void onResume() {
        super.onResume();

        locationToast("Updating Location...");

        LocationListener locationListener = new LocationListener() {

            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.

                if (currentLocation == null || (Locator.isBetterLocation(location, currentLocation))) {
                    currentLocationPlace = new Place(location);
                    currentLocation = location;

                    locationToast("Location Updated");
                    Logger.debug("BETTER LOCATION UPDATE " + location.getLatitude());

                    Place closestStation = mLandmarks.getClosestPlace(
                            location.getLatitude(),
                            location.getLongitude(),
                            15.0
                    );

                    String clientId = context.getString(R.string.foursquare_client_id);
                    String clientSecret = context.getString(R.string.foursquare_client_secret);

                    locationView = (TextView) findViewById(R.id.location);
                    nearestStationView = (TextView) findViewById(R.id.nearestStation);
                    distanceAwayView = (TextView) findViewById(R.id.distanceAway);
                    lastUpdateView = (TextView) findViewById(R.id.lastUpdatedView);

                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    Calendar cal = Calendar.getInstance();
                    String lastUpdated = sdf.format(cal.getTime());

                    locationView.setText(location.getLatitude() + ", " + location.getLongitude());
                    lastUpdateView.setText(lastUpdated);

                    if (nearestStationView != null) {
                        if (closestStation != null) {
                            nearestStationView.setText(closestStation.getName());
                            distanceAwayView.setText(closestStation.getDistanceAway() + " km away");

                            DetermineScheduleTask scheduleDeterminer = new DetermineScheduleTask();
                            scheduleDeterminer.execute(closestStation.getVenue());

                            Logger.debug("Shutting down notifications from Location Manager");
                            mLocationManager.removeUpdates(this);

                        } else {
                            nearestStationView.setText(R.string.no_station);
                        }
                    }

                }

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30, 0, locationListener);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, 0, locationListener);
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

    private class DetermineScheduleTask extends AsyncTask<String, Void, TrainSchedule> {

        protected TrainSchedule doInBackground(String... params) {

            Logger.debug("Starting ASYNC Task");
            TrainSchedule schedule = new TrainSchedule();

            for (String venue: params) {
                schedule = TrainSchedule.getSchedule(venue);
            }

            return schedule;
        }

        protected void onPostExecute(TrainSchedule schedule) {

        }
    }

    private class DetermineLocationTask extends AsyncTask<String, Void, Hashtable<String, Place>> {

        @Override
        protected Hashtable<String, Place> doInBackground(String... params) {

            Locator locator = new Locator();

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);

            String bestProvider = mLocationManager.getBestProvider(criteria, true);

            Hashtable<String, Place> results = new Hashtable<String, Place>(2);

            /**
             * Arts Center Marta Station
             */
            double myLatitude = 33.789715;
            double myLongitude = -84.387769;

            if (bestProvider != null) {
                Location mLocation = mLocationManager.getLastKnownLocation(bestProvider);

                Place currentPlace = new Place(mLocation);

                if (mLocation != null) {
                    Logger.debug("LATx " + mLocation.getLatitude());
                    Place closestStation = mLandmarks.getClosestPlace(
                            mLocation.getLatitude(),
                            mLocation.getLongitude()
                    );

                    results.put("location", currentPlace);
                    if (closestStation != null) {
                        Logger.debug("CLOSEST: " + closestStation.getVenue());
                        results.put("closest", closestStation);
                    }

                } else {
                    Logger.debug("last known location unavailable");
                }
            } else {
                Logger.debug("No Provider Available");
            }

            try {
                Thread.sleep(500);
//                Logger.debug("Ok I'm awake");

            } catch (InterruptedException e) {

            }

            return results;
        }

        protected void onPostExecute(Hashtable<String, Place> results) {

            Place mLocation = results.get("location");
            Place closestStation = results.get("closest");

//            Logger.debug("LOCATION FOUND LAT " + mLocation.getLatitude() + " LONG " + mLocation.getLongitude());

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

                if (nearestStationView == null) {
                    Logger.debug("nearestStationView is null!");
                } else {
                    nearestStationView.setText(closestStation.getName());
                    distanceAwayView.setText(closestStation.getDistanceAway() + " km away");
                }
            }

            DetermineScheduleTask scheduleDeterminer = new DetermineScheduleTask();
            scheduleDeterminer.execute(closestStation.getVenue());


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
