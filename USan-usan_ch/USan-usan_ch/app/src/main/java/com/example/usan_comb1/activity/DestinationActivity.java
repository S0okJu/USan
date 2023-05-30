package com.example.usan_comb1.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import com.example.usan_comb1.R;

import org.json.JSONObject;

import io.socket.client.Socket;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.emitter.Emitter;

public class DestinationActivity extends AppCompatActivity {
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://13.124.74.226:59557");
            mSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination);

        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on("chat-message", onMessageReceived);
    }

    // Socket서버에 connect 된 후, 서버로부터 전달받은 'Socket.EVENT_CONNECT' Event 처리.
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // your code...
        }
    };
    // 서버로부터 전달받은 'chat-message' Event 처리.
    private Emitter.Listener onMessageReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // 전달받은 데이터는 아래와 같이 추출할 수 있습니다.
            JSONObject receivedData = (JSONObject) args[0];
            // your code...
        }
    };
}