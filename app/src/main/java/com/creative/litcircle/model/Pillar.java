package com.creative.litcircle.model;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by comsol on 27-Feb-17.
 */
public class Pillar implements ClusterItem {
    private final LatLng mPosition;
    String id;
    String name;
    String latitude;
    String longitude;
    String url;
    private final String mTitle;
    private final String mSnippet;
    private final Location location;

    public Pillar(String id, String name, String latitude, String longitude, String url) {

        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.url = url;
        mTitle = name;
        mSnippet = name;
        if (!latitude.equals("null") && !longitude.equals("null")) {
            this.mPosition = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
            this.location = new Location("");
            this.location.setLatitude(Double.parseDouble(latitude));
            this.location.setLongitude(Double.parseDouble(longitude));
        } else {
            this.mPosition = new LatLng(0.0, 0.0);
            this.location = new Location("");
        }

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }

    public Location getLocation() {
        return location;
    }
}
