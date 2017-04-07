package com.creative.litcircle.helperActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.creative.litcircle.NewPillarsEntry;
import com.creative.litcircle.R;
import com.creative.litcircle.appdata.AppConstant;
import com.creative.litcircle.appdata.AppController;
import com.creative.litcircle.appdata.Url;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONException;
import org.json.JSONObject;

public class UploadActivity extends AppCompatActivity implements UploadStatusDelegate {

    public static final String KEY_UPLOAD_RESULT = "upload_result";
    private ProgressBar upload_progressbar;
    private Button btn_upload_cancel;
    private TextView tv_upload_progress, tv_upload_title;
    private String uploadId = "";

    private static final int SUCCESS_CODE = 1;
    private static final int ERROR_CODE = 2;

    private boolean isFreezeActivity = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_upload_progress);

        init();

        Intent i = getIntent();

        String upload_type = i.getStringExtra(NewPillarsEntry.KEY_UPLOAD_TYPE);
        String filePath = i.getStringExtra(NewPillarsEntry.KEY_FILE_PATH);
        String pillar_id = i.getStringExtra(NewPillarsEntry.KEY_PILLAR_ID);
        String pillar_condition = i.getStringExtra(NewPillarsEntry.KEY_PILLAR_CONDITION);
        String lat = i.getStringExtra(NewPillarsEntry.KEY_LAT);
        String lng = i.getStringExtra(NewPillarsEntry.KEY_LNG);


        startUploadingToServer(filePath, pillar_id, pillar_condition, lat, lng, upload_type);


        btn_upload_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!uploadId.isEmpty()) {
                    UploadService.stopUpload(uploadId);
                }
            }
        });
    }

    private void init() {
        upload_progressbar = (ProgressBar) findViewById(R.id.uploadProgress);
        upload_progressbar.setMax(100);
        btn_upload_cancel = (Button) findViewById(R.id.btn_upload_cancel);
        tv_upload_progress = (TextView) findViewById(R.id.tv_progress);
        tv_upload_progress.setText("0%");
        tv_upload_title = (TextView) findViewById(R.id.tv_upload_title);
       // tv_upload_title.setText("Creating New Pillar......");
    }

    private void startUploadingToServer(String path, String pillar_id, String pillar_condition, String lat, String lng,
                                        String pillar_entry_code) {
        //Uploading code
        //upload_progressbar.setVisibility(View.VISIBLE);


        try {
            //String uploadId = UUID.randomUUID().toString();
            //Creating a multi part request
            MultipartUploadRequest req = new MultipartUploadRequest(this,
                    AppController.getInstance().getPrefManger().getBaseUrl() + Url.URL_PILLAR_UPDATE)
                    .addParameter("authImie", AppController.getInstance().getPrefManger().getUserProfile().getImieNumber())
                    .addFileToUpload(path, "file") //Adding file
                    .addParameter("id", pillar_id)
                    .addParameter("situation", pillar_condition)
                    .addParameter("latitude", lat)
                    .addParameter("longitude", lng)
                    .addParameter("newEntry", pillar_entry_code)
                    .addParameter("soldierId", AppController.getInstance().getPrefManger().getUserProfile().getId())
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setAutoDeleteFilesAfterSuccessfulUpload(true)
                    .setMaxRetries(2); //Starting the upload


            uploadId = req.setDelegate(this).startUpload();

        } catch (Exception exc) {
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onProgress(Context context, UploadInfo uploadInfo) {
        upload_progressbar.setProgress(uploadInfo.getProgressPercent());
        tv_upload_progress.setText(uploadInfo.getProgressPercent() + "%");
    }

    @Override
    public void onError(Context context, UploadInfo uploadInfo, Exception exception) {
        showAlertDialog(ERROR_CODE);

        isFreezeActivity = false;
    }

    @Override
    public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {


        try {
            JSONObject jsonObject = new JSONObject(serverResponse.getBodyAsString());

            String result = jsonObject.getString("result");

            if (result.equals("1")) {
                showAlertDialog(SUCCESS_CODE);
            } else {
                showAlertDialog(ERROR_CODE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            showAlertDialog(ERROR_CODE);
        }

        isFreezeActivity = false;
    }

    @Override
    public void onCancelled(Context context, UploadInfo uploadInfo) {
        isFreezeActivity = false;
        returnData(ERROR_CODE);
    }

    @Override
    public void onBackPressed() {
        if (!isFreezeActivity) {
            super.onBackPressed();
        }
    }

    private void showAlertDialog(final int code) {
        final Dialog dialog_start = new Dialog(this,
                android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog_start.setCancelable(true);
        switch (code) {
            case 1:
                dialog_start.setContentView(R.layout.dialog_success);
                dialog_start.show();
                break;
            case 2:
                dialog_start.setContentView(R.layout.dialog_error);
                dialog_start.show();
                break;
            case 3:
                dialog_start.setContentView(R.layout.dialog_error);
                TextView tv_error_text = (TextView) dialog_start.findViewById(R.id.tv_error_text);
                tv_error_text.setText("There is something went wrong while uploading to server!");
                dialog_start.show();
                break;
            case 4:
                dialog_start.setContentView(R.layout.dialog_error);
                TextView tv_error_text2 = (TextView) dialog_start.findViewById(R.id.tv_error_text);
                tv_error_text2.setText("You already have an open shift.");
                dialog_start.show();
                break;
        }
        new CountDownTimer(2000, 1000) {

            public void onTick(long millisUntilFinished) {
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                dialog_start.dismiss();

                returnData(code);


            }

        }.start();
    }


    private void returnData(int RESULT_CODE) {
        Intent data = new Intent();
        data.putExtra(KEY_UPLOAD_RESULT, RESULT_CODE == SUCCESS_CODE ? true : false);
        setResult(RESULT_OK, data);
        finish();
    }


}
