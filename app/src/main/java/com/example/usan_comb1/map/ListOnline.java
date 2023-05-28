package com.example.usan_comb1.map;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.Toolbar;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usan_comb1.R;

import com.example.usan_comb1.map.Tracking;
import com.example.usan_comb1.response.LoginResponse;
import com.firebase.ui.auth.ui.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ListOnline extends AppCompatActivity  implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //Firebase
    DatabaseReference onlineRef, currentUserRef, counterRef, locations;
    FirebaseRecyclerAdapter<LoginResponse, ListOnlineViewHolder> adapter;

    //View
    RecyclerView listOnline;
    RecyclerView.LayoutManager layoutManager;

    //Location
    private static final int MY_PERMISSION_REQUEST_CODE = 7171;
    private static final int PLAY_SERVICES_RES_REQUEST = 7172;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISTANCE = 10;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_online);

        //Init views
        listOnline = (RecyclerView) findViewById(R.id.listOnline);
        listOnline.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        listOnline.setLayoutManager(layoutManager);

        //Set toolbar and Logout / Join menu
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle("EDMT Presence System");
        setSupportActionBar(toolbar);

        //Firebase
        locations = FirebaseDatabase.getInstance().getReference("Locations");
        onlineRef = FirebaseDatabase.getInstance().getReference().child("./info/connected");
        counterRef = FirebaseDatabase.getInstance().getReference("lastOnline");
        currentUserRef = FirebaseDatabase.getInstance().getReference("lastOnline")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            },MY_PERMISSION_REQUEST_CODE);
        }
        else {
            if(checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }

        setupSystem();
        updateList();
    }

    private void setSupportActionBar(Toolbar toolbar) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
            }
        }
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation != null) {
            // Update to Firebase
            locations.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(new Tracking(FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                            FirebaseAuth.getInstance().getCurrentUser().getUid(),
                            String.valueOf(mLastLocation.getLatitude()),
                            String.valueOf(mLastLocation.getLongitude())));
        }
        else {
            Toast.makeText(this, "위치를 얻을 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }


    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(DISTANCE);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS) {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RES_REQUEST).show();
            } else {
                Toast.makeText(this, "이 기기에서 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void updateList() {
        FirebaseRecyclerOptions<LoginResponse> options =
                new FirebaseRecyclerOptions.Builder<LoginResponse>()
                        .setQuery(counterRef, LoginResponse.class)
                        .build();


        adapter = new FirebaseRecyclerAdapter<LoginResponse,ListOnlineViewHolder>(options){
            @NonNull
            @Override
            public ListOnlineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return null;
            }

            @Override
            protected void onBindViewHolder(@NonNull ListOnlineViewHolder viewHolder, int position, @NonNull LoginResponse model) {
                viewHolder.txtEmail.setText(model.getEmail());

                viewHolder.itemClickListener = new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        // If model is current user, note set click event
                        if(!model.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                            Intent map = new Intent(ListOnline.this, MapTracking.class);
                            map.putExtra("email", model.getEmail());
                            map.putExtra("lat",mLastLocation.getLatitude());
                            map.putExtra("lng",mLastLocation.getLongitude());
                            startActivity(map);
                        }
                    }
                };
            }
        };
        adapter.notifyDataSetChanged();
        listOnline.setAdapter(adapter);
    }

    private void setupSystem() {
        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Boolean.class)) {
                    currentUserRef.onDisconnect().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLastLocation = location;
        displayLocation();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }
}