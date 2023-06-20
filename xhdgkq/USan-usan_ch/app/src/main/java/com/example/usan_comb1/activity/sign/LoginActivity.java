package com.example.usan_comb1.activity.sign;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.usan_comb1.ProductService;
import com.example.usan_comb1.R;
import com.example.usan_comb1.RetrofitClient;
import com.example.usan_comb1.activity.MainActivity;
import com.example.usan_comb1.activity.sign.RegisterActivity;
import com.example.usan_comb1.request.LoginData;
import com.example.usan_comb1.response.LoginResponse;
import com.example.usan_comb1.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 로그인
public class LoginActivity extends AppCompatActivity {
    private EditText EtEmail, EtPwd;
    private Button btnlgn, btnreg;
    private CheckBox cb_save;
    private ProductService service;
    private PreferenceManager preferenceManager;

    // 앱 종료를 위한 시간 변수 설정
    private final long finishtimeed = 1000;
    private long presstime = 0;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        EtEmail = findViewById(R.id.et_email);
        EtPwd = findViewById(R.id.et_pwd);
        btnlgn = findViewById(R.id.btnlgn);
        btnreg = findViewById(R.id.btnreg);
        cb_save = (CheckBox) findViewById(R.id.cb_save);

        preferenceManager = new PreferenceManager(getApplicationContext());
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

        cb_save.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    String email = EtEmail.getText().toString().trim();
                    String password = EtPwd.getText().toString().trim();
                    preferenceManager.putString("saved_email", email);
                    preferenceManager.putString("saved_password", password);
                } else {
                    preferenceManager.removeString("saved_email");
                    preferenceManager.removeString("saved_password");
                }
            }
        });

// 이메일과 비밀번호를 SharedPreferences에서 가져와서 EditText에 설정
        String savedEmail = preferenceManager.getString("saved_email");
        String savedPassword = preferenceManager.getString("saved_password");
        if (savedEmail != null && savedPassword != null) {
            EtEmail.setText(savedEmail);
            EtPwd.setText(savedPassword);
            cb_save.setChecked(true);
        }
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
                    preferenceManager.putString("access_token","Bearer " +result.getAccessToken());

                    // 사용자 username를 추가한다.
                    editor.putString("username", newUsername);
                    editor.apply();

                    // 저장된 이메일과 비밀번호를 체크박스 상태에 따라 다시 저장
                    if (cb_save.isChecked()) {
                        String email = EtEmail.getText().toString().trim();
                        String password = EtPwd.getText().toString().trim();
                        preferenceManager.putString("saved_email", email);
                        preferenceManager.putString("saved_password", password);
                    } else {
                        preferenceManager.removeString("saved_email");
                        preferenceManager.removeString("saved_password");
                    }

                    // Firebase
                    // Firebase에 있는 사용자 정보를 가져온다.
                    FirebaseFirestore database = FirebaseFirestore.getInstance();
                    database.collection("users").whereEqualTo("username",newUsername).get()
                            .addOnCompleteListener(task ->{
                                if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size()>0){
                                    DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                    preferenceManager.putString("userId",documentSnapshot.getId());
                                    preferenceManager.putString("username",documentSnapshot.getString("username"));
                                    // preferenceManager.putString("image",documentSnapshot.getString("image"));
                                }
                            });

                    // Profile Intent에 username 추가
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);

                    // LoginActivity를 스택에서 제거
                    finish();

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

    // 뒤로가기 버튼 누를 시 Toast 메시지 출력 / 연속으로 누를 시 앱 종료
    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - presstime;

        if (0 <= intervalTime && finishtimeed >= intervalTime)
        {
            finish();
        }
        else
        {
            presstime = tempTime;
            Toast.makeText(getApplicationContext(), "한 번 더 누르시면 앱이 종료됩니다", Toast.LENGTH_SHORT).show();
        }
    }
}