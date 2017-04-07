package com.creative.litcircle.sharedprefs;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.creative.litcircle.appdata.AppConstant;
import com.creative.litcircle.appdata.Url;
import com.creative.litcircle.model.User;
import com.creative.litcircle.utils.DeviceInfoUtils;
import com.google.gson.Gson;


public class PrefManager {
    private static final String TAG = PrefManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    private static Gson GSON = new Gson();
    // Sharedpref file name
    private static final String PREF_NAME = "com.creative.roboticcameraapp";

    private static final String KEY_LOGIN_TYPE = "login_type";

    private static final String KEY_APP_VERSION = "app_version";

    private static final String KEY_USER = "user";

    private static final String KEY_GPS_INTERVAL = "gps_interval";

    private static final String KEY_PETROL_ID = "patrol_id";

    private static final String KEY_BASE_URL = "base_url";


    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);

    }

    public void setLoginType(String type) {
        editor = pref.edit();

        editor.putString(KEY_LOGIN_TYPE, type);

        // commit changes
        editor.commit();
    }

    public String getLoginType() {
        return pref.getString(KEY_LOGIN_TYPE, "");
    }


    public void setBaseUrl(String type) {
        editor = pref.edit();

        editor.putString(KEY_BASE_URL, type);

        // commit changes
        editor.commit();
    }

    public String getBaseUrl() {
        return pref.getString(KEY_BASE_URL, Url.BaseUrl);
    }

    public void setAppVersion(String value) {
        editor = pref.edit();

        editor.putString(KEY_APP_VERSION, value);

        // commit changes
        editor.commit();
    }

    public String getAppVersion() {
        return pref.getString(KEY_APP_VERSION, DeviceInfoUtils.getAppVersionName());
    }

    public void setPetrolId(String type) {
        editor = pref.edit();

        editor.putString(KEY_PETROL_ID, type);
        // commit changes
        editor.commit();
    }

    public String getPetrolId() {
        return pref.getString(KEY_PETROL_ID, "");
    }


    public void setUserProfile(User obj) {
        editor = pref.edit();

        editor.putString(KEY_USER,GSON.toJson(obj) );

        // commit changes
        editor.commit();
    }
    public void setUserProfile(String obj) {
        editor = pref.edit();

        editor.putString(KEY_USER,obj);

        // commit changes
        editor.commit();
    }

    public User getUserProfile() {

        String gson = pref.getString(KEY_USER,"");
        if(gson.isEmpty())return null;
        return GSON.fromJson(gson,User.class);
    }

    public void setGpsInterval(int value) {
        editor = pref.edit();
        editor.putInt(KEY_GPS_INTERVAL, value);
        editor.commit();
    }

    public int getGpsInterval() {
        return pref.getInt(KEY_GPS_INTERVAL, Integer.parseInt(AppConstant.gps_interval[1]));
    }


}