package com.example.usan_comb1.activity.map;

import static com.example.usan_comb1.utilities.Constants.BUYER;
import static com.example.usan_comb1.utilities.Constants.SELLER;
import static com.example.usan_comb1.utilities.CustomMath.checkDistDifference;
import static com.example.usan_comb1.utilities.CustomMath.extractProductIdFromChatId;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
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

    public void getDest(){

        Call<Loc> call = service.getDestLocation(chatId);
        dest = new LatLng(0.0,0.0);
        call.enqueue(new Callback<Loc>() {

            @Override
            public void onResponse(Call<Loc> call, retrofit2.Response<Loc> response) {
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


    // 거리를 시각적으로 표현하기 위해 사용됨.
    private String callDirectionsAPI(Location origin, Location destination) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin.getLatitude() + "," + origin.getLongitude() +
                "&destination=" + destination.getLatitude() + "," + destination.getLongitude() +
                "&key=AIzaSyCoYvOJIo6-rpl-GJvxPa7SwtuCeQrXBw4";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    private String extractPolyline(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray routes = jsonObject.getJSONArray("routes");

            if (routes.length() > 0) {  // Check if the array has elements
                JSONObject route = routes.getJSONObject(0);
                JSONObject overview_polyline = route.getJSONObject("overview_polyline");
                String polyline = overview_polyline.getString("points");

                return polyline;
            } else {
                System.err.println("No routes available in the response.");
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

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



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

//
//    private void showDealButton() {
//        isDealButtonVisible = true;
//        dealButton.setVisibility(View.VISIBLE);
//    }
//
//    private void hideDealButton() {
//        isDealButtonVisible = false;
//        dealButton.setVisibility(View.GONE);
//    }
//


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
    public void CalcDestDistance(Location user, Location dest){
        boolean check = checkDistDifference(user,dest); //
        if(check==true){
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            String productId = extractProductIdFromChatId(chatId);
            System.out.println("productId : "+productId);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("거래 상대가 근처에 있습니다.")
                    .setMessage("결제창으로 넘어가시겠습니까?")
                    .setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 이 시점에서 dialog를 dismiss 합니다.
                            if(alertDialog != null && alertDialog.isShowing()){
                                alertDialog.dismiss();
                            }
                            System.out.println("My Log.. role " + role);
                            // 거리가 특정 범위 이내에 있으면 역할에 따른 Activity로 전송됩니다.
                            if(role == BUYER){
                                Intent intent = new Intent(getApplicationContext(), BuyerPaymentActivity.class);
                                intent.putExtra("role",role);
                                intent.putExtra("productId",productId);
                                startActivity(intent);
                                finish();
                            }else if(role==SELLER){
                                Intent intent = new Intent(getApplicationContext(), SellerPaymentActivity.class);
                                intent.putExtra("role",role);
                                intent.putExtra("productId",productId);
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

        } else {
            finish();
        }
    }
}