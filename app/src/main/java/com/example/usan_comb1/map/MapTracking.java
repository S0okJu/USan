package com.example.usan_comb1.map;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.app.Activity;
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

    private HashMap<String, Marker> markers = new HashMap<>();

    private String username;
    private String other_username;
    DatabaseReference locations;
    double lat, lng;

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
        other_username = "testdy";

        if (!TextUtils.isEmpty(username)) {

            locationMarking(username, other_username);
        }

        /*
        dealButton = findViewById(R.id.deal_button);
        dealButton.setVisibility(View.GONE);

        dealButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog();
            }
        });
         */
    }


    // 지도에 정보를 marking하는 함수
    private void locationMarking(String username, String other_username) {
        Query user_location = locations.orderByChild("username").equalTo(username);
        Query other_user_location = locations.orderByChild("username").equalTo(other_username);

        Location currentUser = new Location(""); // Declare currentUser variable outside the ValueEventListener
        Location friend = new Location(""); // Declare friend variable outside the ValueEventListener

        user_location.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMap.clear(); // Clear old markers

                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    Tracking tracking = postSnapShot.getValue(Tracking.class);

                    if (tracking != null) {
                        double user_lat = tracking.getLat();
                        double user_lng = tracking.getLng();

                        LatLng currentUserLocation = new LatLng(user_lat, user_lng);

                        currentUser.setLatitude(user_lat);
                        currentUser.setLongitude(user_lng);

                        Marker currentUserMarker = mMap.addMarker(new MarkerOptions()
                                .position(currentUserLocation)
                                .title(username)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                        markers.put(username, currentUserMarker);
                    }
                }

                // Call distance calculation after updating currentUser and friend locations
                if (friend.getLatitude() != 0.0 && friend.getLongitude() != 0.0) {
                    updateDistance(currentUser, friend);
                }

                // Zoom to current user's location
                if (currentUser.getLatitude() != 0.0 && currentUser.getLongitude() != 0.0) {
                    LatLng current = new LatLng(currentUser.getLatitude(), currentUser.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 12.0f));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MapTracking", "Failed to read value.", error.toException());
            }
        });

        other_user_location.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    Tracking tracking = postSnapShot.getValue(Tracking.class);

                    if (tracking != null) {
                        double other_lat = tracking.getLat();
                        double other_lng = tracking.getLng();

                        LatLng otherLocation = new LatLng(other_lat, other_lng);

                        friend.setLatitude(other_lat);
                        friend.setLongitude(other_lng);

                        Marker friendMarker = mMap.addMarker(new MarkerOptions()
                                .position(otherLocation)
                                .title(other_username)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                        markers.put(other_username, friendMarker);
                    }
                }

                // Call distance calculation after updating currentUser and friend locations
                if (currentUser.getLatitude() != 0.0 && currentUser.getLongitude() != 0.0) {
                    updateDistance(currentUser, friend);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MapTracking", "Failed to read value.", error.toException());
            }
        });
    }

    private void updateDistance(Location currentUser, Location friend) {
        // Calculate the distance between currentUser and friend
        double distance = distance(currentUser, friend);
        String distanceString = new DecimalFormat("#.#m").format(distance);

        // Update the distance value in the markers' snippets
        Marker currentUserMarker = markers.get(username);
        if (currentUserMarker != null) {
            currentUserMarker.setSnippet("Distance: " + distanceString);
        }

        Marker friendMarker = markers.get(other_username);
        if (friendMarker != null) {
            friendMarker.setSnippet("Distance: " + distanceString);
        }

        /*
        // Check if the distance is within 5 meters
        if (distance <= 5 && !isDealButtonVisible) {
            showDealButton();
        } else if (distance > 5 && isDealButtonVisible) {
            hideDealButton();
        }
         */
    }



    public void updateMarker(String username, double latitude, double longitude) {

        if (mMap == null) {
            // mMap이 초기화되지 않았으므로 아무 작업도 수행하지 않음
            return;
        }
        // Update marker for friend location
        LatLng friendLocation = new LatLng(latitude, longitude);

        // Check if the marker exists in the map
        if (markers.containsKey(username)) {
            // Marker already exists, update its position
            Marker friendMarker = markers.get(username);
            friendMarker.setPosition(friendLocation);
        } else {
            // Marker doesn't exist, create a new marker
            Marker friendMarker = mMap.addMarker(new MarkerOptions()
                    .position(friendLocation)
                    .title(username)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

            markers.put(username, friendMarker);
        }

        mMap.clear(); // Clear old markers

        // Create marker for current user
        if (lat != 0.0 && lng != 0.0) {
            LatLng current = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(current).title(username));
        }
    }

    public double distance(Location currentUser, Location friend) {
        double lat1 = currentUser.getLatitude();
        double lon1 = currentUser.getLongitude();
        double lat2 = friend.getLatitude();
        double lon2 = friend.getLongitude();

        if (lat1 == lat2 && lon1 == lon2) {
            return 0.0; // Same coordinates, distance is 0
        }

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
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

    /*
    private void showDealButton() {
        isDealButtonVisible = true;
        dealButton.setVisibility(View.VISIBLE);
    }

    private void hideDealButton() {
        isDealButtonVisible = false;
        dealButton.setVisibility(View.GONE);
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("거래 상대가 근처에 있습니다.")
                .setMessage("결제창으로 넘어가시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO: 결제 Activity로 이동하는 코드 작성
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
     */

    @Override
    public void onBackPressed() {
        /*
        if (isDealButtonVisible) {
            hideDealButton();
        } else {
                }
         */
            setResult(Activity.RESULT_OK);
            finish();
    }

}