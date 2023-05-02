package com.example.usan_comb1.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.usan_comb1.AuthInterceptor;
import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.request.ProductRequest;
import com.example.usan_comb1.response.ProductResponse;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 상품 추가 Activity
public class UploadActivity extends AppCompatActivity {
    //private ImageView mImage;
    private EditText mTitle;
    private EditText mAuthor;
    private EditText mContent;
    private EditText mAddress;
    private EditText mPrice;
    private ProductService mProductService;

    private ProgressBar mProgressView;

    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        //mImage = (ImageView) findViewById(R.id.uploadimg);
        mTitle = (EditText) findViewById(R.id.uploadTitle);
        mAuthor = (EditText) findViewById(R.id.uploadAuthor);
        mContent = (EditText) findViewById(R.id.uploadContent);
        mAddress = (EditText) findViewById(R.id.uploadAddress);
        mPrice = (EditText) findViewById(R.id.uploadPrice);

        mProgressView = (ProgressBar) findViewById(R.id.product_progress);

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        // clickListeners(); 이미지
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveData() {
        mTitle.setError(null);
        mAuthor.setError(null);
        mContent.setError(null);
        mAddress.setError(null);
        mPrice.setError(null);

        String title = mTitle.getText().toString();
        String author = mAuthor.getText().toString();
        String content = mContent.getText().toString();
        String address = mAddress.getText().toString();
        String price = mPrice.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // 제목의 유효성 검사
        if (title.isEmpty()) {
            mTitle.setError("제목을 입력해주세요.");
            focusView = mTitle;
            cancel = true;
        }

        // 작성자의 유효성 검사
        if (author.isEmpty()) {
            mAuthor.setError("작성자를 입력해주세요.");
            focusView = mAuthor;
            cancel = true;
        }

        // 내용의 유효성 검사
        if (content.isEmpty()) {
            mContent.setError("내용을 입력해주세요.");
            focusView = mContent;
            cancel = true;
        }

        // 주소의 유효성 검사
        if (address.isEmpty()) {
            mAddress.setError("주소를 입력해주세요.");
            focusView = mAddress;
            cancel = true;
        }

        // 가격의 유효성 검사
        if (price.isEmpty()) {
            mPrice.setError("가격을 입력해주세요.");
            focusView = mPrice;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            uploadData(new ProductRequest(title, author, content, address, price));
            showProgress(true);
        }
    }

    private void uploadData(ProductRequest data) {

        mProductService.postProduct(data).enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProductResponse result = response.body();
                    // save access token and refresh token to SharedPreferences
                    SharedPreferences sharedPrefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString("access_token", result.getAccessToken());
                    editor.apply();

                    Toast.makeText(UploadActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    showProgress(false);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Toast.makeText(UploadActivity.this, "서버 통신 에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("서버 통신 에러 발생", t.getMessage());
                showProgress(false);
            }
        });
    }

    private void showProgress(boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    /* 이미지 관련
        private void clickListeners() {
        mImage.setOnClickListener(v-> {
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 10);
            } else {
                ActivityCompat.requestPermissions(UploadActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        });
    }

    @Override

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==10 && resultCode== Activity.RESULT_OK) {
            Uri uri = data.getData();
            Context context = UploadActivity.this;
            path = RealPathUtil.getRealPath(context, uri);
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            mImage.setImageBitmap(bitmap);
        }
    }

     */
}