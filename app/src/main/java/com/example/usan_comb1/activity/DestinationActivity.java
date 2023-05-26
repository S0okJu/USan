package com.example.usan_comb1.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.usan_comb1.R;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Locale;

public class DestinationActivity extends AppCompatActivity {
    private Socket mSocket;

    {
        try {
            mSocket = IO.socket("http://13.124.53.124:56336");
            mSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private double destinationLatitude;  // 목적지의 위도
    private double destinationLongitude; // 목적지의 경도

    private TextView txtDistance;  // 거리를 표시할 TextView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination);

        // 거리를 표시할 TextView 초기화
        txtDistance = findViewById(R.id.txtDistance);

        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on("location-update", onLocationUpdate);
    }

    // Socket 서버에 connect 된 후, 서버로부터 전달받은 'Socket.EVENT_CONNECT' Event 처리.
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // 연결이 성공하면 서버에 목적지 정보를 요청할 수 있습니다.
            requestDestinationInfo();
        }
    };

    // 서버로부터 전달받은 'location-update' Event 처리.
    private Emitter.Listener onLocationUpdate = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // 전달받은 데이터는 아래와 같이 추출할 수 있습니다.
            JSONObject locationData = (JSONObject) args[0];

            try {
                double userLatitude = locationData.getDouble("latitude");   // 사용자의 위도
                double userLongitude = locationData.getDouble("longitude"); // 사용자의 경도

                // 사용자의 위치 정보와 목적지 정보를 사용하여 거리를 계산합니다.
                double distance = calculateDistance(userLatitude, userLongitude, destinationLatitude, destinationLongitude);

                // 거리를 TextView에 표시합니다.
                updateDistance(distance);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    // 서버에 목적지 정보를 요청하는 메소드
    private void requestDestinationInfo() {
        // 서버에 목적지 정보 요청하는 로직 작성
        // ...

        // 목적지 정보를 받았다고 가정하고 아래와 같이 목적지의 위도와 경도를 설정합니다.
        destinationLatitude = 37.1234;     // 목적지의 위도 설정
        destinationLongitude = 127.5678;   // 목적지의 경도 설정
    }

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
    private void updateDistance(double distance) {
        // 거리를 TextView에 표시하는 로직 작성
        // ...
        txtDistance.setText(String.format(Locale.getDefault(), "Distance: %.2f km", distance));
    }
}