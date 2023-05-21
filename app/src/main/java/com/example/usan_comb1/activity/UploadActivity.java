package com.example.usan_comb1.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.usan_comb1.AuthInterceptor;
import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.request.ProductRequest;
import com.example.usan_comb1.response.ProductResponse;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 상품 추가 Activity
public class UploadActivity extends AppCompatActivity {
    //private ImageView mImage;
    private EditText mTitle, mContent, mAddress, mPrice;
    private Button btnAddress;
    private ProductService mProductService;
    private ProductRequest.Address addressObj;

    private ProgressBar mProgressView;

    private String path;

    private String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        //mImage = (ImageView) findViewById(R.id.uploadimg);
        mTitle = (EditText) findViewById(R.id.uploadTitle);
        mContent = (EditText) findViewById(R.id.uploadContent);
        mAddress = (EditText) findViewById(R.id.uploadAddress);
        mPrice = (EditText) findViewById(R.id.uploadPrice);
        btnAddress = (Button) findViewById(R.id.AddressBtn);

        mProgressView = (ProgressBar) findViewById(R.id.product_progress);

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        // Authorization
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access_token", "");
        username = prefs.getString("username", "");

        // clickListeners(); 이미지

        btnAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = mAddress.getText().toString();
                if (!address.isEmpty()) {
                    new GeocodeAsyncTask().execute(address);
                }
            }
        });

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
        mContent.setError(null);
        mAddress.setError(null);
        mPrice.setError(null);

        String title = mTitle.getText().toString();
        String author = username;
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
            uploadData(new ProductRequest(title, author, content, addressObj, price));
            showProgress(true);
        }
    }

    private void uploadData(ProductRequest data) {
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access_token", "");

        mProductService.postProduct(accessToken, data).enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProductResponse result = response.body();
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

    // 토스트 메시지를 출력하는 메서드
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private class GeocodeAsyncTask extends AsyncTask<String, Void, Address> {
        @Override
        protected Address doInBackground(String... strings) {
            String address = strings[0];
            Geocoder geocoder = new Geocoder(UploadActivity.this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(address, 1);
                if (addresses.size() > 0) {
                    return addresses.get(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Address locationAddress) {
            if (locationAddress != null) {
                double latitude = locationAddress.getLatitude();
                double longitude = locationAddress.getLongitude();
                showToast("위도 : " + latitude + "\n경도 : " + longitude);

                addressObj = new ProductRequest.Address(locationAddress.getAddressLine(0), latitude, longitude);
            } else {
                showToast("위치를 찾을 수 없습니다.");
            }
        }
    }
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
