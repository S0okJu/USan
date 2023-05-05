package com.example.usan_comb1.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.usan_comb1.FavoriteProduct;
import com.example.usan_comb1.R;
import com.example.usan_comb1.adapter.FavoriteAdapter;
import com.example.usan_comb1.adapter.HomeAdapter;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FavoriteActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FavoriteAdapter adapter;
    private ArrayList<FavoriteProduct> dataArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        dataArrayList = new ArrayList<>();
        // SharedPreferences에서 데이터를 가져와서 ArrayList에 저장
        SharedPreferences sharedPreferences = getSharedPreferences("my_prefs", MODE_PRIVATE);
        Set<String> data = sharedPreferences.getStringSet("my_list_key", new HashSet<>());
        for (String item : data) {
            Gson gson = new Gson();
            FavoriteProduct product = gson.fromJson(item, FavoriteProduct.class);
            dataArrayList.add(product);
        }

        adapter = new FavoriteAdapter(this, dataArrayList);
        recyclerView.setAdapter(adapter);
    }
}