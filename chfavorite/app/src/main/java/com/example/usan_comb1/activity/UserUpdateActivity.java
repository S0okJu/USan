package com.example.usan_comb1.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.request.ProfileUpRequest;
import com.example.usan_comb1.response.ProfileResponse;
import com.example.usan_comb1.response.UploadResponse;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserUpdateActivity extends AppCompatActivity {

    private Button btnimgupdate, btn_ch, btn_ok;
    private TextView tvname;
    private EditText et_updatename;
    private ProductService mProductService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_update);

        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access_token", "");
        mProductService = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        final String username = prefs.getString("username", "");

        tvname = findViewById(R.id.tv_name);
        et_updatename = findViewById(R.id.et_nameupdate);
        btn_ch = findViewById(R.id.btn_ch);
        btn_ok = findViewById(R.id.btn_ok);

        et_updatename.setVisibility(View.INVISIBLE);
        btn_ch.setVisibility(View.VISIBLE);
        btn_ok.setVisibility(View.INVISIBLE);

        Call<ProfileResponse> call = mProductService.getProfile(accessToken, username);
        call.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful()) {
                    ProfileResponse profile = response.body();
                    // 프로필 정보를 사용하여 필요한 작업 수행
                    String username = profile.getUsername(); // 프로필 정보에서 username 값을 가져옵니다.
                    tvname.setText(username); // TextView에 username 값을 설정합니다.
                } else {
                    // 오류 처리
                    Toast.makeText(UserUpdateActivity.this, "데이터 통신 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Toast.makeText(UserUpdateActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btn_ch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvname.setVisibility(View.INVISIBLE);
                et_updatename.setVisibility(View.VISIBLE);
                btn_ch.setVisibility(View.INVISIBLE);
                btn_ok.setVisibility(View.VISIBLE);

                et_updatename.setText(username);

                btn_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String updatedName = et_updatename.getText().toString();
                        ProfileUpRequest profileUpRequest = new ProfileUpRequest(updatedName);
                        Call<Void> call = mProductService.modifyProfile(accessToken, username, profileUpRequest);
                        call.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    // 수정 성공 처리
                                    Toast.makeText(UserUpdateActivity.this, "수정되었습니다.", Toast.LENGTH_SHORT).show();

                                    // 수정된 username을 가져온 후 SharedPreferences에 다시 저장
                                    SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("username", updatedName); // 수정된 username을 저장
                                    editor.apply();

                                    tvname.setVisibility(View.VISIBLE);
                                    et_updatename.setVisibility(View.INVISIBLE);
                                    btn_ch.setVisibility(View.VISIBLE);
                                    btn_ok.setVisibility(View.INVISIBLE);
                                } else {
                                    // 수정 실패 처리
                                    Toast.makeText(UserUpdateActivity.this, "수정에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                // 네트워크 오류 처리
                                Toast.makeText(UserUpdateActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }
}