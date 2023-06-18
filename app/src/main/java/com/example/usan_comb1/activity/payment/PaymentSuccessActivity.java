package com.example.usan_comb1.activity.payment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.usan_comb1.R;
import com.example.usan_comb1.activity.MainActivity;
import com.example.usan_comb1.fragment.HomeFragment;

public class PaymentSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_success_activity);

        Button goToListButton = findViewById(R.id.goToListButton);
        goToListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // HomeFragment로 이동
                Intent intent = new Intent(PaymentSuccessActivity.this, MainActivity.class);
                startActivity(intent);

                finish();
            }
        });
    }
}