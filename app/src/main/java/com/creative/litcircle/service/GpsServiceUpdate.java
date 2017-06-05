package com.creative.litcircle.service;

import android.Manifest;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.creative.litcircle.NewPillarsEntry;
import com.creative.litcircle.R;
import com.creative.litcircle.appdata.AppController;
import com.creative.litcircle.appdata.Url;
import com.creative.litcircle.model.PillarValid;
import com.creative.litcircle.model.SortPlaces;
import com.creative.litcircle.model.UserLocation;
import com.creative.litcircle.utils.ConnectionDetector;
import com.creative.litcircle.utils.DeviceInfoUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class GpsServiceUpdate extends Service {
    // Connection detector class
    ConnectionDetector cd;

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private Context _context;

    private PowerManager.WakeLock mWakeLock;

    private static long lastUpdateForGpsInterval = 0;

    private static Location previousLocation = null;

    private static Location previousBestLocation = null;

    public LocationManager locationManager;

    public MyLocationListener listener;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 20000; // 1 minute

    private  List<PillarValid> pillarValids;

    private HashMap<Integer,Float> pillarNotificationMap = new HashMap<>();


    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        this._context = this;

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        mWakeLock.acquire();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

        cd = new ConnectionDetector(_context);
        pillarValids = AppController.getInstance().getPrefManger().getPillars();
        pillarNotificationMap = AppController.getInstance().getPrefManger().getPillarNotificationMap();

        if(listener == null){
            listener = new MyLocationListener();
        }
        locationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
        LocationProvider gpsProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);

        if (gpsProvider != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return 0;
            }
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    listener
            );
        }


        return START_STICKY;
    }

    public class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
          //  Log.d("DEBUG", "change");

            final double loc_lat = (double) Math.round(location.getLatitude() * 100000d) / 100000d;
            final double loc_lng = (double) Math.round(location.getLongitude() * 100000d) / 100000d;
            location.setLatitude(loc_lat);
            location.setLongitude(loc_lng);

            if (isBetterLocation(location, previousLocation) &&
                    isBetterLocationCustom(location, previousLocation)) {

                if (location == null) {
                    // Log.d("DEBUG_ALERT", "location become null");
                    return;
                }


                String user_lat = String.valueOf(loc_lat);
                String user_lang = String.valueOf(loc_lng);

                                /*Save User Location In Shared Pref*/
                AppController.getInstance().getPrefManger().setUserLastKnownLat(user_lat);
                AppController.getInstance().getPrefManger().setUserLastKnownLang(user_lang);
                                /*Save User Location In Database*/
                AppController.getsqliteDbInstance().addLocation(location);

                if (cd == null) {
                    cd = new ConnectionDetector(_context);
                }
                                /*Fetching data from database again*/
                UserLocation userLocation = AppController.getsqliteDbInstance().getLocation();

                if (cd.isConnectingToInternet()) {

                    if (AppController.getInstance().getPrefManger().getPetrolId().isEmpty()) {
                        hitUrlForStartGps(
                                AppController.getInstance().getPrefManger().getBaseUrl() + Url.URL_SOLDIER_LOCATION,
                                AppController.getInstance().getPrefManger().getUserProfile().getId(),
                                AppController.getInstance().getPrefManger().getUserStartLat(),
                                AppController.getInstance().getPrefManger().getUserStartLang());
                    } else if (userLocation != null) {
                        hitUrlForGps(
                                AppController.getInstance().getPrefManger().getBaseUrl() + Url.URL_SOLDIER_LOCATION,
                                AppController.getInstance().getPrefManger().getUserProfile().getId(),
                                user_lat,
                                user_lang,
                                userLocation.getId());
                    }


                }

                //sort the list, give the Comparator the current location
                Collections.sort(pillarValids, new SortPlaces(location));
                float distance = location.distanceTo(pillarValids.get(0).getPillar().getLocation());
                //Log.d("DEBUG_ID",String.valueOf(pillarValids.get(0).getId()));
                if(distance < 50){
                    if(pillarNotificationMap.get(pillarValids.get(0).getId()) != null){

                       // Log.d("DEBUG_distance", String.valueOf(pillarNotificationMap.get(pillarValids.get(0).getId())));

                    }else{
                        pillarNotificationMap.put(pillarValids.get(0).getId(),distance);
                        AppController.getInstance().getPrefManger().setPillarNotificationMap(pillarNotificationMap);

                       // Log.d("DEBUG_DISTANCE",String.valueOf(distance));

                        String main_sub[] = pillarValids.get(0).getPillar().getName().split("/", 2);

                        DeviceInfoUtils.increaseDeviceSound(_context);
                        if (main_sub.length > 1) {
                            if (main_sub[1].length() > 0) {
                                createNotification(main_sub[0],main_sub[1],pillarValids.get(0).getPillar().getName());
                            }else{
                                createNotification(main_sub[0],"-100",pillarValids.get(0).getPillar().getName());
                            }
                        }else{
                            createNotification(main_sub[0],"-100",pillarValids.get(0).getPillar().getName());
                        }

                    }
                }


                //Log.d("DEBUG_DISTANCE",String.valueOf(distance));

                if(previousBestLocation != null){
                    float distance_traveled = previousBestLocation.distanceTo(location);
                    AppController.getInstance().getPrefManger().setUserDistanceTraveled(
                            AppController.getInstance().getPrefManger().getUserDistanceTraveled() + distance_traveled
                    );
                }



                previousBestLocation = location;
            }

            previousLocation = location;

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }
    }

    private void hitUrlForGps(String url, final String id, final String lat, final String lng, final int locationId) {
        // TODO Auto-generated method stub

        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        response.replaceAll("\\s+", "");
                        AppController.getsqliteDbInstance().deleteLocation(locationId);


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);
                params.put("latitude", lat);
                params.put("longitude", lng);
                if (!AppController.getInstance().getPrefManger().getPetrolId().isEmpty())
                    params.put("patrolId", AppController.getInstance().getPrefManger().getPetrolId());
                params.put("authImie", AppController.getInstance().getPrefManger().getUserProfile().getImieNumber());
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }

    private void hitUrlForStartGps(String url, final String id, final String lat, final String lng) {
        // TODO Auto-generated method stub

        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        response = response.replaceAll("\\s+", "");

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String result = jsonObject.getString("result");
                            if (result.equals("1") && AppController.getInstance().getPrefManger().getPetrolId().isEmpty()) {

                                AppController.getInstance().getPrefManger().setPetrolId(jsonObject.getString("patrolId"));

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Log.d("DEBUG",String.valueOf(error));


            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);
                params.put("latitude", lat);
                params.put("longitude", lng);
                params.put("authImie", AppController.getInstance().getPrefManger().getUserProfile().getImieNumber());
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        //Log.d("DEBUG", "onBind");
        return null;
    }


    private boolean isBetterLocationCustom(Location currentLocation, Location previousLocation) {
        if (previousLocation == null) return true;

        else {
            double current_lat = currentLocation.getLatitude();
            double current_lang = currentLocation.getLongitude();
            if ((currentLocation.getLatitude() == previousLocation.getLatitude()) && (currentLocation.getLongitude()
                    == previousLocation.getLongitude())) {

                return false;
            } else if ((current_lat == current_lang) || current_lat == 0 || current_lang == 0) {
                return false;
            } else {
                double distance = 0;

                distance = currentLocation.distanceTo(previousLocation);
                if (distance < 12) return false;

            }
            return true;
        }
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        //Log.v("STOP_SERVICE", "DONE");

        if(mWakeLock!=null){
            mWakeLock.release();
        }

        if (ContextCompat.checkSelfPermission(_context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(_context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (locationManager != null) {
            locationManager.removeUpdates(listener);
            locationManager = null;
        }
    }



    public void createNotification(String main_pillar_name,String sub_pillar_name,String pillar_full_name) {
        // Prepare intent which is triggered if the
        // notification is selected
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Intent notifyIntent = new Intent(this, NewPillarsEntry.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notifyIntent.putExtra("pillar_id", main_pillar_name);
        notifyIntent.putExtra("sub_pillar_name", sub_pillar_name);
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );
        builder.setSmallIcon(R.mipmap.ic_launcher);

        builder.setContentTitle("PILLAR IS NEAR!!!");
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(pillar_full_name + " pillar is near to you!! Take a picture of this pillar when you are more closer to this pillar"));
        builder.setContentText(pillar_full_name + " pillar is near to you!! Take a picture of this pillar when you are more closer to this pillar");
        builder.setContentIntent(notifyPendingIntent);
        builder.setAutoCancel(true);


        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);
        //Vibration And Sound
        builder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});

        Uri notification = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(this, notification);
        r.play();


        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mNotificationManager.notify(NOTIFICATION_ID, builder.build());
        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;
        mNotificationManager.notify(m, builder.build());


    }
}
