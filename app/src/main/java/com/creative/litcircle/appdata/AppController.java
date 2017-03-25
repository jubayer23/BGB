package com.creative.litcircle.appdata;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.creative.litcircle.BuildConfig;
import com.creative.litcircle.HomeActivity;
import com.creative.litcircle.alertbanner.AlertDialogForAnything;
import com.creative.litcircle.model.User;
import com.creative.litcircle.sharedprefs.PrefManager;
import com.creative.litcircle.utils.LruBitmapCache;

import net.gotev.uploadservice.UploadService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class AppController extends Application {


    public static final String TAG = AppController.class.getSimpleName();

    private LruBitmapCache mLruBitmapCache;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static AppController mInstance;

    private PrefManager pref;

    public  static  int count = 0;


    private float scale;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;


        pref = new PrefManager(this);
        this.scale = getResources().getDisplayMetrics().density;
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;

        hitUrlForCheckUpdate(Url.URL_CHECK_APP_UPDATE);

    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }


    public PrefManager getPrefManger() {
        if (pref == null) {
            pref = new PrefManager(this);
        }

        return pref;
    }

    public RequestQueue getRequestQueue() {


        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            getLruBitmapCache();
            mImageLoader = new ImageLoader(this.mRequestQueue, mLruBitmapCache);
        }

        return this.mImageLoader;
    }

    public LruBitmapCache getLruBitmapCache() {
        if (mLruBitmapCache == null)
            mLruBitmapCache = new LruBitmapCache();
        return this.mLruBitmapCache;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public int getPixelValue(int dps) {
        int pixels = (int) (dps * scale + 0.5f);
        return pixels;
    }

    private void hitUrlForCheckUpdate(String url) {
        // TODO Auto-generated method stub
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage("Login In...");
        progressDialog.dismiss();

        final StringRequest req = new StringRequest(com.android.volley.Request.Method.POST, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                       // progressDialog.dismiss();

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = Integer.parseInt(jsonObject.getString("result"));

                            if (status == 1) {

                            } else {


                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

             //  progressDialog.dismiss();

//                AlertDialogForAnything.showAlertDialogWhenComplte(getApplicationContext(),"yes","yes",false);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                Map<String, String> params = new HashMap<String, String>();
                //params.put("mobileNumber",mobileNumber);
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        addToRequestQueue(req);
    }



}