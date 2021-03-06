package com.creative.litcircle.appdata;

import android.app.Application;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.creative.litcircle.BuildConfig;
import com.creative.litcircle.database.SqliteDb;
import com.creative.litcircle.sharedprefs.PrefManager;
import com.creative.litcircle.utils.LruBitmapCache;

import net.gotev.uploadservice.UploadService;


public class AppController extends MultiDexApplication {


    public static final String TAG = AppController.class.getSimpleName();

    private LruBitmapCache mLruBitmapCache;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static AppController mInstance;

    private PrefManager pref;

    public  static  int count = 0;

    private static SqliteDb sqliteDbInstance;

    private float scale;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        sqliteDbInstance = new SqliteDb(this);
        sqliteDbInstance.open();

        pref = new PrefManager(this);
        this.scale = getResources().getDisplayMetrics().density;
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;


    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public static synchronized SqliteDb getsqliteDbInstance() {
        return sqliteDbInstance;
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




}