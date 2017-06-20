package com.creative.litcircle.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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
import com.creative.litcircle.MapActivity;
import com.creative.litcircle.NewPillarsEntry;
import com.creative.litcircle.R;
import com.creative.litcircle.SpeacialOps;
import com.creative.litcircle.alertbanner.AlertDialogForAnything;
import com.creative.litcircle.appdata.AppConstant;
import com.creative.litcircle.appdata.AppController;
import com.creative.litcircle.appdata.Url;
import com.creative.litcircle.helperActivity.UploadActivity;
import com.creative.litcircle.helperActivity.UploadMultipleFilesActivity;
import com.creative.litcircle.model.UserLocation;
import com.creative.litcircle.service.GpsService;
import com.creative.litcircle.service.GpsServiceUpdate;
import com.creative.litcircle.utils.ConnectionDetector;
import com.creative.litcircle.utils.DeviceInfoUtils;
import com.creative.litcircle.utils.GpsEnableTool;
import com.creative.litcircle.utils.LastLocationOnly;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private ProgressBar progressBar;
    private Button btn_start_petroling, btn_new_pillar_entry, btn_stop_patrolling, btn_special_ops, btn_upload_pending_pillar;

    private ProgressDialog progressDialog;

    private ConnectionDetector cd;

    private static int how_many_time_user_press_start = 0;

    private static final int UPLOAD_REQUEST = 3333;

    private FloatingActionButton btn_map;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container,
                false);

        how_many_time_user_press_start = 0;

        init(view);

        if (!AppController.getInstance().getPrefManger().getPetrolId().isEmpty() ||
                (!AppController.getInstance().getPrefManger().getUserStartLat().equals("0")
                        && !AppController.getInstance().getPrefManger().getUserStartLang().equals("0"))) {


            //RESTART SERVICE
            getActivity().stopService(new Intent(getActivity(), GpsServiceUpdate.class));
            getActivity().startService(new Intent(getActivity(), GpsServiceUpdate.class));

            btn_stop_patrolling.setVisibility(View.VISIBLE);
            btn_start_petroling.setVisibility(View.GONE);
            btn_new_pillar_entry.setVisibility(View.VISIBLE);
        }

        return view;

    }

    public void onActivityCreated(Bundle SavedInstanceState) {
        super.onActivityCreated(SavedInstanceState);

        if (SavedInstanceState == null) {


            String version = DeviceInfoUtils.getAppVersionName();

            if (!version.equalsIgnoreCase(AppController.getInstance().getPrefManger().getAppVersion())) {

                AlertDialogForAnything.showAlertDialogForceUpdateFromDropBox(getActivity(),
                        "App Update", "Press Download To Download The Updated App", "DOWNLOAD",
                        AppConstant.APP_UPDATE_URL);

            }

        } else {

        }
    }

    private void init(View view) {


        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage("Please Wait...");

        cd = new ConnectionDetector(getActivity());

        btn_start_petroling = (Button) view.findViewById(R.id.btn_startpetroling);
        btn_start_petroling.setOnClickListener(this);
        btn_new_pillar_entry = (Button) view.findViewById(R.id.btn_new_pillar_entry);
        btn_new_pillar_entry.setVisibility(View.GONE);
        btn_new_pillar_entry.setOnClickListener(this);
        btn_stop_patrolling = (Button) view.findViewById(R.id.btn_stoppatrolling);
        btn_stop_patrolling.setOnClickListener(this);
        btn_special_ops = (Button) view.findViewById(R.id.btn_special_ops);
        btn_special_ops.setOnClickListener(this);

        btn_upload_pending_pillar = (Button) view.findViewById(R.id.btn_upload_pending_pillar);
        btn_upload_pending_pillar.setVisibility(View.GONE);
        btn_upload_pending_pillar.setOnClickListener(this);

        btn_map = (FloatingActionButton) view.findViewById(R.id.map);
        btn_map.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        int id = view.getId();


        LastLocationOnly lastLocationOnly = new LastLocationOnly(getActivity());

        if (!lastLocationOnly.canGetLocation()) {
            GpsEnableTool gpsEnableTool = new GpsEnableTool(getActivity());
            gpsEnableTool.enableGPs();
            return;
        }

        // Log.d("DEBUG_LAT_S_OR_T", String.valueOf(loc_lat));
        // Log.d("DEBUG_LAT_S_OR_T", String.valueOf(loc_lng));

        if (id == R.id.btn_startpetroling) {

            //showOrHideProgressBar();


            double loc_lat = lastLocationOnly.getLatitude();
            double loc_lng = lastLocationOnly.getLongitude();

            // loc_lat = (double) Math.round(GPSTracker.location.getLatitude() * 100000d) / 100000d;
            // loc_lng = (double) Math.round(GPSTracker.location.getLongitude() * 100000d) / 100000d;

            if ((loc_lat == loc_lng) || (loc_lat == 0) || (loc_lng == 0)) {

                if (how_many_time_user_press_start > 3) {
                    how_many_time_user_press_start = 0;
                } else {

                    how_many_time_user_press_start++;

                    AlertDialogForAnything.showAlertDialogWhenComplte(getActivity(), "GPS problem",
                            "Please Restart Your Gps and press the \"START PATROLLING\" button again!", false);
                    //stop executing code by return
                    return;
                }
            }

            how_many_time_user_press_start = 0;

            loc_lat = (double) Math.round(lastLocationOnly.getLatitude() * 100000d) / 100000d;
            loc_lng = (double) Math.round(lastLocationOnly.getLongitude() * 100000d) / 100000d;

            String start_lat = String.valueOf(loc_lat);
            String start_lang = String.valueOf(loc_lng);

            if (cd.isConnectingToInternet()) {
                hitUrlForStartGps(AppController.getInstance().getPrefManger().getBaseUrl() + Url.URL_SOLDIER_LOCATION,
                        AppController.getInstance().getPrefManger().getUserProfile().getId(),
                        start_lat, start_lang);

            } else {
                AppController.getInstance().getPrefManger().setUserStartLat(start_lat);
                AppController.getInstance().getPrefManger().setUserStartLang(start_lang);

                startPatrollingSuccessUpdateUi();
            }


        }


        if (id == R.id.btn_stoppatrolling) {

            if (!cd.isConnectingToInternet()) {
                //Internet Connection is not present
                AlertDialogForAnything.showAlertDialogWhenComplte(getActivity(), "Internet Connection Error",
                        "Please connect to working Internet connection", false);
                //stop executing code by return
                return;
            }

            // lastLocationOnly = new LastLocationOnly(getActivity());

            double loc_lat = (double) Math.round(lastLocationOnly.getLatitude() * 100000d) / 100000d;
            double loc_lng = (double) Math.round(lastLocationOnly.getLongitude() * 100000d) / 100000d;

            final String stop_lat;
            final String stop_lang;

            if ((loc_lat == loc_lng) || (loc_lat == 0) || (loc_lng == 0)) {
                //stop executing code by return
                if (AppController.getInstance().getPrefManger().getUserLastKnownLat().equalsIgnoreCase("0") ||
                        AppController.getInstance().getPrefManger().getUserLastKnownLang().equalsIgnoreCase("0")) {
                    AlertDialogForAnything.showAlertDialogWhenComplte(getActivity(), "GPS problem",
                            "Please Restart GPS then press the \"STOP PATROLLING\" button again!", false);
                    return;
                } else {
                    stop_lat = AppController.getInstance().getPrefManger().getUserLastKnownLat();
                    stop_lang = AppController.getInstance().getPrefManger().getUserLastKnownLang();
                }

            } else {
                stop_lat = String.valueOf(loc_lat);
                stop_lang = String.valueOf(loc_lng);

            }


            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

            alertDialog.setTitle("Alert!!");

            alertDialog.setMessage("Are you sure to stop patrolling.");

            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    if (AppController.getInstance().getPrefManger().getPetrolId().isEmpty()) {

                        hitUrlToGetPatrolIdAndStopPatrolling(
                                AppController.getInstance().getPrefManger().getBaseUrl() + Url.URL_SOLDIER_LOCATION,
                                AppController.getInstance().getPrefManger().getUserProfile().getId(),
                                AppController.getInstance().getPrefManger().getUserStartLat(),
                                AppController.getInstance().getPrefManger().getUserStartLang(),
                                stop_lat,
                                stop_lang);
                    } else {
                        processStopPatrolling(stop_lat, stop_lang);
                    }


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


                    NewPillarsEntry.st_time = 0;
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


        if (id == R.id.btn_upload_pending_pillar) {
            if (!cd.isConnectingToInternet()) {
                //Internet Connection is not present
                AlertDialogForAnything.showAlertDialogWhenComplte(getActivity(), "Internet Connection Error",
                        "Please connect to working Internet connection", false);
                //stop executing code by return
                return;
            }

            Intent intent = new Intent(getActivity(), UploadMultipleFilesActivity.class);

            startActivityForResult(intent, UPLOAD_REQUEST);


        }


        if (id == R.id.map) {
            if (!cd.isConnectingToInternet()) {
                //Internet Connection is not present
                AlertDialogForAnything.showAlertDialogWhenComplte(getActivity(), "Internet Connection Error",
                        "Please connect to working Internet connection", false);
                //stop executing code by return
                return;
            }

            Intent intent = new Intent(getActivity(), MapActivity.class);

            startActivity(intent);


        }


    }


    private void processStopPatrolling(String stop_lat, String stop_lang) {

/*Fetching user pendings location from the database */
        final List<UserLocation> pendingLocations = AppController.getsqliteDbInstance().getAllPendingLocations();

        if (!pendingLocations.isEmpty()) {
            /**
             *Stoping the Service First
             * If the request is failed we will restart this service again
             * */
            getActivity().stopService(new Intent(getActivity(), GpsService.class));

            sendAllPendingLocationsToServer(
                    AppController.getInstance().getPrefManger().getBaseUrl() + Url.URL_SOLDIER_LOCATION,
                    AppController.getInstance().getPrefManger().getUserProfile().getId(),
                    pendingLocations, stop_lat, stop_lang);
        } else {
                         /*If there is no pendings location in the database that means we need to send the latest user
                    * location to the server. */
            hitUrlForStopGps(AppController.getInstance().getPrefManger().getBaseUrl() + Url.URL_SOLDIER_LOCATION,
                    AppController.getInstance().getPrefManger().getUserProfile().getId(),
                    stop_lat, stop_lang);
        }
    }

    private void hitUrlForStartGps(String url, final String id, final String lat, final String lng) {
        // TODO Auto-generated method stub

        showProgressDialog("Start Patrolling....", true, false);

        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        dismissProgressDialog();



                        response = response.replaceAll("\\s+", "");

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String result = jsonObject.getString("result");
                            if (result.equals("1") ) {

                                AppController.getInstance().getPrefManger().setPetrolId(jsonObject.getString("patrolId"));

                                startPatrollingSuccessUpdateUi();

                            } else {
                                AlertDialogForAnything.showAlertDialogWhenComplte(getActivity(),
                                        "Error", response, false);
                               // AlertDialogForAnything.showAlertDialogWhenComplte(getActivity(),
                               //         "Error", "Something went wrong!", false);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            AlertDialogForAnything.showAlertDialogWhenComplte(getActivity(),
                                    "Error", "Server Down!! Please contact with server person!!!", false);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                dismissProgressDialog();

                AppController.getInstance().getPrefManger().setUserStartLat(lat);
                AppController.getInstance().getPrefManger().setUserStartLang(lng);

                startPatrollingSuccessUpdateUi();


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


    private void hitUrlToGetPatrolIdAndStopPatrolling(String url, final String id, final String start_lat, final String start_lng,
                                                      final String stop_lat, final String stop_lang) {
        // TODO Auto-generated method stub

        showProgressDialog("Getting Patrol id....", true, false);

        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        //dismissProgressDialog();

                        response = response.replaceAll("\\s+", "");

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String result = jsonObject.getString("result");
                            if (result.equals("1") && AppController.getInstance().getPrefManger().getPetrolId().isEmpty()) {

                                AppController.getInstance().getPrefManger().setPetrolId(jsonObject.getString("patrolId"));


                                processStopPatrolling(stop_lat, stop_lang);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            dismissProgressDialog();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                dismissProgressDialog();
                AlertDialogForAnything.showAlertDialogWhenComplte(
                        getActivity(),
                        "Error",
                        "Internet Connection Slow. Please Go Near a Good Internet Connection Network!!",
                        false
                );

                // Log.d("DEBUG",String.valueOf(error));


            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);
                params.put("latitude", start_lat);
                params.put("longitude", start_lng);
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

        showProgressDialog("Stop Patrolling....", true, false);

        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        dismissProgressDialog();
                        response = response.replaceAll("\\s+", "");
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String result = jsonObject.getString("result");
                            if (result.equals("1")) {

                                stopPatrollingSuccessUpdateUi();
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

                dismissProgressDialog();

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


    private void sendAllPendingLocationsToServer(String url, final String user_id, final List<UserLocation> pendingLocations,
                                                 final String stop_lat, final String stop_lang) {
        // TODO Auto-generated method stub

        showProgressDialog("Sending Pending coordinates....", true, false);

        final StringBuilder lat = new StringBuilder(String.valueOf(pendingLocations.get(0).getLat()));
        final StringBuilder lang = new StringBuilder(String.valueOf(pendingLocations.get(0).getLang()));
        for (int i = 1; i < pendingLocations.size(); i++) {
            lat.append("-" + String.valueOf(pendingLocations.get(i).getLat()));
            lang.append("-" + String.valueOf(pendingLocations.get(i).getLang()));
        }

        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        response.replaceAll("\\s+", "");
                        AppController.getsqliteDbInstance().truncateTable();

                        hitUrlForStopGps(AppController.getInstance().getPrefManger().getBaseUrl() + Url.URL_SOLDIER_LOCATION,
                                AppController.getInstance().getPrefManger().getUserProfile().getId(),
                                stop_lat,
                                stop_lang);


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


                stopPatrollingUnSuccessUpdateUi();


            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", user_id);
               // Log.d("DEBUG_LAT", String.valueOf(lat));
               // Log.d("DEBUG_LANG", String.valueOf(lang));
                params.put("latitude", String.valueOf(lat));
                params.put("longitude", String.valueOf(lang));
                if (!AppController.getInstance().getPrefManger().getPetrolId().isEmpty())
                    params.put("patrolId", AppController.getInstance().getPrefManger().getPetrolId());
                params.put("authImie", AppController.getInstance().getPrefManger().getUserProfile().getImieNumber());
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);

    }


    private void startPatrollingSuccessUpdateUi() {

        AppController.getInstance().getPrefManger().setPillarNotificationMap("");
        AppController.getInstance().getPrefManger().setUserDistanceTraveled(0);
        //RESTART SERVICE
        getActivity().stopService(new Intent(getActivity(), GpsServiceUpdate.class));
        getActivity().startService(new Intent(getActivity(), GpsServiceUpdate.class));

        btn_stop_patrolling.setVisibility(View.VISIBLE);
        btn_start_petroling.setVisibility(View.GONE);
        btn_new_pillar_entry.setVisibility(View.VISIBLE);
        // Log.d("DEBUG",String.valueOf(error));
    }

    private void stopPatrollingSuccessUpdateUi() {
        getActivity().stopService(new Intent(getActivity(), GpsServiceUpdate.class));

        AppController.getInstance().getPrefManger().setPillarNotificationMap("");

        btn_start_petroling.setVisibility(View.VISIBLE);
        btn_stop_patrolling.setVisibility(View.GONE);
        btn_new_pillar_entry.setVisibility(View.GONE);

        AppController.getInstance().getPrefManger().setPetrolId("");
        AppController.getInstance().getPrefManger().setUserLastKnownLat("0");
        AppController.getInstance().getPrefManger().setUserLastKnownLang("0");

        AppController.getInstance().getPrefManger().setUserStartLat("0");
        AppController.getInstance().getPrefManger().setUserStartLang("0");

        AppController.getsqliteDbInstance().truncateTable();
    }

    private void stopPatrollingUnSuccessUpdateUi() {
        //RESTART SERVICE
        getActivity().stopService(new Intent(getActivity(), GpsServiceUpdate.class));
        getActivity().startService(new Intent(getActivity(), GpsServiceUpdate.class));

        dismissProgressDialog();
        AlertDialogForAnything.showAlertDialogWhenComplte(
                getActivity(),
                "Error",
                "Internet Connection Slow. Please Go Near a Good Internet Connection Network!!",
                false
        );
    }


    public void showProgressDialog(String message, boolean isIntermidiate, boolean isCancelable) {
       /**/
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
        }
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog.setIndeterminate(isIntermidiate);
        progressDialog.setCancelable(isCancelable);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog == null) {
            return;
        }
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


    public void checkOfflinePillarUpdate() {


        if (AppController.getInstance().getPrefManger().getUploadPillars().size() > 0) {
            btn_upload_pending_pillar.setText("Upload Pending Pillars " + "(" +
                    AppController.getInstance().getPrefManger().getUploadPillars().size() + ")");
            btn_upload_pending_pillar.setVisibility(View.VISIBLE);
        } else {
            btn_upload_pending_pillar.setVisibility(View.GONE);
        }


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPLOAD_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                boolean isSuccess = data.getBooleanExtra(UploadActivity.KEY_UPLOAD_RESULT, false);

                if (isSuccess) {
                    AlertDialogForAnything.showAlertDialogWhenComplte(
                            getActivity(),
                            "Success",
                            "All pillar info successfully updated!",
                            true
                    );


                    btn_upload_pending_pillar.setVisibility(View.GONE);

                    AppController.getInstance().getPrefManger().setUploadPillars("");
                } else {
                    AlertDialogForAnything.showAlertDialogWhenComplte(
                            getActivity(),
                            "Error",
                            "Something went wrong! Please Try Again.",
                            true
                    );
                }
            }
        }
    }
}
