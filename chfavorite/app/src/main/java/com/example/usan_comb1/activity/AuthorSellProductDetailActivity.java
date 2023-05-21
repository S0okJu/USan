package com.example.usan_comb1.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.adapter.CardAdapter;
import com.example.usan_comb1.response.PostList;
import com.example.usan_comb1.response.PostResult;
import com.example.usan_comb1.response.RetroProduct;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 상세페이지 내의 작성자 판매 물품
public class AuthorSellProductDetailActivity extends AppCompatActivity {

    private String mProductName;
    private int mProductPrice;
    private TextView tvTitle, tvPrice, tvDetail, tvAuthor;
    private ImageButton imgButton;
    private boolean isFavorite = false;
    private ViewPager viewPager;
    private String username;
    private RecyclerView recyclerView;
    private CardAdapter cardadapter;
    private String accessToken;

    private ProductService mProductService;
    private Integer productId;
    private int[] imageReslds = {R.drawable.uploadimg, R.drawable.uploadimg, R.drawable.uploadimg, R.drawable.uploadimg, R.drawable.uploadimg};
    private static final String KEY_IS_FAVORITE = "is_favorite";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author_sell_product_detail);

        if (savedInstanceState != null) {
            mProductName = savedInstanceState.getString("productName");
            mProductPrice = savedInstanceState.getInt("productPrice");
        } else {
            // savedInstanceState가 null이면 초기화
            mProductName = "";
            mProductPrice = 0;
        }

        tvTitle = findViewById(R.id.tv_title);
        tvPrice = findViewById(R.id.tv_price);
        tvDetail = findViewById(R.id.tv_detail);
        tvAuthor = findViewById(R.id.nickname);

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        productId = getIntent().getIntExtra("product_id", -1);

        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        if (intent != null) {
            if (productId != -1) {
                checkData(accessToken, productId);
            }
        }

        // 제목, 가격, 설명 설정
        tvTitle.setText("상품 제목");
        tvPrice.setText("100,000원");
        tvDetail.setText("상품 설명입니다.");

        viewPager = findViewById(R.id.viewPager);
        AuthorSellProductDetailActivity.ImagePagerAdapter adapter = new AuthorSellProductDetailActivity.ImagePagerAdapter(this, imageReslds);
        viewPager.setAdapter(adapter);


        if (savedInstanceState != null) {
            isFavorite = savedInstanceState.getBoolean(KEY_IS_FAVORITE);
        } else {
            isFavorite = false;
        }

        updateFavoriteButtonImage();

        // 즐겨찾기 버튼 초기화
        ImageView favoriteButton = findViewById(R.id.imgbtn);

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFavorite = !isFavorite; // isFavorite 값을 반전시킵니다.

                if (isFavorite) { // 현재 isFavorite 값이 true인 경우 관심상품으로 추가합니다.
                    favoriteButton.setImageResource(R.drawable.select_ic_heart);
                    addFavorite(productId);
                } else { // 현재 isFavorite 값이 false인 경우 관심상품에서 제거합니다.
                    favoriteButton.setImageResource(R.drawable.unselect_ic_heart);
                    removeFavorite(productId);
                }
            }
        });
    }

    // 토스트 메시지를 출력하는 메서드
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // 관심상품 목록에 추가하는 메서드
    public void addFavorite(Integer productId) {
        Call<Void> call = mProductService.setFavorite(accessToken, productId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // * body !=null 삭제
                if (response.isSuccessful()) {
                    showToast("관심물품으로 등록되었습니다.");
                }
                else {
                    showToast("서버에서 정상적으로 처리되지 않았습니다.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("서버 에러가 발생하였습니다.");
            }
        });
    }

    // 관심상품 목록에서 제거하는 메서드
    private void removeFavorite(Integer productId) {
        // 서버와 통신하여 즐겨찾기 목록에서 제거
        // Retrofit2
        Call<Void> call = mProductService.unFavorite(accessToken, productId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showToast("관심물품에서 제거되었습니다.");
                } else {
                    showToast("서버에서 정상적으로 처리되지 않았습니다.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("서버 에러가 발생하였습니다.");
            }
        });
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("productName", mProductName);
        outState.putInt("productPrice", mProductPrice);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mProductName = savedInstanceState.getString("productName");
        mProductPrice = savedInstanceState.getInt("productPrice");
    }

    public void checkData(String accessToken, Integer productId) {
        // 서버 응답이 올 때까지 텍스트 뷰들의 텍스트를 초기화합니다.
        tvTitle.setText("");
        tvPrice.setText("");
        tvDetail.setText("");
        tvAuthor.setText("");

        mProductService.getProduct(accessToken, productId).enqueue(new Callback<PostResult>() {
            @Override
            public void onResponse(Call<PostResult> call, Response<PostResult> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PostResult product = response.body();

                    // 가져온 상품 정보를 화면에 표시합니다.
                    tvTitle.setText(product.getPost_Title());
                    tvPrice.setText(product.getPost_Price());
                    tvDetail.setText(product.getPost_Content());
                    tvAuthor.setText(product.getPost_Author());

                    loadUserPosts(product.getPost_Author());

                    ImageView favoriteButton = findViewById(R.id.imgbtn);

                    isFavorite = product.isFavorite();
                    if (product.isFavorite() == true) {
                        favoriteButton.setImageResource(R.drawable.select_ic_heart);
                    } else {
                        favoriteButton.setImageResource(R.drawable.unselect_ic_heart);
                    }

                } else {
                    Toast.makeText(AuthorSellProductDetailActivity.this, "데이터 가져오기 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PostResult> call, Throwable t) {
                Toast.makeText(AuthorSellProductDetailActivity.this, "서버 통신 에러 발생", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFavoriteButtonImage() {
        ImageView favoriteButton = findViewById(R.id.imgbtn);
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.select_ic_heart);
        } else {
            favoriteButton.setImageResource(R.drawable.unselect_ic_heart);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void loadUserPosts(String username) {
        int page_per = 10;
        int page = 1;
        Call<List<RetroProduct>> call = mProductService.getProductList(accessToken, username, page_per, page);
        call.enqueue(new Callback<List<RetroProduct>>() {
            @Override
            public void onResponse(Call<List<RetroProduct>> call, Response<List<RetroProduct>> response) {
                if (response.isSuccessful()) {
                    generateDataList(response.body());
                } else {
                    Toast.makeText(AuthorSellProductDetailActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RetroProduct>> call, Throwable t) {
                Toast.makeText(AuthorSellProductDetailActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class ImagePagerAdapter extends PagerAdapter {

        private Context context;
        private int[] imageResIds;

        public ImagePagerAdapter(Context context, int[] imageResIds) {
            this.context = context;
            this.imageResIds = imageResIds;
        }

        @Override
        public int getCount() {
            return imageResIds.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource(imageResIds[position]);
            container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            return imageView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    private void generateDataList(List<RetroProduct> productList) {
        recyclerView = findViewById(R.id.recyclerView);
        cardadapter = new CardAdapter(this, productList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(AuthorSellProductDetailActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(cardadapter);
    }
}
