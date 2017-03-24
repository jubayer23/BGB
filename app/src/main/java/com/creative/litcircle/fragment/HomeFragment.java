package com.creative.litcircle.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.creative.litcircle.NewPillarsEntry;
import com.creative.litcircle.R;
import com.creative.litcircle.alertbanner.AlertDialogForAnything;
import com.creative.litcircle.appdata.AppController;
import com.creative.litcircle.appdata.Url;
import com.creative.litcircle.service.GpsService;
import com.creative.litcircle.utils.ConnectionDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    private ProgressBar progressBar;
    private Button btn_start_petroling, btn_new_pillar_entry;

    private ProgressDialog progressDialog;

    private Fragment fragment_update_pillar;

    private static final String TAG_BTN_START = "Start Patrolling";
    private static final String TAG_BTN_STOP = "Stop Patrolling";

    private ConnectionDetector cd;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container,
                false);

        fragment_update_pillar = new UpdatePillarInfoFragment();

        btn_start_petroling = (Button) view.findViewById(R.id.btn_startpetroling);

        btn_new_pillar_entry = (Button) view.findViewById(R.id.btn_new_pillar_entry);


        if(!AppController.getInstance().getPrefManger().getPetrolId().isEmpty()){
            btn_start_petroling.setText(TAG_BTN_STOP);
            btn_start_petroling.setBackgroundColor(getActivity().getResources().getColor(R.color.red));
            btn_new_pillar_entry.setVisibility(View.VISIBLE);
        }

        btn_start_petroling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Button btn = (Button) view;

                if (!cd.isConnectingToInternet()) {
                    //Internet Connection is not present
                    AlertDialogForAnything.showAlertDialogWhenComplte(getActivity(), "Internet Connection Error",
                            "Please connect to working Internet connection", false);
                    //stop executing code by return
                    return;
                }

                if (btn.getText().toString().equalsIgnoreCase(TAG_BTN_START)) {

                    //RESTART SERVICE
                    getActivity().stopService(new Intent(getActivity(), GpsService.class));
                    getActivity().startService(new Intent(getActivity(), GpsService.class));

                    btn.setText(TAG_BTN_STOP);
                    btn.setBackgroundColor(getActivity().getResources().getColor(R.color.red));

                    btn_new_pillar_entry.setVisibility(View.VISIBLE);


                } else {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

                    alertDialog.setTitle("Alert!!");

                    alertDialog.setMessage("Are you sure to stop petroling.");

                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            //STOP SERVICE
                            String user_lat = String.valueOf(GpsService.gpsTracker.getLatitude());
                            String user_lang = String.valueOf(GpsService.gpsTracker.getLongitude());


                            hitUrlForStopGps(Url.URL_SOLDIER_LOCATION, AppController.getInstance().getPrefManger().getUserProfile().getId(),
                                    user_lat, user_lang,btn);
                        }
                    });

                    alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();

                        }
                    });

                    alertDialog.show();

                }

            }
        });

        btn_new_pillar_entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });

        return view;

    }

    public void onActivityCreated(Bundle SavedInstanceState) {
        super.onActivityCreated(SavedInstanceState);

        if (SavedInstanceState == null) {
            init();

        } else {

        }
    }

    private void init() {


        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage("Please Wait For Server Response...");

        cd = new ConnectionDetector(getActivity());


    }


    private void hitUrlForStopGps(String url, final String id, final String lat, final String lng, final Button btn) {
        // TODO Auto-generated method stub

        showOrHideProgressBar();

        final StringRequest req = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        showOrHideProgressBar();

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String result = jsonObject.getString("result");
                            if(result.equals("1")){
                                getActivity().stopService(new Intent(getActivity(), GpsService.class));

                                btn.setText(TAG_BTN_START);
                                btn.setBackgroundColor(getActivity().getResources().getColor(R.color.green));

                                btn_new_pillar_entry.setVisibility(View.GONE);
                            }else{
                                AlertDialogForAnything.showAlertDialogWhenComplte(getActivity(),"Alert","There is something wrong when stop patrolling",false);
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
                params.put("patrolId",AppController.getInstance().getPrefManger().getPetrolId());
                params.put("authUsername",AppController.getInstance().getPrefManger().getUserProfile().getUser_id());
                params.put("endPatrol","true");
                // params.put("latitude", "24.898325");
                //  params.put("longitude", "91.902535");

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
