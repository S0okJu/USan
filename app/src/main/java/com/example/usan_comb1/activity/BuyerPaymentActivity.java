package com.example.usan_comb1.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.response.PaymentProductResponse;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuyerPaymentActivity extends AppCompatActivity {
    private ProductService service;
    private String productId;
    public int role;
    private String accessToken;

    // TODO 추후 추가할 예정
//    private ImageView productImage;
    private TextView productName;
    private TextView productAuthor;
    private TextView productPrice;
    private Button paymentBtn;
    private ProgressBar progressBar;
    public DatabaseReference transRef;

/*
* 초반에 seller, buyer status 값을 false로 지정한 후 해당 Activity에 접속하면 true로 변경하도록 한다.
* */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buyer_payment_activity);

        //setup Layout
        productName = findViewById(R.id.product_name);
        productAuthor= findViewById(R.id.seller_name);
        productPrice = findViewById(R.id.price);
        // productImage = findViewById(R.id.product_image);
        paymentBtn = findViewById(R.id.payment_btn);
        progressBar = findViewById(R.id.progressBar);

        // setup
        init();
        showProductInfo();
    }

    private void init(){
        productId = getIntent().getStringExtra("productId");
        role = getIntent().getIntExtra("role",-1);
        service = RetrofitClient.getRetrofitInstance().create(ProductService.class);
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");

        transRef = FirebaseDatabase.getInstance().getReference().child("transaction");
        transRef.child("chat_"+productId).child("buyerStatus").setValue(true);
    }

    private void showProductInfo() {
        Call<PaymentProductResponse> call = service.getPaymentProduct(accessToken, Integer.valueOf(productId));

        //
        progressBar.setVisibility(View.VISIBLE);

        call.enqueue(new Callback<PaymentProductResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaymentProductResponse> call, @NonNull Response<PaymentProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PaymentProductResponse result = response.body();

                    // TODO 최종 결제 시 상품 목록을 호출하고자 했습니다.
                    //  만약에 필요없다고 생각하시면 삭제하셔도 됩니다.
                    //  !참고 - 이를 호출하는 함수가 따로 있음 -> /pinfo
                    productName.setText(result.getTitle());
                    productAuthor.setText(result.getAuthor());
                    productPrice.setText(String.valueOf(result.getPrice()));

                    // buyer가 접속 중이면 "구매하기" 버튼이 보이도록 설정했습니다.
                    transRef.child("chat_" + productId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                boolean buyerStatus = dataSnapshot.child("buyerStatus").getValue(Boolean.class);
                                if (buyerStatus) {
                                    paymentBtn.setVisibility(View.VISIBLE);
                                }

                                paymentBtn.setOnClickListener(view -> initiatePayment());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
            @Override
            public void onFailure(Call<PaymentProductResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }



    private void initiatePayment() {
        paymentBtn.setEnabled(false);

        // 지불과 관련된 API를 호출합니다.
        // 지불하는 사람, 상품이 명확하게 정해져있기 때문에 많은 정보가 필요없습니다.
        // 나머지는 서버가 처리하도록 구현했습니다.
        Call<Void> call1 = service.getBuyerPayment(accessToken, "chat_" + productId);
        call1.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call1, @NonNull Response<Void> response1) {
                if (response1.isSuccessful()) {
                    Toast.makeText(BuyerPaymentActivity.this, "Payment Success", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(BuyerPaymentActivity.this, PaymentSuccessActivity.class); // 성공 페이지로 이동
                    startActivity(intent);
                } else {
                    Toast.makeText(BuyerPaymentActivity.this, "Payment Failed", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(BuyerPaymentActivity.this, PaymentFailActivity.class); // 실패 페이지로 이동
                    startActivity(intent);
                }
                paymentBtn.setEnabled(true);
            }

            @Override
            public void onFailure(@NonNull Call<Void> call1, @NonNull Throwable t) {
                Toast.makeText(BuyerPaymentActivity.this, "Payment Failed", Toast.LENGTH_SHORT).show();
                paymentBtn.setEnabled(true);
            }
        });
    }




}
