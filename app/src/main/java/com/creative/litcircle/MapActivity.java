package com.creative.litcircle;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.creative.litcircle.appdata.AppController;
import com.creative.litcircle.map.BaseMapActivity;
import com.creative.litcircle.map.OwnIconRendered;
import com.creative.litcircle.model.Pillar;
import com.creative.litcircle.model.PillarValid;
import com.creative.litcircle.utils.LastLocationOnly;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;


/**
 * Created by comsol on 01-Jun-17.
 */
public class MapActivity extends BaseMapActivity {

    List<PillarValid> pillars;

    // Declare a variable for the cluster manager.
    private ClusterManager<Pillar> mClusterManager;


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        pillars = AppController.getInstance().getPrefManger().getPillars();
    }

    @Override
    protected void startDemo() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        getMap().setMyLocationEnabled(true);
        getMap().getUiSettings().setMyLocationButtonEnabled(true);
        getMap().getUiSettings().setZoomControlsEnabled(true);
        LastLocationOnly gps = new LastLocationOnly(this);

      //  mClusterManager = new ClusterManager<Pillar>(this, getMap());
        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
      //  getMap().setOnMarkerClickListener(mClusterManager);
      //  getMap().setOnCameraIdleListener(mClusterManager);



        com.google.android.gms.maps.model.LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        if (gps.canGetLocation()) {
            boundsBuilder.include(new LatLng(gps.getLatitude(), gps.getLongitude()));
        }

        for (int i = 0; i < pillars.size(); i++) {

           // if (pillars.get(i).getLatitude().equals("null")) continue;
           // mClusterManager.addItem(pillars.get(i));
            boundsBuilder.include(new LatLng(Double.parseDouble(pillars.get(i).getPillar().getLatitude()),
                    Double.parseDouble(pillars.get(i).getPillar().getLongitude())));


             Marker marker = createMarker(
                    Double.parseDouble(pillars.get(i).getPillar().getLatitude()),
                    Double.parseDouble(pillars.get(i).getPillar().getLongitude()),
                    pillars.get(i).getPillar().getName(), pillars.get(i).getPillar().getName(),
                    R.drawable.marker_red_pin
            );

        }
        final LatLngBounds bounds = boundsBuilder.build();
        getMap().setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 40));
                getMap().setInfoWindowAdapter(new MyInfoWindowAdapter());
            }
        });


       // mClusterManager.setRenderer(new OwnIconRendered(this.getApplicationContext(), getMap(), mClusterManager));
       // mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(new MyInfoWindowAdapter());

    }


    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        MyInfoWindowAdapter() {
            myContentsView = getLayoutInflater().inflate(R.layout.custom_infowindow, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            //TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.tv_pillar_title));
            //tvTitle.setText(marker.getTitle());
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {

            TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.tv_pillar_title));
            tvTitle.setText(marker.getTitle());
            return myContentsView;
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
