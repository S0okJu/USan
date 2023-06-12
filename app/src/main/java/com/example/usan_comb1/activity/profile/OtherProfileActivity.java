package com.example.usan_comb1.activity.profile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.usan_comb1.R;

public class OtherProfileActivity extends AppCompatActivity {
    private TextView othername;
    private ImageView otherprofile;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);

        othername = findViewById(R.id.othername);

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
        }

        othername.setText(username);
    }
}