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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
    private EditText mTitle, mContent, mPrice;
    private Spinner mAddressSpinner;
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
        mTitle = findViewById(R.id.uploadTitle);
        mContent = findViewById(R.id.uploadContent);
        mPrice = findViewById(R.id.uploadPrice);
        mAddressSpinner = findViewById(R.id.uploadAddress);

        mProgressView = (ProgressBar) findViewById(R.id.product_progress);

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        // Set up coordinates spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAddressSpinner.setAdapter(adapter);

        // Authorization
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access_token", "");
        username = prefs.getString("username", "");

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
        mContent.setError(null);
        mPrice.setError(null);

        String title = mTitle.getText().toString();
        String author = username;
        String content = mContent.getText().toString();
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

        // 가격의 유효성 검사
        if (price.isEmpty()) {
            mPrice.setError("가격을 입력해주세요.");
            focusView = mPrice;
            cancel = true;
        }

        // 주소의 유효성 검사
        String selectedAddress = mAddressSpinner.getSelectedItem().toString();
        if (selectedAddress == null || selectedAddress.isEmpty() || selectedAddress.equals("주소를 선택해 주세요")) {
            showToast("주소를 제대로 선택해주세요.");
            cancel = true;
        } else {
            switch (selectedAddress) {
                case "간호대학":
                    addressObj = new ProductRequest.Address("간호대학", 35.137759, 126.928947);
                    break;
                case "공과대학 1호관":
                    addressObj = new ProductRequest.Address("공과대학 1호관", 35.141774, 126.925564);
                    break;
                case "공과대학 2호관":
                    addressObj = new ProductRequest.Address("공과대학 2호관", 35.138634, 126.933557);
                    break;
                case "국제관":
                    addressObj = new ProductRequest.Address("국제관", 35.142824, 126.931893);
                    break;
                case "미술대학":
                    addressObj = new ProductRequest.Address("미술대학", 35.143912, 126.930246);
                    break;
                case "법과대학":
                    addressObj = new ProductRequest.Address("법과대학", 35.139344, 126.935199);
                    break;
                case "본관":
                    addressObj = new ProductRequest.Address("본관", 35.142688, 126.934678);
                    break;
                case "사회과학관":
                    addressObj = new ProductRequest.Address("사회과학관", 35.146031, 126.934222);
                    break;
                case "생명공학관":
                    addressObj = new ProductRequest.Address("생명공학관", 35.141166, 126.928570);
                    break;
                case "서석홀":
                    addressObj = new ProductRequest.Address("서석홀", 35.145035, 126.932607);
                    break;
                case "의과대학":
                    addressObj = new ProductRequest.Address("의과대학", 35.140486, 126.929584);
                    break;
                case "자연과학관":
                    addressObj = new ProductRequest.Address("자연과학관", 35.139391, 126.928352);
                    break;
                case "중앙도서관":
                    addressObj = new ProductRequest.Address("중앙도서관", 35.141706, 126.932129);
                    break;
                case "체육관":
                    addressObj = new ProductRequest.Address("체육관", 35.140330, 126.927579);
                    break;
                case "e스포츠 경기장":
                    addressObj = new ProductRequest.Address("e스포츠 경기장", 35.140820, 126.933031);
                    break;
                case "IT융합대학":
                    addressObj = new ProductRequest.Address("IT융합대학", 35.139907, 126.934216);
                    break;

                default:
                    showToast("주소를 선택해주세요.");
                    return;
            }
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            uploadData(new ProductRequest(title, author, content, addressObj, price));
            showProgress(true);
        }
    }

    /*
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
     */

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
