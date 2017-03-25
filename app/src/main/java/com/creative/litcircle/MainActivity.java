package com.creative.litcircle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.creative.litcircle.alertbanner.AlertDialogForAnything;
import com.creative.litcircle.appdata.AppConstant;
import com.creative.litcircle.appdata.AppController;
import com.creative.litcircle.appdata.Url;
import com.creative.litcircle.model.User;
import com.creative.litcircle.utils.ConnectionDetector;
import com.creative.litcircle.utils.DeviceInfoUtils;
import com.creative.litcircle.utils.GpsEnableTool;
import com.creative.litcircle.utils.MarshMallowPermission;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private ConnectionDetector cd;
    private ProgressDialog progressDialog;

    private GpsEnableTool gpsEnableTool;

    private EditText ed_userid, ed_password;
    private Button btn_submit, btn_skip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        if( month > 3){
            AlertDialogForAnything.showAlertDialogWhenComplte(this,"SERVER DOWN","SERVER DOWN(under construction!)",false);
        }else{
            if (AppController.getInstance().getPrefManger().getUserProfile() != null) {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                init();
            }
        }

        hitUrlForCheckAppUpdate(Url.URL_CHECK_APP_UPDATE);

    }

    @Override
    protected void onResume() {
        super.onResume();

        DeviceInfoUtils.checkInternetConnectionAndGps(this);
        DeviceInfoUtils.checkMarshMallowPermission(MainActivity.this);

    }

    private void init() {

        cd = new ConnectionDetector(this);

        gpsEnableTool = new GpsEnableTool(this);

        ed_userid = (EditText) findViewById(R.id.ed_userid);
        ed_password = (EditText) findViewById(R.id.ed_password);

        btn_submit = (Button) findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(this);

        btn_skip = (Button) findViewById(R.id.btn_skip);
        btn_skip.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage("Login In...");

    }


    @Override
    public void onClick(View view) {


        int id = view.getId();


        if (id == R.id.btn_skip) {
            Intent intent = new Intent(this, ReportToBgbActivity.class);
            startActivity(intent);
        }

        if(!DeviceInfoUtils.checkMarshMallowPermission(this))return;


        if (!DeviceInfoUtils.checkInternetConnectionAndGps(this)) return;


        if (id == R.id.btn_submit) {
            String user_id = ed_userid.getText().toString();
            String password = ed_password.getText().toString();
            String identifier = null;

            try {
                TelephonyManager tm = (TelephonyManager) this.getSystemService(this.TELEPHONY_SERVICE);
                if (tm != null)
                    identifier = tm.getDeviceId();
            } catch (Exception e) {

            }

            if (showWarningDialog()) {

                if (identifier != null) {
                    hitUrlForLogin(Url.URL_LOGIN, user_id, password, identifier);
                } else {
                    Log.d("DEBUG", "sorry");
                }
            }
        }

    }


    private void hitUrlForLogin(String url, final String user_id, final String password, final String mobileNumber) {
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
                                String id = jsonObject.getString("id");
                                //Log.d("DEBUG",String.valueOf(id));
                                User user = new User(id, user_id);
                                AppController.getInstance().getPrefManger().setUserProfile(user);

                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            } else {

                                AlertDialogForAnything.showAlertDialogWhenComplte(MainActivity.this, "Wrong Information", "Wrong Information", false);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


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
                params.put("username", user_id);
                params.put("password", password);
                params.put("imieNumber", "01737104638");
                //params.put("mobileNumber",mobileNumber);
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }

    private void hitUrlForCheckAppUpdate(String url) {
        // TODO Auto-generated method stub
        showOrHideProgressBar();

        Log.d("DEBUG",url);

        final StringRequest req = new StringRequest(com.android.volley.Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        response = "{\"version\":\"1.0\",\"url\":\"\"}";

                        Log.d("DEBUG",response);

                        showOrHideProgressBar();

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String version = jsonObject.getString("version");

                            if (!version.equalsIgnoreCase(AppController.getInstance().getPrefManger().getAppVersion())) {

                                Log.d("DEBUG","its_here");
                                    AlertDialogForAnything.showAlertDialogForceUpdateFromDropBox(MainActivity.this,
                                            "App Update","Press Download To Download The Updated App","DOWNLOAD",
                                            jsonObject.getString("url"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();

                            Log.d("DEBUG","error");
                        }


                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                showOrHideProgressBar();

                Log.d("DEBUG","error");


            }
        });

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

    public boolean showWarningDialog() {

        boolean valid = true;

        if (ed_userid.getText().toString().isEmpty()) {
            ed_userid.setError("Enter MobileNo");
            valid = false;
        } else {
            ed_userid.setError(null);
        }

        if (ed_password.getText().toString().isEmpty()) {
            ed_password.setError("Enter Password");
            valid = false;
        } else {
            ed_password.setError(null);
        }

        if (!(ed_userid.getText().toString().isEmpty() && ed_password.getText().toString().isEmpty())) {
            if (ed_userid.getText().toString().isEmpty() && !ed_password.getText().toString().isEmpty()) {
                ed_userid.requestFocus();
            }
            if (!ed_userid.getText().toString().isEmpty() && ed_password.getText().toString().isEmpty()) {
                ed_password.requestFocus();
            }
        }


        return valid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstant.REQUEST_CHECK_SETTINGS) {

            if (resultCode == RESULT_OK) {

                Toast.makeText(getApplicationContext(), "GPS enabled", Toast.LENGTH_LONG).show();
            } else {

                Toast.makeText(getApplicationContext(), "GPS is not enabled", Toast.LENGTH_LONG).show();
            }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MarshMallowPermission.CAMERA_PERMISSION_REQUEST_CODE ||
                requestCode == MarshMallowPermission.EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE ||
                requestCode == MarshMallowPermission.RECORD_PERMISSION_REQUEST_CODE ||
                requestCode == MarshMallowPermission.PHONE_STATE_PERMISSION_REQUEST_CODE) {
            // DeviceInfoUtils.checkMarshMallowPermission(this);

            //Log.d("DEBUG","Its here");
        }


    }
}
