package com.example.usan_comb1.map;

public class Tracking {
    private String username, lat, lng;

    public Tracking() {
    }

    public Tracking(String username, String lat, String lng) {
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
