package com.creative.litcircle;

import android.content.Intent;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.creative.litcircle.appdata.AppConstant;
import com.creative.litcircle.appdata.AppController;
import com.creative.litcircle.appdata.Url;
import com.creative.litcircle.utils.ConnectionDetector;
import com.creative.litcircle.utils.DeviceInfoUtils;

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
            proceedToMainApp();
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



                      // response = "{\n" +
                      //         "\"version\": \"1.1\",\n" +
                      //         "\"url\": \"https://www.dropbox.com/s/jw798ao77hdi0tq/app-debug.apk?dl=1\"\n" +
                      //         "}";

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String server_version = jsonObject.getString("version");

                            String current_version = DeviceInfoUtils.getAppVersionName();

                           // Log.d("DEBUG_S_CUR_V", current_version);

                           // Log.d("DEBUG_S_SER_V", server_version);

                           // Log.d("DEBUG_S_PREF_V", AppController.getInstance().getPrefManger().getAppVersion());

                            AppController.getInstance().getPrefManger().setAppVersion(server_version);
                            if (!server_version.equalsIgnoreCase(current_version)) {
                                 //Log.d("DEBUG","its in here");

                                AppConstant.APP_UPDATE_URL = jsonObject.getString("url");

                                if(AppConstant.isForceLogout){
                                    AppController.getInstance().getPrefManger().setUserProfile("");
                                }

                            } else {
                                //Log.d("DEBUG","waiting stage make false");
                               // AppController.getInstance().getPrefManger().setAppUpdateWaitingStage(false);
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


        if(AppController.getInstance().getPrefManger().getAppFirstTimeInstall()){
            AppController.getInstance().getPrefManger().setAppFirstTimeInstall(false);
            try{
                createShortCut();
            }catch (Exception e ){

            }
        }


        if (AppController.getInstance().getPrefManger().getUserProfile() != null ) {
            Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }



    }

    private void createShortCut(){
        Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        shortcutintent.putExtra("duplicate", false);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
        Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_launcher);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(getApplicationContext(), SplashActivity.class));
        sendBroadcast(shortcutintent);
    }

    @Override
    public void onBackPressed() {

    }
}
