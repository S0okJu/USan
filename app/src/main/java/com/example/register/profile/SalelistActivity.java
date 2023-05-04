package com.example.register.profile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.example.register.R;
import com.example.register.RetrofitClient;
import com.example.register.ServiceApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SalelistActivity extends AppCompatActivity {
    private static final String TAG = "Salelist_Activity";

    private RecyclerView recyclerView;
    private CustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salelist);

        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access_token", "");

        ServiceApi service = RetrofitClient.getClient().create(ServiceApi.class);


        // Intent로부터 값을 가져옴
        // Intent로 데이터를 전달합니다.
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");


        int page_per = 10;
        int page = 1;
        Call<List<RetroProduct>> call = service.getProductList("Bearer " + accessToken, username, page_per, page);
        call.enqueue(new Callback<List<RetroProduct>>() {
            @Override
            public void onResponse(Call<List<RetroProduct>> call, Response<List<RetroProduct>> response) {
                if (response.isSuccessful()) {
                    generateDataList(response.body());
                } else {
                    Toast.makeText(SalelistActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RetroProduct>> call, Throwable t) {
                Toast.makeText(SalelistActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateDataList(List<RetroProduct> productList) {
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new CustomAdapter(this, productList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(SalelistActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(null);
    }
}
