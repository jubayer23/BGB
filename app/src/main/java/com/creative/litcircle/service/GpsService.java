package com.creative.litcircle.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.creative.litcircle.appdata.AppController;
import com.creative.litcircle.appdata.Url;
import com.creative.litcircle.utils.ConnectionDetector;
import com.creative.litcircle.utils.GPSTracker;

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

                        cd = new ConnectionDetector(_context);

                        if (cd.isConnectingToInternet()) {
                            //Log.d("DEBUG", "user enable");

                            //Log.d("DEBUG", "user connextion ok");
                            if (GPSTracker.isGPSEnabled &&
                                    ((curTime - lastUpdateForGpsInterval) >= AppController.getInstance().getPrefManger()
                                            .getGpsInterval() * 1000)) {


                                lastUpdateForGpsInterval = curTime;

                                Location locaion = GPSTracker.location;

                                if(locaion == null)return;

                                double loc_lat = (double) Math.round(locaion.getLatitude() * 100000d) / 100000d;
                                double loc_lng = (double) Math.round(locaion.getLongitude() * 100000d) / 100000d;
                                locaion.setLatitude(loc_lat);
                                locaion.setLongitude(loc_lng);

                                Log.d("DEBUG_IN_LAT_1",String.valueOf(loc_lat));
                                Log.d("DEBUG_IN_LANG_1",String.valueOf(loc_lng));


                                if (isBetterLocation(locaion, previousLocation)) {
                                    String user_lat = String.valueOf(locaion.getLatitude());
                                    String user_lang = String.valueOf(locaion.getLongitude());

                                    Log.d("DEBUG_IN_LAT_2",user_lat);
                                    Log.d("DEBUG_IN_LANG_2",user_lang);

                                    hitUrlForGps(Url.URL_SOLDIER_LOCATION, AppController.getInstance().getPrefManger().getUserProfile().getId(),
                                            user_lat, user_lang);
                                }

                                previousLocation = locaion;

                            }


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

    private void hitUrlForGps(String url, final String id, final String lat, final String lng) {
        // TODO Auto-generated method stub

        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        response.replaceAll("\\s+", "");


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
                params.put("authUsername", AppController.getInstance().getPrefManger().getUserProfile().getUser_id());
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
            if ((currentLocation.getLatitude() == previousLocation.getLatitude()) && (currentLocation.getLongitude()
                    == previousLocation.getLongitude())) {

                return false;
            } else {
                double distance = 0;

                distance = currentLocation.distanceTo(previousLocation);
                if(distance < 12)return false;

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

        AppController.getInstance().getPrefManger().setPetrolId("");

        //stopSelf();
    }
}
