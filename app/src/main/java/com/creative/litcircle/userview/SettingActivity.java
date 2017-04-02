package com.creative.litcircle.userview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.creative.litcircle.R;
import com.creative.litcircle.appdata.AppController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by comsol on 13-Jan-16.
 */
public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    EditText ed_server_url;

    Button btn_save;


    public static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_setting);

        init();
    }

    private void init() {

        ed_server_url = (EditText)findViewById(R.id.ed_server_url);

        btn_save = (Button) findViewById(R.id.setting_save);

        btn_save.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.setting_save) {

            String prefix = "http://";

            if(!ed_server_url.getText().toString().isEmpty()){
                String current_url = ed_server_url.getText().toString();

                if(!current_url.contains(prefix)){
                    current_url = prefix + current_url;
                }

                AppController.getInstance().getPrefManger().setBaseUrl(current_url);

                Toast.makeText(SettingActivity.this, "Saved Successfull", Toast.LENGTH_LONG).show();

                finish();
            }else{
                Toast.makeText(SettingActivity.this, "The Filed is Empty", Toast.LENGTH_LONG).show();
            }



        }
    }
}
