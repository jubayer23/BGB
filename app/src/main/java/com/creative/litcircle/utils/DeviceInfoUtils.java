package com.creative.litcircle.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.creative.litcircle.BuildConfig;
import com.creative.litcircle.alertbanner.AlertDialogForAnything;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * Created by comsol on 22-May-16.
 */
public class DeviceInfoUtils {
    public static boolean isPlugged(Context context) {
        boolean isPlugged = false;
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        isPlugged = plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            isPlugged = isPlugged || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
        }
        return isPlugged;
    }

    public static int getBatteryLevel(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        int batteryPct = 100;
        // Calculate Battery Pourcentage ...
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        if (level != -1 && scale != -1) {
            batteryPct = (int) ((level / (float) scale) * 100f);
        }
        return batteryPct;

    }

    public  static String getPhoneNumber(Context context){
        TelephonyManager tMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getLine1Number();

    }


    public static String getAppVersionName(){

        return BuildConfig.VERSION_NAME;

    }

    /**
     * Get IP address from first non-localhost interface
     * @param useIPv4  true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }

    public static   boolean checkGps(Context context){

        ConnectionDetector cd = new ConnectionDetector(context);
        GpsEnableTool gpsEnableTool =  new GpsEnableTool(context);

        if (!cd.isGoogelPlayInstalled()) {
            //Internet Connection is not present
            AlertDialogForAnything.showAlertDialogWhenComplte(context, "Google Play Services",
                    "No google play services!!!Please Install google play services", false);
            return false;
            //stop executing code by return
        }

        LastLocationOnly lGps = new LastLocationOnly(context);


        if(!lGps.canGetLocation()){
            gpsEnableTool.enableGPs();
            return false;
        }
        //CHECK PERMISSOIN
        return true;
    }

    public static String getNetworkType(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = mTelephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "Unknown";
        }
    }

    /**
     * To check device has internet
     *
     * @param context
     * @return boolean as per status
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public static   boolean checkInternetConnectionAndGps(Context context){

        ConnectionDetector cd = new ConnectionDetector(context);
        GpsEnableTool gpsEnableTool =  new GpsEnableTool(context);

        if (!cd.isConnectingToInternet()) {
            //Internet Connection is not present
            AlertDialogForAnything.showAlertDialogWhenComplte(context, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            //stop executing code by return
            return false;
        }
        if (!cd.isGoogelPlayInstalled()) {
            //Internet Connection is not present
            AlertDialogForAnything.showAlertDialogWhenComplte(context, "Google Play Services",
                    "No google play services!!!Please Install google play services", false);
            return false;
            //stop executing code by return
        }

        LastLocationOnly lGps = new LastLocationOnly(context);


        if(!lGps.canGetLocation()){
            gpsEnableTool.enableGPs();
            return false;
        }
        //CHECK PERMISSOIN
        return true;
    }


    public static boolean checkMarshMallowPermission(Context context) {

        MarshMallowPermission mp = new MarshMallowPermission((Activity) context);


        boolean return_value = true;

        if (!mp.checkPermissionForCamera()) {
            mp.requestPermissionForCamera();
            return false;
        }

        if (!mp.checkPermissionForExternalStorage()) {
            mp.requestPermissionForExternalStorage();
            return  false;
        }

        if (!mp.checkPermissionForPhoneState()) {
            mp.requestPermissionForPhoneState();
            return  false;
        }
        if (!mp.checkPermissionForWakeLock()) {
            mp.requestPermissionForWakeLock();
            return  false;
        }
        if (!mp.checkPermissionForUserLocation()) {
            mp.requestPermissionForLocation();
            return  false;
        }

        return return_value;
    }


    public static String getDeviceImieNumber(Context context) {
        String imie = "";
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
            if (tm != null)
                imie = tm.getDeviceId();
        } catch (Exception e) {

        }
        return imie;
    }


    public static void increaseDeviceSound(Context mContext){
        AudioManager mobilemode = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
//   int streamMaxVolume = mobilemode.getStreamMaxVolume(AudioManager.STREAM_RING);
       // mobilemode.setStreamVolume(AudioManager.STREAM_MUSIC, 100, 0);
       // mobilemode.setStreamVolume(AudioManager.STREAM_RING, 100, 0);
        switch (mobilemode.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:

                mobilemode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                mobilemode.setStreamVolume(AudioManager.STREAM_RING,mobilemode.getStreamMaxVolume(AudioManager.STREAM_RING),0);
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                mobilemode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                mobilemode.setStreamVolume(AudioManager.STREAM_RING,mobilemode.getStreamMaxVolume(AudioManager.STREAM_RING),0);
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                break;

        }
    }
}
