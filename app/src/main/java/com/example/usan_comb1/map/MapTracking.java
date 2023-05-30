package com.example.usan_comb1.map;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.example.usan_comb1.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.usan_comb1.databinding.ActivityMapTrackingBinding;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.HashMap;

import io.reactivex.rxjava3.internal.operators.maybe.MaybeHide;

public class MapTracking extends FragmentActivity implements OnMapReadyCallback {

    private static MapTracking instance;
    private GoogleMap mMap;

    private HashMap<String, Marker> markers = new HashMap<>(); // markers 변수 추가

    private String username;
    DatabaseReference locations;
    Double lat, lng;

    public static MapTracking getInstance() {
        return instance;
    }

    public static boolean isActive() {
        return instance != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        setContentView(R.layout.activity_map_tracking);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Ref to firebase first
        locations = FirebaseDatabase.getInstance().getReference("locations");

        if (getIntent() != null) {
            username = getIntent().getStringExtra("username");
        }
        if (!TextUtils.isEmpty(username)) {
            loadLocationForThisUser(username);
        }
    }

    // 지도에 정보를 marking하는 함수
    private void loadLocationForThisUser(String username) {
        Query user_location = locations.orderByChild("username").equalTo(username);

        user_location.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMap.clear(); // Clear old markers

                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    Tracking tracking = postSnapShot.getValue(Tracking.class);

                    if (tracking != null) {
                        String latString = tracking.getLat();
                        String lngString = tracking.getLng();
                        if (latString != null && lngString != null) {
                            lat = Double.valueOf(latString);
                            lng = Double.valueOf(lngString);
                            LatLng friendLocation = new LatLng(lat, lng);

                            if (lat != null && lng != null) {
                                // Add marker for friend location
                                // Create location from user coordinates
                                Location currentUser = new Location("");
                                currentUser.setLatitude(lat);
                                currentUser.setLongitude(lng);

                                Location friend = new Location("");
                                friend.setLatitude(lat);
                                friend.setLongitude(lng);

                                if (lat != null && lng != null) {
                                    // Add friend Marker
                                    Marker friendMarker = mMap.addMarker(new MarkerOptions().position(friendLocation).title(tracking.getUsername()).snippet("Distance " + new DecimalFormat("#.#").format(distance(currentUser, friend))).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                                    markers.put(tracking.getUsername(), friendMarker);
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 12.0f));
                                }
                            }
                        }
                    }
                }
                // Create marker for current user
                if (lat != null && lng != null) {
                    LatLng current = new LatLng(lat, lng);
                    mMap.addMarker(new MarkerOptions().position(current).title(username));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MapTracking", "Failed to read value.", error.toException());
            }
        });
    }

    public void updateMarker(String username, double latitude, double longitude) {
        // Update marker for friend location
        LatLng friendLocation = new LatLng(latitude, longitude);

        // Check if the marker exists in the map
        if (markers.containsKey(username)) {
            // Marker already exists, update its position
            Marker friendMarker = markers.get(username);
            friendMarker.setPosition(friendLocation);
        } else {
            // Marker doesn't exist, create a new marker
            Marker friendMarker = mMap.addMarker(new MarkerOptions().position(friendLocation).title(username).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            markers.put(username, friendMarker);
        }

        mMap.clear(); // Clear old markers

        // Create marker for current user
        if (lat != null && lng != null) {
            LatLng current = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(current).title(username));
        }
    }

    public double distance(Location currentUser, Location friend) {
        double theta = currentUser.getLongitude() - friend.getLongitude();
        double dist = Math.sin(deg2rad(currentUser.getLatitude())) * Math.sin(deg2rad(friend.getLatitude()))
                * Math.cos(deg2rad(currentUser.getLongitude())) * Math.cos(deg2rad(friend.getLongitude()))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 1.609344;

        return dist;
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }
}