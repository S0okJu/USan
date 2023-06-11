package com.example.usan_comb1.map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.activity.ProductActivity;
import com.example.usan_comb1.models.Loc;
import com.example.usan_comb1.response.PostResult;
import com.example.usan_comb1.response.ProfileResponse;
import com.example.usan_comb1.utilities.PreferenceManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.usan_comb1.databinding.ActivityMapTrackingBinding;

import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;


import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.internal.operators.maybe.MaybeHide;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapTracking extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private HashMap<String, Marker> markers = new HashMap<>();
    private String email;
    DatabaseReference locations;
    Double lat, lng;
    private String chatId;
    private String otherUser;

    private String accessToken;

    private Button dealButton;
    private boolean isDealButtonVisible;

    private ProductService service;
    private LatLng dest;
    private DatabaseReference locationRef;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Polyline distanceLine; // 둘 사이의 거리를 나타내기 위해 사용됨
    private PreferenceManager preferenceManager;
    private String curUsername;
    private int role;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map_tracking);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        service = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");

        if(getIntent() != null){
            chatId = getIntent().getStringExtra("chatId");
            otherUser = getIntent().getStringExtra("otherUser");
            role = getIntent().getIntExtra("role",-1);
        }
        curUsername = preferenceManager.getString("username");

        // Ref to firebase first
        locationRef = FirebaseDatabase.getInstance().getReference("locations").child(chatId);

