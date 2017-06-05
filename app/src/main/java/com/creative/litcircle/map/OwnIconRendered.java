package com.creative.litcircle.map;

import android.content.Context;

import com.creative.litcircle.R;
import com.creative.litcircle.model.Pillar;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by comsol on 02-Jun-17.
 */
public class OwnIconRendered extends DefaultClusterRenderer<Pillar> {

    public OwnIconRendered(Context context, GoogleMap map,
                           ClusterManager<Pillar> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(Pillar item, MarkerOptions markerOptions) {
        markerOptions.icon( BitmapDescriptorFactory.fromResource(R.drawable.marker_red_pin));
        markerOptions.snippet(item.getSnippet());
        markerOptions.title(item.getTitle());
        super.onBeforeClusterItemRendered(item, markerOptions);
    }
}