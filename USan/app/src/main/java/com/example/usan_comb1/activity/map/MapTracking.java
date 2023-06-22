package com.example.usan_comb1.activity.map;

import static com.example.usan_comb1.utilities.Constants.BUYER;
import static com.example.usan_comb1.utilities.Constants.SELLER;
import static com.example.usan_comb1.utilities.CustomMath.checkDistDifference;
import static com.example.usan_comb1.utilities.CustomMath.extractProductIdFromChatId;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.activity.payment.BuyerPaymentActivity;
import com.example.usan_comb1.activity.payment.SellerPaymentActivity;
import com.example.usan_comb1.models.Loc;
import com.example.usan_comb1.utilities.PreferenceManager;
import com.google.android.gms.common.api.ResolvableApiException;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class MapTracking extends AppCompatActivity implements OnMapReadyCallback {

    private static MapTracking instance;
    private GoogleMap mMap;
    private HashMap<String, Marker> markers = new HashMap<>();
    private String email;
    DatabaseReference locations;
    Double lat, lng;
    private String chatId;
    private String otherUser;

    private Button dealButton;
    private boolean isDealButtonVisible;

    private LatLng userLocation;

    private ProductService service;
    private LatLng dest;
    private DatabaseReference locationRef;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private PreferenceManager preferenceManager;
    private String curUsername;
    private int role;
    String TAG = "MapTracking";
    AlertDialog alertDialog;
    public String accessToken;

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    public static MapTracking getInstance() {
        return instance;
    }

    public static boolean isActive() {
        return instance != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map_tracking);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // FusedLocationProviderClient 객체 초기화
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        service = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        preferenceManager = new PreferenceManager(getApplicationContext());

        if (getIntent() != null) {
            chatId = getIntent().getStringExtra("chatId");
            otherUser = getIntent().getStringExtra("otherUser");
            role = getIntent().getIntExtra("role", -1);
        }
        curUsername = preferenceManager.getString("username");
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");

        // Ref to firebase first
        locationRef = FirebaseDatabase.getInstance().getReference("locations").child(chatId);

        // 임의로 사용자 LatLng 값 설정
        //userLocation = new LatLng(35.137759, 126.928947); // 사용자 위치 설정

        getDest();

        // 위치 업데이트 시작
        startLocationUpdates();


        //fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

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

                    Marker curUserMarker = mMap.addMarker(new MarkerOptions().position(current).title(curUsername).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    markers.put(curUsername, curUserMarker);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 14.0f));

                    latLng.put("lat", lat);
                    latLng.put("lng", lng);
                    locationRef.child(curUsername).updateChildren(latLng);
                    Log.d("curUser", "lat : " + lat);
                    Log.d("curUser", "lng : " + lng);

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

                            Log.d("otherUser", "lat : " + lat);
                            Log.d("otherUser", "lng : " + lng);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MapTracking", "Failed to read value.", error.toException());
                    }
                });
            }
        };

        dealButton = findViewById(R.id.deal_button);
        dealButton.setVisibility(View.GONE);

    }

    public void getDest() {
        Call<Loc> call = service.getDestLocation(accessToken, chatId);
        call.enqueue(new Callback<Loc>() {
            @Override
            public void onResponse(Call<Loc> call, retrofit2.Response<Loc> response) {
                if (response.isSuccessful()) {
                    Loc result = response.body();
                    double lat = result.getLat();
                    double lng = result.getLng();
                    dest = new LatLng(lat, lng);

                    Marker destMarker = mMap.addMarker(new MarkerOptions().position(dest).title("거래 장소").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

                    // 거리 정보를 마커에 표시
                    //destMarker.setSnippet("거리: " + checkDistDifference(, dest) + "m");
                    markers.put("dest", destMarker);

                    Log.d("Dest", "lat : " + lat);
                    Log.d("Dest", "lng : " + lng);

                }
            }

            @Override
            public void onFailure(Call<Loc> call, Throwable t) {
                Log.d("Dest Location", "Failed to load dest location");
                mMap.addMarker(new MarkerOptions().position(new LatLng(0.0, 0.0)).title("Dest"));

                // 이곳에서 위치 업데이트 시작
                startLocationUpdates();
            }
        });
    }



    // Directions API 요청을 보내고 응답을 처리하는 메소드

    // 인코딩된 폴리라인 데이터를 디코딩하여 LatLng 리스트로 변환하는 메소드
    private List<LatLng> decodePolyline(String encodedPath) {
        List<LatLng> path = new ArrayList<>();
        PolylineEncoding.decode(encodedPath).forEach(latLng -> path.add(new LatLng(latLng.lat, latLng.lng)));
        return path;
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (checkLocationPermission()) {
            checkSettingsAndStartLocationUpdates();
        } else {
            requestLocationPermission();
        }
    }

    private boolean checkLocationPermission() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkSettingsAndStartLocationUpdates();
            } else {
                // 위치 권한이 거부되었을 경우에 대한 처리
                // 사용자에게 위치 권한이 필요하다는 메시지를 표시하고, 권한 요청을 다시 수행할 수 있는 방법을 제공해야 합니다.
                Toast.makeText(this, "사용자 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void checkSettingsAndStartLocationUpdates() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();
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
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                if (ActivityCompat.checkSelfPermission(MapTracking.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapTracking.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MapTracking.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                    return;
                }
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MapTracking.this, REQUEST_LOCATION_PERMISSION);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        startLocationUpdates();

        //mMap.setMinZoomPreference(14.0f); // 최소 줌 레벨 설정

        //getDest(); // dest 변수 초기화
    }


    private void showDealButton() {
        isDealButtonVisible = true;
        dealButton.setVisibility(View.VISIBLE);
    }

    private void hideDealButton() {
        isDealButtonVisible = false;
        dealButton.setVisibility(View.GONE);
    }



    @Override
    public void onBackPressed() {

        if (isDealButtonVisible) {
            hideDealButton();
        }

        setResult(Activity.RESULT_OK);
        finish();
    }

    public void CalcDestDistance(Location user, Location dest) {
        boolean check = checkDistDifference(user, dest); //
        if (check == true) {
            showDealButton();

            dealButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    String productId = extractProductIdFromChatId(chatId);
                    System.out.println("productId : " + productId);

                    AlertDialog.Builder builder = new AlertDialog.Builder(MapTracking.this);
                    builder.setTitle("거래 상대가 근처에 있습니다.")
                            .setMessage("결제창으로 넘어가시겠습니까?")
                            .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 이 시점에서 dialog를 dismiss 합니다.
                                    if (alertDialog != null && alertDialog.isShowing()) {
                                        alertDialog.dismiss();
                                    }
                                    System.out.println("My Log.. role " + role);
                                    // 거리가 특정 범위 이내에 있으면 역할에 따른 Activity로 전송됩니다.
                                    if (role == BUYER) {
                                        Intent intent = new Intent(getApplicationContext(), BuyerPaymentActivity.class);
                                        intent.putExtra("role", role);
                                        intent.putExtra("productId", productId);
                                        startActivity(intent);
                                        finish();
                                    } else if (role == SELLER) {
                                        Intent intent = new Intent(getApplicationContext(), SellerPaymentActivity.class);
                                        intent.putExtra("role", role);
                                        intent.putExtra("productId", productId);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            })
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            });
                    alertDialog = builder.create();
                    alertDialog.show();

                } });
        }
    }
}