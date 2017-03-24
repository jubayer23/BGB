package com.creative.litcircle;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.creative.litcircle.alertbanner.AlertDialogForAnything;
import com.creative.litcircle.appdata.AppController;
import com.creative.litcircle.appdata.Url;
import com.creative.litcircle.utils.ConnectionDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ReportToBgbActivity extends AppCompatActivity {

    private EditText ed_name,ed_mobile,ed_address,ed_message;

    private Button btn_submit;

    private ConnectionDetector cd;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_to_bgb);


        init();


        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!cd.isConnectingToInternet()){
                    AlertDialogForAnything.showAlertDialogWhenComplte(ReportToBgbActivity.this,"Alert","No InterNet Connection!",false);
                    return;
                }


                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ReportToBgbActivity.this);

                alertDialog.setTitle("Alert!!");

                alertDialog.setMessage("Are you sure to SUBMIT?");

                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(showWarningDialog()){

                            String name = ed_name.getText().toString().isEmpty()? "" : ed_name.getText().toString();
                            String mobile = ed_mobile.getText().toString().isEmpty()? "" : ed_mobile.getText().toString();
                            String address = ed_address.getText().toString().isEmpty()? "" : ed_address.getText().toString();

                            String message =  ed_message.getText().toString();

                            hitUrlToSubmitUserReport(Url.URL_REPORT_TO_BGB,name,mobile,address,message);


                        }
                    }
                });

                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                    }
                });

                alertDialog.show();



            }
        });
    }


    private void init(){

        cd = new ConnectionDetector(this);


        ed_name = (EditText) findViewById(R.id.ed_name);
        ed_mobile = (EditText) findViewById(R.id.ed_mobile);
        ed_address = (EditText) findViewById(R.id.ed_address);
        ed_message = (EditText) findViewById(R.id.ed_message);


        btn_submit = (Button)findViewById(R.id.btn_submit);


        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage("Submitting...");
    }


    private void hitUrlToSubmitUserReport(String url, final String name, final String mobile, final String address, final String message) {
        // TODO Auto-generated method stub
        showOrHideProgressBar();

        final StringRequest req = new StringRequest(com.android.volley.Request.Method.POST, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        showOrHideProgressBar();

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = Integer.parseInt(jsonObject.getString("result"));

                            if (status == 1) {

                                AlertDialogForAnything.showAlertDialogWhenComplte(ReportToBgbActivity.this,"Alert","Report To BGB Successfull!!",false);
                                finish();
                            } else {

                                AlertDialogForAnything.showAlertDialogWhenComplte(ReportToBgbActivity.this,"Alert","Report To BGB Failed!!",false);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                showOrHideProgressBar();

                AlertDialogForAnything.showAlertDialogWhenComplte(ReportToBgbActivity.this,"Alert","Report To BGB Failed!!",false);

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                params.put("mobile", mobile);
                params.put("address", address);
                params.put("message", message);
                //params.put("mobileNumber",mobileNumber);
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }



    public boolean showWarningDialog() {

        boolean valid = true;

        if (ed_message.getText().toString().isEmpty()) {
            ed_message.setError("Write Your Complain Here");
            valid = false;
        } else {
            ed_message.setError(null);
        }

        return valid;
    }


    private void showOrHideProgressBar() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        } else
            progressDialog.show();
    }
}
