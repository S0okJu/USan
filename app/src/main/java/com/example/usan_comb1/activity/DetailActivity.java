package com.example.usan_comb1.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.response.PostList;
import com.example.usan_comb1.response.PostResult;
import com.google.android.material.appbar.MaterialToolbar;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {


    private TextView tvTitle, tvPrice, tvDetail, tvAuthor;
    private ProductService mProductService;
    private boolean isFavorite = false;
    private ViewPager viewPager;
    private int[] imageReslds = {R.drawable.uploadimg, R.drawable.uploadimg, R.drawable.uploadimg, R.drawable.uploadimg, R.drawable.uploadimg};
    private Integer productId;

    private SharedPreferences sharedPreferences; // 즐겨찾기 목록을 저장하는 SharedPreferences

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        tvTitle = findViewById(R.id.tv_title);
        tvPrice = findViewById(R.id.tv_price);
        tvDetail = findViewById(R.id.tv_detail);
        tvAuthor = findViewById(R.id.tv_author);

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        productId = getIntent().getIntExtra("product_id", -1);

        Intent intent = getIntent();
        if (intent != null) {
            // product_id 값을 받아옵니다.
            //int product_id = intent.getIntExtra("product_id", -1);
            if (productId != -1) {
                checkData(productId);
            }
        }

        // 제목, 가격, 설명 설정
        tvTitle.setText("상품 제목");
        tvPrice.setText("100,000원");
        tvDetail.setText("상품 설명입니다.");

        viewPager = findViewById(R.id.viewPager);
        ImagePagerAdapter adapter = new ImagePagerAdapter(this, imageReslds);
        viewPager.setAdapter(adapter);

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences("favorites", MODE_PRIVATE);

        // 즐겨찾기 버튼 초기화
        ImageView favoriteButton = findViewById(R.id.imgbtn);
        isFavorite = sharedPreferences.getBoolean(String.valueOf(productId), false);


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
        // 카드뷰 객체 생성
        CardView cardView1 = findViewById(R.id.card1);
        CardView cardView2= findViewById(R.id.card2);


        // 카드뷰1 클릭 이벤트 처리
        cardView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 클릭 이벤트 발생 시 실행될 코드 작성
                Intent intent = new Intent(getApplicationContext(), AuthorSellProductDetailActivity.class);
                startActivity(intent);
            }
        });

        // 카드뷰2 클릭 이벤트 처리
        cardView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 클릭 이벤트 발생 시 실행될 코드 작성
                Intent intent = new Intent(getApplicationContext(), AuthorSellProductDetailActivity.class);
                startActivity(intent);
            }
        });

    }

    // 토스트 메시지를 출력하는 메서드
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // 관심상품 목록에 추가하는 메서드
    public void addFavorite(Integer productId) {
        //isFavorite = !isFavorite;
        Call<Void> call = mProductService.setFavorite(productId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // * body !=null 삭제
                if (response.isSuccessful()) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove(String.valueOf(productId));
                    editor.apply();
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
        Call<Void> call = mProductService.unFavorite(productId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // SharedPreferences에서 제거
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove(String.valueOf(productId));
                    editor.apply();
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

        mProductService.getProduct(productId).enqueue(new Callback<PostResult>() {
            @Override
            public void onResponse(Call<PostResult> call, Response<PostResult> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PostResult product = response.body();

                    tvTitle.setText(product.getPost_Title());
                    tvPrice.setText(product.getPost_Price());
                    tvDetail.setText(product.getPost_Content());
                    tvAuthor.setText(product.getPost_Author());

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
}