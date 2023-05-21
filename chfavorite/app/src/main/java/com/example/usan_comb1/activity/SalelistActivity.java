package com.example.usan_comb1.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.adapter.SalelistAdapter;
import com.example.usan_comb1.response.RetroProduct;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// UserFragment에 존재하는 판매 내역
public class SalelistActivity extends AppCompatActivity {
    private static final String TAG = "Salelist_Activity";

    private RecyclerView recyclerView;
    private SalelistAdapter adapter;
    private List<RetroProduct> productList;
    private ProductService mProductService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salelist);

        recyclerView = findViewById(R.id.recyclerView);

        // productList를 초기화합니다.
        productList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        adapter = new SalelistAdapter(this, productList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access_token", "");

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        // Intent로부터 값을 가져옴
        //Intent intent = getIntent();
        // String username = intent.getStringExtra("username");

        String username = prefs.getString("username", "");

        int page_per = 10;
        int page = 1;
        Call<List<RetroProduct>> call = mProductService.getProductList(accessToken, username, page_per, page);
        call.enqueue(new Callback<List<RetroProduct>>() {
            @Override
            public void onResponse(Call<List<RetroProduct>> call, Response<List<RetroProduct>> response) {
                if (response.isSuccessful()) {
                    List<RetroProduct> products = response.body();
                    System.out.println(products.get(0).toString());
                    if (products != null) {
                        Log.d(TAG, "Received " + products.size() + " products from server.");
                        productList.clear();
                        productList.addAll(products);
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "Received null product list from server.");
                    }
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
}