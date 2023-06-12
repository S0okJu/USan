package com.example.usan_comb1.models;

import com.google.gson.annotations.SerializedName;

public class Loc {
    @SerializedName("lat")
    Double lat;
    @SerializedName("lng")
    Double lng;

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}