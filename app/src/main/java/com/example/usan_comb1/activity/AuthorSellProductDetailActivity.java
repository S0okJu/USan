package com.example.usan_comb1.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.example.usan_comb1.response.PostResult;
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

    private ProductService mProductService;
    private Integer productId;
    private int[] imageResIds = {R.drawable.uploadimg, R.drawable.uploadimg, R.drawable.uploadimg, R.drawable.uploadimg, R.drawable.uploadimg};

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author_sell_product_detail);
        SharedPreferences prefs =getSharedPreferences("auth", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access_token", "");

        if(savedInstanceState != null){
            mProductName = savedInstanceState.getString("productName");
            mProductPrice = savedInstanceState.getInt("productPrice");
        }else{
            // savedInstanceState가 null이면 초기화
            mProductName = "";
            mProductPrice = 0;
        }

        tvTitle = findViewById(R.id.tv_title);
        tvPrice = findViewById(R.id.tv_price);
        tvDetail = findViewById(R.id.tv_detail);
        tvAuthor = findViewById(R.id.tv_author);

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        Intent intent = getIntent();
        if (intent != null) {
            // product_id 값을 받아옵니다.
            int productId = intent.getIntExtra("product_id", -1);
            if (productId != -1) {
                checkData(accessToken, productId);
            }
        }

        // 제목, 가격, 설명 설정
        tvTitle.setText("상품 제목");
        tvPrice.setText("100,000원");
        tvDetail.setText("상품 설명입니다.");

        viewPager = findViewById(R.id.viewPager);
        ImagePagerAdapter adapter = new ImagePagerAdapter(this, imageResIds);
        viewPager.setAdapter(adapter);

        imgButton = findViewById(R.id.imgbtn);
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼 아이콘 변경 코드 작성
                isFavorite = !isFavorite;
                if (isFavorite) {
                    imgButton.setImageResource(R.drawable.select_ic_heart);
                } else {
                    imgButton.setImageResource(R.drawable.unselect_ic_heart);
                }
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

        // 작성자 id를 가져옵니다.
        SharedPreferences sharedPref = getSharedPreferences("user", Context.MODE_PRIVATE);
        int authorId = sharedPref.getInt("id", -1);

        mProductService.getProductsByAuthor(accessToken, authorId).enqueue(new Callback<List<PostResult>>() {
            @Override
            public void onResponse(Call<List<PostResult>> call, Response<List<PostResult>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PostResult> productList = response.body();

                    // 상품 리스트에서 product_id와 일치하는 상품 정보를 가져옵니다.
                    for (PostResult product : productList) {
                        if (product.getProduct_Id() == productId) {
                            tvTitle.setText(product.getPost_Title());
                            tvPrice.setText(product.getPost_Price());
                            tvDetail.setText(product.getPost_Content());
                            tvAuthor.setText(product.getPost_Author());
                            break;
                        }
                    }
                    Log.d("Server Response", "Reponse: " + response.toString());
                } else {
                    Toast.makeText(AuthorSellProductDetailActivity.this, "데이터 가져오기 실패", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<PostResult>> call, Throwable t) {
                Toast.makeText(AuthorSellProductDetailActivity.this, "서버 통신 에러 발생", Toast.LENGTH_SHORT).show();
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
}
