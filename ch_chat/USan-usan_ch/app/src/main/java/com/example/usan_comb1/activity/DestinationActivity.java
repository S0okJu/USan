package com.example.usan_comb1.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.response.GpsResponse;
import com.example.usan_comb1.response.PostResult;
import com.example.usan_comb1.response.RetroProduct;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

public class DestinationActivity extends AppCompatActivity {
    private static final String TAG = "DestinationActivity";

    private Socket mSocket;
    private TextView txtDistance_user, txtDistance_destination;  // 거리를 표시할 TextView
    private int product_id;
    private String username, accessToken;
    private double buyerLatitude, buyerLongitude, sellerLatitude, sellerLongitude, destinationLatitude, destinationLongitude;
    private ProductService mProductService;

    {
        try {
            mSocket = IO.socket("http://43.200.6.34:57663ㅕ");
            mSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination);

        // Authorization
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
        username = prefs.getString("username", "");

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        // 거리를 표시할 TextView 초기화
        txtDistance_user = findViewById(R.id.txtDistance_user);
        txtDistance_destination = findViewById(R.id.txtDistance_destination);

        // 소켓 통신 시작
        startConnection();

        // location_data 이벤트를 보내는 코드
        sendLocationData();

        // integrated_data 이벤트에 대한 응답을 수신하는 코드
        mSocket.on("integrated_data", onIntegratedData);
    }

    private void startConnection() {
        Call<GpsResponse> call = mProductService.getGps(accessToken, product_id);
        call.enqueue(new Callback<GpsResponse>() {
            @Override
            public void onResponse(Call<GpsResponse> call, Response<GpsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mSocket.connect();
                    GpsResponse gps_dest = response.body();
                    destinationLatitude = gps_dest.getLatitude();
                    destinationLongitude = gps_dest.getLongitude();
                } else {
                    showToast("통신이 불가능합니다.");
                }
            }

            @Override
            public void onFailure(Call<GpsResponse> call, Throwable t) {
                showToast("서버에 연결이 불가능합니다.");
            }
        });
    }

    private void finishConnection() {
        Call<Void> call = mProductService.finishGps(accessToken, product_id);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mSocket.disconnect();
                } else {
                    showToast("통신이 불가능합니다.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("서버에 연결이 불가능합니다.");
            }
        });
    }


    private void sendLocationData() {
        try {
            JSONObject locationData = new JSONObject();

            // room
            locationData.put("room", product_id);

            // username
            locationData.put("username", username);

            // location
            JSONObject location = new JSONObject();
            double latitude = 231.213; // Replace with actual latitude
            double longitude = 2312.2; // Replace with actual longitude
            location.put("latitude", latitude);
            location.put("longitude", longitude);
            locationData.put("location", location);

            // role
            if (username.equals(product_id)) {
                locationData.put("role", 0); // 구매자
            } else {
                locationData.put("role", 1); // 판매자
            }

            // Emit 'location_data' event and send the JSON data to the server
            mSocket.emit("location_data", locationData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Emitter.Listener onIntegratedData = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (args[0] instanceof JSONObject) {
                JSONObject integratedData = (JSONObject) args[0];

                try {
                    // 수신한 데이터 처리
                    int room = integratedData.getInt("room");
                    JSONObject buyer = integratedData.getJSONObject("buyer");
                    String buyerUsername = buyer.getString("username");
                    JSONObject buyerLocation = buyer.getJSONObject("location");
                    buyerLatitude = buyerLocation.getDouble("latitude");
                    buyerLongitude = buyerLocation.getDouble("longitude");

                    JSONObject seller = integratedData.getJSONObject("seller");
                    String sellerUsername = seller.getString("username");
                    JSONObject sellerLocation = seller.getJSONObject("location");
                    sellerLatitude = sellerLocation.getDouble("latitude");
                    sellerLongitude = sellerLocation.getDouble("longitude");

                    // 사용자의 위치 정보를 사용하여 거리를 계산합니다.
                    double distance = calculateDistance(buyerLatitude, buyerLongitude, sellerLatitude, sellerLongitude);

                    // 거리를 TextView에 표시합니다.
                    updateDistance_user(distance);

                    // 데이터 사용 예시
                    Log.d(TAG, "Room: " + room);
                    Log.d(TAG, "Buyer Username: " + buyerUsername);
                    Log.d(TAG, "Buyer Latitude: " + buyerLatitude);
                    Log.d(TAG, "Buyer Longitude: " + buyerLongitude);
                    Log.d(TAG, "Seller Username: " + sellerUsername);
                    Log.d(TAG, "Seller Latitude: " + sellerLatitude);
                    Log.d(TAG, "Seller Longitude: " + sellerLongitude);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    // 두 지점 사이의 거리를 계산하는 메소드 (예시: Haversine Formula 활용)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371; // 지구 반지름 (단위: km)

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    // 거리를 업데이트하여 TextView에 표시하는 메소드
    private void updateDistance_user(double distance_user) {
        // 거리를 TextView에 표시하는 로직 작성

        txtDistance_user.setText(String.format(Locale.getDefault(), "사용자와의 거리: %.2f km", distance_user));
    }

    // 거리를 업데이트하여 TextView에 표시하는 메소드
    private void updateDistance_destination(double distance_destination) {
        // 거리를 TextView에 표시하는 로직 작성

        txtDistance_destination.setText(String.format(Locale.getDefault(), "목적지와의 거리: %.2f km", distance_destination));
    }

    // 토스트 메시지를 출력하는 메서드
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}