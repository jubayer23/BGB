package com.creative.litcircle.userview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;

import com.creative.litcircle.R;
import com.creative.litcircle.utils.DeviceInfoUtils;

public class AboutActivity extends AppCompatActivity {

    private TextView tv_all_rights_reserve,tv_developed_by,tv_app_version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        tv_all_rights_reserve = (TextView)findViewById(R.id.tv_all_rights_reserve);

        tv_developed_by = (TextView)findViewById(R.id.tv_developed_by);

        tv_app_version = (TextView)findViewById(R.id.tv_app_version);
        tv_app_version.setText("App Version : " + DeviceInfoUtils.getAppVersionName());

        tv_all_rights_reserve.setText(Html.fromHtml("@All Rights Reserved To" + "<font color=blue><b>" + " BGB" + "</font></b>"));

        tv_developed_by.setText(Html.fromHtml("Developed By" + "<font color=blue><b>" + " Jubayer" + "</font></b>"));
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
