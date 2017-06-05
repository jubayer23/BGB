package com.creative.litcircle;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.creative.litcircle.alertbanner.AlertDialogForAnything;
import com.creative.litcircle.appdata.AppConstant;
import com.creative.litcircle.appdata.AppController;
import com.creative.litcircle.appdata.Url;
import com.creative.litcircle.model.User;
import com.creative.litcircle.userview.SettingActivity;
import com.creative.litcircle.utils.ConnectionDetector;
import com.creative.litcircle.utils.DeviceInfoUtils;
import com.creative.litcircle.utils.GpsEnableTool;
import com.creative.litcircle.utils.MarshMallowPermission;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private ConnectionDetector cd;
    private ProgressDialog progressDialog;

    private GpsEnableTool gpsEnableTool;

    private EditText ed_userid, ed_password;
    private Button btn_submit, btn_skip;

    private ImageView btn_setting;

    private TextView tv_version_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        String version = DeviceInfoUtils.getAppVersionName();

        // Log.d("DEBUG_CURRENT_V_O", version);

        // Log.d("DEBUG_PREF_V_O", AppController.getInstance().getPrefManger().getAppVersion());

        if (!version.equalsIgnoreCase(AppController.getInstance().getPrefManger().getAppVersion())) {

            if (DeviceInfoUtils.checkMarshMallowPermission(this)) {
                AlertDialogForAnything.showAlertDialogForceUpdateFromDropBox(MainActivity.this,
                        "App Update", "Press Download To Download The Updated App", "DOWNLOAD",
                        AppConstant.APP_UPDATE_URL);
            }
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        DeviceInfoUtils.checkInternetConnectionAndGps(this);
        DeviceInfoUtils.checkMarshMallowPermission(MainActivity.this);

        //  String version = DeviceInfoUtils.getAppVersionName();

        // Log.d("DEBUG_CURRENT_R", version);

        // Log.d("DEBUG_PREF_R", AppController.getInstance().getPrefManger().getAppVersion());

    }

    private void init() {

        cd = new ConnectionDetector(this);

        gpsEnableTool = new GpsEnableTool(this);

        tv_version_name = (TextView) findViewById(R.id.tv_app_version);
        tv_version_name.setText("V " + DeviceInfoUtils.getAppVersionName());

        ed_userid = (EditText) findViewById(R.id.ed_userid);
        ed_password = (EditText) findViewById(R.id.ed_password);

        btn_submit = (Button) findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(this);

        btn_skip = (Button) findViewById(R.id.btn_skip);
        btn_skip.setOnClickListener(this);

        btn_setting = (ImageView) findViewById(R.id.btn_usersetting);
        btn_setting.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage("Login In...");

    }


    @Override
    public void onClick(View view) {


        int id = view.getId();

        if (id == R.id.btn_usersetting) {

            showDialogForSetting();


        }


        if (id == R.id.btn_skip) {
            Intent intent = new Intent(this, ReportToBgbActivity.class);
            startActivity(intent);
        }

        if (!DeviceInfoUtils.checkMarshMallowPermission(this)) return;


        if (!DeviceInfoUtils.checkInternetConnectionAndGps(this)) return;


        if (id == R.id.btn_submit) {
            String user_id = ed_userid.getText().toString();
            String password = ed_password.getText().toString();
            String imie = DeviceInfoUtils.getDeviceImieNumber(this);

            if (showWarningDialog()) {

                if (!imie.isEmpty()) {
                    //Log.d("DEBUG_ID", imie);
                    hitUrlForLogin(AppController.getInstance().getPrefManger().getBaseUrl() + Url.URL_LOGIN, user_id, password, imie);
                } else {
                    AlertDialogForAnything.showAlertDialogWhenComplte(this, "ALERT!", "Your Device Imie Number Not Found. Please Contact with Developer!", false);
                }
            }
        }

    }


    private void hitUrlForLogin(String url, final String user_id, final String password, final String imieNumber) {
        // TODO Auto-generated method stub
        showOrHideProgressBar();

        final StringRequest req = new StringRequest(com.android.volley.Request.Method.POST, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        response = response.replaceAll("\\s+", "");

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = Integer.parseInt(jsonObject.getString("result"));

                            if (status == 1) {
                                String id = jsonObject.getString("id");
                                //Log.d("DEBUG",String.valueOf(id));
                                User user = new User(id, user_id, imieNumber);
                                AppController.getInstance().getPrefManger().setUserProfile(user);


                                hitUrlForPillarInfo(
                                        AppController.getInstance().getPrefManger().getBaseUrl()
                                                + Url.URL_PILLAR_INFO);

                            } else if (status == -1) {
                                showOrHideProgressBar();
                                AlertDialogForAnything.showAlertDialogWhenComplte(MainActivity.this, "UnAuthorized", "Your Device Is Not Authorized", false);

                            } else {
                                showOrHideProgressBar();
                                AlertDialogForAnything.showAlertDialogWhenComplte(MainActivity.this, "Wrong Information", "Wrong Information", false);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showOrHideProgressBar();
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
                params.put("authImie", imieNumber);
                //params.put("mobileNumber",mobileNumber);
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }

    private void goTotheHomePage() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void hitUrlForPillarInfo(String url) {

        // TODO Auto-generated method stub
        // showOrHideProgressBar();

        final StringRequest req = new StringRequest(com.android.volley.Request.Method.POST, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        response = response.replaceAll("\\s+", "");

                        AppController.getInstance().getPrefManger().setPillarInfoResponse(response);

                        showOrHideProgressBar();

                        goTotheHomePage();

                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                showOrHideProgressBar();

                goTotheHomePage();

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //userId=XXX&routeId=XXX&selected=XXX
                Map<String, String> params = new HashMap<String, String>();
                params.put("authImie", AppController.getInstance().getPrefManger().getUserProfile().getImieNumber());
                return params;
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(60000,
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

    public void showDialogForSetting() {
        final Dialog dialog = new Dialog(this,
                android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_settingpassword);


        final EditText et_dialog_password = (EditText) dialog.findViewById(R.id.dialog_password);

        Button btn_submit = (Button) dialog.findViewById(R.id.dialog_submit);
        Button btn_cancel = (Button) dialog.findViewById(R.id.dialog_cancel);
        btn_cancel.setVisibility(View.VISIBLE);

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = et_dialog_password.getText().toString().trim();


                if (password.isEmpty()) {
                    et_dialog_password.setError("Enter Password");
                    return;
                }
                if (password.equals(AppConstant.ADMIN_PASSWORD)) {
                    Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                    startActivity(intent);

                    dialog.dismiss();
                } else {
                    et_dialog_password.setError("Wrong Password");
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();


    }

}
