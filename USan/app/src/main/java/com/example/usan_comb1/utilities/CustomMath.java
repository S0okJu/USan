package com.example.usan_comb1.utilities;

import android.location.Location;

public class CustomMath {

    public static double getDistanceBetween(Location currentUser, Location friend){
        double R = 6371e3; // Earth's radius in meters
        double lat1 = deg2rad(currentUser.getLatitude());
        double lat2 = deg2rad(friend.getLatitude());
        double deltaLat = deg2rad(friend.getLatitude() - currentUser.getLatitude());
        double deltaLon = deg2rad(friend.getLongitude() - currentUser.getLongitude());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }


    public static double deg2rad(double deg){
        return (deg * Math.PI / 180.0);
    }

    public static String extractProductIdFromChatId(String input) {
        return input.replaceAll("[^\\d]", "");
    }

    public static boolean checkDistDifference(Location src, Location dest){
        Double distance = getDistanceBetween(src, dest);
        System.out.println(distance);
        // 거리 차이 확인

        if(distance < 30 ){
            return true;
        }else{
            return false;
        }
    }

}