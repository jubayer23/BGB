package com.creative.litcircle.model;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.Comparator;

/**
 * Created by comsol on 02-Jun-17.
 */
public class SortPlaces implements Comparator<PillarValid> {
    Location currentLoc;

    public SortPlaces(Location current){
        currentLoc = current;
    }
    @Override
    public int compare(final PillarValid place1, final PillarValid place2) {



        Location locationA = place1.getPillar().getLocation();


        Location locationB =place2.getPillar().getLocation();

        locationB.setLatitude(Double.parseDouble(place2.getPillar().getLatitude()));

        locationB.setLongitude(Double.parseDouble(place2.getPillar().getLongitude()));

        //distance = locationA.distanceTo(locationB);
        double distanceToPlace1 = currentLoc.distanceTo(locationA);
        double distanceToPlace2 = currentLoc.distanceTo(locationB);

        return (int) (distanceToPlace1 - distanceToPlace2);
    }

    public double distance(double fromLat, double fromLon, double toLat, double toLon) {
        double radius = 6378137;   // approximate Earth radius, *in meters*
        double deltaLat = toLat - fromLat;
        double deltaLon = toLon - fromLon;
        double angle = 2 * Math.asin( Math.sqrt(
                Math.pow(Math.sin(deltaLat/2), 2) +
                        Math.cos(fromLat) * Math.cos(toLat) *
                                Math.pow(Math.sin(deltaLon/2), 2) ) );
        return radius * angle;
    }
}