package com.creative.litcircle.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.creative.litcircle.appdata.AppController;
import com.creative.litcircle.appdata.Url;
import com.creative.litcircle.model.UserLocation;
import com.creative.litcircle.utils.ConnectionDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class GpsService extends Service {
    // Connection detector class
    ConnectionDetector cd;

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private Context _context;

    private PowerManager.WakeLock mWakeLock;

    private Thread mThread;

    private static long lastUpdateForGpsInterval = 0;

    private static Location previousLocation = null;


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

        stopService(new Intent(_context, GPSTracker.class));
        startService(new Intent(_context, GPSTracker.class));

        cd = new ConnectionDetector(_context);

        doInBackGround();

        return START_STICKY;
    }

    private void doInBackGround() {


        mThread = new Thread(new Runnable() {
            boolean abort = false;

            public void run() {
                // TODO Auto-generated method stub
                while (true && !abort) {


                    try {

                        // LastLocationOnly lastLocationOnly= new LastLocationOnly(_context);

                        final long curTime = System.currentTimeMillis();

                        if (GPSTracker.isGPSEnabled &&
                                ((curTime - lastUpdateForGpsInterval) >= AppController.getInstance().getPrefManger()
                                        .getGpsInterval() * 1000)) {


                            Location location = GPSTracker.location;

                            if (location == null) {
                               // Log.d("DEBUG_ALERT", "location become null");
                                stopService(new Intent(_context, GPSTracker.class));
                                startService(new Intent(_context, GPSTracker.class));
                                return;
                            }

                            double loc_lat = (double) Math.round(location.getLatitude() * 100000d) / 100000d;
                            double loc_lng = (double) Math.round(location.getLongitude() * 100000d) / 100000d;
                            location.setLatitude(loc_lat);
                            location.setLongitude(loc_lng);
                           // Log.d("DEBUG_LAT_1",String.valueOf(location.getLatitude()));
                            //Log.d("DEBUG_LANG_1",String.valueOf(location.getLongitude()));

                            if (isBetterLocation(location, previousLocation)) {
                                String user_lat = String.valueOf(location.getLatitude());
                                String user_lang = String.valueOf(location.getLongitude());

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

                            }
                            previousLocation = location;

                        }

                        Thread.sleep(5000);


                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        abort = true;
                        e.printStackTrace();
                    }

                }

            }
        });
        mThread.start();
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


    private boolean isBetterLocation(Location currentLocation, Location previousLocation) {
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

    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        //Log.v("STOP_SERVICE", "DONE");

        mWakeLock.release();

        mThread.interrupt();

        stopService(new Intent(_context, GPSTracker.class));

        //AppController.getInstance().getPrefManger().setPetrolId("");

        AppController.getInstance().getPrefManger().setUserLastKnownLat("0");
        AppController.getInstance().getPrefManger().setUserLastKnownLat("0");

        //stopSelf();
    }
}
