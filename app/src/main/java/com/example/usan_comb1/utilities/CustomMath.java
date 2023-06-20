package com.example.usan_comb1.utilities;

import android.location.Location;

public class CustomMath {

    public static double getDistanceBetween(Location currentUser, Location friend){
        double theta = currentUser.getLongitude() - friend.getLongitude();
        double dist = Math.sin(deg2rad(currentUser.getLatitude())) * Math.sin(deg2rad(friend.getLatitude()))
                * Math.cos(deg2rad(currentUser.getLongitude()))* Math.cos(deg2rad(friend.getLongitude()))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 1.609344 * 1000; // Convert miles to meters

        return dist;
    }

    public static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public static double deg2rad(double deg){
        return (deg * Math.PI / 180.0);
    }

    public static String extractProductIdFromChatId(String input) {
        return input.replaceAll("[^\\d]", "");
    }

    public static boolean checkDistDifference(Location src, Location dest){
        Double calcLat = src.getLatitude() - dest.getLatitude();
        Double calcLong = src.getLongitude() - dest.getLongitude();

        // 거리 차이 확인
        boolean checkLat = (-0.05 < calcLat) && (calcLat < 0.05);
        boolean checkLong = (-0.05 < calcLong) && (calcLong < 0.05);
        if(checkLat==true && checkLong==true){
            return true;
        }else{
            return false;
        }
    }

}
