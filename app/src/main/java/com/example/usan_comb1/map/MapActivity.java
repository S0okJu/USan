package com.example.usan_comb1.map;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.usan_comb1.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.AppCheckProvider;
import com.google.firebase.appcheck.AppCheckProviderFactory;
import com.google.firebase.appcheck.AppCheckToken;

import java.util.Arrays;

public class MapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Skip login code

        Intent intent = new Intent(MapActivity.this, ListOnline.class);
        startActivity(intent);
        finish();
    }
}

    /*

    public class YourCustomAppCheckToken extends AppCheckToken {
        private String token;
        private long expiration;

        YourCustomAppCheckToken(String token, long expiration) {
            this.token = token;
            this.expiration = expiration;
        }

        @NonNull
        @Override
        public String getToken() {
            return token;
        }

        @Override
        public long getExpireTimeMillis() {
            return expiration;
        }
    }

    public class YourCustomAppCheckProvider implements AppCheckProvider {
        private FirebaseApp firebaseApp;

        public YourCustomAppCheckProvider(FirebaseApp firebaseApp) {
            this.firebaseApp = firebaseApp;
        }

        @Override
        public Task<AppCheckToken> getToken() {
            // Logic to exchange proof of authenticity for an App Check token and
            //   expiration time.
            // ...

            // Get expirationFromServer and tokenFromServer from server or any other source
            long expirationFromServer = getExpirationFromServer(); // Replace with the actual value from the server
            String tokenFromServer = getTokenFromServer(); // Replace with the actual value from the server

            // Refresh the token early to handle clock skew.
            long expMillis = expirationFromServer * 1000 - 60000;

            // Create AppCheckToken object.
            AppCheckToken appCheckToken = new YourCustomAppCheckToken(tokenFromServer, expMillis);

            return Tasks.forResult(appCheckToken);
        }
    }

    public class YourCustomAppCheckProviderFactory implements AppCheckProviderFactory {
        @Override
        public AppCheckProvider create(FirebaseApp firebaseApp) {
            // Create and return an AppCheckProvider object.
            return new YourCustomAppCheckProvider(firebaseApp);
        }
    }

    // Example methods to get expiration and token from server
    private long getExpirationFromServer() {
        // Implement the code to get expiration from the server
        // For example, make an HTTP request to the server and parse the response
        // to extract the expiration value.
        // Return the obtained expiration value.

        // Replace the following line with the actual code to get the expiration from the server
        long expiration = 0; // Assuming the expiration value is 0 for illustration

        return expiration;
    }

    private String getTokenFromServer() {
        // Implement the code to get the token from the server
        // For example, make an HTTP request to the server and parse the response
        // to extract the token value.
        // Return the obtained token value.

        // Replace the following line with the actual code to get the token from the server
        String token = "your_token_from_server";

        return token;
    }

     */
