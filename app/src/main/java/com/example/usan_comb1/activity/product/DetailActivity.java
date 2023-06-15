package com.example.usan_comb1.activity.product;

import static com.example.usan_comb1.utilities.Constants.BUYER;
import static com.example.usan_comb1.utilities.Constants.SELLER;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.chat.ChatActivity;
import com.example.usan_comb1.activity.profile.OtherProfileActivity;
import com.example.usan_comb1.adapter.CardAdapter;
import com.example.usan_comb1.models.Users;
import com.example.usan_comb1.response.PostResult;
import com.example.usan_comb1.utilities.PreferenceManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 상세페이지
public class DetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvDetail, tvAuthor;
    private TextView price;
    private ImageView profile;
    private static ProductService mProductService;
    public boolean isFavorite;
    private ViewPager viewPager;
    private String[] imageUrls;
    private static Integer productId;
    private String username;
    private static String accessToken;
    private static final String KEY_IS_FAVORITE = "is_favorite";
    public String author;
    public Integer role;

    private RecyclerView recyclerView;
    private CardAdapter cardadapter;
    private Button chat;
    public String title;
    private PreferenceManager preferenceManager;

    String TAG = "FirebaseChat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        tvTitle = findViewById(R.id.tv_title);
        tvDetail = findViewById(R.id.tv_detail);
        tvAuthor = findViewById(R.id.nickname);
        profile = findViewById(R.id.profile);
        chat = findViewById(R.id.btnchat); // 채팅 버튼

        mProductService = RetrofitClient.getProductService();

        productId = getIntent().getIntExtra("product_id", -1);

        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");

        Intent intent = getIntent();
        username = preferenceManager.getString("username");
        if(username==null){
            username = prefs.getString("username","");
        }

        if (intent != null) {
            if (productId != -1) {
                checkData(productId);
            }
        }

        viewPager = findViewById(R.id.viewPager);
        ImagePagerAdapter adapter = new ImagePagerAdapter(this, imageUrls);
        viewPager.setAdapter(adapter);

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

        // 채팅 버튼 누르기
        chat.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                createOrJoinChat(String.valueOf(productId));
            }
        });


        //하단바 가격을 나타내는 뷰 객체

        price = findViewById(R.id.txtvprice);
        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);


        // 제목, 가격, 설명 설정
        tvTitle.setText("상품 제목");
        tvDetail.setText("상품 설명입니다.");

        viewPager = findViewById(R.id.viewPager);
        //ImagePagerAdapter adapter = new ImagePagerAdapter(this, imageReslds);
        //viewPager.setAdapter(adapter);


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
                    title = product.getPost_Title();
                    tvDetail.setText(product.getPost_Content());
                    tvAuthor.setText(product.getPost_Author());
                    price.setText(product.getPost_Price()+"원");

                    ImageView favoriteButton = findViewById(R.id.imgbtn);

                    // get Author username
                    author = product.getPost_Author();
                    System.out.println(author);
                    downloadImage();


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


    private static class ImagePagerAdapter extends PagerAdapter {

        private Context context;
        private String[] imageUrls;

        public ImagePagerAdapter(Context context, String[] imageUrls) {
            this.context = context;
            this.imageUrls = imageUrls;
        }

        @Override
        public int getCount() {
            return imageUrls.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            // 이미지 다운로드 및 표시
            downloadAndDisplayImage(imageUrls[position], imageView);

            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        // 이미지 다운로드 및 표시 메서드
        private void downloadAndDisplayImage(String imageUrl, final ImageView imageView) {
            // 이미지 다운로드
            Call<ResponseBody> call = mProductService.downloadImage(accessToken, productId, imageUrl);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        ResponseBody responseBody = response.body();
                        if (responseBody != null) {
                            try {
                                // 이미지 데이터를 읽어옵니다.
                                InputStream inputStream = responseBody.byteStream();
                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                                // 이미지를 이미지 뷰에 설정합니다.
                                imageView.setImageBitmap(bitmap);
                            } catch (Exception e) {
                                e.printStackTrace();
                                // 이미지 데이터 읽기 실패 시 기본 이미지를 설정합니다.
                                imageView.setImageResource(R.drawable.img_error);
                                Toast.makeText(context, "이미지 로딩 실패", Toast.LENGTH_SHORT).show();
                                Log.e("Image loading error", "Failed to load image: " + e.getMessage());
                            }
                        } else {
                            // 이미지 데이터가 없는 경우 기본 이미지를 설정합니다.
                            imageView.setImageResource(R.drawable.img_error);
                            Toast.makeText(context, "이미지가 없습니다.", Toast.LENGTH_SHORT).show();
                            Log.e("Download error", "Download failed: " + response.message());
                        }
                    } else {
                        // 서버 응답이 실패인 경우 기본 이미지를 설정합니다.
                        imageView.setImageResource(R.drawable.img_error);
                        Toast.makeText(context, "서버 응답 실패", Toast.LENGTH_SHORT).show();
                        Log.e("Download error", "Download failed: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    // 이미지 다운로드 중 오류가 발생한 경우 기본 이미지를 설정합니다.
                    imageView.setImageResource(R.drawable.img_error);
                    Toast.makeText(context, "다운로드 오류", Toast.LENGTH_SHORT).show();
                    Log.e("Download error", "Download failed: " + t.getMessage());
                }
            });
        }
    }

    // 채팅
    private void createOrJoinChat(String productId){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats");
        String chatId = "chat_"+productId;
        Users seller = new Users();

        database.collection("users").whereEqualTo("username", author).get().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null){
                QuerySnapshot result = task.getResult();
                QueryDocumentSnapshot document = (QueryDocumentSnapshot) result.getDocuments().get(0);
                String receiverId = document.getId(); // receiver id

                seller.setId(receiverId);
                seller.setName(author);

                if(username.equals(author)){
                    role = SELLER;
                }else{
                    role = BUYER;
                }

                Intent intent = new Intent(DetailActivity.this, ChatActivity.class);
                intent.putExtra("chatId",chatId);
                intent.putExtra("user", seller);
                intent.putExtra("title", title);
                intent.putExtra("role",role); // TODO 본인의 역할을 넘겨줍니다.
                intent.putExtra("prevInfo","detail");
                startActivity(intent);

            }else{
                System.out.println("Nop");
            }
        });

    }

    // 프로필 이미지 다운로드
    private void downloadImage() {
        Call<ResponseBody> call = mProductService.downloadProfileImage(accessToken, author);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        // 이미지 데이터를 읽어옵니다.
                        InputStream inputStream = responseBody.byteStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        // 이미지를 이미지 뷰에 설정합니다.
                        profile.setImageBitmap(bitmap);
                    } else {
                        // 이미지 데이터가 없는 경우 기본 이미지를 설정합니다.
                        profile.setImageResource(R.drawable.ic_default_profile);
                        Toast.makeText(DetailActivity.this, "이미지가 없습니다.", Toast.LENGTH_SHORT).show();
                        Log.e("Download error", "Download failed: " + response.message());
                    }
                } else {
                    // 서버 응답이 실패인 경우 기본 이미지를 설정합니다.
                    profile.setImageResource(R.drawable.ic_default_profile);
                    Toast.makeText(DetailActivity.this, "서버 응답 실패", Toast.LENGTH_SHORT).show();
                    Log.e("Download error", "Download failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 이미지 다운로드 중 오류가 발생한 경우 기본 이미지를 설정합니다.
                profile.setImageResource(R.drawable.ic_default_profile);
                Toast.makeText(DetailActivity.this, "다운로드 오류", Toast.LENGTH_SHORT).show();
                Log.e("Download error", "Download failed: " + t.getMessage());
            }
        });
    }


}