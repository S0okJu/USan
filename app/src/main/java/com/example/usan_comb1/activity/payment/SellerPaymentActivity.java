package com.example.usan_comb1.activity.payment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.usan_comb1.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SellerPaymentActivity extends AppCompatActivity {

    private DatabaseReference payRef;
    private String chatId;
    private ProgressBar progressBar;
    public DatabaseReference transRef;
    /*
     * ERROR! - 현재 Activity가 넘어가지 않는 문제를 가지고 있습니다.
     *   - 혹은 가능하더라도 success로 넘어가지 않음.
     *  추후 해결 예정
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seller_payment_activity);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        init();
        checkPaymentAct();

    }
    private void init(){
        chatId = "chat_" + getIntent().getStringExtra("productId");
        payRef = FirebaseDatabase.getInstance().getReference().child("payments");
        transRef = FirebaseDatabase.getInstance().getReference().child("transaction");
        transRef.child(chatId).child("sellerStatus").setValue(true); // 상태값 업데이트
    }

    private void checkPaymentAct(){
        // 지불에 대한 값을 Firebase에 저장합니다.
        transRef.child(chatId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean sellerStatus = dataSnapshot.child("sellerStatus").getValue(Boolean.class);
                Boolean buyerStatus = dataSnapshot.child("buyerStatus").getValue(Boolean.class);
                System.out.println("sellerStatus : "+ sellerStatus);
                if (dataSnapshot.exists()&&sellerStatus != null && buyerStatus != null && sellerStatus && buyerStatus  ) {

                    Toast.makeText(SellerPaymentActivity.this, "Payment Success", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(SellerPaymentActivity.this, PaymentSuccessActivity.class);
                    startActivity(intent);
                }else{
                        progressBar.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SellerPaymentActivity.this, "Payment Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }


}