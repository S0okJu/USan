package com.example.usan_comb1.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.usan_comb1.response.PostResult;
import com.example.usan_comb1.response.RetroProduct;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 상세페이지
public class DetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvDetail, tvAuthor;
    private TextView price;
    private ImageView profile;
    private ProductService mProductService;
    public boolean isFavorite;
    private ViewPager viewPager;
    private int[] imageReslds = {R.drawable.uploadimg, R.drawable.uploadimg, R.drawable.uploadimg, R.drawable.uploadimg, R.drawable.uploadimg};
    private Integer productId;
    private String username;
    private String accessToken;
    private static final String KEY_IS_FAVORITE = "is_favorite";

    private RecyclerView recyclerView;
    private CardAdapter cardadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        tvTitle = findViewById(R.id.tv_title);
        tvDetail = findViewById(R.id.tv_detail);
        tvAuthor = findViewById(R.id.nickname);
        profile = findViewById(R.id.profile);

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        productId = getIntent().getIntExtra("product_id", -1);

        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        if (intent != null) {
            if (productId != -1) {
                checkData(productId);
            }
        }

        tvAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent otherProfileIntent = new Intent(DetailActivity.this, OtherProfileActivity.class);
                otherProfileIntent.putExtra("username", tvAuthor.getText().toString());
                startActivity(otherProfileIntent);
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent otherProfileIntent = new Intent(DetailActivity.this, OtherProfileActivity.class);
                otherProfileIntent.putExtra("username", tvAuthor.getText().toString());
                startActivity(otherProfileIntent);
            }
        });


        //하단바 가격을 나타내는 뷰 객체
        price = findViewById(R.id.txtvprice);
        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);


        // 제목, 가격, 설명 설정
        tvTitle.setText("상품 제목");
        tvDetail.setText("상품 설명입니다.");

        viewPager = findViewById(R.id.viewPager);
        ImagePagerAdapter adapter = new ImagePagerAdapter(this, imageReslds);
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

    // 상세페이지로 데이터 불러오기
    public void checkData(Integer productId) {

        mProductService.getProduct(accessToken, productId).enqueue(new Callback<PostResult>() {
            @Override
            public void onResponse(Call<PostResult> call, Response<PostResult> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PostResult product = response.body();

                    tvTitle.setText(product.getPost_Title());
                    tvDetail.setText(product.getPost_Content());
                    tvAuthor.setText(product.getPost_Author());
                    price.setText(product.getPost_Price());
                    // tvAuthor 텍스트 설정 후에 호출
                    loadUserPosts(product.getPost_Author());

                    ImageView favoriteButton = findViewById(R.id.imgbtn);

                    isFavorite = product.isFavorite();
                    if (product.isFavorite() == true) {
                        favoriteButton.setImageResource(R.drawable.select_ic_heart);
                    } else {
                        favoriteButton.setImageResource(R.drawable.unselect_ic_heart);
                    }

                } else {
                    Toast.makeText(DetailActivity.this, "데이터 가져오기 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PostResult> call, Throwable t) {
                Toast.makeText(DetailActivity.this, "서버 통신 에러 발생", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(DetailActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RetroProduct>> call, Throwable t) {
                Toast.makeText(DetailActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
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
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(DetailActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(cardadapter);

    }
}