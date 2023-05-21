package com.example.usan_comb1.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.request.UpdateRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 상품 수정 Activity
public class UpdateActivity extends AppCompatActivity {

    private EditText eTitle;
    private EditText eContent;
    private EditText eAddress;
    private EditText ePrice;

    private UpdateRequest previousProduct; // 이전에 올린 게시글의 내용을 담을 변수
    //Authorization
    private SharedPreferences prefs =getSharedPreferences("auth", Context.MODE_PRIVATE);
    private String accessToken = prefs.getString("access_token", "");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        eTitle = (EditText) findViewById(R.id.updateTitle);
        eContent = (EditText) findViewById(R.id.updateContent);
        eAddress = (EditText) findViewById(R.id.updateAddress);
        ePrice = (EditText) findViewById(R.id.updatePrice);


        // 이전에 올린 게시글의 내용 가져오기
        int productId = getIntent().getIntExtra("productId", -1);
        if (productId != -1) {
            getProduct(productId);
        }
    }

    // 서버에서 이전에 올린 게시글의 내용을 가져오는 메소드
    private void getProduct(int productId) {
        ProductService productService = RetrofitClient.getProductService();
        UpdateRequest updateProduct = new UpdateRequest(0,"", "", "", "");  // UpdateProduct 객체 생성
        Call<ResponseBody> call = productService.updateProduct(accessToken, productId, updateProduct);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String json = response.body().string();
                        JSONObject jsonObject = new JSONObject(json);

                        // 이전에 올린 게시글의 내용 저장
                        previousProduct = new UpdateRequest(
                                jsonObject.getInt("product_id"),
                                jsonObject.getString("product_title"),
                                jsonObject.getString("product_content"),
                                jsonObject.getString("address"),
                                jsonObject.getString("price")
                        );

                        // EditText에 이전 게시글의 내용 표시
                        eTitle.setText(previousProduct.getProduct_title());
                        eContent.setText(previousProduct.getProduct_content());
                        eAddress.setText(previousProduct.getAddress());
                        ePrice.setText(previousProduct.getPrice());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    // 서버 오류 처리
                    Toast.makeText(UpdateActivity.this, "상품 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 네트워크 오류 처리
                Toast.makeText(UpdateActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // 수정 버튼 클릭 시 호출되는 메소드
    public void onUpdateButtonClick(View view) {

        if (previousProduct == null) {
            Toast.makeText(this, "이전에 올린 게시글 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 수정할 게시글의 ID
        int productId = previousProduct.getProduct_id();

        // EditText에서 입력한 값 가져오기
        String productTitle = eTitle.getText().toString();
        String productContent = eContent.getText().toString();
        String address = eAddress.getText().toString();
        String price = ePrice.getText().toString();

        // 수정할 내용을 담은 Product 객체 생성
        UpdateRequest product = new UpdateRequest(0,"", "", "", "");
        product.setTitle(productTitle);
        product.setContent(productContent);
        product.setAddress(address);
        product.setPrice(price);

        ProductService productService = RetrofitClient.getProductService();

        Call<ResponseBody> call = productService.updateProduct(accessToken, productId, product);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // 수정 성공 처리
                    showUpdateSuccessDialog();
                } else {
                    // 서버 오류 처리
                    Toast.makeText(UpdateActivity.this, "상품 정보를 수정하지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 네트워크 오류 처리
                Toast.makeText(UpdateActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
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
}