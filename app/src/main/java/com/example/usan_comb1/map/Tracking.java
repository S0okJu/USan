package com.example.usan_comb1.map;

public class Tracking {
    private String username;
    double lat, lng;

    public Tracking() {
    }

    public Tracking(String username, double lat, double lng) {
        this.username = username;
        this.lat = lat;
        this.lng = lng;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
