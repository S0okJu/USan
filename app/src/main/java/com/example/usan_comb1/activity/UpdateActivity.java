package com.example.usan_comb1.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.request.ProductRequest;
import com.example.usan_comb1.request.UpdateRequest;
import com.example.usan_comb1.response.UpdateResponse;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 상품 수정 Activity
public class UpdateActivity extends AppCompatActivity {

    private EditText eTitle, eContent, ePrice;
    private Spinner eAddressSpinner;
    private Integer productId;
    private String username, accessToken;

    private ProgressBar mProgressView;
    private ProductService mProductService;
    private UpdateRequest.Address addressObj;

    private UpdateResponse previousProduct; // 이전에 올린 게시글의 내용을 담을 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        eTitle = findViewById(R.id.updateTitle);
        eContent = findViewById(R.id.updateContent);
        ePrice = findViewById(R.id.updatePrice);
        eAddressSpinner = findViewById(R.id.updateAddress);

        mProgressView = (ProgressBar) findViewById(R.id.product_progress);

        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        // Authorization
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");

        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        productId = getIntent().getIntExtra("productId", -1);

        // Set up coordinates spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eAddressSpinner.setAdapter(adapter);

        if (intent != null) {
            if (productId != -1) {
                getProduct(productId, accessToken);
            }
        }
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
                updateData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void updateData() {
        eTitle.setError(null);
        eContent.setError(null);
        ePrice.setError(null);

        boolean cancel = false;
        View focusView = null;

        String title = eTitle.getText().toString();
        String content = eContent.getText().toString();
        String price = ePrice.getText().toString();

        // 제목의 유효성 검사
        if (title.isEmpty()) {
            eTitle.setError("제목을 입력해주세요.");
            focusView = eTitle;
            //title = "None";
            cancel = true;
        }

        // 내용의 유효성 검사
        if (content.isEmpty()) {
            eContent.setError("내용을 입력해주세요.");
            focusView = eContent;
            cancel = true;
            //content = "None";
        }

        // 가격의 유효성 검사
        if (price.isEmpty()) {
            ePrice.setError("가격을 입력해주세요.");
            focusView = ePrice;
            cancel = true;
            //price = "None";
        }

        // 주소의 유효성 검사
        String selectedAddress = (String) eAddressSpinner.getSelectedItem();
        if (selectedAddress == null || selectedAddress.isEmpty()) {
            showToast("주소를 선택해주세요.");
            cancel = true;
        } else {
            switch (selectedAddress) {
                case "간호대학":
                    addressObj = new UpdateRequest.Address("간호대학", 35.137759, 126.928947);
                    break;
                case "공과대학 1호관":
                    addressObj = new UpdateRequest.Address("공과대학 1호관", 35.141774, 126.925564);
                    break;
                case "공과대학 2호관":
                    addressObj = new UpdateRequest.Address("공과대학 2호관", 35.138634, 126.933557);
                    break;
                case "국제관":
                    addressObj = new UpdateRequest.Address("국제관", 35.142824, 126.931893);
                    break;
                case "미술대학":
                    addressObj = new UpdateRequest.Address("미술대학", 35.143912, 126.930246);
                    break;
                case "법과대학":
                    addressObj = new UpdateRequest.Address("법과대학", 35.139344, 126.935199);
                    break;
                case "본관":
                    addressObj = new UpdateRequest.Address("본관", 35.142688, 126.934678);
                    break;
                case "사회과학관":
                    addressObj = new UpdateRequest.Address("사회과학관", 35.146031, 126.934222);
                    break;
                case "생명공학관":
                    addressObj = new UpdateRequest.Address("생명공학관", 35.141166, 126.928570);
                    break;
                case "서석홀":
                    addressObj = new UpdateRequest.Address("서석홀", 35.145035, 126.932607);
                    break;
                case "의과대학":
                    addressObj = new UpdateRequest.Address("의과대학", 35.140486, 126.929584);
                    break;
                case "자연과학관":
                    addressObj = new UpdateRequest.Address("자연과학관", 35.139391, 126.928352);
                    break;
                case "중앙도서관":
                    addressObj = new UpdateRequest.Address("중앙도서관", 35.141706, 126.932129);
                    break;
                case "체육관":
                    addressObj = new UpdateRequest.Address("체육관", 35.140330, 126.927579);
                    break;
                case "e스포츠 경기장":
                    addressObj = new UpdateRequest.Address("e스포츠 경기장", 35.140820, 126.933031);
                    break;
                case "IT융합대학":
                    addressObj = new UpdateRequest.Address("IT융합대학", 35.139907, 126.934216);
                    break;

                default:
                    showToast("주소를 선택해주세요.");
                    return;
            }
        }


        // 이전 게시글의 내용을 업데이트하기 위해 UpdateRequest 객체 생성
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.setProduct_id(productId);
        updateRequest.setTitle(title);
        updateRequest.setContent(content);
        updateRequest.setPrice(price);
        updateRequest.setAddress(addressObj);


        Call<UpdateRequest> call = mProductService.updateProduct(accessToken, productId, updateRequest);
        call.enqueue(new Callback<UpdateRequest>() {
            @Override
            public void onResponse(Call<UpdateRequest> call, Response<UpdateRequest> response) {
                if (response.isSuccessful()) {

                    // 성공적으로 업데이트된 경우
                    showUpdateSuccessDialog();
                } else {
                    // 업데이트 실패한 경우
                    Toast.makeText(UpdateActivity.this, "게시글 수정에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UpdateRequest> call, Throwable t) {
                // 통신 실패한 경우
                Toast.makeText(UpdateActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
        }
    }


    private void getProduct(Integer productId, String accessToken) {
        Call<UpdateResponse> call = mProductService.getupdateProduct(accessToken, productId);
        call.enqueue(new Callback<UpdateResponse>() {
            @Override
            public void onResponse(Call<UpdateResponse> call, Response<UpdateResponse> response) {
                if (response.isSuccessful()) {
                    UpdateResponse product = response.body();
                    if (product != null) {
                        // 이전에 올린 게시글의 내용을 변수에 저장
                        previousProduct = new UpdateResponse();
                        previousProduct.setProduct_id(product.getProduct_id());
                        previousProduct.setTitle(product.getTitle());
                        previousProduct.setContent(product.getContent());
                        previousProduct.setAddress(product.getAddress());
                        previousProduct.setPrice(product.getPrice());

                        // 이전 게시글의 내용을 화면에 표시
                        eTitle.setText(product.getTitle());
                        eContent.setText(product.getContent());
                        String previousAddress = product.getAddress().getName();

                        // 이전 주소가 spinner의 목록에 있는지 확인하고 인덱스를 찾습니다.
                        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) eAddressSpinner.getAdapter();
                        int addressIndex = adapter.getPosition(previousAddress);

                        // 인덱스를 spinner에 설정하여 이전 주소를 선택합니다.
                        eAddressSpinner.setSelection(addressIndex);
                        ePrice.setText(product.getPrice());
                    }
                } else {
                    Toast.makeText(UpdateActivity.this, "게시글을 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UpdateResponse> call, Throwable t) {
                Toast.makeText(UpdateActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUpdateSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("상품 정보가 성공적으로 수정되었습니다.");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class GeocodeAsyncTask extends AsyncTask<String, Void, Address> {

        @Override
        protected Address doInBackground(String... strings) {
            String address = strings[0];
            Geocoder geocoder = new Geocoder(UpdateActivity.this, Locale.getDefault());
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

                addressObj = new UpdateRequest.Address(locationAddress.getAddressLine(0), latitude, longitude);
                updateData();
            } else {
                showToast("위치를 찾을 수 없습니다.");
            }
        }
    }

    private void showProgress(boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    // 토스트 메시지를 출력하는 메서드
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}