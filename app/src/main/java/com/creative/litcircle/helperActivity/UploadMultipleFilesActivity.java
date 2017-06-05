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
import com.creative.litcircle.appdata.AppController;
import com.creative.litcircle.appdata.Url;
import com.creative.litcircle.model.UploadPillar;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class UploadMultipleFilesActivity extends AppCompatActivity implements UploadStatusDelegate {

    public static final String KEY_UPLOAD_RESULT = "upload_result";
    private ProgressBar upload_progressbar;
    private Button btn_upload_cancel;
    private TextView tv_upload_progress, tv_upload_title,tv_upload_counter;
    private String uploadId = "";

    private static final int SUCCESS_CODE = 1;
    private static final int ERROR_CODE = 2;

    private boolean isFreezeActivity = true;

    List<UploadPillar> uploadPillars;


    private static int total_pillar_need_to_upload = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_upload_progress);

        init();


        uploadPillars = AppController.getInstance().getPrefManger().getUploadPillars();


        total_pillar_need_to_upload = uploadPillars.size();
        tv_upload_counter.setText((total_pillar_need_to_upload - uploadPillars.size() + 1) +"/" + total_pillar_need_to_upload);
        startUploadingToServer(uploadPillars.get(0));


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
        tv_upload_counter = (TextView) findViewById(R.id.tv_upload_counter);
       // tv_upload_title.setText("Creating New Pillar......");
    }

    private void startUploadingToServer(UploadPillar uploadPillar) {
        //Uploading code
        //upload_progressbar.setVisibility(View.VISIBLE);


        try {
            //String uploadId = UUID.randomUUID().toString();
            //Creating a multi part request
            MultipartUploadRequest req = new MultipartUploadRequest(this,
                    AppController.getInstance().getPrefManger().getBaseUrl() + Url.URL_PILLAR_UPDATE)
                    .addParameter("authImie", AppController.getInstance().getPrefManger().getUserProfile().getImieNumber())
                    .addFileToUpload(uploadPillar.getFilePath(), "file") //Adding file
                    .addParameter("id", uploadPillar.getPillar_id())
                    .addParameter("situation", uploadPillar.getPillar_condition())
                    .addParameter("latitude", uploadPillar.getLat())
                    .addParameter("longitude", uploadPillar.getLng())
                    .addParameter("newEntry", uploadPillar.getUpload_type())
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

                uploadPillars.remove(0);

                if (uploadPillars.isEmpty()) {

                    showAlertDialog(SUCCESS_CODE);

                } else {

                    upload_progressbar.setProgress(0);
                    tv_upload_counter.setText((total_pillar_need_to_upload - uploadPillars.size() + 1) +"/" + total_pillar_need_to_upload);
                    startUploadingToServer(uploadPillars.get(0));
                }

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
            case SUCCESS_CODE:
                dialog_start.setContentView(R.layout.dialog_success);
                dialog_start.show();
                break;
            case ERROR_CODE:
                dialog_start.setContentView(R.layout.dialog_error);
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
