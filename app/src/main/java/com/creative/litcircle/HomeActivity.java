package com.creative.litcircle;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.creative.litcircle.alertbanner.AlertDialogForAnything;
import com.creative.litcircle.appdata.AppConstant;
import com.creative.litcircle.appdata.AppController;
import com.creative.litcircle.appdata.Url;
import com.creative.litcircle.drawer.Drawer_list_adapter;
import com.creative.litcircle.fragment.HomeFragment;
import com.creative.litcircle.userview.AboutActivity;
import com.creative.litcircle.utils.ConnectionDetector;
import com.creative.litcircle.utils.DeviceInfoUtils;
import com.creative.litcircle.utils.LastLocationOnly;
import com.creative.litcircle.utils.MarshMallowPermission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG_FRAGMENT = "MainFragment";
    //Drawer
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    private ExpandableListView drawer_list;
    private Drawer_list_adapter drawer_adapter_custom;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    private Toolbar toolbar;

    public static final String DRAWER_LIST_MAP = "Map";
    public static final String DRAWER_LIST_NEWENTRY = "New Pillar Entry";
    public static final String DRAWER_LIST_ACCOUNT = "Account";
    public static final String DRAWER_LIST_ABOUT = "About";
    public static final String DRAWER_LIST_SIGNOUT = "Sign Out";
    public static final String DRAWER_LIST_SETTING = "Setting";


    private ProgressDialog progressDialog;

    private ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.content_layout, new HomeFragment(), TAG_FRAGMENT)
                    .commit();
        }




    }

    @Override
    protected void onResume() {
        super.onResume();
        DeviceInfoUtils.checkInternetConnectionAndGps(this);
        DeviceInfoUtils.checkMarshMallowPermission(HomeActivity.this);
    }

    private void init() {

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(false);
        progressDialog.setMessage("Loading...");

        cd = new ConnectionDetector(this);

        makeDrawer();

    }


    public void makeDrawer() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);


        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.nav_icon_inactive);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.nav_icon_inactive);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.nav_icon_active);

            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        setDrawer();
    }


    protected void setDrawer() {
        prepareListData();
        drawer_list = (ExpandableListView) findViewById(R.id.left_drawer);
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.drawer_list_header, drawer_list, false);


        TextView header2 = (TextView) header.findViewById(R.id.list_header);
        TextView header3 = (TextView) header.findViewById(R.id.list_header_2);

        header2.setText("Profile");
        header3.setText(AppController.getInstance().getPrefManger().getUserProfile().getUser_id());


        drawer_adapter_custom = new Drawer_list_adapter(this, listDataHeader, listDataChild);
        drawer_list.addHeaderView(header, null, false);
        drawer_list.setAdapter(drawer_adapter_custom);

        drawer_list.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {


                return false;
            }
        });
        drawer_list.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {


                if (listDataHeader.get(i).contains(DRAWER_LIST_MAP)) {
                    //Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                    //startActivity(intent);
                    mDrawerLayout.closeDrawers();

                }

                if (listDataHeader.get(i).contains(DRAWER_LIST_NEWENTRY)) {
                    //Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                    //startActivity(intent);
                    // MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
                    //fragment.devices();
                    if (!cd.isConnectingToInternet()) {
                        //Internet Connection is not present
                        AlertDialogForAnything.showAlertDialogWhenComplte(HomeActivity.this, "Internet Connection Error",
                                "Please connect to working Internet connection", false);
                        //stop executing code by return
                        return false;
                    }
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);

                    alertDialog.setTitle("Alert!!");

                    alertDialog.setMessage("Please Insure That You Are Near a Pillar For New Pillars Entry.");

                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            startActivity(new Intent(HomeActivity.this, NewPillarsEntry.class));
                            dialog.cancel();
                        }
                    });

                    alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();

                        }
                    });

                    alertDialog.show();
                }
                if (listDataHeader.get(i).contains(DRAWER_LIST_ABOUT)) {
                    Intent intent = new Intent(HomeActivity.this, AboutActivity.class);
                    startActivity(intent);
                }
                if (listDataHeader.get(i).contains(DRAWER_LIST_SIGNOUT)) {
                    //Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                    //startActivity(intent);
                    // MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
                    // fragment.logout();

                    if (!AppController.getInstance().getPrefManger().getPetrolId().isEmpty()) {

                        AlertDialogForAnything.showAlertDialogWhenComplte(HomeActivity.this, "Alert", "Patrolling is running. Please Stop It Before Singout!", false);
                    } else {


                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);

                        alertDialog.setTitle("Alert!!");

                        alertDialog.setMessage("Are you sure to Sign Out.");

                        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                AppController.getInstance().getPrefManger().setUserProfile("");
                                startActivity(new Intent(HomeActivity.this, MainActivity.class));
                                finish();

                            }
                        });

                        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();

                            }
                        });

                        alertDialog.show();


                    }

                }


                //Toast.makeText(getApplicationContext(), "" + listDataHeader.get(i), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data

        listDataHeader.add(DRAWER_LIST_MAP);
        listDataHeader.add(DRAWER_LIST_NEWENTRY);
        listDataHeader.add(DRAWER_LIST_ABOUT);
        listDataHeader.add(DRAWER_LIST_SIGNOUT);

        // Adding child data
        // List<String> info = new ArrayList<String>();
        //info.add(DRAWER_LIST_HOSPITAL);
        //info.add(DRAWER_LIST_POLICE);


        //listDataChild.put(listDataHeader.get(2), info); // Header, Child data
//        listDataChild.put(listDataHeader.get(1), others);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem bedMenuItem = menu.findItem(R.id.action_name);
        bedMenuItem.setTitle("V " +DeviceInfoUtils.getAppVersionName());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_name) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
