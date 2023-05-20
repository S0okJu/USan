package com.example.usan_comb1.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.request.LoginData;
import com.example.usan_comb1.response.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 로그인
public class LoginActivity extends AppCompatActivity {
    private EditText EtEmail, EtPwd;
    private Button btnlgn, btnreg;
    private ProductService service;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        EtEmail = findViewById(R.id.et_email);
        EtPwd = findViewById(R.id.et_pwd);
        btnlgn = findViewById(R.id.btnlgn);
        btnreg = findViewById(R.id.btnreg);


        service = RetrofitClient.getRetrofitInstance().create(ProductService.class);

        btnlgn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        btnreg.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        EtEmail.setError(null);
        EtPwd.setError(null);

        String email = EtEmail.getText().toString();
        String password = EtPwd.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // 패스워드의 유효성 검사
        if (password.isEmpty()) {
            EtEmail.setError("비밀번호를 입력해주세요.");
            focusView = EtEmail;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            EtPwd.setError("6자 이상의 비밀번호를 입력해 주세요.");
            focusView = EtPwd;
            cancel = true;
        }

        // 이메일의 유효성 검사
        if (email.isEmpty()) {
            EtEmail.setError("아이디를 입력해 주세요.");
            focusView = EtEmail;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            LoginData loginData = new LoginData(email, password);
            startLogin(loginData);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    private void startLogin(LoginData loginData) {
        service.userLogin(loginData).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse result = response.body();
                if (result != null && response.isSuccessful()) {
                    // save access token and refresh token to SharedPreferences
                    SharedPreferences sharedPrefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPrefs.edit();

                    // Check if the username has changed
                    String storedUsername = sharedPrefs.getString("username", "");
                    String newUsername = result.getUsername();
                    if (!newUsername.equals(storedUsername)) {
                        // Clear the previous username
                        editor.remove("username"); }

                    // Fix 처음부터 Bearer와 함께 추가
                    editor.putString("access_token", "Bearer " +result.getAccessToken());

                    // 사용자 username를 추가한다.
                    editor.putString("username", newUsername);
                    editor.apply();

                    // Profile Intent에 username 추가
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);

                    // Intent profileIntent = new Intent(LoginActivity.this, SalelistActivity.class);
                    // profileIntent.putExtra("username", result.getUsername());

                } else {
                    Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "로그인 에러 발생", Toast.LENGTH_SHORT).show();
            }
        });
    }
}