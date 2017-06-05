package com.creative.litcircle.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.creative.litcircle.HomeActivity;

/**
 * Created by comsol on 03-Jun-17.
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkStateReceiver";


    private HomeActivity activity;

    public NetworkStateReceiver(HomeActivity activity){
        this.activity = activity;

    }

    @Override
    public void onReceive(final Context context, final Intent intent) {


        if (intent.getExtras() != null) {
            final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

            if (ni != null && ni.isConnectedOrConnecting()) {
                activity.networkStateChange(true);
            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                activity.networkStateChange(false);
            }
        }
    }
}