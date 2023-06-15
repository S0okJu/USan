package com.example.usan_comb1.activity.product;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.activity.product.DetailActivity;
import com.example.usan_comb1.adapter.FavoriteAdapter;
import com.example.usan_comb1.adapter.RecyclerViewEmptySupport;
import com.example.usan_comb1.response.FavoriteProduct;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 관심목록
public class FavoriteActivity extends AppCompatActivity {

    private static final String TAG = "Favorite_Activity";

    private RecyclerViewEmptySupport recyclerView;
    private FavoriteAdapter adapter;
    private TextView empty;
    NestedScrollView nestedScrollView;
    ProgressBar progressBar;

    private List<FavoriteProduct> favoriteList;
    //ArrayList<FavoriteProduct> dataArrayList = new ArrayList<>();

    private ProductService mProductService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        favoriteList = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_view);
        nestedScrollView = findViewById(R.id.scroll_view);
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        empty = findViewById(R.id.empty_view);

        //adapter = new FavoriteAdapter(this, dataArrayList);
        //recyclerView.setAdapter(adapter);

        adapter = new FavoriteAdapter(this, favoriteList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setEmptyView(empty);

        // Authorization
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access_token", "");
        String username = prefs.getString("username", "");

        // CustomAdapter의 아이템 클릭 리스너를 설정합니다.
        adapter.setOnItemClickListener(new FavoriteAdapter.OnItemClickListener() {
            // 아이템을 클릭했을 때 다른 액티비티로 넘어가는 코드를 추가합니다.
            public void onItemClick(int position, FavoriteProduct data) {
                Intent intent = new Intent(FavoriteActivity.this, DetailActivity.class);
                intent.putExtra("product_id", data.getProductId()); // 넘어갈 데이터를 인텐트에 추가합니다.
                startActivity(intent);
            }
        });

        int page = 1;
        Call<List<FavoriteProduct>> call = mProductService.favorite_list(accessToken, username, page);
        call.enqueue(new Callback<List<FavoriteProduct>>() {
            @Override
            public void onResponse(Call<List<FavoriteProduct>> call, Response<List<FavoriteProduct>> response) {
                if (response.isSuccessful()) {
                    List<FavoriteProduct> favoriteProducts = response.body();
                    if (favoriteProducts != null) {
                        Log.d(TAG, "Received " + favoriteProducts.size() + " favoriteProducts from server.");
                        favoriteList.clear();
                        favoriteList.addAll(favoriteProducts);
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "Received null product list from server.");
                    }
                } else {
                    Toast.makeText(FavoriteActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<List<FavoriteProduct>> call, Throwable t) {

            }
        });

    }

}

/*
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
        int page = 1;
        ProductService productService = RetrofitClient.getProductService();
        Call<List<FavoriteProduct>> call = productService.string_favorite(accessToken, username, page);
        call.enqueue(new Callback<List<FavoriteProduct>>()
        {
            @Override
            public void onResponse(Call<List<FavoriteProduct>> call, Response<List<FavoriteProduct>> response)
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
            public void onFailure(Call<List<FavoriteProduct>> call, Throwable t)
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
                FavoriteProduct data = new FavoriteProduct();
                data.setImg(jsonObject.getString("img"));
                data.setTitle(jsonObject.getString("title"));
                data.setAuthor(jsonObject.getString("author"));
                data.setProductId(jsonObject.getInt("product_id"));
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
}
 */