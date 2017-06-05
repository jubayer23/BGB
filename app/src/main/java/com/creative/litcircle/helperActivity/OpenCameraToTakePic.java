package com.creative.litcircle.helperActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;

import com.creative.litcircle.alertbanner.AlertDialogForAnything;
import com.creative.litcircle.utils.AccessDirectory;
import com.iceteck.silicompressorr.SiliCompressor;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;

/**
 * Created by comsol on 04-Apr-17.
 */
public class OpenCameraToTakePic extends AppCompatActivity {

    public static final String KEY_FILE_URL = "fileUri";
    public static final String KEY_ERROR = "error";
    public static final String USER_CANCEL_TO_TAKE_PIC = "user_cancel_to_take_image";
    public static final String CRUSH = "crush";
    private Uri fileUri;

    private static final int CAMERA_REQUEST = 1888;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    public Uri getOutputMediaFileUri() {
        //return Uri.fromFile(AccessDirectory.getOutputMediaFile());

       return FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", AccessDirectory.getOutputMediaFile());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    CropImage.activity(fileUri)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setMultiTouchEnabled(true)
                            .start(this);
                } catch (Exception e) {
                    AlertDialogForAnything.showAlertDialogWhenComplte(this, "ERROR", "Crop Functionality does not work on your phone!", false);
                }
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                fileUri = result.getUri();
                // Log.d("DEBUG",fileUri.getPath());
                String filePath = SiliCompressor.with(this).compress(fileUri.getPath(), true);
                fileUri = Uri.fromFile(new File(filePath));
                // Log.d("DEBUG",fileUri.getPath());
                Intent returnIntent = new Intent();
                returnIntent.putExtra(KEY_FILE_URL, filePath);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                returnIntent.putExtra(KEY_ERROR, CRUSH);
                finish();
            } else if (resultCode == RESULT_CANCELED) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(KEY_ERROR, USER_CANCEL_TO_TAKE_PIC);
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }

        }


        super.onActivityResult(requestCode, resultCode, data);
    }

}
