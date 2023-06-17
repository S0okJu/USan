package com.example.usan_comb1.request;

// 자신의 위치 서버에 보내는 Request (임시 파일)
public class MyLocationRequest {
    double latitude;
    double longitude;

    public MyLocationRequest(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

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
