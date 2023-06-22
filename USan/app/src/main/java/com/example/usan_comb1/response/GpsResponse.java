package com.example.usan_comb1.response;

import com.google.gson.annotations.SerializedName;

public class GpsResponse {

    @SerializedName("latitude")
    double latitude;

    @SerializedName("longitude")
    double longitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
