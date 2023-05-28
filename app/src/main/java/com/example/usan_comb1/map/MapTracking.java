package com.example.usan_comb1.map;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;

import com.example.usan_comb1.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
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

import io.reactivex.rxjava3.internal.operators.maybe.MaybeHide;

public class MapTracking extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private String email;
    DatabaseReference locations;
    Double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map_tracking);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Ref to firebase first
        locations = FirebaseDatabase.getInstance().getReference("locations");

        if(getIntent() != null){
            email = getIntent().getStringExtra("email");
            lat = getIntent().getDoubleExtra("lat",0);
            lng = getIntent().getDoubleExtra("lng",0);
        }
        if(!TextUtils.isEmpty(email)){
            loadLocationForThisUser(email);
        }

    }
    // 지도에 정보를 marking하는 함수
    private void loadLocationForThisUser(String email) {
        Query user_location = locations.orderByChild("email").equalTo(email);

        user_location.addValueEventListener(new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapShot:dataSnapshot.getChildren()){
                    Tracking tracking =  postSnapShot.getValue(Tracking.class);

                    // Add marker for friend location
                    LatLng friendLocation = new LatLng(Double.parseDouble(tracking.getLat()), Double.parseDouble((tracking.getLng())));
                    // Create location from user coordinates
                    Location currentUser = new Location("");
                    currentUser.setLatitude(lat);
                    currentUser.setLongitude(lng);

                    Location friend = new Location("");
                    friend.setLatitude(Double.parseDouble(tracking.getLat()));
                    friend.setLongitude(Double.parseDouble(tracking.getLng()));

                    // Clear old mark
                    mMap.clear();
                    // Add friend Marker
                    mMap.addMarker(new MarkerOptions().position(friendLocation).title(tracking.getEmail()).snippet("Distance "+ new DecimalFormat("#.#").format(distance(currentUser, friend))).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),12.0f));
                }
                // Create marker for current user
                LatLng current = new LatLng(lat,lng);
                mMap.addMarker(new MarkerOptions().position(current).title(FirebaseAuth.getInstance().getCurrentUser().getEmail()));

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public double distance(Location currentUser, Location friend){
        double theta = currentUser.getLongitude() - friend.getLongitude();
        double dist = Math.sin(deg2rad(currentUser.getLatitude())) * Math.sin(deg2rad(friend.getLatitude()))
                * Math.cos(deg2rad(currentUser.getLongitude()))* Math.cos(deg2rad(friend.getLongitude()))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist  = dist * 60 * 1.1515;
        return dist;
    }

    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    private double deg2rad(double deg){
        return (deg * Math.PI / 180.0);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


    }
}