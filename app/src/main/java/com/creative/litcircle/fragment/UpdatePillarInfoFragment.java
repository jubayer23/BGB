package com.creative.litcircle.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.creative.litcircle.R;
import com.creative.litcircle.appdata.AppController;
import com.creative.litcircle.model.Pillar;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UpdatePillarInfoFragment extends Fragment {

    private ProgressDialog progressDialog;

    private Gson gson;


    private List<Pillar> pillars;

    private Spinner sp_pillars_name, sp_pillars_condition;

    private Button btn_take_pic, btn_submit;

    private List<String> list_pillars_name, list_pillars_condition;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_update_pillar_info, container,
                false);

    }

    public void onActivityCreated(Bundle SavedInstanceState) {
        super.onActivityCreated(SavedInstanceState);


        init();



    }

    private void init(){
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage("Login In...");

        gson = new Gson();

        pillars = new ArrayList<>();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    public  void hitUrlForPillarInfo(String url){

        // TODO Auto-generated method stub
        showOrHideProgressBar();

        final StringRequest req = new StringRequest(com.android.volley.Request.Method.POST, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        //  Log.d("DEBUG",String.valueOf(response));


                        try {

                            pillars.clear();

                            JSONArray jsonArray = new JSONArray(response);

                            for(int i=0;i<jsonArray.length();i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                Pillar pillar = gson.fromJson(jsonObject.toString(), Pillar.class);

                                pillars.add(pillar);

                            }

                            //MANUPULATE SPINNER
                            //manuPulateSpinner();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        showOrHideProgressBar();

                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


                showOrHideProgressBar();

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(30000,
                2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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
