package com.creative.litcircle.sharedprefs;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.creative.litcircle.appdata.AppConstant;
import com.creative.litcircle.appdata.Url;
import com.creative.litcircle.model.Pillar;
import com.creative.litcircle.model.PillarValid;
import com.creative.litcircle.model.UploadPillar;
import com.creative.litcircle.model.User;
import com.creative.litcircle.utils.DeviceInfoUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


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

    private static final String KEY_USER_LAT = "user_lat";
    private static final String KEY_USER_LANG = "user_lng";
    private static final String KEY_USER_DISTANCE_TRAVEL = "user_distance_travel";

    private static final String KEY_USER_START_LAT = "user_start_lat";
    private static final String KEY_USER_START_LANG = "user_start_lng";
    private static final String KEY_PILLAR = "pillars_obj";


    private static final String KEY_BASE_URL = "base_url";

    private static final String KEY_PILLAR_RESPONSE = "pillar_list";


    private static final String KEY_UPLOAD_PILLAR = "upload_pillar";

    private static final String KEY_APP_ALREADY_INSTALLED = "app_first_time_install";

    private static final String KEY_PILLAR_NOTIFICATION = "key_pillar_notification";


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

    public void setPillarInfoResponse(String type) {
        editor = pref.edit();

        editor.putString(KEY_PILLAR_RESPONSE, type);

        // commit changes
        editor.commit();
    }

    public String getPillarInfoResponse() {
        return pref.getString(KEY_PILLAR_RESPONSE, "");
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

        editor.putString(KEY_USER, GSON.toJson(obj));

        // commit changes
        editor.commit();
    }

    public void setUserProfile(String obj) {
        editor = pref.edit();

        editor.putString(KEY_USER, obj);

        // commit changes
        editor.commit();
    }

    public User getUserProfile() {

        String gson = pref.getString(KEY_USER, "");
        if (gson.isEmpty()) return null;
        return GSON.fromJson(gson, User.class);
    }

    public void setUploadPillars(List<UploadPillar> obj) {
        editor = pref.edit();

        editor.putString(KEY_UPLOAD_PILLAR, GSON.toJson(obj));

        // commit changes
        editor.commit();
    }

    public void setUploadPillars(String obj) {
        editor = pref.edit();

        editor.putString(KEY_UPLOAD_PILLAR, obj);

        // commit changes
        editor.commit();
    }


    public List<UploadPillar> getUploadPillars() {

        List<UploadPillar> productFromShared = new ArrayList<>();

        String gson = pref.getString(KEY_UPLOAD_PILLAR, "");

        if (gson.isEmpty()) return productFromShared;

        Type type = new TypeToken<List<UploadPillar>>() {
        }.getType();
        productFromShared = GSON.fromJson(gson, type);

        return productFromShared;
    }


    public void setGpsInterval(int value) {
        editor = pref.edit();
        editor.putInt(KEY_GPS_INTERVAL, value);
        editor.commit();
    }

    public int getGpsInterval() {
        return pref.getInt(KEY_GPS_INTERVAL, Integer.parseInt(AppConstant.gps_interval[1]));
    }


    public void setUserLastKnownLat(String value) {
        editor = pref.edit();
        editor.putString(KEY_USER_LAT, value);
        editor.commit();
    }

    public String getUserLastKnownLat() {
        return pref.getString(KEY_USER_LAT, "0");
    }

    public void setUserLastKnownLang(String value) {
        editor = pref.edit();
        editor.putString(KEY_USER_LANG, value);
        editor.commit();
    }

    public String getUserLastKnownLang() {
        return pref.getString(KEY_USER_LANG, "0");
    }

    public void setUserDistanceTraveled(float value) {
        editor = pref.edit();
        editor.putFloat(KEY_USER_DISTANCE_TRAVEL, value);
        editor.commit();
    }

    public float getUserDistanceTraveled() {
        return pref.getFloat(KEY_USER_DISTANCE_TRAVEL, 0);
    }

    public void setUserStartLat(String value) {
        editor = pref.edit();
        editor.putString(KEY_USER_START_LAT, value);
        editor.commit();
    }

    public String getUserStartLat() {
        return pref.getString(KEY_USER_START_LAT, "0");
    }

    public void setUserStartLang(String value) {
        editor = pref.edit();
        editor.putString(KEY_USER_START_LANG, value);
        editor.commit();
    }

    public String getUserStartLang() {
        return pref.getString(KEY_USER_START_LANG, "0");
    }

    public void setAppFirstTimeInstall(boolean value) {
        editor = pref.edit();
        editor.putBoolean(KEY_APP_ALREADY_INSTALLED, value);
        editor.commit();
    }

    public Boolean getAppFirstTimeInstall() {
        return pref.getBoolean(KEY_APP_ALREADY_INSTALLED, true);
    }


    public void setPillars(List<PillarValid> obj) {
        editor = pref.edit();

        editor.putString(KEY_PILLAR, GSON.toJson(obj));

        // commit changes
        editor.commit();
    }

    public void setPillars(String obj) {
        editor = pref.edit();

        editor.putString(KEY_PILLAR, obj);

        // commit changes
        editor.commit();
    }


    public List<PillarValid> getPillars() {

        List<PillarValid> productFromShared = new ArrayList<>();

        String gson = pref.getString(KEY_PILLAR, "");

        if (gson.isEmpty()) return productFromShared;

        Type type = new TypeToken<List<PillarValid>>() {
        }.getType();
        productFromShared = GSON.fromJson(gson, type);

        return productFromShared;
    }


    public void setPillarNotificationMap(HashMap<Integer, Float> mlist) {

        editor = pref.edit();

        editor.putString(KEY_PILLAR_NOTIFICATION, GSON.toJson(mlist));


        editor.commit();

    }

    public void setPillarNotificationMap(String obj) {
        editor = pref.edit();

        editor.putString(KEY_PILLAR_NOTIFICATION, obj);

        // commit changes
        editor.commit();
    }

    public HashMap<Integer, Float> getPillarNotificationMap() {

        HashMap<Integer, Float> listDayItems = new HashMap<>();

        String gson = pref.getString(KEY_PILLAR_NOTIFICATION, "");

        if (gson.isEmpty()) return listDayItems;

        // Type type = new TypeToken<List<PillarValid>>() {
        // }.getType();
        java.lang.reflect.Type type = new TypeToken<HashMap<Integer, Float>>() {
        }.getType();

        listDayItems = GSON.fromJson(gson, type);

        return listDayItems;
    }
}