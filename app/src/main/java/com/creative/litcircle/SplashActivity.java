package com.creative.litcircle;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.creative.litcircle.alertbanner.AlertDialogForAnything;
import com.creative.litcircle.appdata.AppConstant;
import com.creative.litcircle.appdata.AppController;
import com.creative.litcircle.appdata.Url;
import com.creative.litcircle.utils.ConnectionDetector;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashActivity extends AppCompatActivity {

    private final Handler mHandler = new Handler();
    private static int FATEST_INTERVAL = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        ConnectionDetector cd = new ConnectionDetector(this);

        if (cd.isConnectingToInternet()) {

            hitUrlForCheckAppUpdate(AppController.getInstance().getPrefManger().getBaseUrl() + Url.URL_CHECK_APP_UPDATE);

        } else {
            this.mHandler.postDelayed(this.mPendingLauncherRunnable, 3000L);
        }
    }

    private final Runnable mPendingLauncherRunnable = new Runnable() {
        public void run() {
            Intent localIntent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(localIntent);
            finish();
        }
    };

    private boolean mRequestingLocationUpdates = false;

    static {
        FATEST_INTERVAL = 5000;
    }

    protected void onPause() {
        super.onPause();
        this.mHandler.removeCallbacks(this.mPendingLauncherRunnable);
    }


    private void hitUrlForCheckAppUpdate(String url) {
        // TODO Auto-generated method stub

        final StringRequest req = new StringRequest(com.android.volley.Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String version = jsonObject.getString("version");

                            PackageInfo pInfo = null;
                            try {
                                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }
                            String current_version = pInfo.versionName;

                            if (!version.equalsIgnoreCase(current_version)) {


                                // Log.d("DEBUG","its in here");
                                AppController.getInstance().getPrefManger().setAppVersion(version);

                                AppConstant.APP_UPDATE_URL = jsonObject.getString("url");

                                //AlertDialogForAnything.showAlertDialogForceUpdateFromDropBox(MainActivity.this,
                                //        "App Update","Press Download To Download The Updated App","DOWNLOAD",
                                //       jsonObject.getString("url"));
                            } else {
                                //Log.d("DEBUG","waiting stage make false");
                                AppController.getInstance().getPrefManger().setAppUpdateWaitingStage(false);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();

                        }


                        proceedToMainApp();


                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                proceedToMainApp();


            }
        });

        req.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }


    private void proceedToMainApp() {
        if (AppController.getInstance().getPrefManger().getUserProfile() != null) {
            Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {

    }
}
