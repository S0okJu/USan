package com.example.usan_comb1.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.adapter.FavoriteAdapter;
import com.example.usan_comb1.response.PostList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 관심목록
public class FavoriteActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FavoriteAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private String userName;
    private Integer productId;

    NestedScrollView nestedScrollView;
    ProgressBar progressBar;

    ArrayList<PostList> dataArrayList = new ArrayList<>();

    private ProductService mProductService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        nestedScrollView = findViewById(R.id.scroll_view);
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);

        adapter = new FavoriteAdapter(FavoriteActivity.this, dataArrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Authorization
        SharedPreferences prefs =getSharedPreferences("auth", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access_token", "");

        // CustomAdapter의 아이템 클릭 리스너를 설정합니다.
        adapter.setOnItemClickListener(new FavoriteAdapter.OnItemClickListener() {
            // 아이템을 클릭했을 때 다른 액티비티로 넘어가는 코드를 추가합니다.
            public void onItemClick(int position, PostList data) {
                Intent intent = new Intent(FavoriteActivity.this, DetailActivity.class);
                intent.putExtra("product_id", data.getProduct_id()); // 넘어갈 데이터를 인텐트에 추가합니다.
                startActivity(intent);
            }
        });



        getData(accessToken);

        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener()
        {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY)
            {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())
                {
                    progressBar.setVisibility(View.VISIBLE);
                    getData(accessToken);
                }
            }
        });

    }

    private void getData(String accessToken)
    {


        ProductService productService = RetrofitClient.getProductService();
        //ERROR : page는 어떻게?
        Call<String> call = productService.string_favorite(accessToken, userName);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    progressBar.setVisibility(View.GONE);
                    try
                    {
                        JSONArray jsonArray = new JSONArray(response.body());
                        parseResult(jsonArray);
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e("에러 : ", t.getMessage());
            }
        });
    }

    private void parseResult(JSONArray jsonArray)
    {
        for (int i = 0; i < jsonArray.length(); i++)
        {
            try
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                PostList data = new PostList();
                data.setImg(jsonObject.getString("img"));
                data.setTitle(jsonObject.getString("title"));
                data.setAuthor(jsonObject.getString("author"));
                data.setProduct_id(jsonObject.getInt("product_id"));
                dataArrayList.add(data);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        adapter.notifyDataSetChanged(); // Adapter에 데이터 변경을 알려줌
    }
}