//        initLocation();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 10 seconds}
        locationRequest.setFastestInterval(5000); // 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // update location
                    Map<String, Object> latLng = new HashMap<>();
                    Double lat = location.getLatitude();
                    Double lng = location.getLongitude();
                    latLng.put("lat", lat);
                    latLng.put("lng", lng);

                    LatLng current = new LatLng(lat, lng);
                    // Clear the old marker for curUsername
                    if (markers.get(curUsername) != null) {
                        markers.get(curUsername).remove();
                    }

                    Marker curUserMarker = mMap.addMarker(new MarkerOptions().position(current).title(curUsername));
                    markers.put(curUsername, curUserMarker);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),14.0f));

                    latLng.put("lat", lat);
                    latLng.put("lng", lng);
                    locationRef.child(curUsername).updateChildren(latLng);

                }

                locationRef.child(otherUser).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Checking if the dataSnapshot has the children "lat" and "lng"
                        if (dataSnapshot.hasChild("lat") && dataSnapshot.hasChild("lng")) {
                            Double lat = (Double) dataSnapshot.child("lat").getValue();
                            Double lng = (Double) dataSnapshot.child("lng").getValue();
                            LatLng otherLocation = new LatLng(lat, lng);

                            // Clear the old marker for otherUser
                            if (markers.get(otherUser) != null) {
                                markers.get(otherUser).remove();
                            }
                            // Add new marker for otherUser
                            Marker otherUserMarker = mMap.addMarker(new MarkerOptions().position(otherLocation).title(otherUser).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                            markers.put(otherUser, otherUserMarker);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MapTracking", "Failed to read value.", error.toException());
                    }
                });
            }
        };

        getDest();

        dealButton = findViewById(R.id.deal_button);
        dealButton.setVisibility(View.GONE);

        dealButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog();
            }
        });

    }
    @Override
    protected void onStart(){
        super.onStart();
        checkSettingsAndStartLocationUpdates();
    }

    private void checkSettingsAndStartLocationUpdates() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder().
                addLocationRequest(locationRequest).
                build();
        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates();
            }
        });

    }

    private void startLocationUpdates() {
// 위치 권한 요청
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 위치 권한이 없는 경우 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            // 위치 권한이 있는 경우 위치 업데이트 시작
            startLocationUpdates();
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void initLocation(){
        Map<String, Object> latLng = new HashMap<>();
        latLng.put("lat", 0.0);
        latLng.put("lng", 0.0);
        locationRef.child(curUsername).updateChildren(latLng);
    }
    private void getOtherUserLocation(String other){
        locationRef.child(other).addValueEventListener(new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapShot:dataSnapshot.getChildren()){
                    Double lat = (Double) postSnapShot.child("lat").getValue();
                    Double lng = (Double) postSnapShot.child("lng").getValue();
                    LatLng otherLocation = new LatLng(lat,lng);

                    Location friend = new Location("");
                    friend.setLatitude(lat);
                    friend.setLongitude(lng);

                    // Clear old mark
                    mMap.clear();

                    // Add friend Marker
                    // TODO Snipper를 임시적으로 삭제함
                    //  추후 목적지 거리 차를 구하기 위해 사용할 예정
                    mMap.addMarker(new MarkerOptions().position(otherLocation).title(otherUser).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),12.0f));
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    // 지도에 정보를 marking하는 함수
    private void loadLocationForThisUser(String username) {


        locationRef.child(username).addValueEventListener(new ValueEventListener(){

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


    public void getDest(){

        Call<Loc> call = service.getDestLocation(accessToken, chatId);
        dest = new LatLng(0.0,0.0);
        call.enqueue(new Callback<Loc>() {
            @Override
            public void onResponse(Call<Loc> call, Response<Loc> response) {
                if(response.isSuccessful()){
                    Loc result = response.body();
                    System.out.println(result.getLat());
                    dest = new LatLng(result.getLat(), result.getLng());
                    Marker destMarker = mMap.addMarker(new MarkerOptions().position(dest).title("거래 장소"));
                    markers.put("거래 장소",destMarker);

                }
            }
            @Override
            public void onFailure(Call<Loc> call, Throwable t) {
                Log.d("Dest Location", "Failed to load dest location");
                mMap.addMarker(new MarkerOptions().position(new LatLng(0.0,0.0)).title("Dest"));
            }
        });

    }

    // Directions API 요청을 보내고 응답을 처리하는 메소드
    private void requestDirections(LatLng user, LatLng destination) {
        // Directions API를 사용하여 경로 요청 생성
        DirectionsResult result;
        try {
            result = DirectionsApi.newRequest(getGeoContext())
                    .origin(new com.google.maps.model.LatLng(user.latitude, user.longitude))
                    .destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                    .mode(TravelMode.WALKING) // 도보 모드로 설정
                    .await(); // 동기적으로 응답 받기
        } catch (Exception e) {
            // 경로 요청에 실패했을 때 처리하는 부분
            // 에러 처리 로직을 구현하면 됩니다.
            return;
        }

        // 응답을 처리하는 부분
        if (result != null && result.routes != null && result.routes.length > 0) {
            // 경로 정보가 있을 경우 처리
            DirectionsRoute route = result.routes[0]; // 첫 번째 경로 사용

            // 경로를 따라 지도에 폴리라인 그리기
            List<LatLng> path = decodePolyline(route.overviewPolyline.getEncodedPath());
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(path)
                    .width(10)
                    .color(Color.BLUE);
            mMap.addPolyline(polylineOptions);

            // 출발지와 목적지가 보이도록 카메라 이동
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            boundsBuilder.include(user);
            boundsBuilder.include(destination);
            LatLngBounds bounds = boundsBuilder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        }
    }

    // 인코딩된 폴리라인 데이터를 디코딩하여 LatLng 리스트로 변환하는 메소드
    private List<LatLng> decodePolyline(String encodedPath) {
        List<LatLng> path = new ArrayList<>();
        PolylineEncoding.decode(encodedPath).forEach(latLng -> path.add(new LatLng(latLng.lat, latLng.lng)));
        return path;
    }

    // Geocoding API와 Directions API에 필요한 GeoApiContext 생성
    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyCoYvOJIo6-rpl-GJvxPa7SwtuCeQrXBw4") // Directions API 및 Geocoding API를 사용하려면 API 키를 지정해야 합니다.
                .build();
        return geoApiContext;
    }



    public double distance(Location currentUser, Location friend){
        double theta = currentUser.getLongitude() - friend.getLongitude();
        double dist = Math.sin(deg2rad(currentUser.getLatitude())) * Math.sin(deg2rad(friend.getLatitude()))
                * Math.cos(deg2rad(currentUser.getLongitude()))* Math.cos(deg2rad(friend.getLongitude()))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 1.609344 * 1000; // Convert miles to meters
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
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

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
                        Intent intent = new Intent(MapTracking.this, ProductActivity.class);
                        startActivity(intent);
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

    @Override
    public void onBackPressed() {
        if (isDealButtonVisible) {
            hideDealButton();
        } else {
                }
        setResult(Activity.RESULT_OK);
        finish();
    }
}