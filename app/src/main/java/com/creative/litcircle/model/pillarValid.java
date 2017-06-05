package com.creative.litcircle.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by comsol on 02-Jun-17.
 */
public class PillarValid implements ClusterItem {

    Pillar pillar;
int id;
    public PillarValid(Pillar pillar, int id) {
        this.pillar = pillar;
        this.id = id;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Pillar getPillar() {
        return pillar;
    }

    public void setPillar(Pillar pillar) {
        this.pillar = pillar;
    }

    @Override
    public LatLng getPosition() {
        return this.pillar.getPosition();
    }

    @Override
    public String getTitle() {
        return this.pillar.getName();
    }

    @Override
    public String getSnippet() {
        return this.pillar.getName();
    }
}
