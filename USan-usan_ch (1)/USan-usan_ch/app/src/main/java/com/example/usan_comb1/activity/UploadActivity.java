package com.example.usan_comb1.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.usan_comb1.AuthInterceptor;
import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.request.ProductRequest;
import com.example.usan_comb1.response.ProductImageResponse;
import com.example.usan_comb1.response.ProductResponse;
import com.example.usan_comb1.response.UploadResponse;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 상품 추가 Activity
public class UploadActivity extends AppCompatActivity {
    //private ImageView mImage;
    private EditText mTitle, mContent, mAddress, mPrice;
    private ImageView productImg;
    private ProductService mProductService;
    private ProductRequest.Address addressObj;
    private static final int REQUEST_SELECT_IMAGE = 2;
    private static final int REQUEST_CROP_IMAGE = 3;
    private static final int REQUEST_READ_MEDIA_IMAGES = 1;
    private Uri imageUri; // Added variable to store selected image URI
    private String accessToken;
    private int productId;

    private ProgressBar mProgressView;

    private String path;

    private String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        productImg = (ImageView) findViewById(R.id.productImage);
        mTitle = (EditText) findViewById(R.id.uploadTitle);
        mContent = (EditText) findViewById(R.id.uploadContent);
        mAddress = (EditText) findViewById(R.id.uploadAddress);
        mPrice = (EditText) findViewById(R.id.uploadPrice);

        mProgressView = (ProgressBar) findViewById(R.id.product_progress);

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        // Authorization
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");
        username = prefs.getString("username", "");
/*
        // clickListeners(); 이미지
        productImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 파일 엑세스 권한 확인
                verifyStoragePermissions(UploadActivity.this);
            }
        });

 */
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
            GeocodeAsyncTask task = new GeocodeAsyncTask();
            task.setAddressInfo(title, author, content, price);
            task.execute(address);
            showProgress(true);
        }
    }

    private class GeocodeAsyncTask extends AsyncTask<String, Void, Address> {

        private String title;
        private String author;
        private String content;
        private String price;


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
                //showToast("위도 : " + latitude + "\n경도 : " + longitude);

                addressObj = new ProductRequest.Address(locationAddress.getAddressLine(0), latitude, longitude);
                uploadData(new ProductRequest(title, author, content, addressObj, price));
            } else {
                showToast("위치를 찾을 수 없습니다.");
            }
        }

        public void setAddressInfo(String title, String author, String content, String price) {
            this.title = title;
            this.author = author;
            this.content = content;
            this.price = price;
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
                    showToast("게시글이 등록되었습니다.");
                    showProgress(false);
                    finish();

                    productId = result.getProduct_id();
                    System.out.println(productId);
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                showToast("게시글 등록이 정상적으로 진행되지 않았습니다.");
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

    /*

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have permission
        int permission = ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.READ_MEDIA_IMAGES);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    REQUEST_READ_MEDIA_IMAGES
            );
        } else {
            // Permission granted, open the gallery
            openGallery(activity);
        }
    }

    private static void openGallery(Activity activity) {
        // Open gallery code
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        activity.startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            String imagePath = getRealPathFromUri(imageUri);
            uploadImage(accessToken, productId, imagePath);
        }
    }

    private String getRealPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), uri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int columnIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(columnIdx);
        cursor.close();
        return result;
    }

    private void uploadproductImage(String accessToken, int productId, String imagePath) {
        // Convert file path to actual path
        String actualPath = Uri.parse(imagePath).getPath();
        File file = new File(actualPath);

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("imgs", file.getName(), requestFile);
        Call<ProductImageResponse> call = mProductService.uploadproductImage(accessToken, productId, body);
        call.enqueue(new Callback<ProductImageResponse>() {
            @Override
            public void onResponse(Call<ProductImageResponse> call, Response<ProductImageResponse> response) {
                if (response.isSuccessful()) {
                    // 이미지 업로드 성공 처리
                    Toast.makeText(UpdateActivity.this, "사진을 업로드했습니다.", Toast.LENGTH_SHORT).show();
                    Log.i("Upload success", "Successfully uploaded image");
                } else {
                    // 이미지 업로드 실패 처리
                    Toast.makeText(UpdateActivity.this, "사진 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    Log.e("Upload error", "Upload failed: " + response.message());
                    return;
                }
            }

            @Override
            public void onFailure(Call<ProductImageResponse> call, Throwable t) {
                // 네트워크 오류 처리
                Toast.makeText(UpdateActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
                Log.e("Upload error", t.getMessage());
            }

        });
    }

     */
}

