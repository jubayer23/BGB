package com.creative.litcircle.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.TelephonyManager;

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
            return_value = false;
        }

        if (!mp.checkPermissionForExternalStorage()) {
            mp.requestPermissionForExternalStorage();
            return_value =  false;
        }

        if (!mp.checkPermissionForPhoneState()) {
            mp.requestPermissionForPhoneState();
            return_value =  false;
        }

        return return_value;
    }


}
