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
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
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
import com.creative.litcircle.model.Pillar;
import com.creative.litcircle.utils.AccessDirectory;
import com.creative.litcircle.utils.DeviceInfoUtils;
import com.creative.litcircle.utils.LastLocationOnly;
import com.google.gson.Gson;
import com.iceteck.silicompressorr.SiliCompressor;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewPillarsEntry extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "UploadServiceDemo";
    public static final String KEY_FILE_PATH = "file_path";
    public static final String KEY_LAT = "lat";
    public static final String KEY_LNG = "lng";
    public static final String KEY_PILLAR_CONDITION = "pillar_condition";
    public static final String KEY_PILLAR_ID = "pillar_name";
    public static final String KEY_UPLOAD_TYPE = "upload_type";
    public static final int UPLOAD_REQUEST = 3333;
    private static final String TAG_SELECT_PILLAR = "Select Pillar";
    private static final String TAG_SELECT_CONDITION = "Select Condition";


    private Spinner sp_pillars_name, sp_pillars_condition;

    private Button btn_take_pic, btn_submit;

    private ImageView img_from_camera;

    private static final int CAMERA_REQUEST = 1888;

    private List<String> list_pillars_name, list_pillars_condition;

    private Uri fileUri;

    private boolean isFreezeActivity = false;

    private ProgressDialog progressDialog;

    private HashMap<String, Pillar> map_pillar_info = new HashMap<>();


    private List<Pillar> pillars;

    private Gson gson;

    private boolean isAbleToBack = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_pillars_entry);

        init();

        hitUrlForPillarInfo(Url.URL_PILLAR_INFO);

    }

    private void manuPulateSpinner() {


        /********************************************************************************************/
        list_pillars_name.add(TAG_SELECT_PILLAR);

        for(int i=0;i<pillars.size();i++){
            try {
                int id = Integer.parseInt(pillars.get(i).getId().trim());
                if(id > 37){
                    list_pillars_name.add(pillars.get(i).getName());
                }
            }catch (Exception e){

            }

        }
        ArrayAdapter<String> dataAdapter_pillars_name = new ArrayAdapter<String>
                (this, R.layout.spinner_item, list_pillars_name);

        sp_pillars_name.setAdapter(dataAdapter_pillars_name);

        /********************************************************************************************/
        list_pillars_condition.add(TAG_SELECT_CONDITION);
        for (int i = 0; i < AppConstant.pillars_condition.length; i++) {

            list_pillars_condition.add(AppConstant.pillars_condition[i]);

        }
        ArrayAdapter<String> dataAdapter_pillars_condition = new ArrayAdapter<String>
                (this, R.layout.spinner_item, list_pillars_condition);

        sp_pillars_condition.setAdapter(dataAdapter_pillars_condition);

    }

    private void init() {

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage("Loading...");


        pillars = new ArrayList<>();

        list_pillars_name = new ArrayList<String>();

        list_pillars_condition = new ArrayList<String>();

        gson = new Gson();


        sp_pillars_name = (Spinner) findViewById(R.id.sp_pillars_id);
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

                        try {
                            map_pillar_info.clear();

                            pillars.clear();

                            JSONArray jsonArray = new JSONArray(response);

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                String id = jsonObject.getString("id");
                                String name = jsonObject.getString("name");
                                String lat = jsonObject.getString("latitude");
                                String lang = jsonObject.getString("longitude");
                                String url = jsonObject.getString("url");

                                Pillar pillar = new Pillar(id,name,lat,lang,url);

                                pillars.add(pillar);

                                map_pillar_info.put(pillar.getName(), pillar);


                            }
                            //MANUPULATE SPINNER
                            manuPulateSpinner();

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
                params.put("authUsername", AppController.getInstance().getPrefManger().getUserProfile().getUser_id());
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

    @Override
    public void onClick(View view) {


        int id = view.getId();


        if (id == R.id.btn_take_pic) {

            if (!DeviceInfoUtils.checkMarshMallowPermission(this)) return;

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            fileUri = getOutputMediaFileUri();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, CAMERA_REQUEST);

        }

        if (!DeviceInfoUtils.checkInternetConnectionAndGps(this)) return;


        if (id == R.id.btn_submit) {
            final LastLocationOnly lastLocationOnly = new LastLocationOnly(this);
            final String pillar_name = sp_pillars_name.getSelectedItem().toString();
            final String pillar_condition = sp_pillars_condition.getSelectedItem().toString();

            if (!lastLocationOnly.canGetLocation()) {
                AlertDialogForAnything.showAlertDialogWhenComplte(this, "Location Alert", "Please enable your gps!", false);
                return;
            }
            if (pillar_name.equalsIgnoreCase(TAG_SELECT_PILLAR)) {
                AlertDialogForAnything.showAlertDialogWhenComplte(this, "Alert", "Please select a pillar name!", false);
                return;
            }
            if (pillar_condition.equalsIgnoreCase(TAG_SELECT_CONDITION)) {
                AlertDialogForAnything.showAlertDialogWhenComplte(this, "Alert", "Please select a pillar condition!", false);
                return;
            }
            if (fileUri == null) {
                AlertDialogForAnything.showAlertDialogWhenComplte(this, "Alert", "Please take a pillar image!", false);
                return;
            }


            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

            alertDialog.setTitle("Alert!!");

            alertDialog.setMessage("Are you sure to SUBMIT?");

            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    Intent i = new Intent(NewPillarsEntry.this, UploadActivity.class);
                    if(map_pillar_info.get(pillar_name).getLatitude().equalsIgnoreCase("null")){
                        i.putExtra(KEY_UPLOAD_TYPE, AppConstant.pillar_entry_new);
                    }else{
                        i.putExtra(KEY_UPLOAD_TYPE, AppConstant.pillar_entry_update);
                    }
                    i.putExtra(KEY_FILE_PATH, fileUri.getPath());
                    i.putExtra(KEY_PILLAR_ID, map_pillar_info.get(pillar_name).getId());
                    i.putExtra(KEY_PILLAR_CONDITION, pillar_condition);
                    i.putExtra(KEY_LAT, String.valueOf(lastLocationOnly.getLatitude()));
                    i.putExtra(KEY_LNG, String.valueOf(lastLocationOnly.getLongitude()));

                    startActivityForResult(i, UPLOAD_REQUEST);
                }
            });

            alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();

                }
            });

            alertDialog.show();



        }


    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {

                CropImage.activity(fileUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMultiTouchEnabled(true)
                        .start(this);

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if (resultCode == RESULT_OK) {

                    fileUri = result.getUri();


                    // Log.d("DEBUG",fileUri.getPath());
                    String filePath = SiliCompressor.with(this).compress(fileUri.getPath(), true);
                    fileUri = Uri.fromFile(new File(filePath));
                    // Log.d("DEBUG",fileUri.getPath());


                    Bitmap mphoto = BitmapFactory.decodeFile(filePath);
                    img_from_camera.setVisibility(View.VISIBLE);
                    img_from_camera.setImageBitmap(mphoto);
                    btn_take_pic.setText(getResources().getString(R.string.change_pic));


                    isAbleToBack = false;

                    // moveFile(result.getUri().getPath(),fileUri.getPath());

                    //launchUploadActivity();
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            }

            if (requestCode == UPLOAD_REQUEST) {

                boolean isSuccess = data.getBooleanExtra(UploadActivity.KEY_UPLOAD_RESULT, false);

                if (isSuccess) {
                    finish();
                } else {
                    makeViewDefaultAgain();
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public Uri getOutputMediaFileUri() {
        return Uri.fromFile(AccessDirectory.getOutputMediaFile());
    }


    private void makeViewDefaultAgain() {
        isAbleToBack = true;
        isFreezeActivity = false;
        img_from_camera.setVisibility(View.GONE);
        btn_take_pic.setText(getResources().getString(R.string.take_pic));
        fileUri = null;
    }

    @Override
    public void onBackPressed() {
        if (!isFreezeActivity) {
            if(isAbleToBack){
                super.onBackPressed();
            }else{
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

                alertDialog.setTitle("Alert!!");

                alertDialog.setMessage("Are you sure to return back?");

                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        isAbleToBack = true;
                        onBackPressed();
                    }
                });

                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener(){
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
}
