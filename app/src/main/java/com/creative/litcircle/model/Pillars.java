package com.creative.litcircle.model;

import java.util.List;

/**
 * Created by comsol on 18-Mar-17.
 */
public class Pillars {

    List<Pillar> pillars;

    public Pillars(List<Pillar> pillars) {
        this.pillars = pillars;
    }

    public List<Pillar> getPillars() {
        return pillars;
    }

    public void setPillars(List<Pillar> pillars) {
        this.pillars = pillars;
    }
}
