package com.example.usan_comb1.map;

import android.app.Activity;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.usan_comb1.R;
import com.example.usan_comb1.interfaces.MyCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapTracking extends AppCompatActivity implements OnMapReadyCallback {

    private static MapTracking instance;
    private GoogleMap mMap;

    private HashMap<String, Marker> markers = new HashMap<>();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Polyline distanceLine; // 둘 사이의 거리를 나타내기 위해 사용됨
    private String username;
    private String otherUser;
    DatabaseReference locationRef; // 이름이 헷갈려서 ref로 변경합니다. - D7MEKZ
    double lat, lng;
    String TAG = "MapTracking";

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
        locationRef = FirebaseDatabase.getInstance().getReference("locations");

        if (getIntent() != null) {
            username = getIntent().getStringExtra("username");
        }

//        String chatId = getIntent().getStringExtra("chatId");
        String chatId = "chat_50";
//        getOtherUser(chatId, new MyCallback() {
//            @Override
//            public void onCallback(String value) {
//                otherUser = value;
//                Log.d(TAG, "Other user: " + otherUser);
//            }
//        });

        if (!username.equals("helloworld2")){
            otherUser = "helloworld2";
        }else{
            otherUser = "helloworld3";
        }

//        // 지속적인 위치 추적을 위해
//        locationRequest = LocationRequest.create();
//        locationRequest.setInterval(10000); // 10 seconds
//        locationRequest.setFastestInterval(5000); // 5 seconds
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//        locationCallback = new LocationCallback(){
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                super.onLocationResult(locationResult);
//                if(locationResult == null){
//                    return ;
//                }
//
//                for (Location location : locationResult.getLocations()) {
//                    if (location != null) {
//                        // updateLocationInFirebase(location);
//                    }
//                }
//            }
//        };

        if (!TextUtils.isEmpty(username)) {
            locationMarking(chatId, username, otherUser);
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
    private void locationMarking(String chatId, String username, String other_username) {
        // Query user_location = locationRef.child(chatId).orderByKey().equalTo(username);

        Location currentUser = new Location(""); // Declare currentUser variable outside the ValueEventListener
        Location friend = new Location(""); // Declare friend variable outside the ValueEventListener

        locationRef.child(chatId).child(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot postSnapShot) {
                mMap.clear(); // Clear old markers

                double lat = postSnapShot.child("lat").getValue(Double.class);
                double lng = postSnapShot.child("lng").getValue(Double.class);
                Tracking tracking = new Tracking(lat, lng);

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

        locationRef.child(chatId).child(other_username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                double lat = dataSnapshot.child("lat").getValue(Double.class);
                double lng = dataSnapshot.child("lng").getValue(Double.class);
                Tracking tracking = new Tracking(lat, lng);

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

        Marker friendMarker = markers.get(otherUser);
        if (friendMarker != null) {
            friendMarker.setSnippet("Distance: " + distanceString);
        }

        // Execute the Directions API request in a new thread
        new Thread(() -> {
            String response = callDirectionsAPI(currentUser, friend);

            if(response == null) {
                Log.d(TAG, "API CALL NULL");
                return;
            }

            // Extract the polyline from the response
            String polyline = extractPolyline(response);

            if(polyline == null) {

                return;
            }

            List<LatLng> latLngs = PolyUtil.decode(polyline);

            runOnUiThread(() -> {
                if(distanceLine != null) {
                    distanceLine.remove();
                }

                // Draw the new polyline
                distanceLine = mMap.addPolyline(new PolylineOptions()
                        .addAll(latLngs)
                        .width(10)
                        .color(Color.RED));
            });
        }).start();

        /*
        // Check if the distance is within 5 meters
        if (distance <= 5 && !isDealButtonVisible) {
            showDealButton();
        } else if (distance > 5 && isDealButtonVisible) {
            hideDealButton();
        }
         */
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



    private void getOtherUser(String chatId, MyCallback myCallback){
        DatabaseReference transRef = FirebaseDatabase.getInstance().getReference("transaction").child(chatId);
        transRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String sellerName = snapshot.child("sellerName").getValue(String.class);
                String buyerName = snapshot.child("buyerName").getValue(String.class);
                if(sellerName!=null && sellerName.equals(username)) {
                    myCallback.onCallback(buyerName);
                }else if (buyerName != null) {
                    myCallback.onCallback(sellerName);
                } else {
                    myCallback.onCallback(null); // Null 처리 추가
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                System.out.println(error);
                otherUser = "ErrorUser";
            }
        });
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