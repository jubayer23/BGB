package com.creative.litcircle;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.creative.litcircle.alertbanner.AlertDialogForAnything;
import com.creative.litcircle.appdata.AppConstant;
import com.creative.litcircle.appdata.AppController;
import com.creative.litcircle.appdata.Url;
import com.creative.litcircle.helperActivity.UploadActivity;
import com.creative.litcircle.model.Pillar;
import com.creative.litcircle.model.UploadPillar;
import com.creative.litcircle.model.PillarValid;
import com.creative.litcircle.utils.AccessDirectory;
import com.creative.litcircle.utils.ConnectionDetector;
import com.creative.litcircle.utils.DeviceInfoUtils;
import com.creative.litcircle.utils.LastLocationOnly;
import com.creative.litcircle.helperActivity.OpenCameraToTakePic;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewPillarsEntry extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "UploadServiceDemo";
    public static final String KEY_FILE_PATH = "file_path";
    public static final String KEY_LAT = "lat";
    public static final String KEY_LNG = "lng";
    public static final String KEY_PILLAR_CONDITION = "pillar_condition";
    public static final String KEY_PILLAR_ID = "pillar_name";
    public static final String KEY_UPLOAD_TYPE = "upload_type";
    public static final int UPLOAD_REQUEST = 3333;
    private static final String TAG_SELECT_MAIN_PILLAR = "Select Main Pillar";
    private static final String TAG_SELECT_SUB_PILLAR = "Select Sub Pillar";
    private static final String TAG_NO_MAIN_PILLAR_SELECTED = "No Main Pillar Selected";
    private static final String TAG_NO_SUB_PILLAR_FOR_MAIN_PILLAR = "No Sub Pillar For This Main Pillar";
    private static final String TAG_SELECT_CONDITION = "Select Condition";


    private Spinner sp_pillars_name, sp_sub_pillars_name, sp_pillars_condition;

    private Button btn_take_pic, btn_submit;

    private ImageView img_from_camera;

    private static final int CAMERA_REQUEST = 1888;

    private List<String> main_pillar_names, list_pillars_condition, sub_pillar_names;

    private String filePath = "";

    private boolean isFreezeActivity = false;

    private ProgressDialog progressDialog;

    private HashMap<String, Pillar> map_pillar_info = new HashMap<>();

    private HashMap<String, List<String>> map_sub_pillar = new HashMap<>();

    private ArrayAdapter<String> dataAdapter_sub_pillars_name;


    private static final int CAMERA_ACTIVITY_REQUEST = 1000;


    // private List<Pillar> pillars;

    private Gson gson;

    private boolean isAbleToBack = true;

    private ConnectionDetector cd;

    private static boolean isOffline = false;

    String pillar_id = "-100",sub_pillar_name = "-100";

    private UploadPillar uploadPillar;

    private static final int KEY_OFFLINE_SAVE = 1;
    private static final int KEY_ERROR_SAVE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_pillars_entry);

        onNewIntent(getIntent());

        init();


        if (cd.isConnectingToInternet()) {
            isOffline = false;
            hitUrlForPillarInfo(AppController.getInstance().getPrefManger().getBaseUrl() + Url.URL_PILLAR_INFO);
        } else if (!AppController.getInstance().getPrefManger().getPillarInfoResponse().isEmpty()) {
            isOffline = true;
            parseResponse(AppController.getInstance().getPrefManger().getPillarInfoResponse());
        }


    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        try {
            if (extras.containsKey("pillar_id")) {
                pillar_id = extras.getString("pillar_id");
                sub_pillar_name = extras.getString("sub_pillar_name");
            } else {
                // Log.d("DEBUG_inInternt", "No");
                pillar_id = "-100";
            }
        } catch (Exception e) {
            pillar_id = "-100";
        }

    }

    private void manuPulateSpinner() {


        /********************************************************************************************/
        main_pillar_names.add(TAG_SELECT_MAIN_PILLAR);

        Map<String, List<String>> map = map_sub_pillar;
        int counter = 0;
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {


            main_pillar_names.add(String.valueOf(entry.getKey()));

        }
        Collections.sort(main_pillar_names, new Comparator<String>() {
            public int compare(String o1, String o2) {
                if (o1.equals(TAG_SELECT_MAIN_PILLAR))
                    return -1;
                if (o2.equals(TAG_SELECT_MAIN_PILLAR))
                    return 1;
                if (o1.equals("General"))
                    return -1;
                if (o2.equals("General"))
                    return 1;
                return o1.compareTo(o2);
            }
        });
        //Collections.sort(main_pillar_names);
        ArrayAdapter<String> dataAdapter_pillars_name = new ArrayAdapter<String>
                (this, R.layout.spinner_item, main_pillar_names);

        sp_pillars_name.setAdapter(dataAdapter_pillars_name);

        /********************************************************************************************/

        sub_pillar_names.add(TAG_NO_MAIN_PILLAR_SELECTED);
        dataAdapter_sub_pillars_name = new ArrayAdapter<String>
                (this, R.layout.spinner_item, sub_pillar_names);

        sp_sub_pillars_name.setAdapter(dataAdapter_sub_pillars_name);

        /********************************************************************************************/
        list_pillars_condition.add(TAG_SELECT_CONDITION);
        for (int i = 0; i < AppConstant.pillars_condition.length; i++) {

            list_pillars_condition.add(AppConstant.pillars_condition[i]);

        }
        ArrayAdapter<String> dataAdapter_pillars_condition = new ArrayAdapter<String>
                (this, R.layout.spinner_item, list_pillars_condition);

        sp_pillars_condition.setAdapter(dataAdapter_pillars_condition);


        if (!pillar_id.equals("-100")) {
            selectSpinnerItemProgramatically();
        }

    }

    private void selectSpinnerItemProgramatically() {

        for (int i = 0; i < main_pillar_names.size(); i++) {
            if (pillar_id.equals(main_pillar_names.get(i))) {
                sp_pillars_name.setSelection(i);
                manupulateSubPillar(pillar_id);
                break;
            }
        }
    }

    private void init() {

        cd = new ConnectionDetector(NewPillarsEntry.this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage("Loading...");


        // pillars = new ArrayList<>();

        main_pillar_names = new ArrayList<String>();
        sub_pillar_names = new ArrayList<String>();
        list_pillars_condition = new ArrayList<String>();

        gson = new Gson();


        sp_pillars_name = (Spinner) findViewById(R.id.sp_pillars_id);
        sp_pillars_name.setOnItemSelectedListener(this);
        sp_sub_pillars_name = (Spinner) findViewById(R.id.sp_sub_pillars_id);
        sp_pillars_condition = (Spinner) findViewById(R.id.sp_pillars_condition);

        btn_take_pic = (Button) findViewById(R.id.btn_take_pic);
        btn_take_pic.setOnClickListener(this);

        btn_submit = (Button) findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(this);

        img_from_camera = (ImageView) findViewById(R.id.img_from_camera);

    }

    private void hitUrlForPillarInfo(String url) {

        // TODO Auto-generated method stub
        showOrHideProgressBar();

        final StringRequest req = new StringRequest(com.android.volley.Request.Method.POST, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        AppController.getInstance().getPrefManger().setPillarInfoResponse(response);

                        parseResponse(response);

                        showOrHideProgressBar();

                        //AlertDialogForAnything.showAlertDialogWhenComplte(NewPillarsEntry.this,"SERVER PROBLEM","SERVER DOWN. Please Contact With Server Management!",false);

                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


                showOrHideProgressBar();

                isOffline = true;
                parseResponse(AppController.getInstance().getPrefManger().getPillarInfoResponse());

                //AlertDialogForAnything.showAlertDialogWhenComplte(NewPillarsEntry.this, "SERVER PROBLEM", "SERVER DOWN. Please Contact With Server Management!", false);

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

        req.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        AppController.getInstance().addToRequestQueue(req);
    }

    private void parseResponse(String response) {

        response = response.replaceAll("\\s+", "");


        try {
            map_pillar_info.clear();
            map_sub_pillar.clear();
            main_pillar_names.clear();

            // pillars.clear();

            JSONArray jsonArray = new JSONArray(response);


            List<PillarValid> pillars = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String id = jsonObject.getString("id");
                String name = jsonObject.getString("name");
                String lat = jsonObject.getString("latitude");
                String lang = jsonObject.getString("longitude");
                String url = jsonObject.getString("url");


                if (Integer.parseInt(id) <= 37) continue;

                Pillar pillar = new Pillar(id, name, lat, lang, url);
                if (!lat.equals("null") && !lang.equals("null")) {
                    PillarValid pillarValid = new PillarValid(pillar, i);
                    pillars.add(pillarValid);
                }
                //pillars.add(pillar);

                map_pillar_info.put(pillar.getName(), pillar);

                String main_sub[] = name.split("/", 2);
                //main_pillar_names.add(main_sub[0]);

                List<String> temp_list;
                if (map_sub_pillar.get(main_sub[0]) == null) {
                    temp_list = new ArrayList<>();
                } else {
                    temp_list = map_sub_pillar.get(main_sub[0]);
                }
                if (main_sub.length > 1) {
                    if (main_sub[1].length() > 0) {
                        temp_list.add(main_sub[1]);
                    }
                }
                map_sub_pillar.put(main_sub[0], temp_list);

            }


            AppController.getInstance().getPrefManger().setPillars(pillars);
            //MANUPULATE SPINNER
            manuPulateSpinner();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void showOrHideProgressBar() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        } else
            progressDialog.show();
    }

    @Override
    public void onClick(View view) {


        int id = view.getId();


        if (id == R.id.btn_take_pic) {

            if (!DeviceInfoUtils.checkMarshMallowPermission(this)) return;

            Intent intent = new Intent(this, OpenCameraToTakePic.class);
            startActivityForResult(intent, CAMERA_ACTIVITY_REQUEST);

        }

        if (!DeviceInfoUtils.checkGps(this)) return;


        if (id == R.id.btn_submit) {
            final LastLocationOnly lastLocationOnly = new LastLocationOnly(this);

            final String main_pillar_name = sp_pillars_name.getSelectedItem().toString();
            final String sub_pillar_name = sp_sub_pillars_name.getSelectedItem().toString();
            final String pillar_condition = sp_pillars_condition.getSelectedItem().toString();

            if (!lastLocationOnly.canGetLocation()) {
                AlertDialogForAnything.showAlertDialogWhenComplte(this, "Location Alert", "Please enable your gps!", false);
                return;
            }
            if ((lastLocationOnly.getLatitude() == lastLocationOnly.getLongitude()) || lastLocationOnly.getLatitude() == 0 ||
                    lastLocationOnly.getLongitude() == 0) {
                AlertDialogForAnything.showAlertDialogWhenComplte(this, "Location Alert", "Please press submit button again!", false);
                return;
            }
            if (main_pillar_name.equalsIgnoreCase(TAG_SELECT_MAIN_PILLAR)) {
                AlertDialogForAnything.showAlertDialogWhenComplte(this, "Alert", "Please select a Main pillar name!", false);
                return;
            }
            if (pillar_condition.equalsIgnoreCase(TAG_SELECT_CONDITION)) {
                AlertDialogForAnything.showAlertDialogWhenComplte(this, "Alert", "Please select a pillar condition!", false);
                return;
            }
            if (filePath.isEmpty()) {
                AlertDialogForAnything.showAlertDialogWhenComplte(this, "Alert", "Please take a pillar image!", false);
                return;
            }


            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

            alertDialog.setTitle("Alert!!");

            alertDialog.setMessage("Are you sure to SUBMIT?");

            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    String pillar_name = main_pillar_name;
                    if (!sub_pillar_name.equalsIgnoreCase(TAG_SELECT_SUB_PILLAR) && !sub_pillar_name.equalsIgnoreCase(TAG_NO_MAIN_PILLAR_SELECTED)
                            && !sub_pillar_name.equalsIgnoreCase(TAG_NO_SUB_PILLAR_FOR_MAIN_PILLAR)) {

                        pillar_name = pillar_name + "/" + sub_pillar_name;
                    }


                    String upload_type = "";
                    if (map_pillar_info.get(pillar_name).getLatitude().equalsIgnoreCase("null")) {
                        upload_type = AppConstant.pillar_entry_new;
                    } else {
                        upload_type = AppConstant.pillar_entry_update;
                    }

                    uploadPillar = new
                            UploadPillar(
                            upload_type,
                            filePath,
                            map_pillar_info.get(pillar_name).getId(),
                            pillar_condition,
                            String.valueOf(lastLocationOnly.getLatitude()),
                            String.valueOf(lastLocationOnly.getLongitude())
                    );


                    if (!isOffline) {
                        Intent i = new Intent(NewPillarsEntry.this, UploadActivity.class);
                        i.putExtra(KEY_UPLOAD_TYPE, upload_type);
                        i.putExtra(KEY_FILE_PATH, filePath);
                        i.putExtra(KEY_PILLAR_ID, map_pillar_info.get(pillar_name).getId());
                        i.putExtra(KEY_PILLAR_CONDITION, pillar_condition);
                        i.putExtra(KEY_LAT, String.valueOf(lastLocationOnly.getLatitude()));
                        i.putExtra(KEY_LNG, String.valueOf(lastLocationOnly.getLongitude()));

                        startActivityForResult(i, UPLOAD_REQUEST);
                    } else {


                        List<UploadPillar> uploadPillars =
                                AppController.getInstance().getPrefManger().getUploadPillars();

                        uploadPillars.add(uploadPillar);

                        AppController.getInstance().getPrefManger().setUploadPillars(uploadPillars);


                        showOfflineAlert();


                    }

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


    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == UPLOAD_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                boolean isSuccess = data.getBooleanExtra(UploadActivity.KEY_UPLOAD_RESULT, false);

                if (isSuccess) {
                    st_time = 0;
                    finish();
                } else {
                    showPillarUploadFiledAlert();
                }
            }
        }

        if (requestCode == CAMERA_ACTIVITY_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                filePath = data.getStringExtra(OpenCameraToTakePic.KEY_FILE_URL);
                Bitmap mphoto = BitmapFactory.decodeFile(filePath);
                img_from_camera.setVisibility(View.VISIBLE);
                img_from_camera.setImageBitmap(mphoto);
                btn_take_pic.setText(getResources().getString(R.string.change_pic));


                isAbleToBack = false;
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                String error_message = data.getStringExtra(OpenCameraToTakePic.KEY_ERROR);

                if (error_message.equalsIgnoreCase(OpenCameraToTakePic.CRUSH)) {
                    AlertDialogForAnything.showAlertDialogWhenComplte(this, "ERROR", "Crop Functionality does not work on your phone!", false);
                }

            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void showPillarUploadFiledAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Alert!!");

        alertDialog.setMessage("Because of slow internet connection photo upload failed!! Do you want to save this information?");

        alertDialog.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                List<UploadPillar> uploadPillars =
                        AppController.getInstance().getPrefManger().getUploadPillars();

                uploadPillars.add(uploadPillar);

                AppController.getInstance().getPrefManger().setUploadPillars(uploadPillars);

                showOfflineAlert();

            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                makeViewDefaultAgain();
            }
        });


        alertDialog.show();

    }

    private void showOfflineAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Alert!!");


        alertDialog.setMessage("You are now offline mode. The information you submitted right now will be saved temporarily. When you connected to the internet please click \"Submit Pending Pillars\" from the home page.");

        alertDialog.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                finish();
            }
        });


        alertDialog.show();
    }

    public Uri getOutputMediaFileUri() {
        return Uri.fromFile(AccessDirectory.getOutputMediaFile());
    }


    private void makeViewDefaultAgain() {
        isAbleToBack = true;
        isFreezeActivity = false;
        img_from_camera.setVisibility(View.GONE);
        btn_take_pic.setText(getResources().getString(R.string.take_pillar_pic));
        filePath = "";
    }

    @Override
    public void onBackPressed() {
        if (!isFreezeActivity) {
            if (isAbleToBack) {
                st_time = 0;
                super.onBackPressed();
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

                alertDialog.setTitle("Alert!!");

                alertDialog.setMessage("Are you sure to return back?");

                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        isAbleToBack = true;
                        onBackPressed();
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:

                onBackPressed();
                break;

        }

        return true;
    }


    public static int st_time = 0;

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long id) {

        if (st_time == 0) {
            st_time++;
            return;
        }

        manupulateSubPillar(adapterView.getItemAtPosition(i).toString());


    }


    private void manupulateSubPillar(String main_pillar_name){
        showOrHideProgressBar();

        sub_pillar_names.clear();
        sub_pillar_names.add(TAG_SELECT_SUB_PILLAR);


        List<String> temp_sub_pillars = map_sub_pillar.get(main_pillar_name);

        if (!temp_sub_pillars.isEmpty()) {
            sub_pillar_names.addAll(temp_sub_pillars);
        } else {
            sub_pillar_names.add(TAG_NO_SUB_PILLAR_FOR_MAIN_PILLAR);
        }

        dataAdapter_sub_pillars_name.notifyDataSetChanged();

        for (int i = 0; i < sub_pillar_names.size(); i++) {
            if (sub_pillar_name.equals(sub_pillar_names.get(i))) {
                sp_sub_pillars_name.setSelection(i);
                break;
            }
        }

        showOrHideProgressBar();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


}
