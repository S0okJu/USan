package com.example.usan_comb1.map;

import static com.example.usan_comb1.utilities.Constants.BUYER;
import static com.example.usan_comb1.utilities.Constants.SELLER;
import static com.example.usan_comb1.utilities.CustomMath.checkDistDifference;
import static com.example.usan_comb1.utilities.CustomMath.extractProductIdFromChatId;
import static com.example.usan_comb1.utilities.CustomMath.getDistanceBetween;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.activity.BuyerPaymentActivity;
import com.example.usan_comb1.activity.SellerPaymentActivity;
import com.example.usan_comb1.models.Loc;
import com.example.usan_comb1.utilities.PreferenceManager;
import com.google.android.gms.internal.location.zzq;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

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

    private ProductService service;
    private LatLng dest;
    private DatabaseReference locationRef;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
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

        if(getIntent() != null){
            chatId = getIntent().getStringExtra("chatId");
            otherUser = getIntent().getStringExtra("otherUser");
            role = getIntent().getIntExtra("role",-1);
        }
        curUsername = preferenceManager.getString("username");

        // Ref to firebase first
        locationRef = FirebaseDatabase.getInstance().getReference("locations").child(chatId);

        getDest();
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

                    Location destLocation = new Location("DestLocation");
                    destLocation.setLatitude(dest.latitude);
                    destLocation.setLongitude(dest.longitude);

                    // 거리를 계산해주는 함수
                    // 현재 사용자 - 최종 목적지에 대한 거리차를 계산해줍니다. - @D7MeKz
                    CalcDestDistance(location, destLocation);

                }

                locationRef.child(otherUser).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("lat") && dataSnapshot.hasChild("lng")) {
                            Double lat = (Double) dataSnapshot.child("lat").getValue();
                            Double lng = (Double) dataSnapshot.child("lng").getValue();
                            LatLng otherLocation = new LatLng(lat, lng);

                             if (markers.get(otherUser) != null) {
                                markers.get(otherUser).remove();
                            }

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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    // 지도에 정보를 marking하는 함수
    private void loadLocationForThisUser(String username) {
        locationRef.child(username).addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapShot:dataSnapshot.getChildren()){
                    Tracking tracking =  postSnapShot.getValue(Tracking.class);

                    LatLng friendLocation = new LatLng(Double.parseDouble(tracking.getLat()), Double.parseDouble((tracking.getLng())));
                    Location currentUser = new Location("");
                    currentUser.setLatitude(lat);
                    currentUser.setLongitude(lng);

                    Location friend = new Location("");
                    friend.setLatitude(Double.parseDouble(tracking.getLat()));
                    friend.setLongitude(Double.parseDouble(tracking.getLng()));

                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(friendLocation).title(tracking.getEmail()).snippet("Distance "+ new DecimalFormat("#.#").format(getDistanceBetween(currentUser, friend))).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),12.0f));
                }

                LatLng current = new LatLng(lat,lng);
                mMap.addMarker(new MarkerOptions().position(current).title(FirebaseAuth.getInstance().getCurrentUser().getEmail()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    /*
    * Function
    * 거리를 가져오는 함수
    * */
    public void getDest(){

        Call<Loc> call = service.getDestLocation(chatId);
        dest = new LatLng(0.0,0.0);
        call.enqueue(new Callback<Loc>() {
            @Override
            public void onResponse(Call<Loc> call, Response<Loc> response) {
                if(response.isSuccessful()){
                    Loc result = response.body();
                    System.out.println(result.getLat());
                    dest = new LatLng(result.getLat(), result.getLng());

                    Marker destMarker = mMap.addMarker(new MarkerOptions().position(dest).title("Dest"));
                    markers.put("dest",destMarker);
                }
            }
            @Override
            public void onFailure(Call<Loc> call, Throwable t) {
                Log.d("Dest Location", "Failed to load dest location");
                mMap.addMarker(new MarkerOptions().position(new LatLng(0.0,0.0)).title("Dest"));
            }
        });

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    public void CalcDestDistance(Location user, Location dest){

        boolean check = checkDistDifference(user,dest); //
        if(check==true){
            zzq mFusedLocationClient;
            fusedLocationProviderClient.removeLocationUpdates(locationCallback); // 위치 업데이트 해제
            String productId = extractProductIdFromChatId(chatId);

            // 거리가 특정 범위 이내에 있으면 역할에 따른 Activity로 전송됩니다.
            if(role == BUYER){
                Intent intent = new Intent(getApplicationContext(), BuyerPaymentActivity.class);
                intent.putExtra("role",role);
                intent.putExtra("productId",productId);
                startActivity(intent);
            }else if(role==SELLER){
                Intent intent = new Intent(getApplicationContext(), SellerPaymentActivity.class);
                intent.putExtra("role",role);
                intent.putExtra("productId",productId);
                startActivity(intent);
            }
        }
        finish();
    }
}