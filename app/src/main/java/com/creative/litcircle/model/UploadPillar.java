package com.creative.litcircle.model;

import com.creative.litcircle.NewPillarsEntry;

/**
 * Created by comsol on 29-May-17.
 */
public class UploadPillar {

    String upload_type;
    String filePath;
    String pillar_id;
    String pillar_condition;
    String lat;
    String lng;

    public UploadPillar(String upload_type, String filePath, String pillar_id, String pillar_condition, String lat, String lng) {
        this.upload_type = upload_type;
        this.filePath = filePath;
        this.pillar_id = pillar_id;
        this.pillar_condition = pillar_condition;
        this.lat = lat;
        this.lng = lng;
    }

    public String getUpload_type() {
        return upload_type;
    }

    public void setUpload_type(String upload_type) {
        this.upload_type = upload_type;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getPillar_id() {
        return pillar_id;
    }

    public void setPillar_id(String pillar_id) {
        this.pillar_id = pillar_id;
    }

    public String getPillar_condition() {
        return pillar_condition;
    }

    public void setPillar_condition(String pillar_condition) {
        this.pillar_condition = pillar_condition;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
