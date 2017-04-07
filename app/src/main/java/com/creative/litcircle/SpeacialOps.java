package com.creative.litcircle;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.creative.litcircle.alertbanner.AlertDialogForAnything;
import com.creative.litcircle.appdata.AppConstant;
import com.creative.litcircle.helperActivity.UploadActivity;
import com.creative.litcircle.utils.DeviceInfoUtils;
import com.creative.litcircle.utils.LastLocationOnly;
import com.creative.litcircle.helperActivity.OpenCameraToTakePic;

import java.util.ArrayList;
import java.util.List;

public class SpeacialOps extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "UploadServiceDemo";
    public static final String KEY_FILE_PATH = "file_path";
    public static final String KEY_LAT = "lat";
    public static final String KEY_LNG = "lng";
    public static final int UPLOAD_REQUEST = 3333;
    private static final String TAG_SELECT_MISSION = "Select Mission Type";
    public static final String KEY_PILLAR_CONDITION = "pillar_condition";
    public static final String KEY_PILLAR_ID = "pillar_name";
    public static final String KEY_UPLOAD_TYPE = "upload_type";


    private Spinner sp_mission_type;

    private Button btn_take_pic, btn_submit;

    private ImageView img_from_camera;

    private static final int CAMERA_ACTIVITY_REQUEST = 1001;

    private List<String> list_misson_type;

    private ProgressDialog progressDialog;

    private boolean isAbleToBack = true;

    private String filePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speacial_ops);

        init();

        manuPulateSpinner();
    }

    private void init() {

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage("Loading...");


        list_misson_type = new ArrayList<String>();

        sp_mission_type = (Spinner) findViewById(R.id.sp_operation_type);
        //sp_mission_type.setOnItemSelectedListener(this);

        btn_take_pic = (Button) findViewById(R.id.btn_take_pic);
        btn_take_pic.setOnClickListener(this);

        btn_submit = (Button) findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(this);

        img_from_camera = (ImageView) findViewById(R.id.img_from_camera);

    }


    private void manuPulateSpinner() {

        /********************************************************************************************/
        list_misson_type.add(TAG_SELECT_MISSION);
        for (int i = 0; i < AppConstant.mission_type.length; i++) {

            list_misson_type.add(AppConstant.mission_type[i]);

        }
        ArrayAdapter<String> dataAdapter_mission_type = new ArrayAdapter<String>
                (this, R.layout.spinner_item, list_misson_type);

        sp_mission_type.setAdapter(dataAdapter_mission_type);

        /********************************************************************************************/

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();


        if (id == R.id.btn_take_pic) {

            if (!DeviceInfoUtils.checkMarshMallowPermission(this)) return;

            Intent intent = new Intent(this, OpenCameraToTakePic.class);
            startActivityForResult(intent, CAMERA_ACTIVITY_REQUEST);

        }

        if (!DeviceInfoUtils.checkInternetConnectionAndGps(this)) return;


        if (id == R.id.btn_submit) {
            final LastLocationOnly lastLocationOnly = new LastLocationOnly(this);

            final String operation_type = sp_mission_type.getSelectedItem().toString();

            if (!lastLocationOnly.canGetLocation()) {
                AlertDialogForAnything.showAlertDialogWhenComplte(this, "Location Alert", "Please enable your gps!", false);
                return;
            }
            if ((lastLocationOnly.getLatitude() == lastLocationOnly.getLongitude()) || lastLocationOnly.getLatitude() == 0 ||
                    lastLocationOnly.getLongitude() == 0) {
                AlertDialogForAnything.showAlertDialogWhenComplte(this, "Location Alert", "Please press submit button again!", false);
                return;
            }
            if (operation_type.equalsIgnoreCase(TAG_SELECT_MISSION)) {
                AlertDialogForAnything.showAlertDialogWhenComplte(this, "Alert", "Please select a Operation Type!", false);
                return;
            }
            if (filePath.isEmpty()) {
                AlertDialogForAnything.showAlertDialogWhenComplte(this, "Alert", "Please take a image!", false);
                return;
            }

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

            alertDialog.setTitle("Alert!!");

            alertDialog.setMessage("Are you sure to SUBMIT?");

            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {


                    Intent i = new Intent(SpeacialOps.this, UploadActivity.class);

                    i.putExtra(KEY_UPLOAD_TYPE, AppConstant.pillar_entry_new);
                    i.putExtra(KEY_FILE_PATH, filePath);
                    i.putExtra(KEY_PILLAR_ID, "37");
                    i.putExtra(KEY_PILLAR_CONDITION, operation_type);
                    i.putExtra(KEY_LAT, String.valueOf(lastLocationOnly.getLatitude()));
                    i.putExtra(KEY_LNG, String.valueOf(lastLocationOnly.getLongitude()));

                    startActivityForResult(i, UPLOAD_REQUEST);
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
                    finish();
                } else {
                    makeViewDefaultAgain();
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

    private void makeViewDefaultAgain() {
        isAbleToBack = true;
        img_from_camera.setVisibility(View.GONE);
        btn_take_pic.setText(getResources().getString(R.string.take_pillar_pic));
        filePath = "";
    }


    @Override
    public void onBackPressed() {

        if (isAbleToBack) {
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
