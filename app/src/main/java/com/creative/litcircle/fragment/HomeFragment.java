package com.creative.litcircle.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.creative.litcircle.NewPillarsEntry;
import com.creative.litcircle.R;
import com.creative.litcircle.SpeacialOps;
import com.creative.litcircle.alertbanner.AlertDialogForAnything;
import com.creative.litcircle.appdata.AppConstant;
import com.creative.litcircle.appdata.AppController;
import com.creative.litcircle.appdata.Url;
import com.creative.litcircle.service.GpsService;
import com.creative.litcircle.utils.ConnectionDetector;
import com.creative.litcircle.utils.DeviceInfoUtils;
import com.creative.litcircle.utils.GPSTracker;
import com.creative.litcircle.utils.GpsEnableTool;
import com.creative.litcircle.utils.LastLocationOnly;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private ProgressBar progressBar;
    private Button btn_start_petroling, btn_new_pillar_entry, btn_stop_patrolling, btn_special_ops;

    private ProgressDialog progressDialog;

    private ConnectionDetector cd;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container,
                false);

        String version = DeviceInfoUtils.getAppVersionName();


        if (!version.equalsIgnoreCase(AppController.getInstance().getPrefManger().getAppVersion())) {

            if (DeviceInfoUtils.checkMarshMallowPermission(getActivity())) {
                AlertDialogForAnything.showAlertDialogForceUpdateFromDropBox(getActivity(),
                        "App Update", "Press Download To Download The Updated App", "DOWNLOAD",
                        AppConstant.APP_UPDATE_URL);
            }

        }


        init(view);

        if (!AppController.getInstance().getPrefManger().getPetrolId().isEmpty()) {

            btn_stop_patrolling.setVisibility(View.VISIBLE);
            btn_start_petroling.setVisibility(View.GONE);
            btn_new_pillar_entry.setVisibility(View.VISIBLE);
        }

        return view;

    }

    public void onActivityCreated(Bundle SavedInstanceState) {
        super.onActivityCreated(SavedInstanceState);

        if (SavedInstanceState == null) {
            // DO something important
        } else {

        }
    }

    private void init(View view) {


        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage("Please Wait For Server Response...");

        cd = new ConnectionDetector(getActivity());

        btn_start_petroling = (Button) view.findViewById(R.id.btn_startpetroling);
        btn_start_petroling.setOnClickListener(this);
        btn_new_pillar_entry = (Button) view.findViewById(R.id.btn_new_pillar_entry);
        btn_new_pillar_entry.setOnClickListener(this);
        btn_stop_patrolling = (Button) view.findViewById(R.id.btn_stoppatrolling);
        btn_stop_patrolling.setOnClickListener(this);
        btn_special_ops = (Button) view.findViewById(R.id.btn_special_ops);
        btn_special_ops.setOnClickListener(this);


    }


    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (!cd.isConnectingToInternet()) {
            //Internet Connection is not present
            AlertDialogForAnything.showAlertDialogWhenComplte(getActivity(), "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            //stop executing code by return
            return;
        }

        LastLocationOnly lastLocationOnly = new LastLocationOnly(getActivity());

        if (!lastLocationOnly.canGetLocation()) {
            GpsEnableTool gpsEnableTool = new GpsEnableTool(getActivity());
            gpsEnableTool.enableGPs();
            return;
        }

        lastLocationOnly = new LastLocationOnly(getActivity());

        double loc_lat = (double) Math.round(lastLocationOnly.getLatitude() * 100000d) / 100000d;
        double loc_lng = (double) Math.round(lastLocationOnly.getLongitude() * 100000d) / 100000d;

        if ((loc_lat == loc_lng) || (loc_lat == 0) || (loc_lng == 0)) {
            AlertDialogForAnything.showAlertDialogWhenComplte(getActivity(), "GPS problem",
                    "Please press start patrolling again!", false);
            //stop executing code by return
            return;
        }

        final String user_lat = String.valueOf(loc_lat);
        final String user_lang = String.valueOf(loc_lng);

       // Log.d("DEBUG_LAT_S_OR_T", String.valueOf(loc_lat));
       // Log.d("DEBUG_LAT_S_OR_T", String.valueOf(loc_lng));

        if (id == R.id.btn_startpetroling) {

            hitUrlForStartGps(AppController.getInstance().getPrefManger().getBaseUrl() + Url.URL_SOLDIER_LOCATION,
                    AppController.getInstance().getPrefManger().getUserProfile().getId(),
                    user_lat, user_lang);

        }

        if (id == R.id.btn_stoppatrolling) {


            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

            alertDialog.setTitle("Alert!!");

            alertDialog.setMessage("Are you sure to stop patrolling.");

            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    hitUrlForStopGps(AppController.getInstance().getPrefManger().getBaseUrl() + Url.URL_SOLDIER_LOCATION,
                            AppController.getInstance().getPrefManger().getUserProfile().getId(),
                            user_lat, user_lang);
                }
            });

            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();

                }
            });

            alertDialog.show();
        }

        if (id == R.id.btn_new_pillar_entry) {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

            alertDialog.setTitle("Alert!!");

            alertDialog.setMessage("Are You Near A Pillar?");

            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    startActivity(new Intent(getActivity(), NewPillarsEntry.class));
                    dialog.cancel();
                }
            });

            alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    AlertDialogForAnything.showAlertDialogWhenComplte(getActivity(), "Warning", "Please go near a pillar before click this button again", false);

                }
            });

            alertDialog.show();
        }

        if (id == R.id.btn_special_ops) {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

            alertDialog.setTitle("Alert!!");

            alertDialog.setMessage("Are you want to take picture of a special event?");

            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    startActivity(new Intent(getActivity(), SpeacialOps.class));
                    dialog.cancel();
                }
            });

            alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            alertDialog.show();
        }

    }

    private void hitUrlForStartGps(String url, final String id, final String lat, final String lng) {
        // TODO Auto-generated method stub

        showOrHideProgressBar();

        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        showOrHideProgressBar();
                        response = response.replaceAll("\\s+", "");

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String result = jsonObject.getString("result");
                            if (result.equals("1") && AppController.getInstance().getPrefManger().getPetrolId().isEmpty()) {

                                AppController.getInstance().getPrefManger().setPetrolId(jsonObject.getString("patrolId"));

                                //RESTART SERVICE
                                getActivity().stopService(new Intent(getActivity(), GpsService.class));
                                getActivity().startService(new Intent(getActivity(), GpsService.class));

                                btn_stop_patrolling.setVisibility(View.VISIBLE);
                                btn_start_petroling.setVisibility(View.GONE);
                                btn_new_pillar_entry.setVisibility(View.VISIBLE);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                showOrHideProgressBar();

                // Log.d("DEBUG",String.valueOf(error));


            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);
                params.put("latitude", lat);
                params.put("longitude", lng);
                params.put("authImie", AppController.getInstance().getPrefManger().getUserProfile().getImieNumber());
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }


    private void hitUrlForStopGps(String url, final String id, final String lat, final String lng) {
        // TODO Auto-generated method stub

        showOrHideProgressBar();

        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        showOrHideProgressBar();
                        response = response.replaceAll("\\s+", "");
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String result = jsonObject.getString("result");
                            if (result.equals("1")) {
                                getActivity().stopService(new Intent(getActivity(), GpsService.class));


                                btn_start_petroling.setVisibility(View.VISIBLE);
                                btn_stop_patrolling.setVisibility(View.GONE);
                                btn_new_pillar_entry.setVisibility(View.GONE);

                                AppController.getInstance().getPrefManger().setPetrolId("");
                            } else {
                                AlertDialogForAnything.showAlertDialogWhenComplte(getActivity(), "Alert", "There is something wrong when stop patrolling", false);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                showOrHideProgressBar();

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);
                params.put("latitude", lat);
                params.put("longitude", lng);
                params.put("patrolId", AppController.getInstance().getPrefManger().getPetrolId());
                params.put("authImie", AppController.getInstance().getPrefManger().getUserProfile().getImieNumber());
                params.put("endPatrol", "true");
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }

    private void showOrHideProgressBar() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        } else
            progressDialog.show();
    }


}
